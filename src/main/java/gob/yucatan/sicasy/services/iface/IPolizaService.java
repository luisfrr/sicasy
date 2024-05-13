package gob.yucatan.sicasy.services.iface;

import gob.yucatan.sicasy.business.dtos.GrupoPoliza;
import gob.yucatan.sicasy.business.entities.Poliza;

import java.util.List;

public interface IPolizaService {

    List<Poliza> findAllDynamic(Poliza poliza);
    List<GrupoPoliza> findGrupoPoliza(Poliza poliza);

    Poliza findById(Long id);


}
