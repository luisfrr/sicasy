package gob.yucatan.sicasy.repository.iface;

import gob.yucatan.sicasy.business.entities.Siniestro;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.util.Collection;
import java.util.List;

public interface ISiniestroRepository extends JpaRepository<Siniestro, Long>, JpaSpecificationExecutor<Siniestro> {

    @Query("select s from Siniestro s where s.idSiniestro in ?1")
    List<Siniestro> findByIdSiniestroIn(Collection<Long> idSiniestros);

}
