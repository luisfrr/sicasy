package gob.yucatan.sicasy.services.iface;

import gob.yucatan.sicasy.business.entities.EstatusSiniestro;

import java.util.List;

public interface IEstatusSiniestroService {
    List<EstatusSiniestro> findAll();
}
