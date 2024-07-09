package gob.yucatan.sicasy.services.iface;

import gob.yucatan.sicasy.business.entities.Rol;

import java.util.List;
import java.util.Optional;

public interface IRolService {

    List<Rol> findAllDynamic(Rol rol);
    Optional<Rol> findById(Long id);
    void save(Rol rol);
    void delete(Rol rol);
    void update(Rol rol);

}
