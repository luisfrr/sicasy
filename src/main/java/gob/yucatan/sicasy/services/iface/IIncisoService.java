package gob.yucatan.sicasy.services.iface;

import gob.yucatan.sicasy.business.dtos.AcuseImportacion;
import gob.yucatan.sicasy.business.entities.Inciso;

import java.util.List;

public interface IIncisoService {

    List<Inciso> findAllDynamic(Inciso inciso);
    List<Inciso> findByIdPoliza(Long idPoliza);
    Inciso findById(Long id);

    void generarEndosoAlta(Inciso inciso, String username);
    List<AcuseImportacion> importarEndosoAlta(List<Inciso> incisos, String username);

    void solicitarPago(List<Inciso> incisos, String username);
    void autorizarPago(List<Inciso> incisos, String username);
    void rechazarPago(List<Inciso> incisos, String username);
}
