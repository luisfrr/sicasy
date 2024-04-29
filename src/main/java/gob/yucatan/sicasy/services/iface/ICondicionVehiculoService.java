package gob.yucatan.sicasy.services.iface;

import gob.yucatan.sicasy.business.entities.CondicionVehiculo;

import java.util.List;

public interface ICondicionVehiculoService {

    List<CondicionVehiculo> findAll();
    CondicionVehiculo findById(int id);

}
