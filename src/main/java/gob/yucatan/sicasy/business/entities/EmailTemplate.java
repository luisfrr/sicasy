package gob.yucatan.sicasy.business.entities;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "email_template")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class EmailTemplate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "email_template_id")
    private Integer emailTemplateId;

    @Column(name = "codigo", unique = true, nullable = false)
    private String codigo;

    @Column(name = "template", nullable = false)
    private String template;

}
