package gob.yucatan.sicasy.repository.iface;

import gob.yucatan.sicasy.business.entities.MovimientoPoliza;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface IMovimientoPolizaRepository extends JpaRepository<MovimientoPoliza, Long>, JpaSpecificationExecutor<MovimientoPoliza> {
}
