package gob.yucatan.sicasy.services.iface;

import gob.yucatan.sicasy.business.entities.Permiso;

import java.util.List;

public interface IPermisoService {

    List<Permiso> findAllDynamic(Permiso permiso);
    List<Permiso> findAll();
    void updateAll(List<Permiso> permisos);
}
