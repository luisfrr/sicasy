package gob.yucatan.sicasy.services.impl;

import gob.yucatan.sicasy.business.entities.EstatusSiniestro;
import gob.yucatan.sicasy.business.entities.EstatusSiniestro_;
import gob.yucatan.sicasy.business.entities.Siniestro;
import gob.yucatan.sicasy.business.entities.Siniestro_;
import gob.yucatan.sicasy.business.enums.EstatusRegistro;
import gob.yucatan.sicasy.repository.criteria.SearchCriteria;
import gob.yucatan.sicasy.repository.criteria.SearchOperation;
import gob.yucatan.sicasy.repository.criteria.SearchSpecification;
import gob.yucatan.sicasy.repository.iface.ISiniestroRepository;
import gob.yucatan.sicasy.services.iface.ISiniestroService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class SiniestroServiceImpl implements ISiniestroService {

    private final Integer ESTATUS_SINIESTRO_REGISTRADO = 1;

    private final ISiniestroRepository siniestroRepository;

    @Override
    public List<Siniestro> findAllDynamic(Siniestro siniestro) {

        SearchSpecification<Siniestro> specification = new SearchSpecification<>();

        if(siniestro.getResponsable() != null && !siniestro.getResponsable().isEmpty()) {
            specification.add(new SearchCriteria(SearchOperation.EQUAL,
                    siniestro.getResponsable(), Siniestro_.RESPONSABLE));
        }

        if(siniestro.getFechaInicioFilter() != null && siniestro.getFechaFinFilter() != null) {
            specification.add(new SearchCriteria(SearchOperation.GREATER_THAN_EQUAL,
                    siniestro.getFechaInicioFilter(), Siniestro_.FECHA_SINIESTRO));
            specification.add(new SearchCriteria(SearchOperation.LESS_THAN_EQUAL,
                    siniestro.getFechaFinFilter(), Siniestro_.FECHA_SINIESTRO));
        }

        if(siniestro.getEstatusSiniestro() != null && siniestro.getEstatusSiniestro().getIdEstatusSiniestro() != null) {
            specification.add(new SearchCriteria(SearchOperation.GREATER_THAN_EQUAL,
                    siniestro.getEstatusSiniestro().getIdEstatusSiniestro(),
                    Siniestro_.ESTATUS_SINIESTRO, EstatusSiniestro_.ID_ESTATUS_SINIESTRO));
        }

        if(siniestro.getCorralon() != null) {
            specification.add(new SearchCriteria(SearchOperation.EQUAL,
                    siniestro.getCorralon(),
                    Siniestro_.CORRALON));
        }

        if(siniestro.getMultaVehiculo() != null) {
            specification.add(new SearchCriteria(SearchOperation.EQUAL,
                    siniestro.getMultaVehiculo(),
                    Siniestro_.MULTA_VEHICULO));
        }

        if(siniestro.getPerdidaTotal() != null) {
            specification.add(new SearchCriteria(SearchOperation.EQUAL,
                    siniestro.getPerdidaTotal(),
                    Siniestro_.PERDIDA_TOTAL));
        }

        if(siniestro.getPagoDeducible() != null) {
            if(siniestro.getPagoDeducible() == 1) {
                specification.add(new SearchCriteria(SearchOperation.IS_NOT_NULL,
                        null,
                        Siniestro_.DEDUCIBLE));
            } else {
                specification.add(new SearchCriteria(SearchOperation.IS_NULL,
                        null,
                        Siniestro_.DEDUCIBLE));
            }
        }

        return siniestroRepository.findAll(specification);
    }

    @Override
    public Siniestro findById(Long id) {
        return null;
    }

    @Override
    public void registrarNuevoSiniestro(Siniestro siniestro, String userName) {

        if(siniestro.getVehiculo() != null && siniestro.getVehiculo().getIncisoVigente() != null) {
            siniestro.setInciso(siniestro.getVehiculo().getIncisoVigente());
        }

        siniestro.setCorralon(siniestro.isCheckCorralon() ? 1 : 0);
        siniestro.setDanioViaPublica(siniestro.isCheckDanioViaPublica() ? 1 : 0);
        siniestro.setPerdidaTotal(siniestro.isCheckPerdidaTotal() ? 1 : 0);
        siniestro.setMultaVehiculo(siniestro.isCheckMulta() ? 1 : 0);

        siniestro.setEstatusSiniestro(EstatusSiniestro.builder()
                .idEstatusSiniestro(ESTATUS_SINIESTRO_REGISTRADO)
                .build());
        siniestro.setEstatusRegistro(EstatusRegistro.ACTIVO);
        siniestro.setCreadoPor(userName);
        siniestro.setFechaCreacion(new Date());

        siniestroRepository.save(siniestro);
    }
}
