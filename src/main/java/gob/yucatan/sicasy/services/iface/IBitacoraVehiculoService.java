package gob.yucatan.sicasy.services.iface;

import gob.yucatan.sicasy.business.entities.*;

import java.util.List;

public interface IBitacoraVehiculoService {

    List<BitacoraVehiculo> findAllDynamic(BitacoraVehiculo bitacoraVehiculo);
    List<BitacoraVehiculo> findByVehiculoId(Long vehiculoId);
    void save(BitacoraVehiculo bitacoraVehiculo);
    void saveAll(List<BitacoraVehiculo> bitacoraVehiculo);
    BitacoraVehiculo getBitacoraVehiculo(String accion, Vehiculo vehiculoAnterior, Vehiculo vehiculoNuevo, String username);

}
