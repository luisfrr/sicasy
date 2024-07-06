package gob.yucatan.sicasy.services.iface;

import gob.yucatan.sicasy.business.entities.SiniestroFoto;

import java.util.List;

public interface ISiniestroFotoService {
    List<SiniestroFoto> getSiniestroFotos(Long idSiniestro);
    void guardarFoto(SiniestroFoto siniestroFoto);
    void borrarFoto(Long idSiniestroFoto, String username);
}
