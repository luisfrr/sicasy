package gob.yucatan.sicasy.services.impl;

import gob.yucatan.sicasy.business.entities.SiniestroFoto;
import gob.yucatan.sicasy.business.exceptions.NotFoundException;
import gob.yucatan.sicasy.repository.iface.ISiniestroFotoRepository;
import gob.yucatan.sicasy.services.iface.ISiniestroFotoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class SiniestroFotoServiceImpl implements ISiniestroFotoService {

    private final ISiniestroFotoRepository siniestroFotoRepository;

    @Override
    public List<SiniestroFoto> getSiniestroFotos(Long idSiniestro) {
        return siniestroFotoRepository.findSiniestroFotosByIdSiniestro(idSiniestro);
    }

    @Override
    public void guardarFoto(SiniestroFoto siniestroFoto) {
        siniestroFotoRepository.save(siniestroFoto);
    }

    @Override
    public void borrarFoto(Long idSiniestroFoto, String username) {
        SiniestroFoto siniestroFoto = siniestroFotoRepository.findById(idSiniestroFoto)
                .orElseThrow(() -> new NotFoundException("No se ha encontrado la foto"));

        siniestroFoto.setBorrado(1);
        siniestroFoto.setFechaBorrado(new Date());
        siniestroFoto.setBorradoPor(username);

        siniestroFotoRepository.save(siniestroFoto);
    }
}
