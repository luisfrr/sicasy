package gob.yucatan.sicasy.repository.iface;

import gob.yucatan.sicasy.business.entities.Licitacion;
import gob.yucatan.sicasy.business.enums.EstatusRegistro;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;

public interface ILicitacionRepository extends JpaRepository<Licitacion, Integer>, JpaSpecificationExecutor<Licitacion> {

    Optional<Licitacion> findByNombreIgnoreCase(String nombre);

    Optional<Licitacion> findByNumeroLicitacionAndEstatusRegistro(String numeroLicitacion, EstatusRegistro estatusRegistro);

    List<Licitacion> findByEstatusRegistro(EstatusRegistro estatusRegistro);

}
