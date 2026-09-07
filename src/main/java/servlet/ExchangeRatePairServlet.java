package servlet;

import com.fasterxml.jackson.databind.ObjectMapper;
import dto.ExchangeRateDto;
import exception.invalid.*;
import exception.notfound.ExchangeRateNotFoundException;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import service.ExchangeRateService;

import java.io.BufferedReader;
import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

@WebServlet(value = "/exchangeRate/*")
public class ExchangeRatePairServlet extends HttpServlet {
    private final ExchangeRateService exchangeRateService = ExchangeRateService.getInstance();
    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) {
        String pathInfo = req.getPathInfo();

        if (pathInfo == null || pathInfo.equals("/")) {
            // Коды валют пары отсутствуют в адресе - 400
            throw new InvalidExchangeRatePairException("Currency codes are missing");
        }

        String pathInfoWithoutSlash = pathInfo.substring(1);
        if (pathInfoWithoutSlash.length() != 6 || !pathInfoWithoutSlash.matches("[a-zA-Z]{6}")) {
            throw new InvalidExchangeRatePairException("Currency pair must contain exactly 6 Latin letters");
        }

        String firstCurrencyCode = pathInfoWithoutSlash.substring(0, 3);
        String secondCurrencyCode = pathInfoWithoutSlash.substring(3);
        Optional<ExchangeRateDto> exchangeRateDto = exchangeRateService
                .exchangeRateDtoByCurrenciesCodes(firstCurrencyCode, secondCurrencyCode);

        if (exchangeRateDto.isEmpty())
            // Обменный курс для пары не найден - 404
            throw new ExchangeRateNotFoundException("Exchange rate not found");

        try {
            ServletOutputStream outputStream = resp.getOutputStream();
            objectMapper.writeValue(outputStream, exchangeRateDto.get());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected void doPatch(HttpServletRequest req, HttpServletResponse resp) {
        String pathInfo = req.getPathInfo();

        if (pathInfo == null || pathInfo.equals("/")) {
            throw new InvalidExchangeRatePairException("Currencies codes are missing");
        }

        String pathInfoWithoutSlash = pathInfo.substring(1);
        if (pathInfoWithoutSlash.length() != 6 || !pathInfoWithoutSlash.matches("[a-zA-Z]{6}")) {
            throw new InvalidExchangeRatePairException("Currency pair must contain exactly 6 latin letters");
        }
        String firstCurrency = pathInfoWithoutSlash.substring(0, 3);
        String secondCurrency = pathInfoWithoutSlash.substring(3);

        String rate = getFormParameter(req);

        Optional<ExchangeRateDto> exchangeRateDto = exchangeRateService.patchToExchangeRate(firstCurrency.toUpperCase(Locale.ENGLISH), secondCurrency.toUpperCase(Locale.ENGLISH), rate);

        if (exchangeRateDto.isEmpty()) {
            throw new ExchangeRateNotFoundException("Exchange rate not found");
        }

        try {
            ServletOutputStream outputStream = resp.getOutputStream();
            objectMapper.writeValue(outputStream, exchangeRateDto.get());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private String getFormParameter(HttpServletRequest req) {
        try {
            BufferedReader reader = req.getReader();
            List<String> list = reader.lines().toList();
            if (list.size() != 1)
                throw new InvalidCountOfBodyLinesException("Body should contain only one line of parameters.");

            String[] split = list.getFirst().split("&");
            if (split.length != 1)
                throw new InvalidCountOfBodyParametersException("Body should contain only one(key + value) pair of parameters");

            String parameterPair = split[0];
            String[] keyAndValue = parameterPair.split("=");

            if (keyAndValue.length != 2) {
                String encodedKey = URLDecoder.decode(keyAndValue[0], StandardCharsets.UTF_8);
                throw new InvalidException("Parameter " + encodedKey + " doesn't have a value.");
            }

            String encodedKey = URLDecoder.decode(keyAndValue[0], StandardCharsets.UTF_8);
            if (!encodedKey.equals("rate"))
                throw new InvalidNameOfBodyParameterException("Body should contain only one(key + value) pair. Where key = 'rate'. Your key = '" + encodedKey + "'");

            String encodedValue = URLDecoder.decode(keyAndValue[1], StandardCharsets.UTF_8);
            if (!encodedValue.matches("^-?\\d+(\\.\\d+)?$"))
                throw new InvalidException("Rate parameter must contain only numbers.");

            return URLDecoder.decode(keyAndValue[1], StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
