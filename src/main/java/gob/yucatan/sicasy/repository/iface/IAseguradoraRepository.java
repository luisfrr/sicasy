package gob.yucatan.sicasy.repository.iface;

import gob.yucatan.sicasy.business.entities.Aseguradora;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface IAseguradoraRepository extends JpaRepository<Aseguradora, Integer>, JpaSpecificationExecutor<Aseguradora> {

    Optional<Aseguradora> findByNombreIgnoreCase(String nombre);

}
