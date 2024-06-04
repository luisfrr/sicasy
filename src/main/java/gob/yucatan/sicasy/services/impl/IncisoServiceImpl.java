package gob.yucatan.sicasy.services.impl;

import gob.yucatan.sicasy.business.dtos.AcuseImportacion;
import gob.yucatan.sicasy.business.entities.*;
import gob.yucatan.sicasy.business.enums.EstatusRegistro;
import gob.yucatan.sicasy.business.exceptions.BadRequestException;
import gob.yucatan.sicasy.business.exceptions.NotFoundException;
import gob.yucatan.sicasy.repository.criteria.SearchCriteria;
import gob.yucatan.sicasy.repository.criteria.SearchOperation;
import gob.yucatan.sicasy.repository.criteria.SearchSpecification;
import gob.yucatan.sicasy.repository.iface.IIncisoRepository;
import gob.yucatan.sicasy.services.iface.IIncisoService;
import gob.yucatan.sicasy.services.iface.IPolizaService;
import gob.yucatan.sicasy.services.iface.IVehiculoService;
import gob.yucatan.sicasy.utils.date.DateValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class IncisoServiceImpl implements IIncisoService {

    private final Integer ESTATUS_REGISTRADO = 1;

    private final IIncisoRepository incisoRepository;
    private final IPolizaService polizaService;
    private final IVehiculoService vehiculoService;

    @Override
    public List<Inciso> findAllDynamic(Inciso inciso) {
        SearchSpecification<Inciso> specification = new SearchSpecification<>();

        if(inciso.getPoliza() != null && inciso.getPoliza().getIdPoliza() != null) {
            specification.add(new SearchCriteria(SearchOperation.EQUAL,
                    inciso.getPoliza().getIdPoliza(),
                    Inciso_.POLIZA, Poliza_.ID_POLIZA));
        }

        if(inciso.getEstatusRegistro() != null) {
            specification.add(new SearchCriteria(SearchOperation.EQUAL,
                    inciso.getEstatusRegistro(),
                    Inciso_.ESTATUS_REGISTRO));
        }

        if(inciso.getEstatusInciso() != null && inciso.getEstatusInciso().getIdEstatusPoliza() != null) {
            specification.add(new SearchCriteria(SearchOperation.EQUAL,
                    inciso.getEstatusInciso().getIdEstatusPoliza(),
                    Inciso_.ESTATUS_INCISO, EstatusInciso_.ID_ESTATUS_POLIZA));
        }

        if(inciso.getVehiculo() != null && inciso.getVehiculo().getIdVehiculo() != null) {
            specification.add(new SearchCriteria(SearchOperation.EQUAL,
                    inciso.getVehiculo().getIdVehiculo(),
                    Inciso_.VEHICULO, Vehiculo_.ID_VEHICULO));
        }

        if(inciso.getTipoCobertura() != null && !inciso.getTipoCobertura().isEmpty()) {
            specification.add(new SearchCriteria(SearchOperation.EQUAL,
                    inciso.getTipoCobertura(),
                    Inciso_.TIPO_COBERTURA));
        }

        if(inciso.getIdPolizaList() != null && !inciso.getIdPolizaList().isEmpty()){
            specification.add(new SearchCriteria(SearchOperation.IN,
                    inciso.getIdPolizaList(),
                    Inciso_.POLIZA, Poliza_.ID_POLIZA));
        }

        if(inciso.getIncisoList() != null && !inciso.getIncisoList().isEmpty()){
            specification.add(new SearchCriteria(SearchOperation.IN,
                    inciso.getIncisoList(),
                    Inciso_.INCISO));
        }

        if(inciso.getVehiculoNoSerieList() != null && !inciso.getVehiculoNoSerieList().isEmpty()){
            specification.add(new SearchCriteria(SearchOperation.IN,
                    inciso.getVehiculoNoSerieList(),
                    Inciso_.VEHICULO, Vehiculo_.NO_SERIE));
        }

        return incisoRepository.findAll(specification);
    }

    @Override
    public List<Inciso> findByIdPoliza(Long idPoliza) {
        Inciso inciso = Inciso.builder()
                .poliza(Poliza.builder().idPoliza(idPoliza).build())
                .estatusRegistro(EstatusRegistro.ACTIVO)
                .build();
        return this.findAllDynamic(inciso);
    }

    @Override
    public Inciso findById(Long id) {
        return incisoRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("No se ha logrado obtener la información del inciso."));
    }

    @Override
    @Transactional
    public void generarEndosoAlta(Inciso inciso, String username) {

        // Validación de campos
        if(inciso.getPoliza() == null || inciso.getPoliza().getIdPoliza() == null)
            throw new BadRequestException("El campo Póliza es obligatorio");

        Long idPoliza = inciso.getPoliza().getIdPoliza();
        Poliza poliza = polizaService.findById(idPoliza);

        if(poliza.getEstatusRegistro() == EstatusRegistro.BORRADO)
            throw new NotFoundException("No se ha logrado obtener la póliza.");

        if(inciso.getInciso() == null || inciso.getInciso().isEmpty())
            throw new BadRequestException("El campo Inciso es obligatorio");

        if(inciso.getVehiculo() == null || inciso.getVehiculo().getNoSerie() == null)
            throw new BadRequestException("El campo No. Serie es obligatorio");

        Vehiculo vehiculo = vehiculoService.findByNoSerie(inciso.getVehiculo().getNoSerie());
        inciso.setVehiculo(vehiculo);

        if(vehiculo.getIncisoVigente() != null) {
            throw new BadRequestException("El vehículo con número de serie " +
                    inciso.getVehiculo().getNoSerie() +
                    " ya tiene una póliza con un inciso vigente." +
                    " No. póliza: " + vehiculo.getIncisoVigente().getPoliza().getNumeroPoliza() +
                    ". Inciso: " + vehiculo.getIncisoVigente().getInciso());
        }

        validateIncisoPoliza(inciso, poliza);

        if(incisoRepository.existsByIdPolizaAndIncisoAndIdVehiculo(poliza.getIdPoliza(), inciso.getInciso(), inciso.getVehiculo().getIdVehiculo()))
            throw new BadRequestException("La póliza ya existe.");

        // Se agrega el saldo
        setValuesEndosoAlta(username, inciso, poliza);

        incisoRepository.save(inciso);
    }

    @Override
    @Transactional
    public List<AcuseImportacion> importarEndosoAlta(List<Inciso> incisos, String username) {
        List<AcuseImportacion> acuseImportacionList = new ArrayList<>();
        this.validarLayoutRegistroEndosoAlta(acuseImportacionList, incisos, username);

        if(acuseImportacionList.stream().anyMatch(acuseImportacion -> acuseImportacion.getError() == 1)) {
            return acuseImportacionList;
        }

        try {
            incisoRepository.saveAll(incisos);
        } catch (Exception e) {
            acuseImportacionList.add(AcuseImportacion.builder()
                    .titulo("Error al guardar la información")
                    .mensaje(e.getMessage())
                    .error(1)
                    .build());
        }
        return acuseImportacionList;
    }


    private void validarLayoutRegistroEndosoAlta(List<AcuseImportacion> acuseImportacionList, List<Inciso> incisos, String username) {

        Poliza polizaFilter = Poliza.builder()
                .idAseguradoraList(incisos.stream().map(Inciso::getPolizaIdAseguradora).distinct().toList())
                .numeroPolizaList(incisos.stream().map(Inciso::getPolizaNoPoliza).distinct().toList())
                .estatusRegistro(EstatusRegistro.ACTIVO)
                .build();
        List<Poliza> polizaDbList = polizaService.findAllDynamic(polizaFilter);

        Vehiculo vehiculoFilter = Vehiculo.builder()
                .noSerieList(incisos.stream().map(Inciso::getVehiculoNoSerie).toList())
                .estatusRegistro(EstatusRegistro.ACTIVO)
                .build();
        List<Vehiculo> vehiculoDbList = vehiculoService.findAllDynamic(vehiculoFilter);

        Inciso incisoFilter = Inciso.builder()
                .idPolizaList(polizaDbList.stream().map(Poliza::getIdPoliza).distinct().toList())
                .incisoList(incisos.stream().map(Inciso::getInciso).toList())
                .vehiculoNoSerieList(incisos.stream().map(Inciso::getVehiculoNoSerie).distinct().toList())
                .estatusRegistro(EstatusRegistro.ACTIVO)
                .build();
        List<Inciso> incisoDbList = this.findAllDynamic(incisoFilter);

        for (Inciso inciso : incisos) {

            try {
                // Validación de campos
                if(inciso.getPolizaIdAseguradora() == null)
                    throw new BadRequestException("El campo Aseguradora es obligatorio");

                if(inciso.getPolizaNoPoliza() == null)
                    throw new BadRequestException("El campo No. Póliza es obligatorio");

                Poliza poliza = polizaDbList.stream()
                        .filter(p -> Objects.equals(p.getAseguradora().getIdAseguradora(), inciso.getPolizaIdAseguradora())
                                && Objects.equals(p.getNumeroPoliza(), inciso.getPolizaNoPoliza()))
                        .findFirst().orElseThrow(() -> new BadRequestException("No se ha encontrado la información de la póliza"));

                if(inciso.getInciso() == null || inciso.getInciso().isEmpty())
                    throw new BadRequestException("El campo Inciso es obligatorio");

                if(inciso.getVehiculoNoSerie() == null || inciso.getVehiculoNoSerie().isEmpty())
                    throw new BadRequestException("El campo No. Serie es obligatorio");

                Vehiculo vehiculo = vehiculoDbList.stream()
                        .filter(v -> Objects.equals(v.getNoSerie(), inciso.getVehiculoNoSerie()))
                        .findFirst()
                        .orElseThrow(() -> new NotFoundException("No se ha encontrado la información del vehículo"));

                if(vehiculo.getIncisoVigente() != null) {
                    throw new BadRequestException("El vehículo con número de serie " +
                            inciso.getVehiculoNoSerie() +
                            " ya tiene una póliza con un inciso vigente." +
                            " No. póliza: " + vehiculo.getIncisoVigente().getPoliza().getNumeroPoliza() +
                            ". Inciso: " + vehiculo.getIncisoVigente().getInciso());
                }

                validateIncisoPoliza(inciso, poliza);

                if(incisoDbList.stream().anyMatch(i -> Objects.equals(i.getPoliza().getIdPoliza(), poliza.getIdPoliza())
                        && Objects.equals(i.getInciso(), inciso.getInciso())
                        && Objects.equals(i.getVehiculo().getNoSerie(), inciso.getVehiculoNoSerie()))) {
                    throw new BadRequestException("La póliza ya existe.");
                }

                // Se agrega el saldo
                setValuesEndosoAlta(username, inciso, poliza);

            } catch (Exception e) {
                acuseImportacionList.add(AcuseImportacion.builder()
                        .titulo(inciso.getInciso())
                        .mensaje(e.getMessage())
                        .error(1)
                        .build());
            }

        }

        List<Inciso> incisosDuplicados = encontrarDuplicados(incisos);
        if(!incisosDuplicados.isEmpty()) {
            incisosDuplicados.forEach(poliza -> acuseImportacionList.add(AcuseImportacion.builder()
                    .titulo(poliza.getInciso())
                    .mensaje("El inciso está duplicada en este layout.")
                    .error(1)
                    .build()));
        }
    }

    private void validateIncisoPoliza(Inciso inciso, Poliza poliza) {
        if(inciso.getFolioFactura() == null || inciso.getFolioFactura().isEmpty())
            throw new BadRequestException("El campo Folio factura es obligatorio");

        if(inciso.getFechaInicioVigencia() == null)
            throw new BadRequestException("El campo Fecha inicio de vigencia es obligatorio");

        if(inciso.getFechaFinVigencia() == null)
            throw new BadRequestException("El campo Fecha fin de vigencia es obligatorio");

        if(inciso.getCosto() < 0)
            throw new BadRequestException("El campo Costo no puede ser menor a 0.");

        if(!DateValidator.isDateBetween(poliza.getFechaInicioVigencia(), poliza.getFechaFinVigencia(), inciso.getFechaInicioVigencia()))
            throw new BadRequestException("La fecha inicio de vigencia debe estar entre la fecha inicio y fecha fin de la póliza");

        if(!DateValidator.isDateBetween(poliza.getFechaInicioVigencia(), poliza.getFechaFinVigencia(), inciso.getFechaFinVigencia()))
            throw new BadRequestException("La fecha fin de vigencia debe estar entre la fecha inicio y fecha fin de la póliza");

        if(inciso.getFechaInicioVigencia().after(inciso.getFechaFinVigencia()))
            throw new BadRequestException("La fecha inicio de vigencia no puede ser posterior a la fecha fin de vigencia");
    }

    private void setValuesEndosoAlta(String username, Inciso inciso, Poliza poliza) {
        if(inciso.getCosto() > 0)
            inciso.setSaldo(-inciso.getCosto());
        else
            inciso.setSaldo(0d);

        inciso.setPoliza(poliza);
        inciso.setEstatusInciso(EstatusInciso.builder().idEstatusPoliza(ESTATUS_REGISTRADO).build());
        inciso.setEstatusRegistro(EstatusRegistro.ACTIVO);
        inciso.setFechaCreacion(new Date());
        inciso.setCreadoPor(username);
    }

    private static List<Inciso> encontrarDuplicados(List<Inciso> incisos) {
        Set<Inciso> unicos = new HashSet<>();
        List<Inciso> duplicados = new ArrayList<>();

        for (Inciso inciso : incisos) {
            if (!unicos.add(inciso)) { // Si no se puede agregar a 'unicos', es un duplicado
                duplicados.add(inciso);
            }
        }

        return duplicados;
    }
}
