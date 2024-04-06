package gob.yucatan.sicasy.views;

import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.io.Serializable;

@Component
@Scope("view")
@RequiredArgsConstructor
@Slf4j
public class HomeView implements Serializable {

    private @Getter String title;

    @PostConstruct
    private void init() {
        title = "Inicio";
    }

}
