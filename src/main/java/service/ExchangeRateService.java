package service;

import dao.ExchangeRateDao;
import dto.CurrencyDto;
import dto.ExchangeRateDto;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ExchangeRateService {
    private static final ExchangeRateService exchangeRateService = new ExchangeRateService();
    private final ExchangeRateDao exchangeRateDao = ExchangeRateDao.getInstance();

    public List<ExchangeRateDto> getAllExchangeRates() {
        return exchangeRateDao.getExchangeRatesList().stream()
                .map(exchangeRate -> ExchangeRateDto.builder()
                        .id(exchangeRate.getId())
                        .baseCurrency(CurrencyDto.builder()
                                .id(exchangeRate.getBaseCurrency().getId())
                                .code(exchangeRate.getBaseCurrency().getCode())
                                .name(exchangeRate.getBaseCurrency().getFullName())
                                .sign(exchangeRate.getBaseCurrency().getSign())
                                .build()
                        )
                        .targetCurrency(
                                CurrencyDto.builder()
                                        .id(exchangeRate.getTargetCurrency().getId())
                                        .code(exchangeRate.getTargetCurrency().getCode())
                                        .name(exchangeRate.getTargetCurrency().getFullName())
                                        .sign(exchangeRate.getTargetCurrency().getSign())
                                        .build()
                        )
                        .rate(exchangeRate.getRate())
                        .build()
                ).toList();
    }

    public static ExchangeRateService getInstance() {
        return exchangeRateService;
    }
}
