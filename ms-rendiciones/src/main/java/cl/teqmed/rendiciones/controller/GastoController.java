package cl.teqmed.rendiciones.controller;

import cl.teqmed.rendiciones.dto.GastoDtos.*;
import cl.teqmed.rendiciones.model.enums.EstadoRendicion;
import cl.teqmed.rendiciones.service.GastoService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/gastos")
public class GastoController {

    private final GastoService service;

    public GastoController(GastoService service) { this.service = service; }

    // GET /api/gastos?rendicionId=1  |  ?usuarioId=1  |  ?usuarioIds=1,2,3  |  ?estadoRendicion=APROBADA_JEFATURA
    @GetMapping
    public List<GastoDto> listar(@RequestParam(required = false) Long rendicionId,
                                 @RequestParam(required = false) Long usuarioId,
                                 @RequestParam(required = false) List<Long> usuarioIds,
                                 @RequestParam(required = false) EstadoRendicion estadoRendicion) {
        return service.listar(rendicionId, usuarioId, usuarioIds, estadoRendicion);
    }

    // GET /api/gastos/duplicado?rut=76543210-3&folio=184001&excluirId=0
    @GetMapping("/duplicado")
    public DuplicadoDto duplicado(@RequestParam String rut, @RequestParam String folio,
                                  @RequestParam(required = false) Long excluirId) {
        return service.duplicado(rut, folio, excluirId);
    }

    // GET /api/gastos/acumulado?usuarioId=1&categoriaId=4&fecha=2026-11-23&excluirId=0
    @GetMapping("/acumulado")
    public AcumuladoDto acumulado(@RequestParam Long usuarioId, @RequestParam Long categoriaId,
                                  @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha,
                                  @RequestParam(required = false) Long excluirId) {
        return service.acumulado(usuarioId, categoriaId, fecha, excluirId);
    }

    @GetMapping("/{id}")
    public GastoDto obtener(@PathVariable Long id) { return service.obtener(id); }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public GastoDto crear(@Valid @RequestBody GastoRequest r) { return service.crear(r); }

    @PutMapping("/{id}")
    public GastoDto actualizar(@PathVariable Long id, @Valid @RequestBody GastoRequest r) {
        return service.actualizar(id, r);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable Long id) { service.eliminar(id); }

    @PostMapping("/{id}/resolver")                     // jefatura: aprobar, observar o rechazar
    public GastoDto resolver(@PathVariable Long id, @Valid @RequestBody ResolucionRequest r) {
        return service.resolver(id, r);
    }
}
