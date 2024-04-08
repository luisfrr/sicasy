package gob.yucatan.sicasy.views.beans;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.io.Serializable;

@Component
@Scope("session")
@Slf4j
public class MenuSessionBean implements Serializable {

    public String goToHome() {
        return "/views/home?faces-redirect=true";
    }

    public String goToRoles() {
        return "/views/seguridad/roles?faces-redirect=true";
    }

}
