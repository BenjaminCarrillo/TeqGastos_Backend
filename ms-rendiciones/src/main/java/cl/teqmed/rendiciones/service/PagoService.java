package cl.teqmed.rendiciones.service;

import cl.teqmed.rendiciones.dto.PagoDtos.*;
import cl.teqmed.rendiciones.model.Pago;
import cl.teqmed.rendiciones.model.Rendicion;
import cl.teqmed.rendiciones.model.enums.EstadoRendicion;
import cl.teqmed.rendiciones.repository.PagoRepository;
import cl.teqmed.rendiciones.repository.RendicionRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import java.time.LocalDate;
import java.util.List;

@Service
public class PagoService {

    private final PagoRepository pagoRepo;
    private final RendicionRepository rendicionRepo;
    private final RendicionService rendiciones;

    public PagoService(PagoRepository pagoRepo, RendicionRepository rendicionRepo, RendicionService rendiciones) {
        this.pagoRepo = pagoRepo;
        this.rendicionRepo = rendicionRepo;
        this.rendiciones = rendiciones;
    }

    public List<PagoDto> listar() {
        return pagoRepo.findAllByOrderByIdDesc().stream().map(PagoDto::de).toList();
    }

    // Todo en una transacción: se crea el pago y cada rendición queda PAGADA, o no pasa nada
    @Transactional
    public PagoDto registrar(PagoRequest r) {
        List<Rendicion> lista = rendicionRepo.findAllById(r.rendicionIds());
        if (lista.size() != r.rendicionIds().stream().distinct().count())
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Alguna rendición no existe");
        if (lista.stream().anyMatch(x -> x.getEstado() != EstadoRendicion.APROBADA_FINANZAS))
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Solo se pagan rendiciones aprobadas por Finanzas");
        if (lista.stream().map(Rendicion::getUsuarioId).distinct().count() > 1)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Solo puedes pagar juntas rendiciones del mismo técnico");

        Pago pago = new Pago();
        pago.setFecha(r.fecha() != null ? r.fecha() : LocalDate.now());
        pago.setNumeroOperacion(r.numeroOperacion().trim().toUpperCase());
        pago.setMonto(r.monto());
        pago.setObservacion(r.observacion() == null || r.observacion().isBlank() ? null : r.observacion().trim());
        pago.setUsuarioFinanzasId(r.usuarioFinanzasId());
        pagoRepo.save(pago);

        for (Rendicion rendicion : lista) {
            rendicion.setPagoId(pago.getId());
            rendiciones.aplicarEstado(rendicion, EstadoRendicion.PAGADA, r.usuarioFinanzasId(),
                    "Pago " + pago.getNumeroOperacion());
        }
        return PagoDto.de(pago);
    }
}
