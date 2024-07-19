package gob.yucatan.sicasy;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.security.crypto.password.PasswordEncoder;

@SpringBootApplication

@RequiredArgsConstructor
@Slf4j
public class SicasyApplication implements CommandLineRunner {

    private final PasswordEncoder passwordEncoder;

    public static void main(String[] args) {
        SpringApplication.run(SicasyApplication.class, args);
    }

    @Override
    public void run(String... args) {
        String passwordEncoded = passwordEncoder.encode("sicasySaF.5");
        log.info(passwordEncoded);
    }
}
