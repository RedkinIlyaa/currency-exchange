package service;

import dao.CurrencyDao;
import dto.CurrencyDto;
import entity.Currency;
import exception.invalid.InvalidCurrencyCodeException;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Locale;
import java.util.Optional;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CurrencyService {

    private static final CurrencyService currencyService = new CurrencyService();
    private final CurrencyDao currencyDao = CurrencyDao.getInstance();

    public List<CurrencyDto> getAllCurrencies() {
        return currencyDao.findAll().stream().map(
                currency -> CurrencyDto.builder()
                        .id(currency.getId())
                        .name(currency.getFullName())
                        .code(currency.getCode())
                        .sign(currency.getSign())
                        .build()
        ).toList();
    }

    public Optional<CurrencyDto> getCurrencyByCode(String code) {
        return currencyDao.findByCode(code.toUpperCase(Locale.ENGLISH))
                .map(currency -> CurrencyDto.builder()
                        .id(currency.getId())
                        .name(currency.getFullName())
                        .code(currency.getCode())
                        .sign(currency.getSign())
                        .build());
    }

    public CurrencyDto addNewCurrency(String name, String code, String sign) {
        if (doesCurrencyCodeHaveMistake(code))
            throw new InvalidCurrencyCodeException("Code parameter must be exactly 3 char and contain only a-z or A-Z letters");

        Currency currency = Currency.builder()
                .code(code.toUpperCase(Locale.ENGLISH))
                .fullName(name)
                .sign(sign)
                .build();
        Integer integer = currencyDao.save(currency);

        return CurrencyDto.builder()
                .id(integer)
                .name(currency.getFullName())
                .code(currency.getCode())
                .sign(currency.getSign())
                .build();
    }

    private boolean doesCurrencyCodeHaveMistake(String code) {
        return !(code.matches("[a-zA-Z]+") && (code.length() == 3));
    }

    public static CurrencyService getInstance() {
        return currencyService;
    }
}
