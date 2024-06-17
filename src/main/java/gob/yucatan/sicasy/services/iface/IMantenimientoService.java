package gob.yucatan.sicasy.services.iface;

import gob.yucatan.sicasy.business.entities.Mantenimiento;

import java.util.List;

public interface IMantenimientoService {

    List<Mantenimiento> findAllDynamic(Mantenimiento mantenimiento);
    List<Mantenimiento> findByVehiculoId(Long idVehiculo);
    Mantenimiento findById(Long id);
    Mantenimiento save(Mantenimiento mantenimiento);
    void delete(Long id, String username);

}
