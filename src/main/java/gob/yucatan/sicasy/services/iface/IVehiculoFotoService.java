package gob.yucatan.sicasy.services.iface;

import gob.yucatan.sicasy.business.entities.VehiculoFoto;

import java.util.List;

public interface IVehiculoFotoService {
    List<VehiculoFoto> getVehiculoFotos(Integer idVehiculo);
    void guardarFoto(VehiculoFoto vehiculoFoto);
}
