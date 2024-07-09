package gob.yucatan.sicasy.services.impl;

import gob.yucatan.sicasy.business.entities.TipoMantenimiento;
import gob.yucatan.sicasy.repository.iface.ITipoMantenimientoRepository;
import gob.yucatan.sicasy.services.iface.ITipoMantenimientoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class TipoMantenimientoServiceImpl implements ITipoMantenimientoService {

    private final ITipoMantenimientoRepository tipoMantenimientoRepository;

    @Override
    public List<TipoMantenimiento> findAll() {
        return tipoMantenimientoRepository.findAll().stream()
                .sorted(Comparator.comparing(TipoMantenimiento::getIdTipoMantenimiento))
                .toList();
    }

}
