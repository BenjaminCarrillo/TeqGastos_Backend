package cl.teqmed.ms_usuarios.repository;

import cl.teqmed.ms_usuarios.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

// JpaRepository ya trae findAll, findById, save y deleteById.
// Estos métodos extra los escribe Spring a partir de su nombre (como el @Query de Room, pero sin SQL)
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    Optional<Usuario> findByCorreoIgnoreCase(String correo);

    boolean existsByCorreoIgnoreCase(String correo);

    List<Usuario> findByJefaturaId(Long jefaturaId);
}