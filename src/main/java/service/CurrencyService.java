package service;

import dao.CurrencyDao;
import dto.CurrencyDto;
import entity.Currency;
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
        Currency currency = Currency.builder()
                .code(code)
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

    public static CurrencyService getInstance() {
        return currencyService;
    }
}
