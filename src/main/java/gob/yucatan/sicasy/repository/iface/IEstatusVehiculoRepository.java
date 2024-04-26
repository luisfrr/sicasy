package gob.yucatan.sicasy.repository.iface;

import gob.yucatan.sicasy.business.entities.EstatusVehiculo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface IEstatusVehiculoRepository extends JpaRepository<EstatusVehiculo, Integer>, JpaSpecificationExecutor<EstatusVehiculo> {
}
