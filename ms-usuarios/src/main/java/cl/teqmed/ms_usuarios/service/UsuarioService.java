package cl.teqmed.ms_usuarios.service;

import cl.teqmed.ms_usuarios.dto.UsuarioDtos.*;
import cl.teqmed.ms_usuarios.model.Usuario;
import cl.teqmed.ms_usuarios.repository.UsuarioRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import java.util.List;

@Service
public class UsuarioService {

    private final UsuarioRepository repo;

    public UsuarioService(UsuarioRepository repo) {
        this.repo = repo;
    } // Spring lo inyecta solo

    public List<UsuarioDto> listar(Long jefaturaId) {
        List<Usuario> lista = (jefaturaId == null) ? repo.findAll() : repo.findByJefaturaId(jefaturaId);
        return lista.stream().map(UsuarioDto::de).toList();
    }

    public UsuarioDto obtener(Long id) {
        return UsuarioDto.de(buscar(id));
    }

    public UsuarioDto crear(UsuarioRequest r) {
        if (repo.existsByCorreoIgnoreCase(r.correo()))
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ya existe un usuario con ese correo");
        Usuario u = new Usuario(r.nombre(), r.correo().trim().toLowerCase(), r.clave(),
                r.rol(), r.jefaturaId(), r.tieneTarjeta());
        return UsuarioDto.de(repo.save(u));
    }

    public UsuarioDto actualizar(Long id, UsuarioRequest r) {
        Usuario u = buscar(id);
        u.setNombre(r.nombre());
        u.setCorreo(r.correo().trim().toLowerCase());
        u.setClave(r.clave());
        u.setRol(r.rol());
        u.setJefaturaId(r.jefaturaId());
        u.setTieneTarjeta(r.tieneTarjeta());
        return UsuarioDto.de(repo.save(u)); // save con id existente = UPDATE
    }

    public void eliminar(Long id) {
        repo.delete(buscar(id));
    }

    // Mismo criterio que en la app: un solo mensaje, sin decir si falló el correo o
    // la clave
    public UsuarioDto login(LoginRequest r) {
        return repo.findByCorreoIgnoreCase(r.correo().trim())
                .filter(u -> u.getClave().equals(r.clave()))
                .map(UsuarioDto::de)
                .orElseThrow(
                        () -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Correo o contraseña incorrectos"));
    }

    private Usuario buscar(Long id) {
        return repo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));
    }
}