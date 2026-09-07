package dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.math.BigDecimal;

@ToString
@Getter
@Builder
public class ExchangeRateDto {
    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    private int id;
    private CurrencyDto baseCurrency;
    private CurrencyDto targetCurrency;
    private BigDecimal rate;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private BigDecimal amount;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private BigDecimal convertedAmount;
}
