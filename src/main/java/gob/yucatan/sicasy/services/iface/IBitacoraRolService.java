package gob.yucatan.sicasy.services.iface;

import gob.yucatan.sicasy.business.entities.BitacoraRol;

import java.util.List;

public interface IBitacoraRolService {

    List<BitacoraRol> findAllDynamic(BitacoraRol bitacoraRol);
    List<BitacoraRol> findByRolId(Long rolId);
    void save(BitacoraRol bitacoraRol);

}
