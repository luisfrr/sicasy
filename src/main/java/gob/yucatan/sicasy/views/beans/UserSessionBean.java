package gob.yucatan.sicasy.views.beans;

import gob.yucatan.sicasy.business.entities.Usuario;
import jakarta.annotation.PostConstruct;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Scope;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Getter
@Component
@Scope("session")
@RequiredArgsConstructor
@Slf4j
public class UserSessionBean implements Serializable {

    private String userName;
    private String fullName;
    private String firstLetterName;
    private @Getter(AccessLevel.NONE) Collection<? extends GrantedAuthority> authorities;

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
            this.authorities = auth.getAuthorities();
        } catch (Exception e) {
            log.error("No se ha logrado obtener la información del usuario.", e);
            this.userName = "";
            this.fullName = "";
            this.firstLetterName = "U";
        }
    }

    public boolean isOwner() {
        return this.authorities != null && this.authorities.stream().anyMatch(g -> g.getAuthority().equals("ROLE_OWNER"));
    }

}
