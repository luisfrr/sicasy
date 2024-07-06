package gob.yucatan.sicasy.repository.iface;

import gob.yucatan.sicasy.business.entities.SiniestroFoto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ISiniestroFotoRepository extends JpaRepository<SiniestroFoto, Long>, JpaSpecificationExecutor<SiniestroFoto> {

    @Query("select sf from SiniestroFoto sf where sf.borrado = 0 and sf.siniestro.idSiniestro = ?1 order by sf.fechaCreacion desc")
    List<SiniestroFoto> findSiniestroFotosByIdSiniestro(Long idSiniestro);

}
