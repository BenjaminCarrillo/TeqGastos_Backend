package cl.teqmed.rendiciones.dto;

import cl.teqmed.rendiciones.model.HistorialEstado;
import cl.teqmed.rendiciones.model.Rendicion;
import cl.teqmed.rendiciones.model.enums.EstadoRendicion;
import cl.teqmed.rendiciones.model.enums.TipoRendicion;
import jakarta.validation.constraints.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

public final class RendicionDtos {

    public record RendicionDto(Long id, String numero, Long usuarioId, String titulo, TipoRendicion tipo,
                               String destino, LocalDate desde, LocalDate hasta, String centroCosto,
                               String observacion, EstadoRendicion estado, LocalDateTime fechaEnvio, Long pagoId) {
        public static RendicionDto de(Rendicion r) {
            return new RendicionDto(r.getId(), r.getNumero(), r.getUsuarioId(), r.getTitulo(), r.getTipo(),
                    r.getDestino(), r.getDesde(), r.getHasta(), r.getCentroCosto(), r.getObservacion(),
                    r.getEstado(), r.getFechaEnvio(), r.getPagoId());
        }
    }

    // Crear o editar una rendición (el número y el estado los pone el servidor)
    public record RendicionRequest(
            @NotNull Long usuarioId,
            @NotBlank @Size(min = 5, max = 80) String titulo,
            @NotNull TipoRendicion tipo,
            @Size(max = 80) String destino,
            @NotNull LocalDate desde,
            @NotNull LocalDate hasta,
            @NotBlank String centroCosto,
            @Size(max = 200) String observacion) { }

    // Para enviar o aprobar: quién lo hace y un comentario opcional
    public record AccionRequest(@NotNull Long usuarioId, @Size(max = 200) String comentario) { }

    // Para que la jefatura finalice o Finanzas rechace
    public record CambioEstadoRequest(@NotNull EstadoRendicion estado, @NotNull Long usuarioId,
                                      @Size(max = 200) String comentario) { }

    public record HistorialDto(Long id, Long rendicionId, String estadoAnterior, String estadoNuevo,
                               Long usuarioId, String comentario, LocalDateTime fecha) {
        public static HistorialDto de(HistorialEstado h) {
            return new HistorialDto(h.getId(), h.getRendicion().getId(), h.getEstadoAnterior(), h.getEstadoNuevo(),
                    h.getUsuarioId(), h.getComentario(), h.getFecha());
        }
    }
}
