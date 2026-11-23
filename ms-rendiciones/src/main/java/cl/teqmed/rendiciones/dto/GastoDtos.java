package cl.teqmed.rendiciones.dto;

import cl.teqmed.rendiciones.model.Gasto;
import cl.teqmed.rendiciones.model.enums.*;
import jakarta.validation.constraints.*;
import java.time.LocalDate;

public final class GastoDtos {

    public record GastoDto(Long id, Long rendicionId, Long categoriaId, LocalDate fechaDocumento,
                           TipoDocumento tipoDocumento, String rutEmisor, String folio, String proveedor,
                           int monto, int montoAdmitido, String justificacion, MedioPago medioPago,
                           String centroCosto, String descripcion, String ordenTrabajo, String rutaFoto,
                           Double latitud, Double longitud, EstadoGasto estado, Integer montoAprobado,
                           String comentarioRevision, MotivoRechazo motivoRechazo, String respuestaTecnico) {
        public static GastoDto de(Gasto g) {
            return new GastoDto(g.getId(), g.getRendicion().getId(), g.getCategoria().getId(), g.getFechaDocumento(),
                    g.getTipoDocumento(), g.getRutEmisor(), g.getFolio(), g.getProveedor(), g.getMonto(),
                    g.getMontoAdmitido(), g.getJustificacion(), g.getMedioPago(), g.getCentroCosto(),
                    g.getDescripcion(), g.getOrdenTrabajo(), g.getRutaFoto(), g.getLatitud(), g.getLongitud(),
                    g.getEstado(), g.getMontoAprobado(), g.getComentarioRevision(), g.getMotivoRechazo(),
                    g.getRespuestaTecnico());
        }
    }

    // Crear o editar un gasto (el estado lo maneja el servidor)
    public record GastoRequest(
            @NotNull Long rendicionId,
            @NotNull Long categoriaId,
            @NotNull @PastOrPresent LocalDate fechaDocumento,
            @NotNull TipoDocumento tipoDocumento,
            @Size(max = 12) String rutEmisor,
            @Size(max = 10) String folio,
            @NotBlank @Size(max = 80) String proveedor,
            @Positive int monto,
            @PositiveOrZero int montoAdmitido,
            @Size(max = 200) String justificacion,
            @NotNull MedioPago medioPago,
            @NotBlank String centroCosto,
            @NotBlank @Size(max = 200) String descripcion,
            @Size(max = 40) String ordenTrabajo,
            @Size(max = 255) String rutaFoto,
            Double latitud,
            Double longitud,
            @Size(max = 200) String respuestaTecnico) { }

    // Decisión de la jefatura sobre un gasto
    public record ResolucionRequest(
            @NotNull EstadoGasto estado,
            @NotNull Long usuarioId,
            @Positive Integer montoAprobado,
            @Size(max = 200) String comentario,
            MotivoRechazo motivo) { }

    // Respuesta de /duplicado: el número de la rendición donde ya está, o null si no hay duplicado
    public record DuplicadoDto(String rendicion) { }

    // Respuesta de /acumulado
    public record AcumuladoDto(long total) { }
}
