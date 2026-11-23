package cl.teqmed.rendiciones.controller;

import cl.teqmed.rendiciones.dto.CatalogoDtos.*;
import cl.teqmed.rendiciones.service.CatalogoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/categorias")
public class CategoriaController {

    private final CatalogoService service;

    public CategoriaController(CatalogoService service) { this.service = service; }

    @GetMapping
    public List<CategoriaDto> listar() { return service.listarCategorias(); }

    @GetMapping("/{id}")
    public CategoriaDto obtener(@PathVariable Long id) { return service.obtenerCategoria(id); }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CategoriaDto crear(@Valid @RequestBody CategoriaRequest r) { return service.crearCategoria(r); }

    @PutMapping("/{id}")
    public CategoriaDto actualizar(@PathVariable Long id, @Valid @RequestBody CategoriaRequest r) {
        return service.actualizarCategoria(id, r);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable Long id) { service.eliminarCategoria(id); }
}
