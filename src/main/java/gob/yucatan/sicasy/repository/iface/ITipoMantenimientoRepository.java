package gob.yucatan.sicasy.repository.iface;

import gob.yucatan.sicasy.business.entities.TipoMantenimiento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface ITipoMantenimientoRepository extends JpaRepository<TipoMantenimiento, Integer>, JpaSpecificationExecutor<TipoMantenimiento> {
}
