package gob.yucatan.sicasy.repository.iface;

import gob.yucatan.sicasy.business.entities.Poliza;
import gob.yucatan.sicasy.business.enums.EstatusRegistro;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.util.Date;
import java.util.List;

public interface IPolizaRepository extends JpaRepository<Poliza, Long>, JpaSpecificationExecutor<Poliza> {

    @Query("""
            select (count(p) > 0) from Poliza p
            where p.aseguradora.idAseguradora = ?1 and p.numeroPoliza = ?2 and p.estatusRegistro = 1""")
    boolean existsByIdAseguradoraAndNumeroPolizaAndActiva(Integer idAseguradora, String numeroPoliza);

    @Query("""
            select p from Poliza p
            where p.aseguradora.idAseguradora = ?1 and ?2 between p.fechaInicioVigencia and p.fechaFinVigencia and p.estatusRegistro = ?3
            order by p.numeroPoliza""")
    List<Poliza> findByAseguradoraAndVigentes(Integer idAseguradora, Date fecha, EstatusRegistro estatusRegistro);

}
