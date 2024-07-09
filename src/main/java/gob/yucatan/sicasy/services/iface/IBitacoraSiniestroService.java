package gob.yucatan.sicasy.services.iface;

import gob.yucatan.sicasy.business.entities.BitacoraSiniestro;
import gob.yucatan.sicasy.business.entities.Siniestro;

import java.util.List;

public interface IBitacoraSiniestroService {

    List<BitacoraSiniestro> findAllDynamic(BitacoraSiniestro bitacoraSiniestro);
    List<BitacoraSiniestro> findBySiniestroId(Long siniestroId);
    void save(BitacoraSiniestro bitacoraSiniestro);
    void saveAll(List<BitacoraSiniestro> bitacoraSiniestroList);
    void guardarBitacora(String accion, Siniestro siniestroAnterior, Siniestro siniestroNuevo, String username);
    BitacoraSiniestro getBitacora(String accion, Siniestro siniestroAnterior, Siniestro siniestroNuevo, String username);

}
