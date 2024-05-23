package gob.yucatan.sicasy.services.impl;

import gob.yucatan.sicasy.business.entities.MantenimientoFoto;
import gob.yucatan.sicasy.business.exceptions.NotFoundException;
import gob.yucatan.sicasy.repository.iface.IMantenimientoFotoRepository;
import gob.yucatan.sicasy.services.iface.IMantenimientoFotoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class MantenimientoFotoServiceImpl implements IMantenimientoFotoService {

    private final IMantenimientoFotoRepository mantenimientoFotoRepository;

    @Override
    public List<MantenimientoFoto> getFotosMantenimientos(Long idMantenimiento) {
        return mantenimientoFotoRepository.findMantenimientoFotosByIdMantenimiento(idMantenimiento);
    }

    @Override
    public void guardarFoto(MantenimientoFoto foto) {
        mantenimientoFotoRepository.save(foto);
    }

    @Override
    public void borrarFoto(Long idMantenimientoFoto, String username) {

        MantenimientoFoto mantenimientoFoto = mantenimientoFotoRepository.findById(idMantenimientoFoto)
                .orElseThrow(() -> new NotFoundException("No existe la foto"));

        mantenimientoFoto.setBorrado(1);
        mantenimientoFoto.setFechaBorrado(new Date());
        mantenimientoFoto.setBorradoPor(username);

        mantenimientoFotoRepository.save(mantenimientoFoto);

    }
}
