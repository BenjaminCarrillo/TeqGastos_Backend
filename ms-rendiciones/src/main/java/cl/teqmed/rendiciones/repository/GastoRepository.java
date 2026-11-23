package cl.teqmed.rendiciones.repository;

import cl.teqmed.rendiciones.model.Gasto;
import cl.teqmed.rendiciones.model.enums.EstadoGasto;
import cl.teqmed.rendiciones.model.enums.EstadoRendicion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface GastoRepository extends JpaRepository<Gasto, Long> {

    // "Rendicion_Id" = recorre la relación gasto.rendicion.id
    List<Gasto> findByRendicion_IdOrderByFechaDocumentoAsc(Long rendicionId);

    List<Gasto> findByRendicion_UsuarioId(Long usuarioId);

    List<Gasto> findByRendicion_UsuarioIdIn(Collection<Long> usuarioIds);

    List<Gasto> findByRendicion_Estado(EstadoRendicion estado);

    boolean existsByCategoria_Id(Long categoriaId);

    // RN-02: ¿ya existe ese RUT + folio en otro gasto? (excluirId = el gasto que se está editando)
    @Query("SELECT g FROM Gasto g WHERE g.rutEmisor = :rut AND g.folio = :folio AND g.id <> :excluirId")
    Optional<Gasto> buscarDuplicado(@Param("rut") String rut, @Param("folio") String folio,
                                    @Param("excluirId") Long excluirId);

    // RN-01: lo que el técnico ya gastó ese día en esa categoría (para el tope diario)
    @Query("""
        SELECT COALESCE(SUM(g.montoAdmitido), 0) FROM Gasto g
        WHERE g.rendicion.usuarioId = :usuarioId
          AND g.categoria.id = :categoriaId
          AND g.fechaDocumento = :fecha
          AND g.id <> :excluirId
          AND g.estado <> :rechazado
        """)
    long acumuladoDelDia(@Param("usuarioId") Long usuarioId, @Param("categoriaId") Long categoriaId,
                        @Param("fecha") LocalDate fecha, @Param("excluirId") Long excluirId,
                        @Param("rechazado") EstadoGasto rechazado);

    // Al enviar: todos los gastos en borrador u observados pasan a ENVIADO
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE Gasto g SET g.estado = :nuevo WHERE g.rendicion.id = :rendicionId AND g.estado IN :desde")
    int cambiarEstadoDeRendicion(@Param("rendicionId") Long rendicionId,
                                 @Param("desde") Collection<EstadoGasto> desde,
                                 @Param("nuevo") EstadoGasto nuevo);
}
