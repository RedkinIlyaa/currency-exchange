package service;

import dao.CurrencyDao;
import dto.CurrencyDto;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CurrencyService {

    private static final CurrencyService currencyService = new CurrencyService();
    private final CurrencyDao currencyDao = CurrencyDao.getCurrencyDao();

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

    public static CurrencyService getInstance() {
        return currencyService;
    }
}
