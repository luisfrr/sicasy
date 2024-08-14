package gob.yucatan.sicasy.services.impl;

import gob.yucatan.sicasy.business.entities.*;
import gob.yucatan.sicasy.business.enums.EstatusRegistro;
import gob.yucatan.sicasy.business.exceptions.BadRequestException;
import gob.yucatan.sicasy.business.exceptions.NotFoundException;
import gob.yucatan.sicasy.repository.criteria.SearchCriteria;
import gob.yucatan.sicasy.repository.criteria.SearchOperation;
import gob.yucatan.sicasy.repository.criteria.SearchSpecification;
import gob.yucatan.sicasy.repository.iface.IEstatusSiniestroRepository;
import gob.yucatan.sicasy.repository.iface.IIncisoRepository;
import gob.yucatan.sicasy.repository.iface.ISiniestroRepository;
import gob.yucatan.sicasy.services.iface.IBitacoraSiniestroService;
import gob.yucatan.sicasy.services.iface.ISiniestroService;
import gob.yucatan.sicasy.utils.strings.HtmlEntityConverter;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Slf4j
public class SiniestroServiceImpl implements ISiniestroService {

    private final Integer ESTATUS_SINIESTRO_REGISTRADO = 1;
    private final Integer ESTATUS_SINIESTRO_EN_PROCESO_PAGO = 2;
    private final Integer ESTATUS_SINIESTRO_FINALIZADO = 3;

    private final Integer ACCION_SOLICITAR_PAGO_DEDUCIBLE = 1;
    private final Integer ACCION_RECHAZAR_SOLICITUD = 2;
    private final Integer ACCION_AUTORIZAR_PAGO_DEDUCIBLE = 3;
    private final Integer ACCION_FINALIZAR_REGISTRO = 4;

    private final ISiniestroRepository siniestroRepository;
    private final IEstatusSiniestroRepository estatusSiniestroRepository;
    private final IIncisoRepository incisoRepository;
    private final IBitacoraSiniestroService bitacoraSiniestroService;

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
        Siniestro siniestro = siniestroRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("No se ha encontrado el registro del siniestro."));

        if(siniestro.getDeducible() == null) {
            Deducible deducible = Deducible.builder()
                    .vehiculoNoSerie(siniestro.getVehiculo().getNoSerie())
                    .vehiculoPlaca(siniestro.getVehiculo().getPlaca())
                    .vehiculoMarca(siniestro.getVehiculo().getMarca())
                    .vehiculoModelo(siniestro.getVehiculo().getModelo())
                    .vehiculoAnio(siniestro.getVehiculo().getAnio())
                    .vehiculoValorFactura(siniestro.getVehiculo().getMontoFactura())
                    .polizaNumero(siniestro.getInciso() != null ? siniestro.getInciso().getPoliza().getNumeroPoliza() : null)
                    .polizaAseguradora(siniestro.getInciso() != null ? siniestro.getInciso().getPoliza().getAseguradora().getNombre() : null)
                    .incisoNumero(siniestro.getInciso() != null ? siniestro.getInciso().getNumeroInciso() : null)
                    .incisoTipoCobertura(siniestro.getInciso() != null ? siniestro.getInciso().getTipoCobertura() : null)
                    .incisoFechaInicioVigencia(siniestro.getInciso() != null ? siniestro.getInciso().getFechaInicioVigencia() : null)
                    .incisoFechaFinVigencia(siniestro.getInciso() != null ? siniestro.getInciso().getFechaFinVigencia() : null)
                    .tienePolizaVigente(siniestro.getInciso() != null ? 1 : 0)
                    .build();


            siniestro.setDeducible(deducible);

        }

        siniestro.setCheckCorralon(siniestro.getCorralon() == 1);
        siniestro.setCheckMulta(siniestro.getMultaVehiculo() == 1);
        siniestro.setCheckDanioViaPublica(siniestro.getDanioViaPublica() == 1);
        siniestro.setCheckPerdidaTotal(siniestro.getPerdidaTotal() == 1);

        return siniestro;
    }

    @Override
    @Transactional
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

        Siniestro siniestroSaved = siniestroRepository.save(siniestro);
        bitacoraSiniestroService.guardarBitacora("Nuevo registro", null, siniestroSaved, userName);
    }

    @Override
    @Transactional
    public void solicitarPagoDeducible(List<Long> idSiniestroList, String userName) {
        // Al solicitar el pago de deducible cambia a estatus en proceso de pago
        this.cambioEstatus(ACCION_SOLICITAR_PAGO_DEDUCIBLE, ESTATUS_SINIESTRO_EN_PROCESO_PAGO,
                idSiniestroList, null, userName);
    }

    @Override
    @Transactional
    public void autorizarPagoDeducible(List<Long> idSiniestroList, String userName) {
        // Al autorizar el pago de deducible cambia a estatus finalizado
        this.cambioEstatus(ACCION_AUTORIZAR_PAGO_DEDUCIBLE, ESTATUS_SINIESTRO_FINALIZADO,
                idSiniestroList, null, userName);
    }

    @Override
    @Transactional
    public void rechazarSolicitud(List<Long> idSiniestroList, String motivo, String userName) {
        // Al rechazar la solicitud cambia a estatus registrado
        this.cambioEstatus(ACCION_RECHAZAR_SOLICITUD, ESTATUS_SINIESTRO_REGISTRADO,
                idSiniestroList, motivo, userName);
    }

    @Override
    @Transactional
    public void finalizarRegistro(List<Long> idSiniestroList, String userName) {
        // Al finalizar el registro del siniestro cambia a estatus finalizado
        this.cambioEstatus(ACCION_FINALIZAR_REGISTRO, ESTATUS_SINIESTRO_FINALIZADO,
                idSiniestroList, null, userName);
    }

    @Override
    @Transactional
    public void editar(Siniestro siniestro, String userName) {
        Siniestro siniestroAnterior = siniestroRepository.findById(siniestro.getIdSiniestro())
                        .orElseThrow(() -> new NotFoundException("No se ha logrado obtener la información del siniestro"));

        siniestro.setCorralon(siniestro.isCheckCorralon() ? 1 : 0);
        siniestro.setDanioViaPublica(siniestro.isCheckDanioViaPublica() ? 1 : 0);
        siniestro.setPerdidaTotal(siniestro.isCheckPerdidaTotal() ? 1 : 0);
        siniestro.setMultaVehiculo(siniestro.isCheckMulta() ? 1 : 0);

        siniestro.setModificadoPor(userName);
        siniestro.setFechaModificacion(new Date());

        if (siniestro.getDeducible() == null ||
                siniestro.getDeducible().getIdDeducible() == null ||
                siniestro.getDeducible().getIdDeducible() == 0) {
            siniestro.setDeducible(null);
        }

        bitacoraSiniestroService.guardarBitacora("Editar registro", siniestroAnterior, siniestro, userName);
        siniestroRepository.save(siniestro);
    }


    private void cambioEstatus(Integer accion, Integer estatusPorAsignar, List<Long> idSiniestroList, String motivo, String username) {

        Integer VEHICULO_PERDIDA_TOTAL = 1;
        Integer INCISO_MARCAR_BAJA_PENDIENTE = 1;

        List<Siniestro> siniestroToUpdateList = siniestroRepository.findByIdSiniestroIn(idSiniestroList);

        if(!siniestroToUpdateList.isEmpty()) {

            EstatusSiniestro estatusSiniestro = estatusSiniestroRepository.findById(estatusPorAsignar)
                    .orElseThrow(() -> new NotFoundException("No se ha encontrado el estatus seleccionado."));

            List<Integer> distinctEstatusSiniestros = siniestroToUpdateList.stream()
                    .map(Siniestro::getEstatusSiniestro)
                    .map(EstatusSiniestro::getIdEstatusSiniestro)
                    .distinct()
                    .toList();

            // Si los siniestros seleccionados tienen diferentes estatus
            // entonces no se puede actualizar. Todos deben tener el mismo estatus.
            if(distinctEstatusSiniestros.size() > 1) {
                throw new BadRequestException("Asegurate de seleccionar registros con el mismo estatus.");
            }

            Integer estatusActual = distinctEstatusSiniestros.getFirst();

            String accionStr = "";

            if(Objects.equals(accion, ACCION_SOLICITAR_PAGO_DEDUCIBLE)) {
                accionStr = "Solicitar pago de deducible";
                // El estatus actual debe ser REGISTADO, si no entonces marca error
                if(!Objects.equals(estatusActual, ESTATUS_SINIESTRO_REGISTRADO)) {
                    throw new BadRequestException("Para solicitar el pago de deducible asegúrate de seleccionar registros con estatus REGISTRADO.");
                }

                if(!siniestroToUpdateList.stream().allMatch(Siniestro::requierePagoDeducible)) {
                    throw new BadRequestException("Alguno de los registros seleccionados no requiere pago de deducible. Asegúrate de seleccionar registros que requieran pago de deducible.");
                }

            } else if(Objects.equals(accion, ACCION_AUTORIZAR_PAGO_DEDUCIBLE)) {
                accionStr = "Autorizar pago de deducible";
                // El estatus actual debe ser EN PROCESO PAGO, si no entonces marca error
                if(!Objects.equals(estatusActual, ESTATUS_SINIESTRO_EN_PROCESO_PAGO)) {
                    throw new BadRequestException("Para autorizar el pago de deducible asegúrate de seleccionar registros con estatus EN PROCESO DE PAGO.");
                }
            } else if(Objects.equals(accion, ACCION_RECHAZAR_SOLICITUD)) {
                accionStr = "Rechazar solicitud de pago";
                // El estatus actual debe ser EN PROCESO PAGO, si no entonces marca error
                if(!Objects.equals(estatusActual, ESTATUS_SINIESTRO_EN_PROCESO_PAGO)) {
                    throw new BadRequestException("Para rechazar la solicitud de pago asegúrate de seleccionar registros con estatus EN PROCESO DE PAGO.");
                }
            } else if(Objects.equals(accion, ACCION_FINALIZAR_REGISTRO)) {
                accionStr = "Finalizar registro";
                // El estatus actual debe ser EN PROCESO PAGO, si no entonces marca error
                if(!Objects.equals(estatusActual, ESTATUS_SINIESTRO_REGISTRADO)) {
                    throw new BadRequestException("Para finalizar el registro del siniestro asegúrate de seleccionar registros con estatus REGISTRADO.");
                }

                if(siniestroToUpdateList.stream().anyMatch(Siniestro::requierePagoDeducible)) {
                    throw new BadRequestException("Alguno de los registros seleccionados requiere pago de deducible. Solo puedes finalizar el registro del siniestro si no requiere pago de deducible.");
                }
            }

            List<BitacoraSiniestro> bitacoraSiniestroList = new ArrayList<>();
            for(Siniestro siniestro : siniestroToUpdateList) {
                // Se genera la copia de siniestro
                Siniestro siniestroAnterior = siniestro.clone();

                siniestro.setEstatusSiniestro(estatusSiniestro);
                siniestro.setObservaciones(motivo);
                siniestro.setModificadoPor(username);
                siniestro.setFechaModificacion(new Date());

                if(Objects.equals(siniestro.getPerdidaTotal(), VEHICULO_PERDIDA_TOTAL)
                        && siniestro.getInciso() != null) {

                    Inciso inciso = siniestro.getInciso();
                    inciso.setBajaPendienteSiniestro(INCISO_MARCAR_BAJA_PENDIENTE);
                    inciso.setFechaModificacion(new Date());
                    inciso.setModificadoPor(username);

                    incisoRepository.save(inciso);
                }

                bitacoraSiniestroList.add(bitacoraSiniestroService.getBitacora(accionStr, siniestroAnterior, siniestro, username));
            }

            siniestroRepository.saveAll(siniestroToUpdateList);
            bitacoraSiniestroService.saveAll(bitacoraSiniestroList);
        } else {
            throw new BadRequestException("No has seleccionado registros. Intenta de nuevo.");
        }

    }

}
