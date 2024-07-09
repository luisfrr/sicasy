package gob.yucatan.sicasy.views;

import jakarta.annotation.PostConstruct;
import jakarta.faces.context.FacesContext;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Scope;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.Serializable;

@Getter
@Component
@Scope("view")
@RequiredArgsConstructor
@Slf4j
public class LoginView implements Serializable {

    private String title;

    @PostConstruct
    private void init() {
        title = "Login";

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !auth.isAuthenticated() || auth instanceof AnonymousAuthenticationToken) {
            // Usuario no autenticado, permanece en la página de login
            log.info("Usuario anónimo o no autenticado");
        } else {
            // Usuario autenticado, redirigir al inicio
            try {
                FacesContext.getCurrentInstance().getExternalContext().redirect("views/home.faces");
            } catch (IOException e) {
                log.error("Error al redirigir a la página de inicio", e);
            }
        }
    }

}
