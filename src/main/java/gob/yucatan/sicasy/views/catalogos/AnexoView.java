package gob.yucatan.sicasy.views.catalogos;

import gob.yucatan.sicasy.business.annotations.ConfigPermiso;
import gob.yucatan.sicasy.business.entities.Anexo;
import gob.yucatan.sicasy.business.entities.Licitacion;
import gob.yucatan.sicasy.business.enums.EstatusRegistro;
import gob.yucatan.sicasy.business.enums.TipoPermiso;
import gob.yucatan.sicasy.business.exceptions.BadRequestException;
import gob.yucatan.sicasy.business.exceptions.NotFoundException;
import gob.yucatan.sicasy.services.iface.IAnexoService;
import gob.yucatan.sicasy.services.iface.ILicitacionService;
import gob.yucatan.sicasy.views.beans.UserSessionBean;
import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.primefaces.PrimeFaces;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Component
@Scope("view")
@RequiredArgsConstructor
@Slf4j
@ConfigPermiso(tipo = TipoPermiso.VIEW, codigo = "CATALOGO_ANEXO_VIEW", nombre = "Catálogo de Anexos")
public class AnexoView {

    private @Getter String title;
    private @Getter String titleDialog;

    private @Getter Anexo anexoFilter;
    private @Getter Anexo anexoSelected;
    private @Getter List<Anexo> anexoList;
    private @Getter EstatusRegistro[] estatusRegistros;
    private @Getter List<Licitacion> licitacionesActivasList;

    private final IAnexoService anexoService;
    private final ILicitacionService licitacionService;
    private final UserSessionBean userSessionBean;

    @PostConstruct
    public void init(){
        log.info("Inicializando Anexos View");

        this.title = "Anexos";
        this.anexoSelected = null;
        this.licitacionesActivasList = null;
        this.estatusRegistros = EstatusRegistro.values();
        this.limpiarFiltros();
    }

    public void limpiarFiltros(){
        log.info("Limpiando filtros");
        this.anexoFilter = new Anexo();
        this.anexoFilter.setEstatusRegistro(EstatusRegistro.ACTIVO);
        this.licitacionesActivasList = licitacionService.findAllLicitacionActive();
        this.buscar();
    }

    public void buscar(){
        log.info("Buscando Anexos");
        this.anexoList = anexoService.findAllDynamic(this.anexoFilter);
    }

    public void agregarAnexo(){
        log.info("Agregando Anexo");
        this.titleDialog = "Agregar Anexo";
        this.anexoSelected = new Anexo();
        this.anexoSelected.setLicitacion(new Licitacion());

    }

    public void guardarAnexo(){
        log.info("Guardando anexo");

        try {
            if (this.anexoSelected != null) {
                if (this.anexoSelected.getIdAnexo() != null){
                    // si no es null el id entonces es un update
                    this.anexoSelected.setModificadoPor(userSessionBean.getUserName());
                    this.anexoSelected.setFechaModificacion(new Date());
                    anexoService.update(anexoSelected);

                }else {

                    // id null, entonces se inserta nuevo en BD
                    this.anexoSelected.setCreadoPor(userSessionBean.getUserName());
                    this.anexoSelected.setFechaCreacion(new Date());
                    anexoService.save(anexoSelected);

                }

                FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_INFO,
                        "Operación exitosa", "Se ha guardado correctamente la información");
                FacesContext.getCurrentInstance().addMessage(null, msg);
                PrimeFaces.current().executeScript("PF('formDialog').hide();");
                this.buscar();
                this.anexoSelected  = null;

            }
        }catch (Exception ex) {
            String message;
            if(ex instanceof BadRequestException)
                message = ex.getMessage();
            else if(ex instanceof NotFoundException)
                message = ex.getMessage();
            else
                message = "Ocurrió un error innesperado.";
            FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    "Error", message);
            FacesContext.getCurrentInstance().addMessage(null, msg);
        }

    }

    public void editar(Long id) {
        log.info("editar");
        Optional<Anexo> anexoOptional = anexoService.findById(id);

        if(anexoOptional.isEmpty()) {
            FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    "Error", "No se ha encontrado la información de este Anexo.");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            PrimeFaces.current().executeScript("PF('formDialog').hide();");
        } else {
            titleDialog = "Editar Anexo";
            this.anexoSelected = anexoOptional.get();
        }
    }

    public void doEliminar(Long id) {
        log.info("prepare to eliminar");
        Optional<Anexo> anexoOptional = anexoService.findById(id);
        this.anexoSelected = anexoOptional.orElse(null);

    }

    public void eliminar() {
        log.info("eliminar Anexo");

        if(this.anexoSelected == null) {
            FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    "Error", "No se ha encontrado la información de este Anexo.");
            FacesContext.getCurrentInstance().addMessage(null, msg);
        }else {

//            Optional<Licitacion> optionalLicitacion = licitacionService.findById(this.anexoSelected.getLicitacion().getIdLicitacion());
//
//            if (optionalLicitacion.isPresent() && optionalLicitacion.get().getEstatusRegistro().equals(EstatusRegistro.ACTIVO)){
//                FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_WARN,
//                        "Atención", "No se puede eliminar la información cuando la Licitación esta Activa.");
//                FacesContext.getCurrentInstance().addMessage(null, msg);
//            }else {
                FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_INFO,
                        "Aviso", "Se ha eliminado exitósamente la información");
                FacesContext.getCurrentInstance().addMessage(null, msg);
                this.anexoSelected.setBorradoPor(userSessionBean.getUserName());
                anexoService.delete(this.anexoSelected);
                this.anexoSelected = null;
                this.limpiarFiltros();
//            }

        }

        PrimeFaces.current().executeScript("PF('confirmDialog').hide();");

    }

}
