package gob.yucatan.sicasy.services.impl;

import gob.yucatan.sicasy.business.entities.MovimientoPoliza;
import gob.yucatan.sicasy.repository.iface.IMovimientoPolizaRepository;
import gob.yucatan.sicasy.services.iface.IMovimientoPolizaService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
@RequiredArgsConstructor
@Slf4j
public class MovimientoPolizaImpl implements IMovimientoPolizaService {

    private final IMovimientoPolizaRepository movimientoPolizaRepository;

    @Override
    public void save(MovimientoPoliza movimientoPoliza, String username) {
        movimientoPoliza.setCreatedAt(new Date());
        movimientoPoliza.setCreatedBy(username);
        movimientoPolizaRepository.save(movimientoPoliza);
    }

}
