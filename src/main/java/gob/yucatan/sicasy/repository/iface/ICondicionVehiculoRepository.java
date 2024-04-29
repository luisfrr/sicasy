package gob.yucatan.sicasy.repository.iface;

import gob.yucatan.sicasy.business.entities.CondicionVehiculo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface ICondicionVehiculoRepository extends JpaRepository<CondicionVehiculo, Integer>, JpaSpecificationExecutor<CondicionVehiculo> {
}
