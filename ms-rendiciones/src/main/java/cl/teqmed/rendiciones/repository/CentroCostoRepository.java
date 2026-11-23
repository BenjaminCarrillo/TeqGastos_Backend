package cl.teqmed.rendiciones.repository;

import cl.teqmed.rendiciones.model.CentroCosto;
import org.springframework.data.jpa.repository.JpaRepository;

// La clave primaria es el código (String)
public interface CentroCostoRepository extends JpaRepository<CentroCosto, String> { }
