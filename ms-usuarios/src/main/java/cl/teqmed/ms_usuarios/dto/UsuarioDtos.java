package cl.teqmed.ms_usuarios.dto;

import cl.teqmed.ms_usuarios.model.Rol;
import cl.teqmed.ms_usuarios.model.Usuario;
import jakarta.validation.constraints.*;

public final class UsuarioDtos {

    // Lo que el servidor DEVUELVE (sin la clave)
    public record UsuarioDto(Long id, String nombre, String correo, Rol rol, Long jefaturaId, boolean tieneTarjeta) {
        public static UsuarioDto de(Usuario u) {
            return new UsuarioDto(u.getId(), u.getNombre(), u.getCorreo(), u.getRol(), u.getJefaturaId(),
                    u.isTieneTarjeta());
        }
    }

    // Lo que el servidor RECIBE para crear o actualizar
    public record UsuarioRequest(
            @NotBlank @Size(max = 80) String nombre,
            @NotBlank @Email String correo,
            @NotBlank @Size(min = 8) String clave,
            @NotNull Rol rol,
            Long jefaturaId,
            boolean tieneTarjeta) {
    }

    public record LoginRequest(@NotBlank @Email String correo, @NotBlank String clave) {
    }
}