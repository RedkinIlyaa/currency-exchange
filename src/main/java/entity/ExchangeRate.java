package entity;

import lombok.*;

import java.math.BigDecimal;

@ToString
@Getter
@Setter
@Builder
@EqualsAndHashCode
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ExchangeRate {
    @EqualsAndHashCode.Exclude
    private Integer id;

    private Currency baseCurrency;
    private Currency targetCurrency;

    @EqualsAndHashCode.Exclude
    private BigDecimal rate;
}
