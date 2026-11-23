package cl.teqmed.rendiciones.repository;

import cl.teqmed.rendiciones.model.HistorialEstado;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface HistorialRepository extends JpaRepository<HistorialEstado, Long> {
    List<HistorialEstado> findByRendicion_IdOrderByFechaAscIdAsc(Long rendicionId);
}
