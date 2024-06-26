package gob.yucatan.sicasy.services.impl;

import gob.yucatan.sicasy.business.entities.EstatusSiniestro;
import gob.yucatan.sicasy.repository.iface.IEstatusSiniestroRepository;
import gob.yucatan.sicasy.services.iface.IEstatusSiniestroService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class EstatusSiniestroServiceImpl implements IEstatusSiniestroService {

    private final IEstatusSiniestroRepository estatusSiniestroRepository;

    @Override
    public List<EstatusSiniestro> findAll() {
        return estatusSiniestroRepository.findAll();
    }
}
