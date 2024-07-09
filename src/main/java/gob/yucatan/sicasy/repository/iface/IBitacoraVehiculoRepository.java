package gob.yucatan.sicasy.repository.iface;

import gob.yucatan.sicasy.business.entities.BitacoraVehiculo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface IBitacoraVehiculoRepository extends JpaRepository<BitacoraVehiculo, Long>, JpaSpecificationExecutor<BitacoraVehiculo> {
}
