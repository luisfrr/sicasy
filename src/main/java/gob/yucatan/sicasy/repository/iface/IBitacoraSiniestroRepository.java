package gob.yucatan.sicasy.repository.iface;

import gob.yucatan.sicasy.business.entities.BitacoraSiniestro;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface IBitacoraSiniestroRepository extends JpaRepository<BitacoraSiniestro, Long>, JpaSpecificationExecutor<BitacoraSiniestro> {
}
