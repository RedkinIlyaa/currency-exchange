package dto;

import lombok.*;

@ToString
@Getter
@Builder
public class CurrencyDto {
    private Integer id;
    private String name;
    private String code;
    private String sign;
}
