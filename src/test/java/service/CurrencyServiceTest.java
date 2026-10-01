package service;

import dao.CurrencyDao;
import dto.CurrencyDto;
import entity.Currency;
import exception.CurrencyDaoException;
import exception.exist.CurrencyAlreadyExistsException;
import exception.invalid.InvalidCurrencyCodeException;
import exception.invalid.InvalidException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.*;
import org.mockito.Mockito;

import static org.assertj.core.api.Assertions.*;

import java.sql.SQLException;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Stream;

public class CurrencyServiceTest {

    @Test
    void getAllCurrenciesWhenOnlyUsdExists() {
        Currency usd = Currency.builder()
                .id(1)
                .code("USD")
                .sign("$")
                .fullName("United States dollar")
                .build();

        CurrencyDto usdDto = CurrencyDto.builder()
                .id(1)
                .code("USD")
                .name("United States dollar")
                .sign("$")
                .build();

        CurrencyDao currencyDaoMock = Mockito.mock(CurrencyDao.class);
        CurrencyService currencyService = new CurrencyService(currencyDaoMock);

        Mockito.when(currencyDaoMock.findAll()).thenReturn(List.of(usd));
        assertThat(currencyService.getAllCurrencies()).isEqualTo(List.of(usdDto));
    }

    @Test
    void getAllCurrenciesWhenZeroCurrencyExists() {
        CurrencyDao currencyDaoMock = Mockito.mock(CurrencyDao.class);
        CurrencyService currencyService = new CurrencyService(currencyDaoMock);

        Mockito.when(currencyDaoMock.findAll()).thenReturn(List.of());
        assertThat(currencyService.getAllCurrencies()).isEqualTo(List.of());
    }

    @Test
    void getAllCurrenciesWhenAnyCurrenciesExist() {
        Currency aus = Currency.builder()
                .id(1)
                .code("AUD")
                .sign("A$")
                .fullName("Australian dollar")
                .build();

        Currency usd = Currency.builder()
                .id(2)
                .code("USD")
                .sign("$")
                .fullName("United States dollar")
                .build();

        Currency euro = Currency.builder()
                .id(3)
                .code("EUR")
                .sign("€")
                .fullName("Euro")
                .build();

        Currency ruble = Currency.builder()
                .id(4)
                .code("RUB")
                .sign("₽")
                .fullName("Russian ruble")
                .build();

        List<Currency> currencyList = List.of(aus, usd, euro, ruble);

        CurrencyDto ausDto = CurrencyDto.builder()
                .id(1)
                .code("AUD")
                .name("Australian dollar")
                .sign("A$")
                .build();

        CurrencyDto usdDto = CurrencyDto.builder()
                .id(2)
                .code("USD")
                .name("United States dollar")
                .sign("$")
                .build();

        CurrencyDto eurDto = CurrencyDto.builder()
                .id(3)
                .code("EUR")
                .sign("€")
                .name("Euro")
                .build();


        CurrencyDto rubleDto = CurrencyDto.builder()
                .id(4)
                .code("RUB")
                .sign("₽")
                .name("Russian ruble")
                .build();

        List<CurrencyDto> currencyDtoList = List.of(
                ausDto,
                usdDto,
                eurDto,
                rubleDto
        );


        CurrencyDao currencyDaoMock = Mockito.mock(CurrencyDao.class);
        CurrencyService currencyService = new CurrencyService(currencyDaoMock);

        Mockito.when(currencyDaoMock.findAll()).thenReturn(currencyList);
        assertThat(currencyService.getAllCurrencies())
                .hasSize(4)
                .isEqualTo(currencyDtoList);
    }

    @Test
    void getAllCurrenciesWhenDaoThrowException() {
        CurrencyDao currencyDaoMock = Mockito.mock(CurrencyDao.class);
        CurrencyService currencyService = new CurrencyService(currencyDaoMock);

        Mockito.when(currencyDaoMock.findAll()).thenThrow(new CurrencyDaoException("Failed to get List<Currency> from db"));
        assertThatThrownBy(currencyService::getAllCurrencies)
                .isInstanceOf(CurrencyDaoException.class)
                .hasMessage("Failed to get List<Currency> from db");
    }

    @ParameterizedTest
    @ValueSource(strings = {"RUBS", "USDS", "RU", "RU$", "123", "RU.", "RUS.", "/USD", "1USD", "rubs", "liя", "ru."})
    void getCurrencyByCodeWhenCodeHaveAMistake(String code) {
        CurrencyDao currencyDao = Mockito.mock(CurrencyDao.class);
        CurrencyService currencyService = new CurrencyService(currencyDao);

        assertThatThrownBy(() -> currencyService.getCurrencyByCode(code))
                .hasNoCause()
                .hasMessage("Code parameter must be exactly 3 char and contain only a-z or A-Z letters")
                .isInstanceOf(InvalidCurrencyCodeException.class);
        Mockito.verifyNoInteractions(currencyDao);
    }


    @ParameterizedTest
    @CsvSource({"UAE,UAE", "lol,LOL", "aga,AGA", "liv,LIV"})
    void getCurrencyByCodeWhenCodeDoesNotExist(String code, String expectedDaoCode) {
        CurrencyDao currencyDaoMock = Mockito.mock(CurrencyDao.class);
        CurrencyService currencyService = new CurrencyService(currencyDaoMock);

        Mockito.when(currencyDaoMock.findByCode(expectedDaoCode)).thenReturn(Optional.empty());

        assertThat(currencyService.getCurrencyByCode(code)).isEmpty();
        Mockito.verify(currencyDaoMock).findByCode(expectedDaoCode);
    }

    @ParameterizedTest
    @ValueSource(strings = {"USD", "usd", "uSd"})
    void getCurrencyByCode(String code) {
        Currency usd = Currency.builder()
                .id(2)
                .code("USD")
                .sign("$")
                .fullName("United States dollar")
                .build();

        CurrencyDto usdDto = CurrencyDto.builder()
                .id(2)
                .code("USD")
                .name("United States dollar")
                .sign("$")
                .build();

        CurrencyDao currencyDaoMock = Mockito.mock(CurrencyDao.class);
        CurrencyService currencyService = new CurrencyService(currencyDaoMock);

        Mockito.when(currencyDaoMock.findByCode(code.toUpperCase(Locale.ENGLISH))).thenReturn(Optional.of(usd));
        assertThat(currencyService.getCurrencyByCode(code)).isEqualTo(Optional.of(usdDto));
        Mockito.verify(currencyDaoMock).findByCode("USD");
    }

    @ParameterizedTest
    @ValueSource(strings = {"USD", "RUB", "EUR"})
    void getCurrencyByCodeWhenDaoThrowException(String code) {
        CurrencyDao currencyDaoMock = Mockito.mock(CurrencyDao.class);
        CurrencyService currencyService = new CurrencyService(currencyDaoMock);

        Mockito.when(currencyDaoMock.findByCode(code))
                .thenThrow(new CurrencyDaoException("Failed to get Currency from db"));

        assertThatThrownBy(() -> currencyService.getCurrencyByCode(code))
                .hasNoCause()
                .hasMessage("Failed to get Currency from db")
                .isInstanceOf(CurrencyDaoException.class);
    }

    @ParameterizedTest
    @CsvSource({
            "Russian ruble, RUBS, ₽",
            "Russian ruble, RU., ₽",
            "Russian ruble, KZRU, ₽",
            "Russian ruble, ЛЕВ, ₽",
            "Russian ruble, РУБ, ₽",
            "Russian ruble, RU, ₽",
            "Russian ruble, KEKLOL, ₽"
    })
    void addNewCurrencyWhenCodeHasMistake(String name, String code, String sign) {
        CurrencyDao currencyDaoMock = Mockito.mock(CurrencyDao.class);
        CurrencyService currencyService = new CurrencyService(currencyDaoMock);

        assertThatThrownBy(() -> currencyService.addNewCurrency(name, code, sign))
                .isInstanceOf(InvalidCurrencyCodeException.class)
                .hasMessage("Code parameter must be exactly 3 char and contain only a-z or A-Z letters")
                .hasNoCause();
        Mockito.verifyNoInteractions(currencyDaoMock);
    }

    @ParameterizedTest
    @MethodSource(value = "addNewCurrencyWhenNameHasMistake")
    void addNewCurrencyWhenNameHasMistake(String name, String code, String sign) {
        CurrencyDao currencyDaoMock = Mockito.mock(CurrencyDao.class);
        CurrencyService currencyService = new CurrencyService(currencyDaoMock);

        assertThatThrownBy(() -> currencyService.addNewCurrency(name, code, sign))
                .isInstanceOf(InvalidException.class)
                .hasNoCause()
                .hasMessage("Name parameter must be no more than 128 characters long.");
        Mockito.verifyNoInteractions(currencyDaoMock);
    }


    public static Stream<Arguments> addNewCurrencyWhenNameHasMistake() {
        return Stream.of(
                Arguments.of("r".repeat(129), "RUB", "₽"),
                Arguments.of("U".repeat(139), "USD", "$")
        );
    }

    @ParameterizedTest
    @MethodSource(value = "addNewCurrencyWhenSignHaveMistake")
    void addNewCurrencyWhenSignHaveMistake(String name, String code, String sign) {
        CurrencyDao currencyDaoMock = Mockito.mock(CurrencyDao.class);
        CurrencyService currencyService = new CurrencyService(currencyDaoMock);

        assertThatThrownBy(() -> currencyService.addNewCurrency(name, code, sign))
                .isInstanceOf(InvalidException.class)
                .hasMessage("Sign parameter must be no more than 16 characters long")
                .hasNoCause();
        Mockito.verifyNoInteractions(currencyDaoMock);
    }

    public static Stream<Arguments> addNewCurrencyWhenSignHaveMistake() {
        return Stream.of(
                Arguments.of("Russian ruble", "RUB", "₽".repeat(18)),
                Arguments.of("United States dollar", "USD", "$".repeat(17)),
                Arguments.of("Euro", "EUR", "€".repeat(17))
        );
    }

    @Test
    void addNewCurrency() {
        Currency aus = Currency.builder()
                .code("AUD")
                .sign("A$")
                .fullName("Australian dollar")
                .build();

        CurrencyDto ausDto = CurrencyDto.builder()
                .id(1)
                .code("AUD")
                .name("Australian dollar")
                .sign("A$")
                .build();

        CurrencyDao currencyDao = Mockito.mock(CurrencyDao.class);
        CurrencyService currencyService = new CurrencyService(currencyDao);

        Mockito.when(currencyDao.save(aus))
                .thenReturn(1);

        assertThat(currencyService.addNewCurrency("Australian dollar", "aud", "A$"))
                .isEqualTo(ausDto);
    }

    @Test
    void addNewCurrencyWhenItHasBorderName() {
        Currency aus = Currency.builder()
                .code("AUD")
                .sign("A$")
                .fullName("A".repeat(128))
                .build();

        CurrencyDto ausDto = CurrencyDto.builder()
                .id(1)
                .code("AUD")
                .name("A".repeat(128))
                .sign("A$")
                .build();

        CurrencyDao currencyDao = Mockito.mock(CurrencyDao.class);
        CurrencyService currencyService = new CurrencyService(currencyDao);

        Mockito.when(currencyDao.save(aus))
                .thenReturn(1);

        assertThat(currencyService.addNewCurrency("A".repeat(128), "aud", "A$"))
                .isEqualTo(ausDto);
    }

    @Test
    void addNewCurrencyWhenItHasBorderSign() {
        Currency aus = Currency.builder()
                .code("AUD")
                .sign("A".repeat(16))
                .fullName("Australian dollar")
                .build();

        CurrencyDto ausDto = CurrencyDto.builder()
                .id(1)
                .code("AUD")
                .name("Australian dollar")
                .sign("A".repeat(16))
                .build();

        CurrencyDao currencyDao = Mockito.mock(CurrencyDao.class);
        CurrencyService currencyService = new CurrencyService(currencyDao);

        Mockito.when(currencyDao.save(aus))
                .thenReturn(1);

        assertThat(currencyService.addNewCurrency("Australian dollar", "aud", "A".repeat(16)))
                .isEqualTo(ausDto);
    }

    @Test
    void addNewCurrencyWhenDaoThrowCurrencyDaoException() {
        Currency aus = Currency.builder()
                .code("AUD")
                .sign("A$")
                .fullName("Australian dollar")
                .build();

        CurrencyDao currencyDao = Mockito.mock(CurrencyDao.class);
        CurrencyService currencyService = new CurrencyService(currencyDao);

        Mockito.when(currencyDao.save(aus))
                .thenThrow(new CurrencyDaoException("Add 0 currency to db"));

        assertThatThrownBy(() -> currencyService.addNewCurrency("Australian dollar", "aud", "A$"))
                .hasMessage("Add 0 currency to db")
                .hasNoCause()
                .isInstanceOf(CurrencyDaoException.class);
    }

    @Test
    void addNewCurrencyWhenDaoThrowCurrencyAlreadyExistsException() {
        Currency aus = Currency.builder()
                .code("AUD")
                .sign("A$")
                .fullName("Australian dollar")
                .build();

        CurrencyDao currencyDao = Mockito.mock(CurrencyDao.class);
        CurrencyService currencyService = new CurrencyService(currencyDao);
        SQLException sqlException = new SQLException();

        Mockito.when(currencyDao.save(aus))
                .thenThrow(new CurrencyAlreadyExistsException("Currency already exists", sqlException));

        assertThatThrownBy(() -> currencyService.addNewCurrency("Australian dollar", "aud", "A$"))
                .hasMessage("Currency already exists")
                .hasCause(sqlException)
                .isInstanceOf(CurrencyAlreadyExistsException.class);
    }
}
