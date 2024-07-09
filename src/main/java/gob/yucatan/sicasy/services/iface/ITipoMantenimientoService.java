package gob.yucatan.sicasy.services.iface;

import gob.yucatan.sicasy.business.entities.TipoMantenimiento;

import java.util.List;

public interface ITipoMantenimientoService {
    List<TipoMantenimiento> findAll();
}
