package gob.yucatan.sicasy.services.impl;

import gob.yucatan.sicasy.business.entities.VehiculoFoto;
import gob.yucatan.sicasy.repository.iface.IVehiculoFotoRepository;
import gob.yucatan.sicasy.services.iface.IVehiculoFotoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class VehiculoFotoServiceImpl implements IVehiculoFotoService {

    private final IVehiculoFotoRepository vehiculoFotoRepository;

    @Override
    public List<VehiculoFoto> getVehiculoFotos(Integer idVehiculo) {
        return List.of();
    }

    @Override
    public void guardarFoto(VehiculoFoto vehiculoFoto) {
        vehiculoFotoRepository.save(vehiculoFoto);
    }
}
