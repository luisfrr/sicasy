package gob.yucatan.sicasy.views;

import gob.yucatan.sicasy.business.dtos.ExampleDto;
import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Component
@Scope("view")
@RequiredArgsConstructor
@Slf4j
public class HomeView implements Serializable {

    private @Getter String title;
    private @Getter List<ExampleDto> exampleList;
    private @Getter ExampleDto example;
    private @Getter List<ExampleDto> exampleSelectedList;

    @PostConstruct
    private void init() {
        title = "Inicio";
        this.initDataExampleList();
        this.example = new ExampleDto();
        this.exampleSelectedList = new ArrayList<>();
    }

    public void addMessage() {
        FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_INFO,
                "State has been changed", "State is = " + example.getId());
        FacesContext.getCurrentInstance().addMessage(null, msg);
    }

    private void initDataExampleList() {
        ExampleDto exampleDto01 = ExampleDto.builder()
                .id(1)
                .name("Luis Fernando")
                .age(25)
                .heigt(172)
                .weight(86)
                .build();
        ExampleDto exampleDto02 = ExampleDto.builder()
                .id(2)
                .name("Paulina del Rocio")
                .age(15)
                .heigt(130)
                .weight(46)
                .build();
        ExampleDto exampleDto03 = ExampleDto.builder()
                .id(3)
                .name("Maria Guadalupe")
                .age(20)
                .heigt(165)
                .weight(68)
                .build();

        this.exampleList = List.of(exampleDto01, exampleDto02, exampleDto03);
    }

}
