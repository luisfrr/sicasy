package gob.yucatan.sicasy.services.iface;

import gob.yucatan.sicasy.business.dtos.AcuseImportacion;
import gob.yucatan.sicasy.business.entities.Vehiculo;

import java.util.List;

public interface IVehiculoService {

    List<Vehiculo> findAllDynamic(Vehiculo vehiculo);
    List<Vehiculo> findAllByNoSerie(List<String> noSerieList);
    List<String> findDistinctMarcas();
    List<String> findDistinctModelo(String marca);
    List<Integer> findDistinctAnio(String marca, String modelo);
    Vehiculo findById(Long idVehiculo);
    Vehiculo findByNoSerie(String noSerie);
    Vehiculo agregar(Vehiculo vehiculo);
    Vehiculo editar(Vehiculo vehiculo);
    Vehiculo eliminar(Vehiculo vehiculo);
    List<AcuseImportacion> importar(List<Vehiculo> vehiculos, Integer idDependencia, String username);

}
