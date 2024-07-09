package gob.yucatan.sicasy.views;

import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.io.Serializable;

@Getter
@Component
@Scope("view")
@RequiredArgsConstructor
@Slf4j
public class HomeView implements Serializable {

    // Variables Generales
    private String title;

    @PostConstruct
    private void init() {
        log.info("PostConstruct - HomeView");
        title = "Inicio";
    }

}
