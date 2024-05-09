package gob.yucatan.sicasy.repository.iface;

import gob.yucatan.sicasy.business.entities.MantenimientoFoto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface IMantenimientoFotoRepository extends JpaRepository<MantenimientoFoto, Long>, JpaSpecificationExecutor<MantenimientoFoto> {
}
