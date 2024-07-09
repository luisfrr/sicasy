package gob.yucatan.sicasy.services.iface;

import gob.yucatan.sicasy.business.entities.MantenimientoFoto;
import java.util.List;

public interface IMantenimientoFotoService {
    List<MantenimientoFoto> getFotosMantenimientos(Long idMantenimiento);
    void guardarFoto(MantenimientoFoto foto);
    void borrarFoto(Long idMantenimientoFoto, String username);
}
