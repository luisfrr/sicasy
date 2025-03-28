package gob.yucatan.sicasy.repository.iface;

import gob.yucatan.sicasy.business.entities.Inciso;
import gob.yucatan.sicasy.business.enums.EstatusRegistro;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.util.Collection;
import java.util.List;

public interface IIncisoRepository extends JpaRepository<Inciso, Long>, JpaSpecificationExecutor<Inciso> {

    @Query("""
            select (count(i) > 0) from Inciso i
            where i.poliza.idPoliza = ?1 and i.numeroInciso = ?2 and i.vehiculo.idVehiculo = ?3
            and i.vehiculo.estatusRegistro = 1 and i.poliza.estatusRegistro = 1 and i.estatusRegistro = 1""")
    boolean existsByIdPolizaAndIncisoAndIdVehiculo(Long idPoliza, String numeroInciso, Long idVehiculo);

    @Query("select i from Inciso i where i.idInciso in ?1")
    List<Inciso> findByIdIncisoIn(Collection<Long> idIncisos);

    @Query("select i from Inciso i where i.vehiculo.idVehiculo in ?1 and i.estatusRegistro = ?2")
    List<Inciso> findByIdVehiculoAndEstatusRegistro(Long idVehiculo, EstatusRegistro estatusRegistro);

}
