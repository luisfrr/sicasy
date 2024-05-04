package gob.yucatan.sicasy.services.impl;

import gob.yucatan.sicasy.business.dtos.AcuseImportacion;
import gob.yucatan.sicasy.business.entities.*;
import gob.yucatan.sicasy.business.enums.EstatusRegistro;
import gob.yucatan.sicasy.business.exceptions.BadRequestException;
import gob.yucatan.sicasy.business.exceptions.NotFoundException;
import gob.yucatan.sicasy.repository.criteria.SearchCriteria;
import gob.yucatan.sicasy.repository.criteria.SearchOperation;
import gob.yucatan.sicasy.repository.criteria.SearchSpecification;
import gob.yucatan.sicasy.repository.iface.IAnexoRepository;
import gob.yucatan.sicasy.repository.iface.IEstatusVehiculoRepository;
import gob.yucatan.sicasy.repository.iface.ILicitacionRepository;
import gob.yucatan.sicasy.repository.iface.IVehiculoRepository;
import gob.yucatan.sicasy.services.iface.IVehiculoService;
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
public class VehiculoServiceImpl implements IVehiculoService {

    private final Integer ESTATUS_VEHICULO_REGISTRADO = 1;
    private final Integer ESTATUS_VEHICULO_POR_AUTORIZAR = 2;
    private final Integer ESTATUS_VEHICULO_ACTIVO = 3;
    private final Integer ESTATUS_VEHICULO_BAJA = 4;
    private final Integer ESTATUS_VEHICULO_CANCELADO = 5;

    private final IVehiculoRepository vehiculoRepository;
    private final ILicitacionRepository licitacionRepository;
    private final IAnexoRepository anexoRepository;
    private final IEstatusVehiculoRepository estatusVehiculoRepository;

    @Override
    public List<Vehiculo> findAllDynamic(Vehiculo vehiculo) {

        SearchSpecification<Vehiculo> specification = new SearchSpecification<>();

        if(vehiculo.getDependencia() != null && vehiculo.getDependencia().getIdDependencia() != null)
            specification.add(new SearchCriteria(SearchOperation.EQUAL,
                    vehiculo.getDependencia().getIdDependencia(),
                    Vehiculo_.DEPENDENCIA, Dependencia_.ID_DEPENDENCIA));

        if(vehiculo.getCondicionVehiculo() != null &&
                vehiculo.getCondicionVehiculo().getIdCondicionVehiculo() != null)
            specification.add(new SearchCriteria(SearchOperation.EQUAL,
                    vehiculo.getCondicionVehiculo().getIdCondicionVehiculo(),
                    Vehiculo_.CONDICION_VEHICULO, CondicionVehiculo_.ID_CONDICION_VEHICULO));

        if(vehiculo.getEstatusVehiculo() != null &&
                vehiculo.getEstatusVehiculo().getIdEstatusVehiculo() != null)
            specification.add(new SearchCriteria(SearchOperation.EQUAL,
                    vehiculo.getEstatusVehiculo().getIdEstatusVehiculo(),
                    Vehiculo_.ESTATUS_VEHICULO, EstatusVehiculo_.ID_ESTATUS_VEHICULO));

        if(vehiculo.getEstatusVehiculo() != null &&
                vehiculo.getEstatusVehiculo().getIdEstatusVehiculo() != null)
            specification.add(new SearchCriteria(SearchOperation.EQUAL,
                    vehiculo.getEstatusVehiculo().getIdEstatusVehiculo(),
                    Vehiculo_.ESTATUS_VEHICULO, EstatusVehiculo_.ID_ESTATUS_VEHICULO));

        if(vehiculo.getLicitacion() != null && vehiculo.getLicitacion().getIdLicitacion() != null)
            specification.add(new SearchCriteria(SearchOperation.EQUAL,
                    vehiculo.getLicitacion().getIdLicitacion(),
                    Vehiculo_.LICITACION, Licitacion_.ID_LICITACION));

        if(vehiculo.getAnexo() != null && vehiculo.getAnexo().getIdAnexo() != null)
            specification.add(new SearchCriteria(SearchOperation.EQUAL,
                    vehiculo.getAnexo().getIdAnexo(),
                    Vehiculo_.ANEXO, Anexo_.ID_ANEXO));

        if(vehiculo.getMarca() != null && !vehiculo.getMarca().isEmpty())
            specification.add(new SearchCriteria(SearchOperation.EQUAL,
                    vehiculo.getMarca(),
                    Vehiculo_.MARCA));

        if(vehiculo.getModelo() != null && !vehiculo.getModelo().isEmpty())
            specification.add(new SearchCriteria(SearchOperation.EQUAL,
                    vehiculo.getModelo(),
                    Vehiculo_.MODELO));

        if(vehiculo.getAnio() != null)
            specification.add(new SearchCriteria(SearchOperation.EQUAL,
                    vehiculo.getAnio(),
                    Vehiculo_.ANIO));

        if(vehiculo.getNoSerie() != null && !vehiculo.getNoSerie().isEmpty())
            specification.add(new SearchCriteria(SearchOperation.EQUAL,
                    vehiculo.getNoSerie(),
                    Vehiculo_.NO_SERIE));

        if(vehiculo.getEstatusRegistro() != null)
            specification.add(new SearchCriteria(SearchOperation.EQUAL,
                    vehiculo.getEstatusRegistro(),
                    Vehiculo_.ESTATUS_REGISTRO));

        return vehiculoRepository.findAll(specification);
    }

    @Override
    public Vehiculo findById(Long idVehiculo) {
        return vehiculoRepository.findById(idVehiculo)
                .orElseThrow(() -> new NotFoundException("No se ha encontrado el vehículo con el id: " + idVehiculo));
    }

    @Override
    public Vehiculo findFullById(Long idVehiculo) {

        Vehiculo vehiculo = findById(idVehiculo);

        if(vehiculo.getLicitacion() != null && vehiculo.getLicitacion().getIdLicitacion() != null)
            vehiculo.setNumLicitacion(vehiculo.getLicitacion().getNumeroLicitacion());
        else {
            vehiculo.setLicitacion(new Licitacion());
        }

        if(vehiculo.getAnexo() != null && vehiculo.getAnexo().getIdAnexo() != null)
            vehiculo.setAnexoValue(vehiculo.getAnexo().getNombre());
        else {
            vehiculo.setAnexo(new Anexo());
        }

        if(vehiculo.getDependenciaAsignada() == null)
            vehiculo.setDependenciaAsignada(new Dependencia());

        return vehiculo;
    }

    @Override
    public List<Vehiculo> findAllByNoSerie(List<String> noSerieList) {
        return vehiculoRepository.findVehiculosActivosByNoSerie(noSerieList);
    }

    @Override
    public List<String> findDistinctMarcas() {
        return vehiculoRepository.findDistinctMarcasByEstatusActivo();
    }

    @Override
    public List<String> findDistinctModelo(String marca) {
        return vehiculoRepository.findDistinctModeloByEstatusActivo(marca);
    }

    @Override
    public List<Integer> findDistinctAnio(String marca, String modelo) {
        return vehiculoRepository.findDistinctAnioByEstatusActivo(marca, modelo);
    }

    @Override
    public Vehiculo findByNoSerie(String noSerie) {
        return vehiculoRepository.findVehiculoActivoByNoSerie(noSerie)
                .orElseThrow(() -> new NotFoundException("No se ha encontrado el vehículo con el número de serie " + noSerie));
    }

    @Override
    public Vehiculo agregar(Vehiculo vehiculo) {
        Integer ESTATUS_VEHICULO_REGISTRADO = 1;

        // Validar si ya existe un vehiculo con ese no. de serie
        if(vehiculoRepository.existsByEstatusRegistroActivoAndNoSerie(vehiculo.getNoSerie()))
            throw new BadRequestException("Ya existe un vehículo con ese número de serie.");

        if(vehiculo.getAnexo().getIdAnexo() == null)
            vehiculo.setAnexo(null);
        if(vehiculo.getLicitacion().getIdLicitacion() == null)
            vehiculo.setLicitacion(null);
        if(vehiculo.getDependenciaAsignada().getIdDependencia() == null)
            vehiculo.setDependenciaAsignada(null);

        // Se agrega el estatus vehículo
        vehiculo.setEstatusVehiculo(EstatusVehiculo.builder().idEstatusVehiculo(ESTATUS_VEHICULO_REGISTRADO).build());
        vehiculo.setEstatusRegistro(EstatusRegistro.ACTIVO);

        return vehiculoRepository.save(vehiculo);
    }

    @Override
    public void editar(Vehiculo vehiculo) {

        Vehiculo vehiculoToUpdate = this.findById(vehiculo.getIdVehiculo());

        // Validar si ya existe un vehiculo con ese no. de serie
        if(vehiculoRepository.existsByEstatusRegistroActivoAndNoSerieAndIdVehiculoNot(vehiculo.getNoSerie(),
                vehiculo.getIdVehiculo()))
            throw new BadRequestException("Ya existe un vehículo con ese número de serie.");

        if(vehiculo.getLicitacion().getIdLicitacion() == null)
            vehiculoToUpdate.setLicitacion(null);
        else
            vehiculoToUpdate.setLicitacion(vehiculo.getLicitacion());

        if(vehiculo.getAnexo().getIdAnexo() == null)
            vehiculoToUpdate.setAnexo(null);
        else
            vehiculoToUpdate.setAnexo(vehiculo.getAnexo());

        if(vehiculo.getDependenciaAsignada().getIdDependencia() == null)
            vehiculoToUpdate.setDependenciaAsignada(null);
        else
            vehiculoToUpdate.setDependenciaAsignada(vehiculo.getDependenciaAsignada());

        vehiculoToUpdate.setDependencia(vehiculo.getDependencia());
        vehiculoToUpdate.setNoSerie(vehiculo.getNoSerie());
        vehiculoToUpdate.setPlaca(vehiculo.getPlaca());
        vehiculoToUpdate.setMarca(vehiculo.getMarca());
        vehiculoToUpdate.setModelo(vehiculo.getModelo());
        vehiculoToUpdate.setAnio(vehiculo.getAnio());
        vehiculoToUpdate.setNoMotor(vehiculo.getNoMotor());
        vehiculoToUpdate.setColor(vehiculo.getColor());
        vehiculoToUpdate.setCondicionVehiculo(vehiculo.getCondicionVehiculo());
        vehiculoToUpdate.setNoFactura(vehiculo.getNoFactura());
        vehiculoToUpdate.setMontoFactura(vehiculo.getMontoFactura());
        vehiculoToUpdate.setRentaMensual(vehiculo.getRentaMensual());
        vehiculoToUpdate.setDescripcionVehiculo(vehiculo.getDescripcionVehiculo());
        vehiculoToUpdate.setResguardante(vehiculo.getResguardante());
        vehiculoToUpdate.setAreaResguardante(vehiculo.getAreaResguardante());
        vehiculoToUpdate.setProveedor(vehiculo.getProveedor());

        vehiculoToUpdate.setModificadoPor(vehiculo.getModificadoPor());
        vehiculoToUpdate.setFechaModificacion(new Date());

        vehiculoRepository.save(vehiculoToUpdate);
    }

    @Override
    @Transactional
    public List<AcuseImportacion> importar(List<Vehiculo> vehiculos, Integer idDependencia, String username) {

        List<AcuseImportacion> acuseImportacionList = new ArrayList<>();

        this.validarImportacion(acuseImportacionList, vehiculos, idDependencia, username);

        if(acuseImportacionList.stream().anyMatch(acuseImportacion -> acuseImportacion.getError() == 1)) {
            return acuseImportacionList;
        }

        try {
            vehiculoRepository.saveAll(vehiculos);
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
    @Transactional
    public void solicitarAutorizacion(List<Long> idVehiculoList, String username) {
        // Al solicitar una autorizacion cambia a estatus por autorizar
        this.cambioEstatus(ESTATUS_VEHICULO_POR_AUTORIZAR, idVehiculoList, null, username);
    }

    @Override
    @Transactional
    public void autorizarSolicitud(List<Long> idVehiculoList, String username) {
        // Al autorizar una solicitud cambia a estatus activo
        this.cambioEstatus(ESTATUS_VEHICULO_ACTIVO, idVehiculoList, null, username);
    }

    @Override
    @Transactional
    public void rechazarSolicitud(List<Long> idVehiculoList, String motivo, String username) {
        // Al rechazar una solicitud regresa a estatus registrado
        this.cambioEstatus(ESTATUS_VEHICULO_REGISTRADO, idVehiculoList, motivo, username);
    }

    @Override
    @Transactional
    public void cancelarSolicitud(List<Long> idVehiculoList, String motivo, String username) {
        // Al cancelar una solicitud cambia a estatus cancelado
        this.cambioEstatus(ESTATUS_VEHICULO_CANCELADO, idVehiculoList, motivo, username);
    }

    @Override
    @Transactional
    public void solicitarModificacion(List<Long> idVehiculoList, String motivo, String username) {
        // Al solicitar una modificación cambia a estatus registrado
        this.cambioEstatus(ESTATUS_VEHICULO_REGISTRADO, idVehiculoList, motivo, username);
    }

    @Override
    @Transactional
    public void solicitarBaja(List<Long> idVehiculoList, String motivo, String username) {
        // Al solicitar una baja cambia a estatus baja
        this.cambioEstatus(ESTATUS_VEHICULO_BAJA, idVehiculoList, motivo, username);
    }

    private void validarImportacion(List<AcuseImportacion> acuseImportacionList, List<Vehiculo> vehiculos, Integer idDependencia, String username) {
        Integer ESTATUS_VEHICULO_REGISTRADO = 1;

        for (Vehiculo vehiculo : vehiculos) {
            try {
                // Validacion de campos obligatorios
                if(vehiculo.getNoSerie() == null || vehiculo.getNoSerie().isEmpty())
                    throw new BadRequestException("El campo No. Serie es obligatorio");

                if(vehiculo.getPlaca() == null || vehiculo.getPlaca().isEmpty())
                    throw new BadRequestException("El campo Placa es obligatorio");

                if(vehiculo.getMarca() == null || vehiculo.getMarca().isEmpty())
                    throw new BadRequestException("El campo Marca es obligatorio");

                if(vehiculo.getModelo() == null || vehiculo.getModelo().isEmpty())
                    throw new BadRequestException("El campo Modelo es obligatorio");

                if(vehiculo.getAnio() == null)
                    throw new BadRequestException("El campo Año es obligatorio");

                if(vehiculo.getNoMotor() == null || vehiculo.getNoMotor().isEmpty())
                    throw new BadRequestException("El campo No. Motor es obligatorio");

                if(vehiculo.getColor() == null || vehiculo.getColor().isEmpty())
                    throw new BadRequestException("El campo Color es obligatorio");

                if(vehiculo.getCondicionId() == null)
                    throw new BadRequestException("El campo Condición es obligatorio");
                else if(vehiculo.getCondicionId() < 1 || vehiculo.getCondicionId() > 3)
                    throw new BadRequestException("El campo Condición solo acepta valor 1, 2 o 3.");


                if(vehiculo.getNoFactura() == null || vehiculo.getNoFactura().isEmpty())
                    throw new BadRequestException("El campo No. Factura es obligatorio");

                if(vehiculo.getMontoFactura() == null)
                    throw new BadRequestException("El campo Valor Factura es obligatorio");

                if(vehiculo.getRentaMensual() == null)
                    throw new BadRequestException("El campo Renta Mensual es obligatorio");

                // Se agregan los campos que hacen referencia a otras tablas
                vehiculo.setDependencia(Dependencia.builder().idDependencia(idDependencia).build());

                if(vehiculo.getCondicionId() != null)
                    vehiculo.setCondicionVehiculo(CondicionVehiculo.builder()
                            .idCondicionVehiculo(vehiculo.getCondicionId())
                            .build());

                if(vehiculo.getNumLicitacion() != null) {
                    Licitacion licitacion = licitacionRepository
                            .findByNumeroLicitacionAndEstatusRegistro(vehiculo.getNumLicitacion(),
                                    EstatusRegistro.ACTIVO)
                            .orElseThrow(() -> new NotFoundException("No se encontro la licitacion con el número de licitacion: "
                                    + vehiculo.getNumLicitacion()));
                    vehiculo.setLicitacion(licitacion);
                }

                if(vehiculo.getNumLicitacion() != null) {
                    Anexo anexo = anexoRepository
                            .findAnexoActivoByNombreAndLicitacion(vehiculo.getAnexoValue(),
                                    vehiculo.getLicitacion())
                            .orElseThrow(() -> new NotFoundException("No se encontro el anexo. Anexo: " + vehiculo.getAnexoValue() +
                                    ", número de licitación: "+ vehiculo.getNumLicitacion()));
                    vehiculo.setAnexo(anexo);
                }

                vehiculo.setEstatusVehiculo(EstatusVehiculo.builder()
                        .idEstatusVehiculo(ESTATUS_VEHICULO_REGISTRADO)
                        .build());
                vehiculo.setEstatusRegistro(EstatusRegistro.ACTIVO);
                vehiculo.setFechaCreacion(new Date());
                vehiculo.setCreadoPor(username);

            } catch (Exception e) {
                acuseImportacionList.add(AcuseImportacion.builder()
                                .titulo(vehiculo.getNoSerie())
                                .mensaje(e.getMessage())
                                .error(1)
                        .build());
            }
        }

        List<String> noSerieList = vehiculos.stream()
                .map(Vehiculo::getNoSerie)
                .distinct()
                .toList();
        List<Vehiculo> existsVehiculoByNoSerie = vehiculoRepository.findVehiculosActivosByNoSerie(noSerieList);

        // Si la lista no es vacia, quiere decir ya existen algunos vehiculos registrados
        if(!existsVehiculoByNoSerie.isEmpty()) {
            existsVehiculoByNoSerie.forEach(v -> acuseImportacionList.add(AcuseImportacion.builder()
                    .titulo(v.getNoSerie())
                    .mensaje("Ya existe un vehículo registrado con ese número de serie")
                    .error(1)
                    .build()));
        }

        // Si es diferente quiere decir que un número de serie se está duplicando en el layout
        if(noSerieList.size() != vehiculos.size()) {
            List<String> noSerieDuplicadosLayout = vehiculos.stream()
                    .map(Vehiculo::getNoSerie)
                    .filter(noSerie -> !noSerieList.contains(noSerie))
                    .toList();
            noSerieDuplicadosLayout.forEach(noSerie -> acuseImportacionList.add(AcuseImportacion.builder()
                    .titulo(noSerie)
                    .mensaje("El número de serie esta duplicado en este layout.")
                    .error(1)
                    .build()));
        }
    }

    private void cambioEstatus(Integer estatusPorAsignar, List<Long> idVehiculoList, String motivo, String username) {
        List<Vehiculo> vehiculoToUpdateList = vehiculoRepository.findAllByIdVehiculo(idVehiculoList);

        if(!vehiculoToUpdateList.isEmpty()) {

            EstatusVehiculo estatusVehiculo = estatusVehiculoRepository.findById(estatusPorAsignar)
                    .orElseThrow(() -> new NotFoundException("No se ha encontrado el estatus solicitado."));

            List<Integer> distinctEstatusVehiculos = vehiculoToUpdateList.stream()
                    .map(Vehiculo::getEstatusVehiculo)
                    .map(EstatusVehiculo::getIdEstatusVehiculo)
                    .distinct()
                    .toList();

            // Si los vehiculos seleccionados tienen diferentes estatus
            // entonces no se puede actualizar. Todos deben tener el mismo estatus.
            if(distinctEstatusVehiculos.size() > 1) {
                throw new BadRequestException("Asegurate de seleccionar vehículos con el mismo estatus.");
            }

            // Se obtiene el estatus actual de todos los vehiculas a actualizar
            Integer estatusActual = distinctEstatusVehiculos.getFirst();

            boolean cancelado = false;

            // Si estatus que se va a asignar es POR AUTORIZAR
            if(Objects.equals(estatusPorAsignar, ESTATUS_VEHICULO_POR_AUTORIZAR)){
                // Si el estatus actual no es REGISTRADO entonces no se puede cambiar a POR AUTORIZAR
                if(!Objects.equals(estatusActual, ESTATUS_VEHICULO_REGISTRADO)) {
                    throw new BadRequestException("Para solicitar la autorización asegúrate de seleccionar vehículos con estatus REGISTRADO.");
                }
            } // Si estatus que se va a asignar es CANCELADO
            else if (Objects.equals(estatusPorAsignar, ESTATUS_VEHICULO_CANCELADO)) {
                // Si el estatus actual no es POR AUTORIZAR entonces no se puede cambiar a CANCELADO
                if(!Objects.equals(estatusActual, ESTATUS_VEHICULO_POR_AUTORIZAR)) {
                    throw new BadRequestException("Para cancelar las solicitudes asegúrate de seleccionar vehículos con estatus POR AUTORIZAR.");
                }
                cancelado = true;
            } // Si estatus que se va a asignar es ACTIVO
            else if (Objects.equals(estatusPorAsignar, ESTATUS_VEHICULO_ACTIVO)) {
                // Si el estatus actual no es POR AUTORIZAR entonces no se puede cambiar a ACTIVO
                if(!Objects.equals(estatusActual, ESTATUS_VEHICULO_POR_AUTORIZAR)) {
                    throw new BadRequestException("Para autorizar las solicitudes asegúrate de seleccionar vehículos con estatus POR AUTORIZAR.");
                }
            } // Si estatus que se va a asignar es BAJA
            else if(Objects.equals(estatusPorAsignar, ESTATUS_VEHICULO_BAJA)) {
                // Si el estatus actual no es ACTIVO entonces no se puede cambiar a BAJA
                if(!Objects.equals(estatusActual, ESTATUS_VEHICULO_ACTIVO)) {
                    throw new BadRequestException("Para solicitar la baja asegúrate de seleccionar vehículos con estatus ACTIVO.");
                }
            }
            else if(Objects.equals(estatusPorAsignar, ESTATUS_VEHICULO_REGISTRADO)) {
                // Si el estatus actual no es ACTIVO o BAJA entonces no se puede cambiar a REGISTRADO
                if(!Objects.equals(estatusActual, ESTATUS_VEHICULO_ACTIVO) || !Objects.equals(estatusActual, ESTATUS_VEHICULO_BAJA)) {
                    throw new BadRequestException("Para solicitar la modificación asegúrate de seleccionar únicamente vehículos con estatus ACTIVO o BAJA.");
                } // Si el estatus actual no es POR AUTORIZAR entonces no se puede cambiar a REGISTRADO
                else if(!Objects.equals(estatusActual, ESTATUS_VEHICULO_POR_AUTORIZAR)) {
                    throw new BadRequestException("Para rechazar la solicitud asegúrate de seleccionar vehículos con estatus POR AUTORIZAR.");
                }
            }

            for (Vehiculo vehiculo : vehiculoToUpdateList) {
                vehiculo.setEstatusVehiculo(estatusVehiculo);
                vehiculo.setObservaciones(motivo);
                vehiculo.setModificadoPor(username);
                vehiculo.setFechaModificacion(new Date());

                if(cancelado) {
                    vehiculo.setEstatusRegistro(EstatusRegistro.BORRADO);
                    vehiculo.setFechaBorrado(new Date());
                    vehiculo.setBorradoPor(username);
                }
            }

            vehiculoRepository.saveAll(vehiculoToUpdateList);
        } else {
            throw new BadRequestException("No se han recibido los vehículos por actualizar.");
        }
    }
}
