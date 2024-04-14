package gob.yucatan.sicasy.repository.iface;

import gob.yucatan.sicasy.business.entities.Anexo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface IAnexoRepository extends JpaRepository<Anexo, Long>, JpaSpecificationExecutor<Anexo> {


}
