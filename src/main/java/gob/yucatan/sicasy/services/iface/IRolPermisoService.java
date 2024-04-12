package gob.yucatan.sicasy.services.iface;


import gob.yucatan.sicasy.business.entities.Rol;
import gob.yucatan.sicasy.business.entities.RolPermiso;

import java.util.List;

public interface IRolPermisoService {

    List<RolPermiso> findAllDynamic(RolPermiso rolPermiso);
    List<RolPermiso> findByRol(Rol rol);

}
