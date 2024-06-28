package gob.yucatan.sicasy.services.iface;

import gob.yucatan.sicasy.business.entities.Siniestro;

import java.util.List;

public interface ISiniestroService {

    List<Siniestro> findAllDynamic(Siniestro siniestro);
    Siniestro findById(Long id);

}
