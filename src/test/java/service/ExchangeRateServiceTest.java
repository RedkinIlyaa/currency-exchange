package service;

import dao.CurrencyDao;
import dao.ExchangeRateDao;
import dto.CurrencyDto;
import dto.ExchangeRateDto;
import entity.Currency;
import entity.ExchangeRate;
import exception.ExchangeRateDaoException;
import exception.exist.ExchangeRateAlreadyExistsException;
import exception.invalid.InvalidCurrencyCodeException;
import exception.invalid.InvalidException;
import exception.notfound.CurrencyNotFoundException;
import exception.notfound.ExchangeRateNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mockito;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.SQLException;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

public class ExchangeRateServiceTest {

    @Test
    void getAllExchangeRatesWhenNoOneExchangeRateExists() {
        ExchangeRateDao exchangeRateDaoMock = Mockito.mock(ExchangeRateDao.class);
        CurrencyDao currencyDaoMock = Mockito.mock(CurrencyDao.class);
        ExchangeRateService exchangeRateService = new ExchangeRateService(exchangeRateDaoMock, currencyDaoMock);

        Mockito.when(exchangeRateDaoMock.getExchangeRatesList()).thenReturn(List.of());
        assertThat(exchangeRateService.getAllExchangeRates()).isEqualTo(List.of()).hasSize(0);
    }

    @Test
    void getAllExchangeRatesWhenDaoThrowException() {
        ExchangeRateDao exchangeRateDaoMock = Mockito.mock(ExchangeRateDao.class);
        CurrencyDao currencyDaoMock = Mockito.mock(CurrencyDao.class);
        ExchangeRateService exchangeRateService = new ExchangeRateService(exchangeRateDaoMock, currencyDaoMock);
        SQLException sqlException = new SQLException();

        Mockito.when(exchangeRateDaoMock.getExchangeRatesList()).thenThrow(new ExchangeRateDaoException("Failed to get List<ExchangeRate> from db", sqlException));
        assertThatThrownBy(exchangeRateService::getAllExchangeRates).isInstanceOf(ExchangeRateDaoException.class).hasMessage("Failed to get List<ExchangeRate> from db").hasCause(sqlException);
    }

    @Test
    void getAllExchangeRates() {

        Currency baseCurrency1 = Currency.builder().id(3).code("EUR").fullName("Euro").sign("€").build();

        CurrencyDto baseCurrencyDto1 = CurrencyDto.builder().id(3).code("EUR").name("Euro").sign("€").build();

        Currency targetCurrency1 = Currency.builder().id(4).code("RUB").fullName("Russian ruble").sign("₽").build();

        CurrencyDto targetCurrencyDto1 = CurrencyDto.builder().id(4).code("RUB").name("Russian ruble").sign("₽").build();

        ExchangeRate exchangeRate1 = ExchangeRate.builder().id(2).baseCurrency(baseCurrency1).targetCurrency(targetCurrency1).rate(new BigDecimal("94.000000")).build();

        ExchangeRateDto exchangeRateDto1 = ExchangeRateDto.builder().id(2).baseCurrency(baseCurrencyDto1).targetCurrency(targetCurrencyDto1).rate(new BigDecimal("94.000000")).build();

        // new one

        Currency baseCurrency2 = Currency.builder().id(2).code("USD").fullName("United States dollar").sign("$").build();

        CurrencyDto baseCurrencyDto2 = CurrencyDto.builder().id(2).code("USD").name("United States dollar").sign("$").build();

        Currency targetCurrency2 = Currency.builder().id(3).code("EUR").fullName("Euro").sign("€").build();

        CurrencyDto targetCurrencyDto2 = CurrencyDto.builder().id(3).code("EUR").name("Euro").sign("€").build();

        ExchangeRate exchangeRate2 = ExchangeRate.builder().id(4).baseCurrency(baseCurrency2).targetCurrency(targetCurrency2).rate(new BigDecimal("0.850000")).build();

        ExchangeRateDto exchangeRateDto2 = ExchangeRateDto.builder().id(4).baseCurrency(baseCurrencyDto2).targetCurrency(targetCurrencyDto2).rate(new BigDecimal("0.850000")).build();

        List<ExchangeRate> exchangeRateList = List.of(exchangeRate1, exchangeRate2);
        List<ExchangeRateDto> exchangeRateDtoList = List.of(exchangeRateDto1, exchangeRateDto2);

        ExchangeRateDao exchangeRateDaoMock = Mockito.mock(ExchangeRateDao.class);
        CurrencyDao currencyDaoMock = Mockito.mock(CurrencyDao.class);
        ExchangeRateService exchangeRateService = new ExchangeRateService(exchangeRateDaoMock, currencyDaoMock);

        Mockito.when(exchangeRateDaoMock.getExchangeRatesList()).thenReturn(exchangeRateList);
        assertThat(exchangeRateService.getAllExchangeRates()).hasSize(2).isEqualTo(exchangeRateDtoList);
    }

    @ParameterizedTest
    @CsvSource(value = {"RUB, KEK", "USD, LOL", "ALO, IZI", "RTU, KEM"})
    void exchangeRateDtoByCurrenciesCodesWhenThereIsNotSuchExchangeRate(String firstCode, String secondCode) {
        ExchangeRateDao exchangeRateDaoMock = Mockito.mock(ExchangeRateDao.class);
        CurrencyDao currencyDaoMock = Mockito.mock(CurrencyDao.class);
        ExchangeRateService exchangeRateService = new ExchangeRateService(exchangeRateDaoMock, currencyDaoMock);

        Mockito.when(exchangeRateDaoMock.getExchangeRateByCurrencyCodes(firstCode.toUpperCase(Locale.ENGLISH), secondCode.toUpperCase(Locale.ENGLISH))).thenReturn(Optional.empty());
        assertThat(exchangeRateService.exchangeRateDtoByCurrenciesCodes(firstCode.toUpperCase(Locale.ENGLISH), secondCode.toUpperCase(Locale.ENGLISH))).isEqualTo(Optional.empty());
    }

    @ParameterizedTest
    @CsvSource(value = {"EUR, RUB"})
    void exchangeRateDtoByCurrenciesCodes(String firstCode, String secondCode) {
        Currency baseCurrency1 = Currency.builder().id(3).code("EUR").fullName("Euro").sign("€").build();

        CurrencyDto baseCurrencyDto1 = CurrencyDto.builder().id(3).code("EUR").name("Euro").sign("€").build();

        Currency targetCurrency1 = Currency.builder().id(4).code("RUB").fullName("Russian ruble").sign("₽").build();

        CurrencyDto targetCurrencyDto1 = CurrencyDto.builder().id(4).code("RUB").name("Russian ruble").sign("₽").build();

        ExchangeRate exchangeRate1 = ExchangeRate.builder().id(2).baseCurrency(baseCurrency1).targetCurrency(targetCurrency1).rate(new BigDecimal("94.000000")).build();

        ExchangeRateDto exchangeRateDto1 = ExchangeRateDto.builder().id(2).baseCurrency(baseCurrencyDto1).targetCurrency(targetCurrencyDto1).rate(new BigDecimal("94.000000")).build();


        ExchangeRateDao exchangeRateDaoMock = Mockito.mock(ExchangeRateDao.class);
        CurrencyDao currencyDaoMock = Mockito.mock(CurrencyDao.class);
        ExchangeRateService exchangeRateService = new ExchangeRateService(exchangeRateDaoMock, currencyDaoMock);

        Mockito.when(exchangeRateDaoMock.getExchangeRateByCurrencyCodes(firstCode.toUpperCase(Locale.ENGLISH), secondCode.toUpperCase(Locale.ENGLISH))).thenReturn(Optional.ofNullable(exchangeRate1));

        assertThat(exchangeRateService.exchangeRateDtoByCurrenciesCodes(firstCode, secondCode)).isEqualTo(Optional.ofNullable(exchangeRateDto1));
    }

    @ParameterizedTest
    @CsvSource(value = {"KE1, RUB, 15.123", "usdd, TRY, 666.11", "NZD, a%ga, 123.4567", "EUR, YES0, 121.14"})
    void addNewExchangeRateWhenBaseORTargetCurrencyCodeHaveMistake(String baseCurrencyCode, String targetCurrencyCode, String rate) {
        ExchangeRateDao exchangeRateDaoMock = Mockito.mock(ExchangeRateDao.class);
        CurrencyDao currencyDaoMock = Mockito.mock(CurrencyDao.class);
        ExchangeRateService exchangeRateService = new ExchangeRateService(exchangeRateDaoMock, currencyDaoMock);

        assertThatThrownBy(() -> exchangeRateService.addNewExchangeRate(baseCurrencyCode, targetCurrencyCode, rate)).hasMessage("Code parameter must be exactly 3 char and contain only a-z or A-Z letters").hasNoCause().isInstanceOf(InvalidCurrencyCodeException.class);
        Mockito.verifyNoInteractions(exchangeRateDaoMock);
    }

    @ParameterizedTest
    @CsvSource(value = {"USD, USD, 1.00", "usd, USD, 1.251", "usd, uSd, 2.15", "usd, usd, 67.1236", "eur, eur, 129.1242"})
    void tryToAddNewExchangeRateWhenBaseCurrencyCodeIsTheSameAsTargetCurrencyCode(String baseCurrencyCode, String targetCurrencyCode, String rate) {
        ExchangeRateDao exchangeRateDaoMock = Mockito.mock(ExchangeRateDao.class);
        CurrencyDao currencyDaoMock = Mockito.mock(CurrencyDao.class);
        ExchangeRateService exchangeRateService = new ExchangeRateService(exchangeRateDaoMock, currencyDaoMock);

        assertThatThrownBy(() -> exchangeRateService.addNewExchangeRate(baseCurrencyCode, targetCurrencyCode, rate)).hasMessage("Base and target currencies must be different").hasNoCause().isInstanceOf(InvalidCurrencyCodeException.class);
        Mockito.verifyNoInteractions(exchangeRateDaoMock);
    }

    @Test
    void tryToAddNewExchangeRateWhenReverseExchangeRateExist() {
        Currency baseCurrency1 = Currency.builder().id(3).code("EUR").fullName("Euro").sign("€").build();
        String baseCurrencyCode = baseCurrency1.getCode();

        Currency targetCurrency1 = Currency.builder().id(4).code("RUB").fullName("Russian ruble").sign("₽").build();
        String targetCurrencyCode = targetCurrency1.getCode();

        ExchangeRate exchangeRate1 = ExchangeRate.builder().id(2).baseCurrency(baseCurrency1).targetCurrency(targetCurrency1).rate(new BigDecimal("94.000000")).build();

        String rate = exchangeRate1.getRate().toPlainString();

        ExchangeRateDao exchangeRateDaoMock = Mockito.mock(ExchangeRateDao.class);
        CurrencyDao currencyDaoMock = Mockito.mock(CurrencyDao.class);
        ExchangeRateService exchangeRateService = new ExchangeRateService(exchangeRateDaoMock, currencyDaoMock);

        Mockito.when(exchangeRateDaoMock.getExchangeRateByCurrencyCodes(targetCurrency1.getCode().toUpperCase(Locale.ENGLISH), baseCurrency1.getCode().toUpperCase(Locale.ENGLISH))).thenReturn(Optional.of(exchangeRate1));

        assertThatThrownBy(() -> exchangeRateService.addNewExchangeRate(baseCurrencyCode.toUpperCase(Locale.ENGLISH), targetCurrencyCode.toUpperCase(Locale.ENGLISH), rate)).isInstanceOf(ExchangeRateAlreadyExistsException.class).hasMessage("There is an exchange rate: " + targetCurrencyCode + " - " + baseCurrencyCode + ". You can't create reverse exchange rate").hasNoCause();
    }

    @ParameterizedTest
    @CsvSource(value = "eur, rub, 94.000000")
    void addNewExchangeRate(String baseCurrencyCode, String targetCurrencyCode, String rate) {
        Currency baseCurrency1 = Currency.builder().id(3).code("EUR").fullName("Euro").sign("€").build();

        CurrencyDto baseCurrencyDto1 = CurrencyDto.builder().id(3).code("EUR").name("Euro").sign("€").build();

        Currency targetCurrency1 = Currency.builder().id(4).code("RUB").fullName("Russian ruble").sign("₽").build();

        CurrencyDto targetCurrencyDto1 = CurrencyDto.builder().id(4).code("RUB").name("Russian ruble").sign("₽").build();

        ExchangeRate exchangeRate1 = ExchangeRate.builder().id(2).baseCurrency(baseCurrency1).targetCurrency(targetCurrency1).rate(new BigDecimal("94.000000")).build();

        ExchangeRateDto exchangeRateDto1 = ExchangeRateDto.builder().id(2).baseCurrency(baseCurrencyDto1).targetCurrency(targetCurrencyDto1).rate(new BigDecimal("94.000000")).build();


        ExchangeRateDao exchangeRateDaoMock = Mockito.mock(ExchangeRateDao.class);
        CurrencyDao currencyDaoMock = Mockito.mock(CurrencyDao.class);
        ExchangeRateService exchangeRateService = new ExchangeRateService(exchangeRateDaoMock, currencyDaoMock);

        Mockito.when(exchangeRateDaoMock.addExchangeRate(baseCurrencyCode.toUpperCase(Locale.ENGLISH), targetCurrencyCode.toUpperCase(Locale.ENGLISH), new BigDecimal(rate))).thenReturn(Optional.of(exchangeRate1));
        assertThat(exchangeRateService.addNewExchangeRate(baseCurrencyCode, targetCurrencyCode, rate)).isEqualTo(Optional.of(exchangeRateDto1));
    }


    @ParameterizedTest
    @CsvSource(value = {
            "EUR, RUB, 141.kek",
            "EUR, RUB, 123456.1234567",
            "EUR, RUB, 1234567.1234567",
            "EUR, RUB, 1234567.1234567",
            "EUR, RUB, 12e.1234"
    })
    void patchToExchangeRateWhenRateHasMistake(String baseCurrencyCode, String targetCurrencyCode, String rate) {

        ExchangeRateDao exchangeRateDaoMock = Mockito.mock(ExchangeRateDao.class);
        CurrencyDao currencyDaoMock = Mockito.mock(CurrencyDao.class);
        ExchangeRateService exchangeRateService = new ExchangeRateService(exchangeRateDaoMock, currencyDaoMock);
        assertThatThrownBy(() -> exchangeRateService.patchToExchangeRate(baseCurrencyCode, targetCurrencyCode, rate)).isInstanceOf(InvalidException.class).hasNoCause().hasMessage("Rate must contain from 1 to 6 integer digits and up to 6 fractional digits, without exponent");
        Mockito.verifyNoMoreInteractions(exchangeRateDaoMock);
    }

    @ParameterizedTest
    @CsvSource(value = {
            "RUS, EUU, 15.111",
            "RUS, KEK, 666.121",
            "RUS, USS, 1.1111"
    })
    void patchToExchangeRateWhenExchangeRateNotExist(String baseCurrencyCode, String targetCurrencyCode, String rate) {
        ExchangeRateDao exchangeRateDaoMock = Mockito.mock(ExchangeRateDao.class);
        CurrencyDao currencyDaoMock = Mockito.mock(CurrencyDao.class);
        ExchangeRateService exchangeRateService = new ExchangeRateService(exchangeRateDaoMock, currencyDaoMock);

        Mockito.when(exchangeRateDaoMock.updateExchangeRate(baseCurrencyCode, targetCurrencyCode, new BigDecimal(rate)))
                .thenThrow(new ExchangeRateNotFoundException("Exchange rate not found: " + baseCurrencyCode + "-" + targetCurrencyCode));
        assertThatThrownBy(() -> exchangeRateService.patchToExchangeRate(baseCurrencyCode, targetCurrencyCode, rate))
                .isInstanceOf(ExchangeRateNotFoundException.class)
                .hasNoCause()
                .hasMessage("Exchange rate not found: " + baseCurrencyCode + "-" + targetCurrencyCode);
    }

    @Test
    void patchToExchangeRate() {
        Currency baseCurrency1 = Currency.builder().id(3).code("EUR").fullName("Euro").sign("€").build();
        CurrencyDto baseCurrencyDto1 = CurrencyDto.builder().id(3).code("EUR").name("Euro").sign("€").build();

        Currency targetCurrency1 = Currency.builder().id(4).code("RUB").fullName("Russian ruble").sign("₽").build();
        CurrencyDto targetCurrencyDto1 = CurrencyDto.builder().id(4).code("RUB").name("Russian ruble").sign("₽").build();

        ExchangeRate exchangeRate1 = ExchangeRate.builder().id(2).baseCurrency(baseCurrency1).targetCurrency(targetCurrency1).rate(new BigDecimal("100.000000")).build();
        ExchangeRateDto exchangeRateDto1 = ExchangeRateDto.builder().id(2).baseCurrency(baseCurrencyDto1).targetCurrency(targetCurrencyDto1).rate(new BigDecimal("100.000000")).build();

        String baseCurrencyCode = baseCurrency1.getCode();
        String targetCurrencyCode = targetCurrency1.getCode();
        String rate = exchangeRate1.getRate().toPlainString();

        ExchangeRateDao exchangeRateDaoMock = Mockito.mock(ExchangeRateDao.class);
        CurrencyDao currencyDaoMock = Mockito.mock(CurrencyDao.class);
        ExchangeRateService exchangeRateService = new ExchangeRateService(exchangeRateDaoMock, currencyDaoMock);

        Mockito.when(exchangeRateDaoMock.updateExchangeRate(baseCurrencyCode, targetCurrencyCode, new BigDecimal(rate)))
                .thenReturn(Optional.of(exchangeRate1));

        assertThat(exchangeRateService.patchToExchangeRate(baseCurrencyCode, targetCurrencyCode, rate))
                .isEqualTo(Optional.of(exchangeRateDto1));

        Mockito.verifyNoInteractions( currencyDaoMock);
    }

    @ParameterizedTest
    @CsvSource(value = {
            "KE1, RUB, 15.123",
            "usdd, TRY, 666.11",
            "E.R, USD, 123.4567",
            "/al, EUR, 121.14",
            "RRUB, EUR, 12.14"
    })
    void transferFromOneCurrencyToAnotherWhenBaseCurrencyCodeHaveMistake(String baseCurrencyCode, String targetCurrencyCode, String stringAmount) {
        ExchangeRateDao exchangeRateDaoMock = Mockito.mock(ExchangeRateDao.class);
        CurrencyDao currencyDaoMock = Mockito.mock(CurrencyDao.class);
        ExchangeRateService exchangeRateService = new ExchangeRateService(exchangeRateDaoMock, currencyDaoMock);

        assertThatThrownBy(() -> exchangeRateService.transferFromOneCurrencyToAnother(baseCurrencyCode, targetCurrencyCode, stringAmount))
                .hasMessage("Base currency code parameter must be exactly 3 char and contain only a-z or A-Z letters")
                .hasNoCause()
                .isInstanceOf(InvalidCurrencyCodeException.class);
        Mockito.verifyNoInteractions(exchangeRateDaoMock, currencyDaoMock);
    }

    @ParameterizedTest
    @CsvSource(value = {
            "RUB, KE1, 15.123",
            "TRY, usdd, 666.11",
            "USD, E.R, 123.4567",
            "EUR, /al, 121.14",
            "EUR, RRUB, 12.14"
    })
    void transferFromOneCurrencyToAnotherWhenTargetCurrencyCodeHaveMistake(String baseCurrencyCode, String targetCurrencyCode, String stringAmount) {
        ExchangeRateDao exchangeRateDaoMock = Mockito.mock(ExchangeRateDao.class);
        CurrencyDao currencyDaoMock = Mockito.mock(CurrencyDao.class);
        ExchangeRateService exchangeRateService = new ExchangeRateService(exchangeRateDaoMock, currencyDaoMock);

        assertThatThrownBy(() -> exchangeRateService.transferFromOneCurrencyToAnother(baseCurrencyCode, targetCurrencyCode, stringAmount))
                .hasMessage("Target currency code parameter must be exactly 3 char and contain only a-z or A-Z letters")
                .hasNoCause()
                .isInstanceOf(InvalidCurrencyCodeException.class);
        Mockito.verifyNoInteractions(exchangeRateDaoMock, currencyDaoMock);
    }

    @ParameterizedTest
    @CsvSource(value = {
            "USD, USD, 1.00",
            "usd, USD, 1.251",
            "usd, uSd, 2.15",
            "usd, usd, 67.1236",
            "eur, eur, 129.1242"
    })
    void transferFromOneCurrencyToAnotherWhenBaseCurrencyCodeIsTheSameAsTargetCurrencyCode(String baseCurrencyCode, String targetCurrencyCode, String stringAmount) {
        ExchangeRateDao exchangeRateDaoMock = Mockito.mock(ExchangeRateDao.class);
        CurrencyDao currencyDaoMock = Mockito.mock(CurrencyDao.class);
        ExchangeRateService exchangeRateService = new ExchangeRateService(exchangeRateDaoMock, currencyDaoMock);

        assertThatThrownBy(() -> exchangeRateService.transferFromOneCurrencyToAnother(baseCurrencyCode, targetCurrencyCode, stringAmount))
                .hasMessage("Base and target currencies must be different")
                .hasNoCause()
                .isInstanceOf(InvalidCurrencyCodeException.class);
        Mockito.verifyNoInteractions(exchangeRateDaoMock, currencyDaoMock);
    }

    @ParameterizedTest
    @CsvSource(value = {
            "EUR, RUB, 1234567890123456789.123",
            "EUR, RUB, 12345678901234567890.1234",
            "EUR, RUB, 123.1234567",
            "EUR, RUB, 12345.123a",
            "EUR, RUB, 12e.1234"
    })
    void transferFromOneCurrencyToAnotherWhenAmountHasMistake(String baseCurrencyCode, String targetCurrencyCode, String stringAmount) {
        ExchangeRateDao exchangeRateDaoMock = Mockito.mock(ExchangeRateDao.class);
        CurrencyDao currencyDaoMock = Mockito.mock(CurrencyDao.class);
        ExchangeRateService exchangeRateService = new ExchangeRateService(exchangeRateDaoMock, currencyDaoMock);

        assertThatThrownBy(() -> exchangeRateService.transferFromOneCurrencyToAnother(baseCurrencyCode, targetCurrencyCode, stringAmount))
                .isInstanceOf(InvalidException.class)
                .hasNoCause()
                .hasMessage("Amount must contain from 1 to 18 integer digits and up to 6 fractional digits, without exponent");
        Mockito.verifyNoMoreInteractions(exchangeRateDaoMock, currencyDaoMock);
    }

    @ParameterizedTest
    @CsvSource(value = {
            "kek, rub, 12345.124",
            "alo, usd, 123.123456",
            "kak, tur, 654.321"
    })
    void transferFromOneCurrencyToAnotherWhenBaseCurrencyDoesNotExist(String baseCurrencyCode, String targetCurrencyCode, String stringAmount) {
        ExchangeRateDao exchangeRateDaoMock = Mockito.mock(ExchangeRateDao.class);
        CurrencyDao currencyDaoMock = Mockito.mock(CurrencyDao.class);
        ExchangeRateService exchangeRateService = new ExchangeRateService(exchangeRateDaoMock, currencyDaoMock);
        String baseCurrencyCodeUpperCase = baseCurrencyCode.toUpperCase(Locale.ENGLISH);

        Mockito.when(currencyDaoMock.findByCode(baseCurrencyCodeUpperCase))
                        .thenReturn(Optional.empty());
        assertThatThrownBy(() -> exchangeRateService.transferFromOneCurrencyToAnother(baseCurrencyCode, targetCurrencyCode, stringAmount))
                .isInstanceOf(CurrencyNotFoundException.class)
                .hasMessage("Currency " + baseCurrencyCodeUpperCase + " is not found")
                .hasNoCause();
    }

    @ParameterizedTest
    @CsvSource(value = {
            "rub, kek, 12345.124",
            "usd, alo, 123.123456",
            "tur, kak, 654.321"
    })
    void transferFromOneCurrencyToAnotherWhenTargetCurrencyDoesNotExist(String baseCurrencyCode, String targetCurrencyCode, String stringAmount) {
        String baseCurrencyCodeUpperCase = baseCurrencyCode.toUpperCase(Locale.ENGLISH);
        String targetCurrencyCodeUpperCase = targetCurrencyCode.toUpperCase(Locale.ENGLISH);

        Currency baseCurrency = Currency.builder()
                .id(12121)
                .code(baseCurrencyCodeUpperCase)
                .fullName("Fake currency " + baseCurrencyCodeUpperCase)
                .sign("₽₽₽")
                .build();

        ExchangeRateDao exchangeRateDaoMock = Mockito.mock(ExchangeRateDao.class);
        CurrencyDao currencyDaoMock = Mockito.mock(CurrencyDao.class);
        ExchangeRateService exchangeRateService = new ExchangeRateService(exchangeRateDaoMock, currencyDaoMock);

        Mockito.when(currencyDaoMock.findByCode(baseCurrencyCodeUpperCase))
                .thenReturn(Optional.of(baseCurrency));

        Mockito.when(currencyDaoMock.findByCode(targetCurrencyCodeUpperCase))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> exchangeRateService.transferFromOneCurrencyToAnother(baseCurrencyCode, targetCurrencyCode, stringAmount))
                .isInstanceOf(CurrencyNotFoundException.class)
                .hasMessage("Currency " + targetCurrencyCodeUpperCase + " is not found")
                .hasNoCause();
    }

    @Test
    void transferFromEurToRubWhenExchangeRateIsAlreadyExistEurToRub() {
        String amountString = "100";
        BigDecimal amountBigDecimal = new BigDecimal(amountString);

        Currency baseCurrency1 = Currency.builder().id(3).code("EUR").fullName("Euro").sign("€").build();
        CurrencyDto baseCurrencyDto1 = CurrencyDto.builder().id(3).code("EUR").name("Euro").sign("€").build();

        Currency targetCurrency1 = Currency.builder().id(4).code("RUB").fullName("Russian ruble").sign("₽").build();
        CurrencyDto targetCurrencyDto1 = CurrencyDto.builder().id(4).code("RUB").name("Russian ruble").sign("₽").build();

        String baseCurrencyCodeUpperCase = baseCurrency1.getCode();
        String targetCurrencyCodeUpperCase = targetCurrency1.getCode();

        String rateString =  "94.000000";
        BigDecimal rateBigDecimal = new BigDecimal(rateString);

        ExchangeRate exchangeRate1 = ExchangeRate.builder()
                .id(2)
                .baseCurrency(baseCurrency1)
                .targetCurrency(targetCurrency1)
                .rate(rateBigDecimal)
                .build();

        ExchangeRateDto bigExchangeRateDto1 = ExchangeRateDto.builder()
                .baseCurrency(baseCurrencyDto1)
                .targetCurrency(targetCurrencyDto1)
                .rate(new BigDecimal("94.000000"))
                .amount(amountBigDecimal)
                .convertedAmount(amountBigDecimal.multiply(rateBigDecimal).setScale(2, RoundingMode.HALF_UP))
                .build();

        ExchangeRateDao exchangeRateDaoMock = Mockito.mock(ExchangeRateDao.class);
        CurrencyDao currencyDaoMock = Mockito.mock(CurrencyDao.class);
        ExchangeRateService exchangeRateService = new ExchangeRateService(exchangeRateDaoMock, currencyDaoMock);

        Mockito.when(currencyDaoMock.findByCode(baseCurrencyCodeUpperCase))
                .thenReturn(Optional.of(baseCurrency1));

        Mockito.when(currencyDaoMock.findByCode(targetCurrencyCodeUpperCase))
                .thenReturn(Optional.of(targetCurrency1));

        Mockito.when(exchangeRateDaoMock.transferFromOneCurrencyToAnother(baseCurrencyCodeUpperCase, targetCurrencyCodeUpperCase))
                .thenReturn(List.of(exchangeRate1));

        assertThat(exchangeRateService.transferFromOneCurrencyToAnother(baseCurrency1.getCode(), targetCurrency1.getCode(), amountString))
                .isEqualTo(bigExchangeRateDto1);
    }

    @Test
    void transferFromRubToEurWhenReverseExchangeRateIsAlreadyExistEurToRub() {
        String amountString = "100";
        BigDecimal amountBigDecimal = new BigDecimal(amountString);

        Currency baseCurrency1 = Currency.builder()
                .id(4).code("RUB").fullName("Russian ruble").sign("₽").build();
        CurrencyDto baseCurrencyDto1 = CurrencyDto.builder()
                .id(4).code("RUB").name("Russian ruble").sign("₽").build();

        Currency targetCurrency1 = Currency.builder()
                .id(3).code("EUR").fullName("Euro").sign("€").build();
        CurrencyDto targetCurrencyDto1 = CurrencyDto.builder()
                .id(3).code("EUR").name("Euro").sign("€").build();

        String baseCurrencyCodeUpperCase = baseCurrency1.getCode(); // RUB
        String targetCurrencyCodeUpperCase = targetCurrency1.getCode(); // EUR

        ExchangeRate exchangeRateFromEurToRub = ExchangeRate.builder()
                .id(2)
                .baseCurrency(targetCurrency1) // EUR
                .targetCurrency(baseCurrency1) // RUB
                .rate(new BigDecimal("94.000000"))
                .build();

        ExchangeRateDto expected = ExchangeRateDto.builder()
                .baseCurrency(baseCurrencyDto1)
                .targetCurrency(targetCurrencyDto1)
                .rate(new BigDecimal("0.01063829787234042553"))
                .amount(amountBigDecimal)
                .convertedAmount(new BigDecimal("1.06"))
                .build();

        ExchangeRateDao exchangeRateDaoMock = Mockito.mock(ExchangeRateDao.class);
        CurrencyDao currencyDaoMock = Mockito.mock(CurrencyDao.class);
        ExchangeRateService exchangeRateService =
                new ExchangeRateService(exchangeRateDaoMock, currencyDaoMock);

        Mockito.when(currencyDaoMock.findByCode(baseCurrencyCodeUpperCase))
                .thenReturn(Optional.of(baseCurrency1));

        Mockito.when(currencyDaoMock.findByCode(targetCurrencyCodeUpperCase))
                .thenReturn(Optional.of(targetCurrency1));

        Mockito.when(exchangeRateDaoMock.transferFromOneCurrencyToAnother(
                        baseCurrencyCodeUpperCase, targetCurrencyCodeUpperCase))
                .thenReturn(List.of(exchangeRateFromEurToRub));

        assertThat(exchangeRateService.transferFromOneCurrencyToAnother(
                baseCurrencyCodeUpperCase, targetCurrencyCodeUpperCase, amountString))
                .isEqualTo(expected);
    }

    @ParameterizedTest
    @CsvSource({
            "1.0024, 94.23",
            "1.0025, 94.24",
            "1.0026, 94.24"
    })
    void transferFromEurToRubNormalizesCodesAndRoundsConvertedAmount(
            String amount,
            String expectedConvertedAmount
    ) {
        Currency eur = Currency.builder()
                .id(3).code("EUR").fullName("Euro").sign("€").build();

        Currency rub = Currency.builder()
                .id(4).code("RUB").fullName("Russian ruble").sign("₽").build();

        ExchangeRate exchangeRate = ExchangeRate.builder()
                .baseCurrency(eur)
                .targetCurrency(rub)
                .rate(new BigDecimal("94.000000"))
                .build();

        ExchangeRateDao exchangeRateDaoMock = Mockito.mock(ExchangeRateDao.class);
        CurrencyDao currencyDaoMock = Mockito.mock(CurrencyDao.class);
        ExchangeRateService exchangeRateService =
                new ExchangeRateService(exchangeRateDaoMock, currencyDaoMock);

        Mockito.when(currencyDaoMock.findByCode("EUR"))
                .thenReturn(Optional.of(eur));

        Mockito.when(currencyDaoMock.findByCode("RUB"))
                .thenReturn(Optional.of(rub));

        Mockito.when(exchangeRateDaoMock.transferFromOneCurrencyToAnother("EUR", "RUB"))
                .thenReturn(List.of(exchangeRate));

        ExchangeRateDto actual = exchangeRateService
                .transferFromOneCurrencyToAnother("eur", "rUb", amount);

        assertThat(actual.getBaseCurrency().getCode()).isEqualTo("EUR");
        assertThat(actual.getTargetCurrency().getCode()).isEqualTo("RUB");
        assertThat(actual.getRate()).isEqualByComparingTo("94");
        assertThat(actual.getAmount()).isEqualByComparingTo(amount);
        assertThat(actual.getConvertedAmount())
                .isEqualTo(new BigDecimal(expectedConvertedAmount));

        Mockito.verify(exchangeRateDaoMock)
                .transferFromOneCurrencyToAnother("EUR", "RUB");
    }

    @Test
    void transferFromOneCurrencyToAnotherWhenConversionPathDoesNotExist() {
        Currency eur = Currency.builder()
                .id(3).code("EUR").fullName("Euro").sign("€").build();

        Currency rub = Currency.builder()
                .id(4).code("RUB").fullName("Russian ruble").sign("₽").build();

        ExchangeRateDao exchangeRateDaoMock = Mockito.mock(ExchangeRateDao.class);
        CurrencyDao currencyDaoMock = Mockito.mock(CurrencyDao.class);
        ExchangeRateService exchangeRateService =
                new ExchangeRateService(exchangeRateDaoMock, currencyDaoMock);

        Mockito.when(currencyDaoMock.findByCode("EUR"))
                .thenReturn(Optional.of(eur));

        Mockito.when(currencyDaoMock.findByCode("RUB"))
                .thenReturn(Optional.of(rub));

        ExchangeRateNotFoundException exception = new ExchangeRateNotFoundException(
                "Our service can't calculate transfer from: EUR to: RUB"
        );

        Mockito.when(exchangeRateDaoMock.transferFromOneCurrencyToAnother("EUR", "RUB"))
                .thenThrow(exception);

        assertThatThrownBy(() -> exchangeRateService
                .transferFromOneCurrencyToAnother("EUR", "RUB", "100"))
                .isSameAs(exception);
    }

    @ParameterizedTest
    @CsvSource({
            "0",
            "0.000000",
            "-100",
            "-0.000001"
    })
    void transferFromOneCurrencyToAnotherWhenAmountIsNotPositive(String amount) {
        ExchangeRateDao exchangeRateDaoMock = Mockito.mock(ExchangeRateDao.class);
        CurrencyDao currencyDaoMock = Mockito.mock(CurrencyDao.class);
        ExchangeRateService exchangeRateService =
                new ExchangeRateService(exchangeRateDaoMock, currencyDaoMock);

        assertThatThrownBy(() -> exchangeRateService
                .transferFromOneCurrencyToAnother("EUR", "RUB", amount))
                .isInstanceOf(InvalidException.class)
                .hasMessage("Number must be greater than zero")
                .hasNoCause();

        Mockito.verifyNoInteractions(exchangeRateDaoMock, currencyDaoMock);
    }

    @ParameterizedTest
    @CsvSource(
            delimiter = '|',
            value = {
                    "abc | Rate must contain from 1 to 6 integer digits and up to 6 fractional digits, without exponent",
                    "1234567 | Rate must contain from 1 to 6 integer digits and up to 6 fractional digits, without exponent",
                    "1.1234567 | Rate must contain from 1 to 6 integer digits and up to 6 fractional digits, without exponent",
                    "0 | Number must be greater than zero",
                    "-94 | Number must be greater than zero"
            }
    )
    void addNewExchangeRateWhenRateIsInvalid(String rate, String expectedMessage) {
        ExchangeRateDao exchangeRateDaoMock = Mockito.mock(ExchangeRateDao.class);
        CurrencyDao currencyDaoMock = Mockito.mock(CurrencyDao.class);
        ExchangeRateService exchangeRateService =
                new ExchangeRateService(exchangeRateDaoMock, currencyDaoMock);

        assertThatThrownBy(() -> exchangeRateService
                .addNewExchangeRate("EUR", "RUB", rate))
                .isInstanceOf(InvalidException.class)
                .hasMessage(expectedMessage)
                .hasNoCause();

        Mockito.verifyNoInteractions(exchangeRateDaoMock, currencyDaoMock);
    }

    @Test
    void addNewExchangeRateWhenDirectExchangeRateAlreadyExists() {
        ExchangeRateDao exchangeRateDaoMock = Mockito.mock(ExchangeRateDao.class);
        CurrencyDao currencyDaoMock = Mockito.mock(CurrencyDao.class);
        ExchangeRateService exchangeRateService =
                new ExchangeRateService(exchangeRateDaoMock, currencyDaoMock);

        Mockito.when(exchangeRateDaoMock.getExchangeRateByCurrencyCodes("RUB", "EUR"))
                .thenReturn(Optional.empty());

        ExchangeRateAlreadyExistsException exception =
                new ExchangeRateAlreadyExistsException(
                        "Exchange rate with baseCurrencyCode = EUR and targetCurrencyCode = RUB is already exists."
                );

        Mockito.when(exchangeRateDaoMock.addExchangeRate(
                        "EUR", "RUB", new BigDecimal("94.000000")))
                .thenThrow(exception);

        assertThatThrownBy(() -> exchangeRateService
                .addNewExchangeRate("eur", "rub", "94.000000"))
                .isSameAs(exception);
    }

    @Test
    void addNewExchangeRateWhenCurrencyDoesNotExist() {
        ExchangeRateDao exchangeRateDaoMock = Mockito.mock(ExchangeRateDao.class);
        CurrencyDao currencyDaoMock = Mockito.mock(CurrencyDao.class);
        ExchangeRateService exchangeRateService =
                new ExchangeRateService(exchangeRateDaoMock, currencyDaoMock);

        Mockito.when(exchangeRateDaoMock.getExchangeRateByCurrencyCodes("RUB", "EUR"))
                .thenReturn(Optional.empty());

        ExchangeRateNotFoundException exception = new ExchangeRateNotFoundException(
                "One (or both) of the currencies in the currency pair does not exist in the database: EUR RUB"
        );

        Mockito.when(exchangeRateDaoMock.addExchangeRate(
                        "EUR", "RUB", new BigDecimal("94.000000")))
                .thenThrow(exception);

        assertThatThrownBy(() -> exchangeRateService
                .addNewExchangeRate("EUR", "RUB", "94.000000"))
                .isSameAs(exception);
    }

    @ParameterizedTest(name = "reverseFirst={0}, reverseSecond={1}")
    @CsvSource({
            "false, false",
            "true,  false",
            "false, true",
            "true,  true"
    })
    void transferFromRubToEurThroughUsd(
            boolean reverseFirst,
            boolean reverseSecond
    ) {
        Currency rub = Currency.builder()
                .id(4).code("RUB").fullName("Russian ruble").sign("₽").build();

        Currency usd = Currency.builder()
                .id(2).code("USD").fullName("United States dollar").sign("$").build();

        Currency eur = Currency.builder()
                .id(3).code("EUR").fullName("Euro").sign("€").build();

        CurrencyDto rubDto = CurrencyDto.builder()
                .id(4).code("RUB").name("Russian ruble").sign("₽").build();

        CurrencyDto eurDto = CurrencyDto.builder()
                .id(3).code("EUR").name("Euro").sign("€").build();

        // Первый участок: RUB → USD или USD → RUB.
        ExchangeRate firstRate = ExchangeRate.builder()
                .baseCurrency(reverseFirst ? usd : rub)
                .targetCurrency(reverseFirst ? rub : usd)
                .rate(new BigDecimal(reverseFirst ? "50.000000" : "0.020000"))
                .build();

        // Второй участок: USD → EUR или EUR → USD.
        ExchangeRate secondRate = ExchangeRate.builder()
                .baseCurrency(reverseSecond ? eur : usd)
                .targetCurrency(reverseSecond ? usd : eur)
                .rate(new BigDecimal(reverseSecond ? "1.250000" : "0.800000"))
                .build();

        ExchangeRateDao exchangeRateDaoMock = Mockito.mock(ExchangeRateDao.class);
        CurrencyDao currencyDaoMock = Mockito.mock(CurrencyDao.class);
        ExchangeRateService exchangeRateService =
                new ExchangeRateService(exchangeRateDaoMock, currencyDaoMock);

        Mockito.when(currencyDaoMock.findByCode("RUB"))
                .thenReturn(Optional.of(rub));

        Mockito.when(currencyDaoMock.findByCode("EUR"))
                .thenReturn(Optional.of(eur));

        Mockito.when(exchangeRateDaoMock.transferFromOneCurrencyToAnother("RUB", "EUR"))
                .thenReturn(List.of(firstRate, secondRate));

        ExchangeRateDto actual = exchangeRateService
                .transferFromOneCurrencyToAnother("RUB", "EUR", "100");

        assertThat(actual.getBaseCurrency()).isEqualTo(rubDto);
        assertThat(actual.getTargetCurrency()).isEqualTo(eurDto);
        assertThat(actual.getRate()).isEqualByComparingTo("0.016");
        assertThat(actual.getAmount()).isEqualByComparingTo("100");
        assertThat(actual.getConvertedAmount()).isEqualTo(new BigDecimal("1.60"));
    }
}