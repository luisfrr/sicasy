package gob.yucatan.sicasy.repository.iface;

import gob.yucatan.sicasy.business.entities.Inciso;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

public interface IIncisoRepository extends JpaRepository<Inciso, Long>, JpaSpecificationExecutor<Inciso> {

    @Query("""
            select (count(i) > 0) from Inciso i
            where i.poliza.idPoliza = ?1 and i.inciso = ?2 and i.vehiculo.idVehiculo = ?3
            and i.vehiculo.estatusRegistro = 1 and i.poliza.estatusRegistro = 1 and i.estatusRegistro = 1""")
    boolean existsByIdPolizaAndIncisoAndIdVehiculo(Long idPoliza, String inciso, Long idVehiculo);


}
