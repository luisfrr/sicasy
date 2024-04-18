package gob.yucatan.sicasy.views;

import gob.yucatan.sicasy.business.dtos.EmailTemplateMessage;
import gob.yucatan.sicasy.business.dtos.ExampleDto;
import gob.yucatan.sicasy.business.enums.EmailTemplateEnum;
import gob.yucatan.sicasy.services.iface.IEmailService;
import gob.yucatan.sicasy.utils.imports.excel.ConfigHeaderExcelModel;
import gob.yucatan.sicasy.utils.imports.excel.ImportExcelFile;
import gob.yucatan.sicasy.views.beans.Messages;
import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.model.file.UploadedFile;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@Scope("view")
@RequiredArgsConstructor
@Slf4j
public class HomeView implements Serializable {

    private final IEmailService emailService;

    private @Getter String title;
    private @Getter List<ExampleDto> exampleList;
    private @Getter ExampleDto example;
    private @Getter List<ExampleDto> exampleSelectedList;

    private @Getter String dropZoneText;

    @PostConstruct
    private void init() {
        title = "Inicio";
        this.initDataExampleList();
        this.example = new ExampleDto();
        this.exampleSelectedList = new ArrayList<>();

        this.dropZoneText = "Arrastra y suelta aquí el archivo de importación";
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

    public void handleFileUpload(FileUploadEvent event) throws IOException {
        UploadedFile file = event.getFile();
        String fileName = file.getFileName();
        byte[] fileContent = file.getContent();

        // Aquí puedes procesar el archivo según su tipo o contenido
        if (fileName.endsWith(".xls") || fileName.endsWith(".xlsx")) {
            // Si es un archivo Excel, procesarlo utilizando Apache POI
            Class<ExampleDto> exampleDtoClass = ExampleDto.class;
            List<ConfigHeaderExcelModel> list = new ArrayList<>();
            list.add(ConfigHeaderExcelModel.builder().header("NOMBRE").fieldName("name").columnIndex(0).build());
            list.add(ConfigHeaderExcelModel.builder().header("ALTURA").fieldName("heigt").columnIndex(1).build());
            list.add(ConfigHeaderExcelModel.builder().header("PESO").fieldName("weight").columnIndex(2).build());
            list.add(ConfigHeaderExcelModel.builder().header("EDAD").fieldName("age").columnIndex(3).build());
            list.add(ConfigHeaderExcelModel.builder().header("FECHA NACIMIENTO").fieldName("dateOfBirth").columnIndex(4).build());

            ImportExcelFile<ExampleDto> importExcelFile = new ImportExcelFile<>();
            List<ExampleDto> data = importExcelFile.processExcelFile(fileContent, exampleDtoClass, list);

            log.info("Se ha importado correctamente la información. data size: {}", data.size());
        } else {
            // Manejar otros tipos de archivos si es necesario
            // Por ejemplo, mostrar un mensaje de error
            Messages.addError("Error", "Tipo de archivo no válido");
        }
    }

    public void enviarCorreoPrueba() {
        Map<String, String> dataTemplate = new HashMap<>();
        dataTemplate.put("#EMAIL_TITLE#", "Bienvenido");
        dataTemplate.put("#EMAIL_DESCRIPTION#", "Te damos la más cordial bienvenida al Sistema Integral de Control de Arrendamiento y Seguros del Estado de Yucatán. Para continuar, te invitamos a activar tu cuenta.");
        dataTemplate.put("#ACTION_TEXT#", "Activar cuenta");
        dataTemplate.put("#ACTION_URL#", "https://sicasy.yucatan.gob.mx/");

        emailService.sendMail(EmailTemplateMessage.builder()
                .emailTemplate(EmailTemplateEnum.SIMPLE_TEMPLATE)
                        .to("luisfrr27@gmail.com")
                        .subject("Bienvenido a SICASY")
                        .dataTemplate(dataTemplate)
                .build());

//        emailService.sendEmail("luisfrr27@gmail.com", "Bienvenido", "<a href=\"http://localhost:8080/sicasy\">Activar cuenta</a>");
    }

}
