package gob.yucatan.sicasy.repository.iface;

import gob.yucatan.sicasy.business.entities.EmailTemplate;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface IEmailTemplateRepository extends JpaRepository<EmailTemplate, Integer> {
    Optional<EmailTemplate> findByCodigo(String codigo);
}
