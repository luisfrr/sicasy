package gob.yucatan.sicasy.views.seguridad;

import gob.yucatan.sicasy.business.entities.Rol;
import gob.yucatan.sicasy.services.iface.IRolService;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@Scope("view")
@RequiredArgsConstructor
@Slf4j
public class RolView {


    private @Getter String title;
    private @Getter Rol rolFiltros;
    private @Getter List<Rol> roles;

    private final IRolService rolService;

    @PostConstruct
    public void init() {
        log.info("Init RolView");
        this.title = "Roles";

        this.limpiarFiltros();
    }

    public void limpiarFiltros() {
        this.rolFiltros = new Rol();
        this.roles = new ArrayList<>();
    }

    public void buscar() {
        this.roles = rolService.findAllDynamic(this.rolFiltros);
    }

}
