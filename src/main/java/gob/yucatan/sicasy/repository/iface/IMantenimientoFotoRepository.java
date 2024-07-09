package gob.yucatan.sicasy.repository.iface;

import gob.yucatan.sicasy.business.entities.MantenimientoFoto;
import gob.yucatan.sicasy.business.entities.VehiculoFoto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface IMantenimientoFotoRepository extends JpaRepository<MantenimientoFoto, Long>, JpaSpecificationExecutor<MantenimientoFoto> {
    @Query("select vf from MantenimientoFoto vf where vf.borrado = 0 and vf.mantenimiento.idMantenimiento = ?1 order by vf.fechaCreacion desc limit 5")
    List<MantenimientoFoto> findMantenimientoFotosByIdMantenimiento(Long idMantenimiento);
}
