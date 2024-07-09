package gob.yucatan.sicasy.repository.iface;

import gob.yucatan.sicasy.business.entities.BitacoraDeducible;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface IBitacoraDeducibleRepository extends JpaRepository<BitacoraDeducible, Long>, JpaSpecificationExecutor<BitacoraDeducible> {
}
