package gob.yucatan.sicasy.views;

import jakarta.annotation.PostConstruct;
import jakarta.faces.context.ExternalContext;
import jakarta.faces.context.FacesContext;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Scope;
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
        title = "Acceso";
    }

}
