package gob.yucatan.sicasy.repository.iface;

import gob.yucatan.sicasy.business.entities.VehiculoFoto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface IVehiculoFotoRepository extends JpaRepository<VehiculoFoto, Long>, JpaSpecificationExecutor<VehiculoFoto> {

    @Query("select vf from VehiculoFoto vf where vf.borrado = 0 and vf.vehiculo.idVehiculo = ?1 order by vf.fechaCreacion desc limit 12")
    List<VehiculoFoto> findVehiculoFotosByIdVehiculo(Long idVehiculo);

}
