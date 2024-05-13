package gob.yucatan.sicasy.repository.iface;

import gob.yucatan.sicasy.business.entities.Poliza;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface IPolizaRepository extends JpaRepository<Poliza, Long>, JpaSpecificationExecutor<Poliza> {
}
