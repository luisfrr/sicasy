package gob.yucatan.sicasy.services.iface;

import gob.yucatan.sicasy.business.dtos.AcuseImportacion;
import gob.yucatan.sicasy.business.entities.Poliza;

import java.util.List;

public interface IPolizaService {

    List<Poliza> findAllDynamic(Poliza poliza);
    List<Poliza> findAll(Poliza poliza);
    Poliza findFullById(Long id);
    Poliza findById(Long id);

    void registrarPoliza(Poliza poliza, String userName);
    List<AcuseImportacion> importarLayoutRegistro(List<Poliza> polizas, String username);
    void borrar(Long polizaId, String userName);

}
