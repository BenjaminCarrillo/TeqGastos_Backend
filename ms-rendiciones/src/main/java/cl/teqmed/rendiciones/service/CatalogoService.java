package cl.teqmed.rendiciones.service;

import cl.teqmed.rendiciones.dto.CatalogoDtos.*;
import cl.teqmed.rendiciones.model.Categoria;
import cl.teqmed.rendiciones.model.CentroCosto;
import cl.teqmed.rendiciones.repository.CategoriaRepository;
import cl.teqmed.rendiciones.repository.CentroCostoRepository;
import cl.teqmed.rendiciones.repository.GastoRepository;
import cl.teqmed.rendiciones.repository.RendicionRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import java.util.List;

// Categorías y centros de costo: CRUD simple, con dos reglas:
// no se repite el nombre/código y no se borra lo que ya está en uso
@Service
public class CatalogoService {

    private final CategoriaRepository categoriaRepo;
    private final CentroCostoRepository centroRepo;
    private final GastoRepository gastoRepo;
    private final RendicionRepository rendicionRepo;

    public CatalogoService(CategoriaRepository categoriaRepo, CentroCostoRepository centroRepo,
                           GastoRepository gastoRepo, RendicionRepository rendicionRepo) {
        this.categoriaRepo = categoriaRepo;
        this.centroRepo = centroRepo;
        this.gastoRepo = gastoRepo;
        this.rendicionRepo = rendicionRepo;
    }

    // ---------- Categorías ----------
    public List<CategoriaDto> listarCategorias() {
        return categoriaRepo.findAll().stream().map(CategoriaDto::de).toList();
    }

    public CategoriaDto obtenerCategoria(Long id) {
        return CategoriaDto.de(buscarCategoria(id));
    }

    @Transactional
    public CategoriaDto crearCategoria(CategoriaRequest r) {
        if (categoriaRepo.existsByNombreIgnoreCase(r.nombre().trim()))
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ya existe una categoría con ese nombre");
        Categoria c = new Categoria();
        copiar(r, c);
        return CategoriaDto.de(categoriaRepo.save(c));
    }

    @Transactional
    public CategoriaDto actualizarCategoria(Long id, CategoriaRequest r) {
        Categoria c = buscarCategoria(id);
        copiar(r, c);
        return CategoriaDto.de(categoriaRepo.save(c));
    }

    @Transactional
    public void eliminarCategoria(Long id) {
        Categoria c = buscarCategoria(id);
        if (gastoRepo.existsByCategoria_Id(id))
            throw new ResponseStatusException(HttpStatus.CONFLICT, "La categoría tiene gastos asociados");
        categoriaRepo.delete(c);
    }

    private void copiar(CategoriaRequest r, Categoria c) {
        c.setNombre(r.nombre().trim());
        c.setTopePorGasto(r.topePorGasto());
        c.setTopeDiario(r.topeDiario());
        c.setPermiteExceso(r.permiteExceso());
    }

    public Categoria buscarCategoria(Long id) {
        return categoriaRepo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Categoría no encontrada"));
    }

    // ---------- Centros de costo ----------
    public List<CentroCostoDto> listarCentros() {
        return centroRepo.findAll().stream().map(CentroCostoDto::de).toList();
    }

    public CentroCostoDto obtenerCentro(String codigo) {
        return CentroCostoDto.de(buscarCentro(codigo));
    }

    @Transactional
    public CentroCostoDto crearCentro(CentroCostoRequest r) {
        String codigo = r.codigo().trim().toUpperCase();
        if (centroRepo.existsById(codigo))
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ya existe un centro de costo con ese código");
        CentroCosto c = new CentroCosto();
        c.setCodigo(codigo);
        c.setNombre(r.nombre().trim());
        return CentroCostoDto.de(centroRepo.save(c));
    }

    // El código es la clave: solo se cambia el nombre
    @Transactional
    public CentroCostoDto actualizarCentro(String codigo, CentroCostoRequest r) {
        CentroCosto c = buscarCentro(codigo);
        c.setNombre(r.nombre().trim());
        return CentroCostoDto.de(centroRepo.save(c));
    }

    @Transactional
    public void eliminarCentro(String codigo) {
        CentroCosto c = buscarCentro(codigo);
        if (rendicionRepo.existsByCentroCosto(c.getCodigo()))
            throw new ResponseStatusException(HttpStatus.CONFLICT, "El centro de costo tiene rendiciones asociadas");
        centroRepo.delete(c);
    }

    public CentroCosto buscarCentro(String codigo) {
        return centroRepo.findById(codigo.trim().toUpperCase())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Centro de costo no encontrado"));
    }
}
