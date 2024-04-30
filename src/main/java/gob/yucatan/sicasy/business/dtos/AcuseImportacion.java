package gob.yucatan.sicasy.business.dtos;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class AcuseImportacion {
    String titulo;
    String mensaje;
    Integer error;
}
