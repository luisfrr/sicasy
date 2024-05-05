package gob.yucatan.sicasy.repository.iface;

import gob.yucatan.sicasy.business.entities.Vehiculo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface IVehiculoRepository extends JpaRepository<Vehiculo, Long>, JpaSpecificationExecutor<Vehiculo> {

    @Query("select v from Vehiculo v where v.estatusRegistro = 1 and v.noSerie = ?1")
    Optional<Vehiculo> findVehiculoActivoByNoSerie(String noSerie);

    @Query("select v from Vehiculo v where v.estatusRegistro = 1 and v.noSerie in ?1")
    List<Vehiculo> findVehiculosActivosByNoSerie(List<String> noSerie);

    @Query("select distinct v.marca from Vehiculo v where v.estatusRegistro = 1")
    List<String> findDistinctMarcasByEstatusActivo();

    @Query("select distinct v.modelo from Vehiculo v where v.estatusRegistro = 1 and (v.marca = ?1 or ?1 is null)")
    List<String> findDistinctModeloByEstatusActivo(String marca);

    @Query("select distinct v.anio from Vehiculo v where v.estatusRegistro = 1 and (v.marca = ?1 or ?1 is null) and (v.modelo = ?2 or ?2 is null)")
    List<Integer> findDistinctAnioByEstatusActivo(String marca, String modelo);

    @Query("select v from Vehiculo v where v.idVehiculo in ?1")
    List<Vehiculo> findAllByIdVehiculo(List<Long> idVehiculoList);

    @Query("select (count(v) > 0) from Vehiculo v where v.estatusRegistro = 1 and upper(v.noSerie) = upper(?1)")
    boolean existsByEstatusRegistroActivoAndNoSerie(String noSerie);

    @Query("select (count(v) > 0) from Vehiculo v where v.estatusRegistro = 1 and upper(v.noSerie) = upper(?1) and v.idVehiculo <> ?2 ")
    boolean existsByEstatusRegistroActivoAndNoSerieAndIdVehiculoNot(String noSerie, Long idVehiculo);


}
