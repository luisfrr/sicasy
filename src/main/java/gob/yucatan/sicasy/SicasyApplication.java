package gob.yucatan.sicasy;

import gob.yucatan.sicasy.business.entities.Permiso;
import gob.yucatan.sicasy.services.iface.IPermisoScannerService;
import gob.yucatan.sicasy.services.iface.IPermisoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;

@SpringBootApplication

@RequiredArgsConstructor
@Slf4j
public class SicasyApplication implements CommandLineRunner {

    private final PasswordEncoder passwordEncoder;
    private final IPermisoService permisoService;
    private final IPermisoScannerService permisoScannerService;

    public static void main(String[] args) {
        SpringApplication.run(SicasyApplication.class, args);
    }

    @Override
    public void run(String... args) {
        String passwordEncoded = passwordEncoder.encode("sicasySaF.5");
        log.info(passwordEncoded);

        try {
            List<Permiso> permisos = permisoScannerService.getPermisos("gob.yucatan.sicasy",
                    "SICASY");
            permisoService.updateAll(permisos);
            log.info("Configuración de permisos actualizado correctamente");
        } catch (Exception ex) {
            log.error("No se ha logrado actualizar la configuración de permisos", ex);
        }
    }
}
