package gob.yucatan.sicasy.repository.iface;

import gob.yucatan.sicasy.business.entities.Deducible;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface IDeducibleRepository extends JpaRepository<Deducible, Long>, JpaSpecificationExecutor<Deducible> {
}
