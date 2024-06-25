package gob.yucatan.sicasy.repository.iface;

import gob.yucatan.sicasy.business.entities.SiniestroFoto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface ISiniestroFotoRepository extends JpaRepository<SiniestroFoto, Long>, JpaSpecificationExecutor<SiniestroFoto> {
}
