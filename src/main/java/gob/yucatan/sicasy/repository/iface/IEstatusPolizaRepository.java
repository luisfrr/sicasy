package gob.yucatan.sicasy.repository.iface;

import gob.yucatan.sicasy.business.entities.EstatusInciso;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface IEstatusPolizaRepository extends JpaRepository<EstatusInciso, Integer>, JpaSpecificationExecutor<EstatusInciso> {

}
