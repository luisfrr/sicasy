package gob.yucatan.sicasy.services.iface;

import gob.yucatan.sicasy.business.entities.BitacoraRol;
import gob.yucatan.sicasy.business.entities.Rol;
import gob.yucatan.sicasy.business.entities.RolPermiso;

import java.util.List;

public interface IBitacoraRolService {

    List<BitacoraRol> findAllDynamic(BitacoraRol bitacoraRol);
    List<BitacoraRol> findByRolId(Long rolId);
    void save(BitacoraRol bitacoraRol);
    void guardarBitacora(String accion, Rol rolAnterior, Rol rolNuevo, String userName);
    void guardarBitacoraPermisos(Rol rol, List<RolPermiso> rolPermisoAnteriorList,
                                 List<RolPermiso> rolPermisoNuevoList,
                                 String userName);
}
