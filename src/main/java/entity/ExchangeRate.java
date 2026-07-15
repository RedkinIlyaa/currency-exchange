package entity;

import lombok.*;

import java.math.BigDecimal;

@ToString
@Getter
@Builder
@EqualsAndHashCode
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ExchangeRate {
    @EqualsAndHashCode.Exclude
    private Integer id;

    private Integer baseCurrencyId;
    private Integer targetCurrencyId;

    @EqualsAndHashCode.Exclude
    private BigDecimal rate;
}
