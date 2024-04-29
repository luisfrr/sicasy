package gob.yucatan.sicasy.services.iface;

import gob.yucatan.sicasy.business.entities.EstatusVehiculo;

import java.util.List;

public interface IEstatusVehiculoService {

    List<EstatusVehiculo> findAll();
    EstatusVehiculo findById(int id);

}
