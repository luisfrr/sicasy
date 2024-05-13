package gob.yucatan.sicasy.repository.iface;

import gob.yucatan.sicasy.business.entities.EstatusPoliza;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface IEstatusPolizaRepository extends JpaRepository<EstatusPoliza, Integer>, JpaSpecificationExecutor<EstatusPoliza> {

}
