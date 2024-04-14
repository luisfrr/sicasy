package gob.yucatan.sicasy.repository.iface;

import gob.yucatan.sicasy.business.entities.Poliza;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface IPolizaRepository extends CrudRepository<Poliza, Integer>, JpaSpecificationExecutor<Poliza> {

    Optional<Poliza> findById(Integer id);

}
