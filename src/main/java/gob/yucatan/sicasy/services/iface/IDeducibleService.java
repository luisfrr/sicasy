package gob.yucatan.sicasy.services.iface;

import gob.yucatan.sicasy.business.entities.Deducible;
import gob.yucatan.sicasy.business.entities.Siniestro;

public interface IDeducibleService {

    void update(Siniestro siniestro, Deducible deducible, String userName);

}
