package gob.yucatan.sicasy.views.beans;

import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;

@Component
@Scope("application")
@RequiredArgsConstructor
@Getter
@Slf4j
public class AppBean implements Serializable {

    @Value("${spring.application.name}")
    private String appName;

    @Value("${app.version}")
    private String appVersion;

    private Integer year;

    @PostConstruct
    public void init() {
        String message = appName + " v" + appVersion + " is running...";
        log.info(message);
        year = getCurrentYear();
    }

    private int getCurrentYear() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        return calendar.get(Calendar.YEAR);
    }

}
