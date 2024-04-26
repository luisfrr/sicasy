package gob.yucatan.sicasy.services.iface;

import gob.yucatan.sicasy.business.entities.Vehiculo;

import java.util.List;

public interface IVehiculoService {

    List<Vehiculo> findAllDynamic(Vehiculo vehiculo);
    Vehiculo findById(Long idVehiculo);
    Vehiculo findByNoSerie(String noSerie);
    Vehiculo agregar(Vehiculo vehiculo);
    Vehiculo editar(Vehiculo vehiculo);
    Vehiculo eliminar(Vehiculo vehiculo);
    Vehiculo importar(List<Vehiculo> vehiculos);

}
