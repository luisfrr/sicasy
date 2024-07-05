package gob.yucatan.sicasy.services.iface;

import gob.yucatan.sicasy.business.entities.Siniestro;

import java.util.List;

public interface ISiniestroService {

    List<Siniestro> findAllDynamic(Siniestro siniestro);
    Siniestro findById(Long id);
    void registrarNuevoSiniestro(Siniestro siniestro, String userName);

    void solicitarPagoDeducible(List<Long> idSiniestroList, String userName);
    void autorizarPagoDeducible(List<Long> idSiniestroList, String userName);
    void rechazarSolicitud(List<Long> idSiniestroList, String motivo, String userName);
    void finalizarRegistro(List<Long> idSiniestroList, String userName);


}
