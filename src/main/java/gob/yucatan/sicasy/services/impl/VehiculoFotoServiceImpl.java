package gob.yucatan.sicasy.services.impl;

import gob.yucatan.sicasy.business.entities.VehiculoFoto;
import gob.yucatan.sicasy.business.exceptions.NotFoundException;
import gob.yucatan.sicasy.repository.iface.IVehiculoFotoRepository;
import gob.yucatan.sicasy.services.iface.IVehiculoFotoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class VehiculoFotoServiceImpl implements IVehiculoFotoService {

    private final IVehiculoFotoRepository vehiculoFotoRepository;

    @Override
    public List<VehiculoFoto> getVehiculoFotos(Long idVehiculo) {
        return vehiculoFotoRepository.findVehiculoFotosByIdVehiculo(idVehiculo);
    }

    @Override
    public void guardarFoto(VehiculoFoto vehiculoFoto) {
        vehiculoFotoRepository.save(vehiculoFoto);
    }

    @Override
    public void borrarFoto(Long idVehiculoFoto, String username) {
        VehiculoFoto vehiculoFoto = vehiculoFotoRepository.findById(idVehiculoFoto).orElseThrow(() -> new NotFoundException("No se ha encontrado la foto"));

        vehiculoFoto.setBorrado(1);
        vehiculoFoto.setFechaBorrado(new Date());
        vehiculoFoto.setBorradoPor(username);

        vehiculoFotoRepository.save(vehiculoFoto);
    }
}
