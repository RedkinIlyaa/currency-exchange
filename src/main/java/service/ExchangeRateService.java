package service;

import dao.ExchangeRateDao;
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
                        .id(exchangeRate.getBaseCurrencyId())
                        .base_currency_id(exchangeRate.getBaseCurrencyId())
                        .target_currency_id(exchangeRate.getTargetCurrencyId())
                        .rate(exchangeRate.getRate())
                        .build()
                ).toList();
    }

    public static ExchangeRateService getInstance() {
        return exchangeRateService;
    }
}
