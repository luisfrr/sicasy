package gob.yucatan.sicasy.services.iface;

import gob.yucatan.sicasy.business.entities.Dependencia;

import java.util.List;

public interface IDependenciaService {

    List<Dependencia> findAll();
    Dependencia findById(int idDependencia);

}
