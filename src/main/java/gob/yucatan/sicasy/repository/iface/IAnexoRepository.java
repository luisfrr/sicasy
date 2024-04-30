package gob.yucatan.sicasy.repository.iface;

import gob.yucatan.sicasy.business.entities.Anexo;
import gob.yucatan.sicasy.business.entities.Licitacion;
import gob.yucatan.sicasy.business.enums.EstatusRegistro;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface IAnexoRepository extends JpaRepository<Anexo, Long>, JpaSpecificationExecutor<Anexo> {

    Optional<Anexo> findByNombre(String nombre);

    List<Anexo> findByLicitacionAndEstatusRegistro(Licitacion licitacion, EstatusRegistro estatusRegistro);

    List<Anexo> findByNombreAndEstatusRegistroAndLicitacion(String nombre, EstatusRegistro estatusRegistro, Licitacion licitacion);

    @Query("select a from Anexo a where a.estatusRegistro = 1 and a.nombre = ?1 and a.licitacion = ?2 ")
    Optional<Anexo> findAnexoActivoByNombreAndLicitacion(String nombre, Licitacion licitacion);

}
