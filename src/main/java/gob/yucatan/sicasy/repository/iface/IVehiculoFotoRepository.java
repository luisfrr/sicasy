package gob.yucatan.sicasy.repository.iface;

import gob.yucatan.sicasy.business.entities.VehiculoFoto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface IVehiculoFotoRepository extends JpaRepository<VehiculoFoto, Long>, JpaSpecificationExecutor<VehiculoFoto> {
}
