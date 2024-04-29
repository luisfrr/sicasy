package gob.yucatan.sicasy.repository.iface;

import gob.yucatan.sicasy.business.entities.Anexo;
import gob.yucatan.sicasy.business.entities.Licitacion;
import gob.yucatan.sicasy.business.enums.EstatusRegistro;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;

public interface IAnexoRepository extends JpaRepository<Anexo, Long>, JpaSpecificationExecutor<Anexo> {

    Optional<Anexo> findByNombre(String nombre);

    List<Anexo> findByLicitacionAndEstatusRegistro(Licitacion licitacion, EstatusRegistro estatusRegistro);

    List<Anexo> findByNombreAndEstatusRegistroAndLicitacion(String nombre, EstatusRegistro estatusRegistro, Licitacion licitacion);

}
