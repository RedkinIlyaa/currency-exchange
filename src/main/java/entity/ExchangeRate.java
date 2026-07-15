package entity;

import lombok.*;

@ToString
@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ExchangeRate {
    @EqualsAndHashCode.Exclude
    private Integer id;
    private Integer baseCurrencyId;
    private Integer targetCurrencyId;
    private Double rate;
}
