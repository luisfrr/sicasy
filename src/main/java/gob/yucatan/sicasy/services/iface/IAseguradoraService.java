package gob.yucatan.sicasy.services.iface;

import gob.yucatan.sicasy.business.entities.Aseguradora;

import java.util.List;
import java.util.Optional;

public interface IAseguradoraService {

    List<Aseguradora> findAllDynamic(Aseguradora aseguradora);

    Optional<Aseguradora> findById(Integer id);

    Optional<Aseguradora> findByNombre(String nombre);

}
