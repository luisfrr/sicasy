package gob.yucatan.sicasy.services.iface;

import gob.yucatan.sicasy.business.entities.VehiculoFoto;

import java.util.List;

public interface IVehiculoFotoService {
    List<VehiculoFoto> getVehiculoFotos(Long idVehiculo);
    void guardarFoto(VehiculoFoto vehiculoFoto);
    void borrarFoto(Long idVehiculoFoto, String username);
}
