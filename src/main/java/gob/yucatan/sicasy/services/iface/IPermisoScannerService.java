package gob.yucatan.sicasy.services.iface;

import gob.yucatan.sicasy.business.entities.Permiso;

import java.util.List;

public interface IPermisoScannerService {
    List<Permiso> getPermisos(String scanPackage, String userName);
}
