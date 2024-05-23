package gob.yucatan.sicasy.services.iface;

import gob.yucatan.sicasy.business.entities.Inciso;

import java.util.List;

public interface IIncisoService {

    List<Inciso> findAllDynamic(Inciso inciso);
    List<Inciso> findByIdPoliza(Long idPoliza);
    Inciso findById(Long id);

    void generarEndosoAlta(Inciso inciso, String username);
    void importarEndosoAlta(List<Inciso> incisos, String username);

}
