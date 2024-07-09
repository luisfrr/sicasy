package gob.yucatan.sicasy.services.iface;

import gob.yucatan.sicasy.business.entities.EstatusVehiculo;

import java.util.List;

public interface IEstatusVehiculoService {

    List<EstatusVehiculo> findAll();
    List<EstatusVehiculo> findAllDropdown(List<Integer> idEstatusVehiculoList);
    EstatusVehiculo findById(int id);

}
