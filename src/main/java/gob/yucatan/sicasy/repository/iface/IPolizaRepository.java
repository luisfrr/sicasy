package gob.yucatan.sicasy.repository.iface;

import gob.yucatan.sicasy.business.entities.Poliza;
import gob.yucatan.sicasy.business.enums.EstatusRegistro;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface IPolizaRepository extends JpaRepository<Poliza, Long>, JpaSpecificationExecutor<Poliza> {

    @Query("""
            select (count(p) > 0) from Poliza p
            where p.aseguradora.idAseguradora = ?1 and p.numeroPoliza = ?2 and p.estatusRegistro = 1""")
    boolean existsByIdAseguradoraAndNumeroPolizaAndActiva(Integer idAseguradora, String numeroPoliza);

}
