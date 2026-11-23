package cl.teqmed.rendiciones.controller;

import cl.teqmed.rendiciones.dto.RendicionDtos.*;
import cl.teqmed.rendiciones.model.enums.EstadoRendicion;
import cl.teqmed.rendiciones.service.RendicionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/rendiciones")
public class RendicionController {

    private final RendicionService service;

    public RendicionController(RendicionService service) { this.service = service; }

    // GET /api/rendiciones?usuarioId=1
    // GET /api/rendiciones?usuarioIds=1,2,3&estado=ENVIADA
    // GET /api/rendiciones?estado=APROBADA_JEFATURA
    @GetMapping
    public List<RendicionDto> listar(@RequestParam(required = false) Long usuarioId,
                                     @RequestParam(required = false) EstadoRendicion estado,
                                     @RequestParam(required = false) List<Long> usuarioIds) {
        return service.listar(usuarioId, estado, usuarioIds);
    }

    @GetMapping("/{id}")
    public RendicionDto obtener(@PathVariable Long id) { return service.obtener(id); }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public RendicionDto crear(@Valid @RequestBody RendicionRequest r) { return service.crear(r); }

    @PutMapping("/{id}")
    public RendicionDto actualizar(@PathVariable Long id, @Valid @RequestBody RendicionRequest r) {
        return service.actualizar(id, r);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable Long id) { service.eliminar(id); }

    // ---------- Flujo de aprobación ----------
    @PostMapping("/{id}/enviar")                       // técnico
    public RendicionDto enviar(@PathVariable Long id, @Valid @RequestBody AccionRequest a) {
        return service.enviar(id, a);
    }

    @PostMapping("/{id}/estado")                       // jefatura finaliza / finanzas rechaza
    public RendicionDto cambiarEstado(@PathVariable Long id, @Valid @RequestBody CambioEstadoRequest c) {
        return service.cambiarEstado(id, c);
    }

    @PostMapping("/{id}/aprobar-finanzas")             // finanzas aprueba
    public RendicionDto aprobarFinanzas(@PathVariable Long id, @Valid @RequestBody AccionRequest a) {
        return service.aprobarFinanzas(id, a);
    }

    @GetMapping("/{id}/historial")
    public List<HistorialDto> historial(@PathVariable Long id) { return service.historial(id); }
}
