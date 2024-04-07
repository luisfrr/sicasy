package gob.yucatan.sicasy.business.dtos;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class ExampleDto {
    private Integer id;
    private String name;
    private Integer age;
    private Integer heigt;
    private Integer weight;
}
