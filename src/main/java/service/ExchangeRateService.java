package service;

import dao.CurrencyDao;
import dao.ExchangeRateDao;
import dto.CurrencyDto;
import dto.ExchangeRateDto;
import entity.Currency;
import entity.ExchangeRate;
import exception.invalid.InvalidCurrencyCodeException;
import exception.invalid.InvalidTypeOfValueInBodyParameterException;
import exception.notfound.CurrencyNotFoundException;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ExchangeRateService {
    private static final ExchangeRateService exchangeRateService = new ExchangeRateService();
    private final ExchangeRateDao exchangeRateDao = ExchangeRateDao.getInstance();

    public List<ExchangeRateDto> getAllExchangeRates() {
        return exchangeRateDao.getExchangeRatesList().stream()
                .map(ExchangeRateService::createExchangeRateDTO)
                .toList();
    }

    public Optional<ExchangeRateDto> exchangeRateDtoByCurrenciesCodes(String firstCode, String secondCode) {
        Optional<ExchangeRate> exchangeRateByCurrencyCodes = exchangeRateDao.getExchangeRateByCurrencyCodes(firstCode.toUpperCase(Locale.ENGLISH), secondCode.toUpperCase(Locale.ENGLISH));

        return exchangeRateByCurrencyCodes.map(
                ExchangeRateService::createExchangeRateDTO
        );
    }

    public Optional<ExchangeRateDto> addNewExchangeRate(String baseCurrencyCode, String targetCurrencyCode, String rate) {
        if (doesCurrencyCodeHaveMistake(baseCurrencyCode) || doesCurrencyCodeHaveMistake(targetCurrencyCode))
            throw new InvalidCurrencyCodeException("Code parameter must be exactly 3 char and contain only a-z or A-Z letters");

        BigDecimal bigDecimalRate;
        try {
            bigDecimalRate = new BigDecimal(rate);
        } catch (NumberFormatException nfeException) {
            throw new InvalidTypeOfValueInBodyParameterException("Current rate parameter can't be written into db. It must be a digit");
        }

        if (bigDecimalRate.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidTypeOfValueInBodyParameterException("Rate parameter can't be 0 or less. Change it.");
        }

        Optional<ExchangeRate> addedExchangeRate = exchangeRateDao.addExchangeRate(baseCurrencyCode, targetCurrencyCode, bigDecimalRate);
        return addedExchangeRate.map(
                ExchangeRateService::createExchangeRateDTO
        );
    }

    private boolean doesCurrencyCodeHaveMistake(String code) {
        return !(code.matches("[a-zA-Z]+") && (code.length() == 3));
    }

    public Optional<ExchangeRateDto> patchToExchangeRate(String baseCurrencyCode, String targetCurrencyCode, BigDecimal rate) {
        Optional<ExchangeRate> updatedExchangeRate = exchangeRateDao.updateExchangeRate(baseCurrencyCode, targetCurrencyCode, rate);
        return updatedExchangeRate.map(
                ExchangeRateService::createExchangeRateDTOWithoutId
        );
    }

    public ExchangeRateDto transferFromOneCurrencyToAnother(String baseCurrencyCode, String targetCurrencyCode, BigDecimal amount) {
        CurrencyDao currencyDao = CurrencyDao.getInstance();

        // check baseCurrency in currencies table by baseCurrencyCode
        Optional<Currency> baseCurrency = currencyDao.findByCode(baseCurrencyCode);
        if (baseCurrency.isEmpty()) {
            throw new CurrencyNotFoundException("Currency " + baseCurrencyCode + " is not found");
        }

        // check targetCurrency in currencies table by targetCurrencyCode
        Optional<Currency> targetCurrency = currencyDao.findByCode(targetCurrencyCode);
        if (targetCurrency.isEmpty()) {
            throw new CurrencyNotFoundException("Currency " + targetCurrencyCode + " is not found");
        }

        // if baseCurrency and targetCurrency are exist
        List<ExchangeRate> exchangeRates = exchangeRateDao.transferFromOneCurrencyToAnother(baseCurrencyCode, targetCurrencyCode);

        // size == 1, if this Exchange Rate is already exist in db (or exist reverse course)
        if (exchangeRates.size() == 1) {
            ExchangeRate exchangeRate = exchangeRates.getFirst();

            if (!exchangeRate.getBaseCurrency().getCode().equals(baseCurrencyCode)) {
                Currency currency = exchangeRate.getBaseCurrency();
                exchangeRate.setBaseCurrency(exchangeRate.getTargetCurrency());
                exchangeRate.setTargetCurrency(currency);
                exchangeRate.setRate(BigDecimal.valueOf(1).divide(exchangeRate.getRate(), 4, RoundingMode.HALF_UP));
            }

            return createBigExchangeRate(amount, exchangeRate);

        } else if (exchangeRates.size() == 2) { // size == 2, if there is an intermediate currency
            ExchangeRate firstExchangeRate = exchangeRates.get(0);
            ExchangeRate secondExchangeRate = exchangeRates.get(1);

            if (!firstExchangeRate.getBaseCurrency().getCode().equals(baseCurrencyCode)) {
                Currency currency = firstExchangeRate.getBaseCurrency();
                firstExchangeRate.setBaseCurrency(firstExchangeRate.getTargetCurrency());
                firstExchangeRate.setTargetCurrency(currency);
                firstExchangeRate.setRate(BigDecimal.valueOf(1).divide(firstExchangeRate.getRate(), 4, RoundingMode.HALF_UP));
            }

            if (!secondExchangeRate.getTargetCurrency().getCode().equals(targetCurrencyCode)) {
                Currency currency = secondExchangeRate.getBaseCurrency();
                secondExchangeRate.setBaseCurrency(secondExchangeRate.getTargetCurrency());
                secondExchangeRate.setTargetCurrency(currency);
                secondExchangeRate.setRate(BigDecimal.valueOf(1).divide(secondExchangeRate.getRate(), 4, RoundingMode.HALF_UP));
            }

            return ExchangeRateDto
                    .builder()
                    .baseCurrency(
                    CurrencyDto.builder()
                            .id(firstExchangeRate.getBaseCurrency().getId())
                            .code(firstExchangeRate.getBaseCurrency().getCode())
                            .name(firstExchangeRate.getBaseCurrency().getFullName())
                            .sign(firstExchangeRate.getBaseCurrency().getSign())
                            .build()
                    )
                    .targetCurrency(
                    CurrencyDto.builder()
                            .id(secondExchangeRate.getTargetCurrency().getId())
                            .code(secondExchangeRate.getTargetCurrency().getCode())
                            .name(secondExchangeRate.getTargetCurrency().getFullName())
                            .sign(secondExchangeRate.getTargetCurrency().getSign())
                            .build()
                    )
                    .rate(firstExchangeRate.getRate().multiply(secondExchangeRate.getRate()))
                    .amount(amount)
                    .convertedAmount(firstExchangeRate.getRate().multiply(secondExchangeRate.getRate()).multiply(amount).setScale(4, RoundingMode.HALF_UP))
                    .build();
        }

        throw new RuntimeException("transferFromOneCurrencyToAnother(..., ...) return 3 or more ExchangeRates - it's a mistake");
    }

    private static ExchangeRateDto createExchangeRateDTOWithoutId(ExchangeRate exchangeRate) {
        return ExchangeRateDto.builder()
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

    private static ExchangeRateDto createBigExchangeRate(BigDecimal amount, ExchangeRate exchangeRate) {
        return ExchangeRateDto.builder()
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
                .amount(amount)
                .convertedAmount(amount.multiply(exchangeRate.getRate()).setScale(4, RoundingMode.HALF_UP))
                .build();
    }

    public static ExchangeRateService getInstance() {
        return exchangeRateService;
    }
}
