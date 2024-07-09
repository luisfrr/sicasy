package gob.yucatan.sicasy.services.iface;

import gob.yucatan.sicasy.business.dtos.AcuseImportacion;
import gob.yucatan.sicasy.business.entities.Licitacion;

import java.util.List;
import java.util.Optional;

public interface ILicitacionService {

    List<Licitacion> findAllDynamic(Licitacion licitacion);

    Optional<Licitacion> findById(Integer id);

    Optional<Licitacion> findByNombre(String nombre);

    Optional<Licitacion> findByNumeroLicitacion(String numeroLicitacion);

    List<Licitacion> findAllLicitacionActive();

    void save(Licitacion licitacion);

    void delete(Licitacion licitacion);

    void update(Licitacion licitacion);

    List<AcuseImportacion> importar(List<Licitacion> licitacions, String username);

}
