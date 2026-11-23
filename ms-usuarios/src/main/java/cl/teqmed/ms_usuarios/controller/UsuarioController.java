package cl.teqmed.ms_usuarios.controller;

import cl.teqmed.ms_usuarios.dto.UsuarioDtos.*;
import cl.teqmed.ms_usuarios.service.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {

    private final UsuarioService service;

    public UsuarioController(UsuarioService service) {
        this.service = service;
    }

    @GetMapping // GET /api/usuarios o /api/usuarios?jefaturaId=5
    public List<UsuarioDto> listar(@RequestParam(required = false) Long jefaturaId) {
        return service.listar(jefaturaId);
    }

    @GetMapping("/{id}") // GET /api/usuarios/1
    public UsuarioDto obtener(@PathVariable Long id) {
        return service.obtener(id);
    }

    @PostMapping // POST /api/usuarios
    @ResponseStatus(HttpStatus.CREATED)
    public UsuarioDto crear(@Valid @RequestBody UsuarioRequest r) {
        return service.crear(r);
    }

    @PutMapping("/{id}") // PUT /api/usuarios/1
    public UsuarioDto actualizar(@PathVariable Long id, @Valid @RequestBody UsuarioRequest r) {
        return service.actualizar(id, r);
    }

    @DeleteMapping("/{id}") // DELETE /api/usuarios/1
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable Long id) {
        service.eliminar(id);
    }

    @PostMapping("/login") // POST /api/usuarios/login
    public UsuarioDto login(@Valid @RequestBody LoginRequest r) {
        return service.login(r);
    }
}