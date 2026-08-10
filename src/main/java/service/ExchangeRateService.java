package service;

import dao.ExchangeRateDao;
import dto.CurrencyDto;
import dto.ExchangeRateDto;
import entity.ExchangeRate;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ExchangeRateService {
    private static final ExchangeRateService exchangeRateService = new ExchangeRateService();
    private final ExchangeRateDao exchangeRateDao = ExchangeRateDao.getInstance();

    public List<ExchangeRateDto> getAllExchangeRates() {
        return exchangeRateDao.getExchangeRatesList().stream()
                .map(ExchangeRateService::createExchangeRateDTO
                ).toList();
    }

    public Optional<ExchangeRateDto> exchangeRateDtoByCurrenciesCodes(String firstCode, String secondCode) {
        Optional<ExchangeRate> exchangeRateByCurrencyCodes = exchangeRateDao.getExchangeRateByCurrencyCodes(firstCode.toUpperCase(Locale.ENGLISH), secondCode.toUpperCase(Locale.ENGLISH));

        return exchangeRateByCurrencyCodes.map(
                ExchangeRateService::createExchangeRateDTO
        );
    }

    public Optional<ExchangeRateDto> addNewExchangeRate(String baseCurrencyCode, String targetCurrencyCode, BigDecimal rate) {
        Optional<ExchangeRate> addedExchangeRate = exchangeRateDao.addExchangeRate(baseCurrencyCode, targetCurrencyCode, rate);
        return addedExchangeRate.map(
                ExchangeRateService::createExchangeRateDTO
        );
    }

    public Optional<ExchangeRateDto> patchToExchangeRate(String baseCurrencyCode, String targetCurrencyCode, BigDecimal rate) {
        Optional<ExchangeRate> updatedExchangeRate = exchangeRateDao.updateExchangeRate(baseCurrencyCode, targetCurrencyCode, rate);
        return updatedExchangeRate.map(
                ExchangeRateService::createExchangeRateDTO
        );
    }

    private static ExchangeRateDto createExchangeRateDTO(ExchangeRate exchangeRate) {
        return ExchangeRateDto.builder()
                .id(exchangeRate.getId())
                .baseCurrency(
                        CurrencyDto.builder()
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
                .build();
    }

    public static ExchangeRateService getInstance() {
        return exchangeRateService;
    }
}
