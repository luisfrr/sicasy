package gob.yucatan.sicasy.services.impl;

import gob.yucatan.sicasy.business.dtos.AcuseImportacion;
import gob.yucatan.sicasy.business.entities.*;
import gob.yucatan.sicasy.business.enums.EstatusRegistro;
import gob.yucatan.sicasy.business.exceptions.BadRequestException;
import gob.yucatan.sicasy.business.exceptions.NotFoundException;
import gob.yucatan.sicasy.repository.criteria.SearchCriteria;
import gob.yucatan.sicasy.repository.criteria.SearchOperation;
import gob.yucatan.sicasy.repository.criteria.SearchSpecification;
import gob.yucatan.sicasy.repository.iface.IEstatusIncisoRepository;
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

    private final Integer ESTATUS_INCISO_REGISTRADA = 1;
    private final Integer ESTATUS_INCISO_EN_PROCESO_PAGO = 2;
    private final Integer ESTATUS_INCISO_PAGADA = 3;
    private final Integer ESTATUS_INCISO_BAJA = 4;

    private final Integer ACCION_SOLICITAR_PAGO = 1;
    private final Integer ACCION_AUTORIZAR_PAGO = 2;
    private final Integer ACCION_RECHAZAR_PAGO = 3;

    private final IIncisoRepository incisoRepository;
    private final IPolizaService polizaService;
    private final IVehiculoService vehiculoService;
    private final IEstatusIncisoRepository estatusIncisoRepository;

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

        if(inciso.getEstatusInciso() != null && inciso.getEstatusInciso().getIdEstatusInciso() != null) {
            specification.add(new SearchCriteria(SearchOperation.EQUAL,
                    inciso.getEstatusInciso().getIdEstatusInciso(),
                    Inciso_.ESTATUS_INCISO, EstatusInciso_.ID_ESTATUS_INCISO));
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

    @Override
    public void solicitarPago(List<Inciso> incisos, String username) {
        List<Long> idIncisoList = incisos.stream()
                .map(Inciso::getIdInciso)
                .toList();
        // Al solicitar el pago cambia a estatus en proceso de pago
        this.cambioEstatus(ACCION_SOLICITAR_PAGO, ESTATUS_INCISO_EN_PROCESO_PAGO,
                idIncisoList, null, username);
    }

    @Override
    public void autorizarPago(List<Inciso> incisos, String username) {
        List<Long> idIncisoList = incisos.stream()
                .map(Inciso::getIdInciso)
                .toList();
        // Al autorizar el pago cambia a estatus en pagada
        this.cambioEstatus(ACCION_AUTORIZAR_PAGO, ESTATUS_INCISO_PAGADA,
                idIncisoList, null, username);
    }

    @Override
    public void rechazarSolicitud(List<Inciso> incisos, String motivo, String username) {
        List<Long> idIncisoList = incisos.stream()
                .map(Inciso::getIdInciso)
                .toList();
        // Al autorizar el pago cambia a estatus en pagada
        this.cambioEstatus(ACCION_RECHAZAR_PAGO, ESTATUS_INCISO_REGISTRADA,
                idIncisoList, motivo, username);
    }

    @Override
    public void editar(Inciso inciso, String username) {

        Inciso incisoToUpdate = this.findById(inciso.getIdInciso());

        validateIncisoPoliza(inciso, inciso.getPoliza());

        incisoToUpdate.setFolioFactura(inciso.getFolioFactura());
        incisoToUpdate.setFechaInicioVigencia(inciso.getFechaInicioVigencia());
        incisoToUpdate.setFechaFinVigencia(inciso.getFechaFinVigencia());
        incisoToUpdate.setCosto(inciso.getCosto());
        if(incisoToUpdate.getCosto() > 0)
            incisoToUpdate.setSaldo(-inciso.getCosto());
        else
            incisoToUpdate.setSaldo(0d);

        Integer ESTATUS_REGISTRADO = 1;

        incisoToUpdate.setObservaciones(null);
        incisoToUpdate.setEstatusInciso(EstatusInciso.builder().idEstatusInciso(ESTATUS_REGISTRADO).build());
        incisoToUpdate.setEstatusRegistro(EstatusRegistro.ACTIVO);
        incisoToUpdate.setFechaModificacion(new Date());
        incisoToUpdate.setModificadoPor(username);

        incisoRepository.save(incisoToUpdate);
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
                .fetchIncisoSet(true)
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

                inciso.setVehiculo(vehiculo);
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

        if(inciso.getCosto() == null)
            throw new BadRequestException("El campo Costo es obligatorio.");

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
        Integer ESTATUS_REGISTRADO = 1;

        if(inciso.getCosto() > 0)
            inciso.setSaldo(-inciso.getCosto());
        else
            inciso.setSaldo(0d);

        inciso.setPoliza(poliza);
        inciso.setEstatusInciso(EstatusInciso.builder().idEstatusInciso(ESTATUS_REGISTRADO).build());
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

    private void cambioEstatus(Integer accion, Integer estatusPorAsignar, List<Long> idIncisoList, String motivo, String username) {
        List<Inciso> incisoToUpdateList = incisoRepository.findByIdIncisoIn(idIncisoList);

        if(!incisoToUpdateList.isEmpty()) {

            EstatusInciso estatusInciso = estatusIncisoRepository.findById(estatusPorAsignar)
                    .orElseThrow(() -> new NotFoundException("No se ha encontrado el estatus solicitado."));

            List<Integer> distinctEstatusIncisos = incisoToUpdateList.stream()
                    .map(Inciso::getEstatusInciso)
                    .map(EstatusInciso::getIdEstatusInciso)
                    .distinct()
                    .toList();

            // Si los vehiculos seleccionados tienen diferentes estatus
            // entonces no se puede actualizar. Todos deben tener el mismo estatus.
            if(distinctEstatusIncisos.size() > 1) {
                throw new BadRequestException("Asegurate de seleccionar incisos con el mismo estatus.");
            }

            // Se obtiene el estatus actual de todos los vehiculas a actualizar
            Integer estatusActual = distinctEstatusIncisos.getFirst();

            boolean cancelado = false;
            String accionStr = "";

            // Si la accion es solicitar pago
            if(Objects.equals(accion, ACCION_SOLICITAR_PAGO)) {
                accionStr = "Solicitar pago";
                // El estatus actual debe ser REGISTADO, si no entonces marca error
                if(!Objects.equals(estatusActual, ESTATUS_INCISO_REGISTRADA)) {
                    throw new BadRequestException("Para solicitar la autorización asegúrate de seleccionar incisos con estatus REGISTRADA.");
                }
            } // Si la accion es autorizar pago
            else if(Objects.equals(accion, ACCION_AUTORIZAR_PAGO)) {
                accionStr = "Autorizar solicitud";
                // El estatus actual debe ser POR AUTORIZAR, si no entonces marca error
                if(!Objects.equals(estatusActual, ESTATUS_INCISO_EN_PROCESO_PAGO)) {
                    throw new BadRequestException("Para autorizar el pago asegúrate de seleccionar incisos con estatus EN PROCESO DE PAGO.");
                }
            }  // Si la accion es rechazar solicitud
            else if (Objects.equals(accion, ACCION_RECHAZAR_PAGO)) {
                accionStr = "Rechazar solicitud";
                // El estatus actual debe ser POR AUTORIZAR, si no entonces marca error
                if(!Objects.equals(estatusActual, ESTATUS_INCISO_EN_PROCESO_PAGO)) {
                    throw new BadRequestException("Para rechazar el asegúrate de seleccionar incisos con estatus EN PROCESO DE PAGO.");
                }
            }

            for (Inciso inciso : incisoToUpdateList) {

                inciso.setEstatusInciso(estatusInciso);
                inciso.setObservaciones(motivo);
                inciso.setModificadoPor(username);
                inciso.setFechaModificacion(new Date());

            }

            incisoRepository.saveAll(incisoToUpdateList);
        } else {
            throw new BadRequestException("No se han recibido los incisos por actualizar.");
        }
    }

}
