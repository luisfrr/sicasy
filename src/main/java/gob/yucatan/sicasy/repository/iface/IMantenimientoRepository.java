package gob.yucatan.sicasy.repository.iface;

import gob.yucatan.sicasy.business.entities.Mantenimiento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface IMantenimientoRepository extends JpaRepository<Mantenimiento, Long>, JpaSpecificationExecutor<Mantenimiento> {
}
