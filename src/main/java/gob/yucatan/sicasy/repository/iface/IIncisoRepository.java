package gob.yucatan.sicasy.repository.iface;

import gob.yucatan.sicasy.business.entities.Inciso;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface IIncisoRepository extends JpaRepository<Inciso, Long>, JpaSpecificationExecutor<Inciso> {
}
