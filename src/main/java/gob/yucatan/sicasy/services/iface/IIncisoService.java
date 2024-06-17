package gob.yucatan.sicasy.services.iface;

import gob.yucatan.sicasy.business.dtos.AcuseImportacion;
import gob.yucatan.sicasy.business.dtos.EndosoBaja;
import gob.yucatan.sicasy.business.dtos.EndosoModificacion;
import gob.yucatan.sicasy.business.dtos.PagoInciso;
import gob.yucatan.sicasy.business.entities.Inciso;

import java.util.List;

public interface IIncisoService {

    List<Inciso> findAllDynamic(Inciso inciso);
    List<Inciso> findByIdPoliza(Long idPoliza);
    Inciso findById(Long id);

    void generarEndosoAlta(Inciso inciso, String username);
    List<AcuseImportacion> importarEndosoAlta(List<Inciso> incisos, String username);

    void solicitarPago(List<Inciso> incisos, String username);
    void rechazarSolicitud(List<Inciso> incisos, String motivo, String username);
    void editar(Inciso inciso, String username);

    PagoInciso getDetallePagoIncisos(List<Inciso> incisosPorPagar);
    void registarPagoIncisos(PagoInciso pagoInciso, String username);

    void generarEndosoModificacion(EndosoModificacion endosoModificacion, String username);
    void generarEndosoBaja(EndosoBaja endosoBaja, String username);
}

