package gob.yucatan.sicasy.business.dtos;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class BitacoraCambios {
    private String campo;
    private String valorAnterior;
    private String valorNuevo;
}
