package cl.teqmed.rendiciones.service;

import cl.teqmed.rendiciones.dto.GastoDtos.*;
import cl.teqmed.rendiciones.model.Gasto;
import cl.teqmed.rendiciones.model.Rendicion;
import cl.teqmed.rendiciones.model.enums.*;
import cl.teqmed.rendiciones.repository.GastoRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import java.time.LocalDate;
import java.util.List;

@Service
public class GastoService {

    private final GastoRepository gastoRepo;
    private final RendicionService rendiciones;
    private final CatalogoService catalogo;

    public GastoService(GastoRepository gastoRepo, RendicionService rendiciones, CatalogoService catalogo) {
        this.gastoRepo = gastoRepo;
        this.rendiciones = rendiciones;
        this.catalogo = catalogo;
    }

    // ---------- Consultas ----------
    //   ?rendicionId=1                → gastos de una rendición
    //   ?usuarioId=1                  → todos los gastos de un técnico (Inicio, Mis rendiciones)
    //   ?usuarioIds=1,2,3             → los del equipo de una jefatura
    //   ?estadoRendicion=APROBADA_JEFATURA → los de las rendiciones en ese estado (Finanzas)
    public List<GastoDto> listar(Long rendicionId, Long usuarioId, List<Long> usuarioIds, EstadoRendicion estadoRendicion) {
        List<Gasto> lista;
        if (rendicionId != null) lista = gastoRepo.findByRendicion_IdOrderByFechaDocumentoAsc(rendicionId);
        else if (usuarioId != null) lista = gastoRepo.findByRendicion_UsuarioId(usuarioId);
        else if (usuarioIds != null && !usuarioIds.isEmpty()) lista = gastoRepo.findByRendicion_UsuarioIdIn(usuarioIds);
        else if (estadoRendicion != null) lista = gastoRepo.findByRendicion_Estado(estadoRendicion);
        else lista = gastoRepo.findAll();
        return lista.stream().map(GastoDto::de).toList();
    }

    public GastoDto obtener(Long id) {
        return GastoDto.de(buscar(id));
    }

    // RN-02 para la app: ¿en qué rendición está ya ese comprobante? (null = en ninguna)
    public DuplicadoDto duplicado(String rut, String folio, Long excluirId) {
        if (rut == null || folio == null || !RutUtil.esValido(rut)) return new DuplicadoDto(null);
        return gastoRepo.buscarDuplicado(RutUtil.normalizar(rut), folio.trim(), excluirId == null ? 0L : excluirId)
                .map(g -> new DuplicadoDto(g.getRendicion().getNumero()))
                .orElse(new DuplicadoDto(null));
    }

    // RN-01 para la app: lo ya gastado ese día en esa categoría (para el tope diario)
    public AcumuladoDto acumulado(Long usuarioId, Long categoriaId, LocalDate fecha, Long excluirId) {
        return new AcumuladoDto(gastoRepo.acumuladoDelDia(usuarioId, categoriaId, fecha,
                excluirId == null ? 0L : excluirId, EstadoGasto.RECHAZADO));
    }

    // ---------- CRUD (técnico) ----------
    @Transactional
    public GastoDto crear(GastoRequest r) {
        Rendicion rendicion = rendiciones.buscar(r.rendicionId());
        rendiciones.exigirEditable(rendicion);
        Gasto g = new Gasto();
        g.setRendicion(rendicion);
        g.setEstado(EstadoGasto.BORRADOR);
        copiar(r, g, 0L);
        return GastoDto.de(guardar(g));
    }

    @Transactional
    public GastoDto actualizar(Long id, GastoRequest r) {
        Gasto g = buscar(id);
        exigirEditable(g);
        copiar(r, g, id);                                    // el estado no cambia: lo maneja el flujo
        return GastoDto.de(guardar(g));
    }

    @Transactional
    public void eliminar(Long id) {
        Gasto g = buscar(id);
        exigirEditable(g);
        gastoRepo.delete(g);
    }

    // ---------- Revisión (jefatura) ----------
    @Transactional
    public GastoDto resolver(Long id, ResolucionRequest r) {
        Gasto g = buscar(id);
        Rendicion rendicion = g.getRendicion();
        if (rendicion.getEstado() != EstadoRendicion.ENVIADA)
            throw new ResponseStatusException(HttpStatus.CONFLICT, "La rendición no está en revisión");

        switch (r.estado()) {
            case APROBADO -> {
                int aprobado = r.montoAprobado() != null ? r.montoAprobado() : g.getMontoAdmitido();
                if (aprobado <= 0 || aprobado > g.getMonto())
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El monto aprobado no puede superar el del comprobante");
                g.setMontoAprobado(aprobado);
                g.setMotivoRechazo(null);
            }
            case OBSERVADO -> {
                if (r.comentario() == null || r.comentario().trim().length() < 10)
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Explica qué debe corregir el técnico (mínimo 10 caracteres)");
                g.setMontoAprobado(null);
                g.setMotivoRechazo(null);
                g.setRespuestaTecnico(null);                 // el técnico debe responder de nuevo
            }
            case RECHAZADO -> {
                if (r.motivo() == null)
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Elige el motivo del rechazo");
                if (r.motivo() == MotivoRechazo.OTRO && (r.comentario() == null || r.comentario().trim().length() < 10))
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Describe el motivo del rechazo (mínimo 10 caracteres)");
                g.setMontoAprobado(null);
                g.setMotivoRechazo(r.motivo());
            }
            default -> throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La decisión debe ser APROBADO, OBSERVADO o RECHAZADO");
        }

        String anterior = g.getEstado().name();
        g.setEstado(r.estado());
        g.setComentarioRevision(r.comentario() == null || r.comentario().isBlank() ? null : r.comentario().trim());
        gastoRepo.save(g);
        // Queda en el historial de la rendición: "GASTO_APROBADO · Copec Concepción"
        rendiciones.anotar(rendicion, anterior, "GASTO_" + r.estado().name(), r.usuarioId(), g.getProveedor());
        return GastoDto.de(g);
    }

    // ---------- Apoyo ----------
    // Copia los datos del request al gasto, aplicando las reglas del servidor
    private void copiar(GastoRequest r, Gasto g, Long excluirId) {
        g.setCategoria(catalogo.buscarCategoria(r.categoriaId()));
        catalogo.buscarCentro(r.centroCosto());

        boolean conRut = r.tipoDocumento() != TipoDocumento.TICKET;
        if (conRut) {
            if (r.rutEmisor() == null || !RutUtil.esValido(r.rutEmisor()))
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "RUT inválido, revisa el dígito verificador");
            if (r.folio() == null || r.folio().isBlank() || !r.folio().trim().chars().allMatch(Character::isDigit))
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El folio solo lleva números");
            String rut = RutUtil.normalizar(r.rutEmisor());
            gastoRepo.buscarDuplicado(rut, r.folio().trim(), excluirId).ifPresent(otro -> {
                throw new ResponseStatusException(HttpStatus.CONFLICT,
                        "Este comprobante ya está rendido en " + otro.getRendicion().getNumero());
            });
            g.setRutEmisor(rut);
            g.setFolio(r.folio().trim());
        } else {
            g.setRutEmisor(null);
            g.setFolio(null);
        }

        g.setFechaDocumento(r.fechaDocumento());
        g.setTipoDocumento(r.tipoDocumento());
        g.setProveedor(r.proveedor().trim());
        g.setMonto(r.monto());
        g.setMontoAdmitido(Math.min(r.montoAdmitido(), r.monto()));   // nunca más que el comprobante
        g.setJustificacion(vacioANull(r.justificacion()));
        g.setMedioPago(r.medioPago());
        g.setCentroCosto(r.centroCosto().trim().toUpperCase());
        g.setDescripcion(r.descripcion().trim());
        g.setOrdenTrabajo(vacioANull(r.ordenTrabajo()));
        g.setRutaFoto(vacioANull(r.rutaFoto()));
        g.setLatitud(r.latitud());
        g.setLongitud(r.longitud());
        if (r.respuestaTecnico() != null) g.setRespuestaTecnico(vacioANull(r.respuestaTecnico()));
    }

    // Respaldo: si dos gastos iguales llegan al mismo tiempo, el índice único de MySQL rechaza el segundo
    private Gasto guardar(Gasto g) {
        try {
            return gastoRepo.saveAndFlush(g);
        } catch (DataIntegrityViolationException e) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Este comprobante ya fue rendido");
        }
    }

    private void exigirEditable(Gasto g) {
        if (g.getEstado() != EstadoGasto.BORRADOR && g.getEstado() != EstadoGasto.OBSERVADO)
            throw new ResponseStatusException(HttpStatus.CONFLICT, "El gasto ya está en revisión o aprobado");
        rendiciones.exigirEditable(g.getRendicion());
    }

    private Gasto buscar(Long id) {
        return gastoRepo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Gasto no encontrado"));
    }

    private static String vacioANull(String s) {
        return (s == null || s.isBlank()) ? null : s.trim();
    }
}
