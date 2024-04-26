package gob.yucatan.sicasy.repository.iface;

import gob.yucatan.sicasy.business.entities.Dependencia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface IDependenciaRepository extends JpaRepository<Dependencia, Integer>, JpaSpecificationExecutor<Dependencia> {
}
