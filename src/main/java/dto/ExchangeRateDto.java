package dto;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.math.BigDecimal;

@ToString
@Getter
@Builder
public class ExchangeRateDto {
    private int id;
    private int base_currency_id;
    private int target_currency_id;
    private BigDecimal rate;
}
