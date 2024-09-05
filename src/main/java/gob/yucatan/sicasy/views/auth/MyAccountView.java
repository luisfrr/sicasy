package gob.yucatan.sicasy.views.auth;

import gob.yucatan.sicasy.business.entities.Usuario;
import gob.yucatan.sicasy.services.iface.IUsuarioService;
import gob.yucatan.sicasy.views.beans.Messages;
import gob.yucatan.sicasy.views.beans.UserSessionBean;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.Optional;

@Component
@Scope("view")
@RequiredArgsConstructor
@Slf4j
public class MyAccountView implements Serializable {

    // Inyección de dependencias
    private final UserSessionBean userSessionBean;
    private final IUsuarioService usuarioService;

    // Variables
    private @Getter String title;
    private @Getter Usuario usuarioSelected;

    @PostConstruct
    public void init() {
        log.info("init - MyAccountView");
        title = "Mi cuenta";

        Optional<Usuario> usuarioOptional = usuarioService.findByUsuario(this.userSessionBean.getUserName());
        if (usuarioOptional.isPresent()) {
            this.usuarioSelected = usuarioOptional.get();
        } else {
            Messages.addError("No se ha logrado obtener la información de tu cuenta. Intenta más tarde. Consulta al administrador si el problema persiste.");
        }

    }

}
