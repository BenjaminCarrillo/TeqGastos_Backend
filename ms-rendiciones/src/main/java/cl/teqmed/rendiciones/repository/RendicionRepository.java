package cl.teqmed.rendiciones.repository;

import cl.teqmed.rendiciones.model.Rendicion;
import cl.teqmed.rendiciones.model.enums.EstadoRendicion;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Collection;
import java.util.List;

public interface RendicionRepository extends JpaRepository<Rendicion, Long> {

    // Mis rendiciones (técnico): la más nueva primero
    List<Rendicion> findByUsuarioIdOrderByIdDesc(Long usuarioId);

    // Finanzas: todas las de un estado, la más antigua primero
    List<Rendicion> findByEstadoOrderByFechaEnvioAsc(EstadoRendicion estado);

    // Bandeja de jefatura: las de su equipo (los ids vienen de ms-usuarios) en un estado
    List<Rendicion> findByUsuarioIdInAndEstadoOrderByFechaEnvioAsc(Collection<Long> usuarioIds, EstadoRendicion estado);

    boolean existsByCentroCosto(String centroCosto);
}
