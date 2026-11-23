package cl.teqmed.rendiciones.service;

import cl.teqmed.rendiciones.dto.RendicionDtos.*;
import cl.teqmed.rendiciones.model.Gasto;
import cl.teqmed.rendiciones.model.HistorialEstado;
import cl.teqmed.rendiciones.model.Rendicion;
import cl.teqmed.rendiciones.model.enums.EstadoGasto;
import cl.teqmed.rendiciones.model.enums.EstadoRendicion;
import cl.teqmed.rendiciones.model.enums.TipoRendicion;
import cl.teqmed.rendiciones.repository.GastoRepository;
import cl.teqmed.rendiciones.repository.HistorialRepository;
import cl.teqmed.rendiciones.repository.RendicionRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class RendicionService {

    private final RendicionRepository rendicionRepo;
    private final GastoRepository gastoRepo;
    private final HistorialRepository historialRepo;
    private final CatalogoService catalogo;

    // Cambios de estado que se pueden hacer con POST /{id}/estado
    // (enviar, aprobar por Finanzas y pagar tienen su propio endpoint)
    private static final Map<EstadoRendicion, Set<EstadoRendicion>> TRANSICIONES = Map.of(
            EstadoRendicion.ENVIADA, Set.of(EstadoRendicion.OBSERVADA, EstadoRendicion.RECHAZADA, EstadoRendicion.APROBADA_JEFATURA),
            EstadoRendicion.APROBADA_JEFATURA, Set.of(EstadoRendicion.RECHAZADA)
    );

    public RendicionService(RendicionRepository rendicionRepo, GastoRepository gastoRepo,
                            HistorialRepository historialRepo, CatalogoService catalogo) {
        this.rendicionRepo = rendicionRepo;
        this.gastoRepo = gastoRepo;
        this.historialRepo = historialRepo;
        this.catalogo = catalogo;
    }

    // ---------- Consultas ----------
    // Un solo GET con filtros opcionales:
    //   ?usuarioId=1                    → mis rendiciones (técnico)
    //   ?usuarioIds=1,2,3&estado=ENVIADA → bandeja de la jefatura
    //   ?estado=APROBADA_JEFATURA        → bandeja de Finanzas
    public List<RendicionDto> listar(Long usuarioId, EstadoRendicion estado, List<Long> usuarioIds) {
        List<Rendicion> lista;
        if (usuarioIds != null && !usuarioIds.isEmpty() && estado != null)
            lista = rendicionRepo.findByUsuarioIdInAndEstadoOrderByFechaEnvioAsc(usuarioIds, estado);
        else if (usuarioId != null)
            lista = rendicionRepo.findByUsuarioIdOrderByIdDesc(usuarioId);
        else if (estado != null)
            lista = rendicionRepo.findByEstadoOrderByFechaEnvioAsc(estado);
        else
            lista = rendicionRepo.findAll();
        return lista.stream().map(RendicionDto::de).toList();
    }

    public RendicionDto obtener(Long id) {
        return RendicionDto.de(buscar(id));
    }

    public List<HistorialDto> historial(Long id) {
        buscar(id);                                         // 404 si no existe
        return historialRepo.findByRendicion_IdOrderByFechaAscIdAsc(id).stream().map(HistorialDto::de).toList();
    }

    // ---------- CRUD ----------
    @Transactional
    public RendicionDto crear(RendicionRequest r) {
        validar(r);
        Rendicion nueva = new Rendicion();
        copiar(r, nueva);
        nueva.setEstado(EstadoRendicion.BORRADOR);
        rendicionRepo.save(nueva);                          // primero se guarda para obtener el id...
        nueva.setNumero(String.format("RND-%d-%06d", nueva.getDesde().getYear(), nueva.getId()));   // ...y con él el número
        anotar(nueva, null, EstadoRendicion.BORRADOR.name(), r.usuarioId(), "Creada");
        return RendicionDto.de(nueva);
    }

    @Transactional
    public RendicionDto actualizar(Long id, RendicionRequest r) {
        Rendicion actual = buscar(id);
        exigirEditable(actual);
        validar(r);
        copiar(r, actual);
        return RendicionDto.de(rendicionRepo.save(actual));
    }

    @Transactional
    public void eliminar(Long id) {
        Rendicion actual = buscar(id);
        if (actual.getEstado() != EstadoRendicion.BORRADOR)
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Solo se puede eliminar una rendición en borrador");
        rendicionRepo.delete(actual);                       // CASCADE borra sus gastos e historial
    }

    // ---------- Flujo de aprobación ----------
    // Técnico: gastos → ENVIADO y rendición → ENVIADA
    @Transactional
    public RendicionDto enviar(Long id, AccionRequest a) {
        Rendicion r = buscar(id);
        exigirEditable(r);
        List<Gasto> gastos = gastoRepo.findByRendicion_IdOrderByFechaDocumentoAsc(id);
        if (gastos.isEmpty())
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Agrega al menos un gasto antes de enviar");
        boolean sinResponder = gastos.stream().anyMatch(g ->
                g.getEstado() == EstadoGasto.OBSERVADO && (g.getRespuestaTecnico() == null || g.getRespuestaTecnico().isBlank()));
        if (sinResponder)
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Responde las observaciones de la jefatura antes de reenviar");

        gastoRepo.cambiarEstadoDeRendicion(id, List.of(EstadoGasto.BORRADOR, EstadoGasto.OBSERVADO), EstadoGasto.ENVIADO);
        Rendicion recargada = buscar(id);                   // el UPDATE masivo limpió el contexto: se vuelve a leer
        aplicarEstado(recargada, EstadoRendicion.ENVIADA, a.usuarioId(),
                a.comentario() != null ? a.comentario() : "Enviada a jefatura");
        return RendicionDto.de(recargada);
    }

    // Jefatura (finalizar revisión) o Finanzas (rechazar)
    @Transactional
    public RendicionDto cambiarEstado(Long id, CambioEstadoRequest c) {
        Rendicion r = buscar(id);
        Set<EstadoRendicion> permitidos = TRANSICIONES.getOrDefault(r.getEstado(), Set.of());
        if (!permitidos.contains(c.estado()))
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "No se puede pasar de " + r.getEstado() + " a " + c.estado());

        // La jefatura solo finaliza cuando todos los gastos tienen decisión
        if (r.getEstado() == EstadoRendicion.ENVIADA) {
            boolean pendientes = gastoRepo.findByRendicion_IdOrderByFechaDocumentoAsc(id).stream()
                    .anyMatch(g -> g.getEstado() == EstadoGasto.ENVIADO);
            if (pendientes)
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Quedan gastos sin revisar");
        }
        aplicarEstado(r, c.estado(), c.usuarioId(), c.comentario());
        return RendicionDto.de(r);
    }

    // Finanzas: gastos APROBADO → APROBADO_FINAL y rendición → APROBADA_FINANZAS
    @Transactional
    public RendicionDto aprobarFinanzas(Long id, AccionRequest a) {
        Rendicion r = buscar(id);
        if (r.getEstado() != EstadoRendicion.APROBADA_JEFATURA)
            throw new ResponseStatusException(HttpStatus.CONFLICT, "La rendición no está aprobada por la jefatura");
        gastoRepo.cambiarEstadoDeRendicion(id, List.of(EstadoGasto.APROBADO), EstadoGasto.APROBADO_FINAL);
        Rendicion recargada = buscar(id);
        aplicarEstado(recargada, EstadoRendicion.APROBADA_FINANZAS, a.usuarioId(),
                a.comentario() != null ? a.comentario() : "Aprobada por Finanzas");
        return RendicionDto.de(recargada);
    }

    // ---------- Apoyo (también lo usan GastoService y PagoService) ----------
    public void aplicarEstado(Rendicion r, EstadoRendicion nuevo, Long usuarioId, String comentario) {
        String anterior = r.getEstado().name();
        r.setEstado(nuevo);
        if (nuevo == EstadoRendicion.ENVIADA) r.setFechaEnvio(LocalDateTime.now());
        rendicionRepo.save(r);
        anotar(r, anterior, nuevo.name(), usuarioId, comentario);
    }

    public void anotar(Rendicion r, String anterior, String nuevo, Long usuarioId, String comentario) {
        historialRepo.save(new HistorialEstado(r, anterior, nuevo, usuarioId, comentario));
    }

    public Rendicion buscar(Long id) {
        return rendicionRepo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Rendición no encontrada"));
    }

    public void exigirEditable(Rendicion r) {
        if (r.getEstado() != EstadoRendicion.BORRADOR && r.getEstado() != EstadoRendicion.OBSERVADA)
            throw new ResponseStatusException(HttpStatus.CONFLICT, "La rendición ya no se puede modificar");
    }

    // Las mismas reglas que RendicionValidator en la app: el servidor no confía en el cliente
    private void validar(RendicionRequest r) {
        if (r.hasta().isBefore(r.desde()))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La fecha de término no puede ser anterior al inicio");
        if (ChronoUnit.DAYS.between(r.desde(), r.hasta()) > 31)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El período no puede superar 31 días");
        if (r.tipo() == TipoRendicion.COMISION_SERVICIO && (r.destino() == null || r.destino().trim().length() < 3))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Indica el destino de la comisión");
        catalogo.buscarCentro(r.centroCosto());              // 404 si el centro de costo no existe
    }

    private void copiar(RendicionRequest r, Rendicion destino) {
        destino.setUsuarioId(r.usuarioId());
        destino.setTitulo(r.titulo().trim());
        destino.setTipo(r.tipo());
        destino.setDestino(r.tipo() == TipoRendicion.COMISION_SERVICIO ? r.destino().trim() : null);
        destino.setDesde(r.desde());
        destino.setHasta(r.hasta());
        destino.setCentroCosto(r.centroCosto().trim().toUpperCase());
        destino.setObservacion(r.observacion() == null || r.observacion().isBlank() ? null : r.observacion().trim());
    }
}
