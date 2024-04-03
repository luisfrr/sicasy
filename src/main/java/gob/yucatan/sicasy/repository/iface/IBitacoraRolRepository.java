package gob.yucatan.sicasy.repository.iface;

import gob.yucatan.sicasy.business.entities.BitacoraRol;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface IBitacoraRolRepository extends JpaRepository<Long, BitacoraRol>, JpaSpecificationExecutor<BitacoraRol> {
}
