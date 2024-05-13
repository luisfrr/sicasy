package gob.yucatan.sicasy.services.impl;

import gob.yucatan.sicasy.business.dtos.AcuseImportacion;
import gob.yucatan.sicasy.business.dtos.GrupoPoliza;
import gob.yucatan.sicasy.business.entities.*;
import gob.yucatan.sicasy.business.enums.EstatusRegistro;
import gob.yucatan.sicasy.business.exceptions.BadRequestException;
import gob.yucatan.sicasy.business.exceptions.NotFoundException;
import gob.yucatan.sicasy.repository.criteria.SearchCriteria;
import gob.yucatan.sicasy.repository.criteria.SearchOperation;
import gob.yucatan.sicasy.repository.criteria.SearchSpecification;
import gob.yucatan.sicasy.repository.iface.IAseguradoraRepository;
import gob.yucatan.sicasy.repository.iface.IPolizaRepository;
import gob.yucatan.sicasy.services.iface.IAseguradoraService;
import gob.yucatan.sicasy.services.iface.IPolizaService;
import gob.yucatan.sicasy.services.iface.IVehiculoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class PolizaServiceImpl implements IPolizaService {

    private final IPolizaRepository polizaRepository;
    private final IVehiculoService vehiculoService;
    private final IAseguradoraRepository aseguradoraRepository;

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

        if(poliza.isIncisoNotNull()) {
            specification.add(new SearchCriteria(SearchOperation.IS_NOT_NULL,
                    "",
                    Poliza_.INCISO));
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
    public List<Poliza> findByGrupoPoliza(GrupoPoliza grupoPoliza) {

        Poliza poliza = Poliza.builder()
                .numeroPoliza(grupoPoliza.getNumeroPoliza())
                .aseguradora(Aseguradora.builder().idAseguradora(grupoPoliza.getIdAseguradora()).build())
                .estatusRegistro(EstatusRegistro.ACTIVO)
                .incisoNotNull(true)
                .build();

        return this.findAllDynamic(poliza);
    }

    @Override
    public Poliza findById(Long id) {
        return polizaRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("No se ha encontrado la información de la póliza"));
    }

    @Override
    public List<AcuseImportacion> importar(List<Poliza> polizas, String username) {
        List<AcuseImportacion> acuseImportacionList = new ArrayList<>();

        this.validarImportacion(acuseImportacionList, polizas, username);

        if(acuseImportacionList.stream().anyMatch(acuseImportacion -> acuseImportacion.getError() == 1)) {
            return acuseImportacionList;
        }


        return List.of();
    }

    private GrupoPoliza convertToGrupoPoliza(Poliza poliza) {
        return GrupoPoliza.builder()
                .numeroPoliza(poliza.getNumeroPoliza())
                .idAseguradora(poliza.getAseguradora().getIdAseguradora())
                .aseguradora(poliza.getAseguradora().getNombre())
                .build();
    }

    private void validarImportacion(List<AcuseImportacion> acuseImportacionList, List<Poliza> polizas, String username) {

        List<String> vehiculoNoSerieList = polizas.stream()
                .map(Poliza::getVehiculoNoSerie)
                .filter(Objects::nonNull)
                .distinct()
                .toList();

        List<Vehiculo> vehiculos = vehiculoService.findAllByNoSerie(vehiculoNoSerieList);
        List<Aseguradora> aseguradoras = aseguradoraRepository.findByEstatus(EstatusRegistro.ACTIVO);

        for(Poliza poliza : polizas) {

            try {
                // Validación de campos obligatorios
                if(poliza.getIdAseguradora() == null)
                    throw new BadRequestException("El campo Aseguradora es obligatorio");
                else {
                    Aseguradora aseguradora = aseguradoras.stream()
                            .filter(a -> Objects.equals(a.getIdAseguradora(), poliza.getIdAseguradora()))
                            .findFirst().orElseThrow(() -> new NotFoundException("No se ha logrado obtener la aseguradora: " + poliza.getIdAseguradora()));
                    poliza.setAseguradora(aseguradora);
                }

                if(poliza.getNumeroPoliza() == null || poliza.getNumeroPoliza().isEmpty())
                    throw new BadRequestException("El campo No. Póliza es obligatorio");

                // Validaciones extra
                if(poliza.getVehiculoNoSerie() != null && !poliza.getVehiculoNoSerie().isEmpty()) {
                    Vehiculo vehiculo = vehiculos.stream()
                            .filter(p -> Objects.equals(p.getNoSerie(), poliza.getVehiculoNoSerie()))
                            .findFirst()
                            .orElseThrow(() -> new NotFoundException("No se ha encontrado el vehículo con el No. Serie: " + poliza.getVehiculoNoSerie()));
                    poliza.setVehiculo(vehiculo);
                }

                // Validación de fechas
                if(poliza.getFechaInicioVigencia() != null) {
                    if(poliza.getFechaFinVigencia() == null)
                        throw new BadRequestException("Se espera ingrese la fecha final de vigencia.");

                    if(poliza.getFechaInicioVigencia().after(poliza.getFechaFinVigencia()))
                        throw new BadRequestException("La fecha inicio de vigencia no puede ser posterior a la fecha fin de vigencia.");
                }

            } catch (Exception e) {
                acuseImportacionList.add(AcuseImportacion.builder()
                        .titulo(poliza.getNumeroPoliza())
                        .mensaje(e.getMessage())
                        .error(1)
                        .build());
            }
        }
    }


}
