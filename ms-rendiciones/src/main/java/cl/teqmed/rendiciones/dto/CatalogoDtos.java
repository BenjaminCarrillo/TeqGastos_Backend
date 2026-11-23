package cl.teqmed.rendiciones.dto;

import cl.teqmed.rendiciones.model.Categoria;
import cl.teqmed.rendiciones.model.CentroCosto;
import jakarta.validation.constraints.*;

// DTO = lo que viaja como JSON. Así la base de datos no queda expuesta tal cual.
public final class CatalogoDtos {

    public record CategoriaDto(Long id, String nombre, Integer topePorGasto, Integer topeDiario, boolean permiteExceso) {
        public static CategoriaDto de(Categoria c) {
            return new CategoriaDto(c.getId(), c.getNombre(), c.getTopePorGasto(), c.getTopeDiario(), c.isPermiteExceso());
        }
    }

    public record CategoriaRequest(
            @NotBlank @Size(max = 60) String nombre,
            @PositiveOrZero Integer topePorGasto,
            @PositiveOrZero Integer topeDiario,
            boolean permiteExceso) { }

    public record CentroCostoDto(String codigo, String nombre) {
        public static CentroCostoDto de(CentroCosto c) { return new CentroCostoDto(c.getCodigo(), c.getNombre()); }
    }

    public record CentroCostoRequest(
            @NotBlank @Size(max = 20) String codigo,
            @NotBlank @Size(max = 80) String nombre) { }
}
