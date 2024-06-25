package gob.yucatan.sicasy.repository.iface;

import gob.yucatan.sicasy.business.entities.Siniestro;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface ISiniestroRepository extends JpaRepository<Siniestro, Long>, JpaSpecificationExecutor<Siniestro> {
}
