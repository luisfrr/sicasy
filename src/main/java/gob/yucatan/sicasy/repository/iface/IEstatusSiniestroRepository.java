package gob.yucatan.sicasy.repository.iface;

import gob.yucatan.sicasy.business.entities.EstatusSiniestro;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IEstatusSiniestroRepository extends JpaRepository<EstatusSiniestro, Integer> {
}
