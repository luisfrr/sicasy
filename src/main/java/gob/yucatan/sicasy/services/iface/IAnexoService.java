package gob.yucatan.sicasy.services.iface;

import gob.yucatan.sicasy.business.entities.Anexo;

import java.util.List;
import java.util.Optional;

public interface IAnexoService {

    List<Anexo> findAllDynamic(Anexo anexo);

    Optional<Anexo> findById(Integer id);

    Optional<Anexo> findByNombre(String nombre);

    void save(Anexo anexo);

    void delete(Anexo anexo);

    void update(Anexo anexo);

}
