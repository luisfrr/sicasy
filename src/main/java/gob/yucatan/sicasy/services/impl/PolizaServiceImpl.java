package gob.yucatan.sicasy.services.impl;

import gob.yucatan.sicasy.business.dtos.GrupoPoliza;
import gob.yucatan.sicasy.business.entities.Aseguradora_;
import gob.yucatan.sicasy.business.entities.Poliza;
import gob.yucatan.sicasy.business.entities.Poliza_;
import gob.yucatan.sicasy.business.exceptions.NotFoundException;
import gob.yucatan.sicasy.repository.criteria.SearchCriteria;
import gob.yucatan.sicasy.repository.criteria.SearchOperation;
import gob.yucatan.sicasy.repository.criteria.SearchSpecification;
import gob.yucatan.sicasy.repository.iface.IPolizaRepository;
import gob.yucatan.sicasy.services.iface.IPolizaService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class PolizaServiceImpl implements IPolizaService {

    private final IPolizaRepository polizaRepository;

    @Override
    public List<Poliza> findAllDynamic(Poliza poliza) {

        SearchSpecification<Poliza> specification = new SearchSpecification<>();

        if(poliza.getNumeroPoliza() != null && !poliza.getNumeroPoliza().isEmpty()) {
            specification.add(new SearchCriteria(SearchOperation.EQUAL,
                    poliza.getNumeroPoliza(),
                    Poliza_.NUMERO_POLIZA));
        }

        if(poliza.getAseguradora() != null && poliza.getAseguradora().getIdAseguradora() != null) {
            specification.add(new SearchCriteria(SearchOperation.EQUAL,
                    poliza.getAseguradora().getIdAseguradora(),
                    Poliza_.ASEGURADORA, Aseguradora_.ID_ASEGURADORA));
        }

        if(poliza.getEstatusRegistro() != null) {
            specification.add(new SearchCriteria(SearchOperation.EQUAL,
                    poliza.getEstatusRegistro(),
                    Poliza_.ESTATUS_REGISTRO));
        }

        return polizaRepository.findAll(specification);
    }

    @Override
    public List<GrupoPoliza> findGrupoPoliza(Poliza poliza) {
        List<Poliza> polizaList = this.findAllDynamic(poliza);
        List<GrupoPoliza> grupoPolizaList = new ArrayList<>();
        if(!polizaList.isEmpty()) {
            grupoPolizaList = polizaList.stream()
                    .map(this::convertToGrupoPoliza)
                    .distinct()
                    .toList();

            for (GrupoPoliza grupoPoliza : grupoPolizaList) {

                double totalCostoPoliza = polizaList.stream()
                        .filter(p -> Objects.equals(p.getAseguradora().getIdAseguradora(), grupoPoliza.getIdAseguradora())
                                && Objects.equals(p.getNumeroPoliza(), grupoPoliza.getNumeroPoliza())
                                && p.getCostoPoliza() != null)
                        .mapToDouble(Poliza::getCostoPoliza)
                        .sum();

                double totalSaldoPoliza = polizaList.stream()
                        .filter(p -> Objects.equals(p.getAseguradora().getIdAseguradora(), grupoPoliza.getIdAseguradora())
                                && Objects.equals(p.getNumeroPoliza(), grupoPoliza.getNumeroPoliza())
                                && p.getSaldoPoliza() != null)
                        .mapToDouble(Poliza::getSaldoPoliza)
                        .sum();

                List<Date> fechasInicioVigencia = polizaList.stream()
                        .filter(p -> Objects.equals(p.getAseguradora().getIdAseguradora(), grupoPoliza.getIdAseguradora())
                            && Objects.equals(p.getNumeroPoliza(), grupoPoliza.getNumeroPoliza())
                                && p.getSaldoPoliza() != null)
                        .map(Poliza::getFechaInicioVigencia)
                        .toList();

                // Luego, encuentras la fecha mínima
                Optional<Date> minFechaInicioVigencia = fechasInicioVigencia.stream()
                        .min(Date::compareTo);

                List<Date> fechasFinVigencia = polizaList.stream()
                        .filter(p -> Objects.equals(p.getAseguradora().getIdAseguradora(), grupoPoliza.getIdAseguradora())
                                && Objects.equals(p.getNumeroPoliza(), grupoPoliza.getNumeroPoliza())
                                && p.getSaldoPoliza() != null)
                        .map(Poliza::getFechaInicioVigencia)
                        .toList();

                // Luego, encuentras la fecha mínima
                Optional<Date> maxFechaFinVigencia = fechasFinVigencia.stream()
                        .max(Date::compareTo);

                grupoPoliza.setTotalCostoPoliza(totalCostoPoliza);
                grupoPoliza.setTotalSaldoPoliza(totalSaldoPoliza);
                grupoPoliza.setFechaInicio(minFechaInicioVigencia.orElse(null));
                grupoPoliza.setFechaFin(maxFechaFinVigencia.orElse(null));
            }
        }
        return grupoPolizaList;
    }

    @Override
    public Poliza findById(Long id) {
        return polizaRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("No se ha encontrado la información de la póliza"));
    }

    private GrupoPoliza convertToGrupoPoliza(Poliza poliza) {
        return GrupoPoliza.builder()
                .numeroPoliza(poliza.getNumeroPoliza())
                .idAseguradora(poliza.getAseguradora().getIdAseguradora())
                .aseguradora(poliza.getAseguradora().getNombre())
                .build();
    }

}
