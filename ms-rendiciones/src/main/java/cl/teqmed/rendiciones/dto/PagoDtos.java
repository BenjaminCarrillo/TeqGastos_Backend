package cl.teqmed.rendiciones.dto;

import cl.teqmed.rendiciones.model.Pago;
import jakarta.validation.constraints.*;
import java.time.LocalDate;
import java.util.List;

public final class PagoDtos {

    public record PagoDto(Long id, LocalDate fecha, String numeroOperacion, int monto,
                          String observacion, Long usuarioFinanzasId) {
        public static PagoDto de(Pago p) {
            return new PagoDto(p.getId(), p.getFecha(), p.getNumeroOperacion(), p.getMonto(),
                    p.getObservacion(), p.getUsuarioFinanzasId());
        }
    }

    public record PagoRequest(
            LocalDate fecha,                                         // si no viene, se usa la fecha de hoy
            @NotBlank @Size(min = 4, max = 20) String numeroOperacion,
            @PositiveOrZero int monto,
            @Size(max = 200) String observacion,
            @NotNull Long usuarioFinanzasId,
            @NotEmpty List<Long> rendicionIds) { }
}
