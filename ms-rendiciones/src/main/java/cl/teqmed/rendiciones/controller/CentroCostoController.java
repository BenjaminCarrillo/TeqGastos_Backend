package cl.teqmed.rendiciones.controller;

import cl.teqmed.rendiciones.dto.CatalogoDtos.*;
import cl.teqmed.rendiciones.service.CatalogoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/centros-costo")
public class CentroCostoController {

    private final CatalogoService service;

    public CentroCostoController(CatalogoService service) { this.service = service; }

    @GetMapping
    public List<CentroCostoDto> listar() { return service.listarCentros(); }

    @GetMapping("/{codigo}")
    public CentroCostoDto obtener(@PathVariable String codigo) { return service.obtenerCentro(codigo); }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CentroCostoDto crear(@Valid @RequestBody CentroCostoRequest r) { return service.crearCentro(r); }

    @PutMapping("/{codigo}")
    public CentroCostoDto actualizar(@PathVariable String codigo, @Valid @RequestBody CentroCostoRequest r) {
        return service.actualizarCentro(codigo, r);
    }

    @DeleteMapping("/{codigo}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable String codigo) { service.eliminarCentro(codigo); }
}
