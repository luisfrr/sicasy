package gob.yucatan.sicasy.views.beans;

import gob.yucatan.sicasy.business.entities.Usuario;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Scope;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.io.Serializable;

@Getter
@Component
@Scope("session")
@RequiredArgsConstructor
@Slf4j
public class UserSessionBean implements Serializable {

    private String userName;
    private String fullName;
    private String firstLetterName;

    @PostConstruct
    public void init() {
        log.info("UserSessionBean init");
        this.loadUserInfo();
    }

    private void loadUserInfo() {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            Usuario usuario = (Usuario) auth.getDetails();
            this.userName = usuario.getUsername();
            this.fullName = usuario.getNombre();
            this.firstLetterName = this.fullName.substring(0, 1).toUpperCase();

        } catch (Exception e) {
            log.error("No se ha logrado obtener la información del usuario.", e);
            this.userName = "";
            this.fullName = "";
            this.firstLetterName = "U";
        }
    }


}
