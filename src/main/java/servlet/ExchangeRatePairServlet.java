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

import java.io.IOException;
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
}
