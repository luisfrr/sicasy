package gob.yucatan.sicasy.repository.iface;

import gob.yucatan.sicasy.business.entities.Vehiculo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface IVehiculoRepository extends JpaRepository<Vehiculo, Long>, JpaSpecificationExecutor<Vehiculo> {

    @Query("select v from Vehiculo v where v.estatusRegistro = 1 and v.noSerie = ?1")
    Optional<Vehiculo> findVehiculoActivoByNoSerie(String noSerie);

}
