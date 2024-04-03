package gob.yucatan.sicasy.repository.iface;

import gob.yucatan.sicasy.business.entities.Rol;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface IRolRepository extends JpaRepository<Long, Rol>, JpaSpecificationExecutor<Rol> {
}
