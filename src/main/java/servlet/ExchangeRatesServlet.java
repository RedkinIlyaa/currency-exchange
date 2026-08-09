package servlet;

import com.fasterxml.jackson.databind.ObjectMapper;
import dto.ExchangeRateDto;
import exception.notfound.ExchangeRateNotFoundException;
import exception.invalid.InvalidExclusionOfRequiredParameter;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import service.ExchangeRateService;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

@WebServlet(value = "/exchangeRates")
public class ExchangeRatesServlet extends HttpServlet {
    private final ExchangeRateService exchangeRateService = ExchangeRateService.getInstance();
    private final static ObjectMapper objectMapper = new ObjectMapper();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) {
        List<ExchangeRateDto> allExchangeRates = exchangeRateService.getAllExchangeRates();

        try {
            objectMapper.writeValue(resp.getOutputStream(), allExchangeRates);
        } catch (IOException e) {
            throw new RuntimeException("Failed to write JSON response", e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) {
        String baseCurrencyCode = req.getParameter("baseCurrencyCode");
        String targetCurrencyCode = req.getParameter("targetCurrencyCode");
        String rate = req.getParameter("rate");

        if (baseCurrencyCode == null || targetCurrencyCode == null || rate == null)
            throw new InvalidExclusionOfRequiredParameter("Missing parameter(baseCurrencyCode/targetCurrencyCode/rate) in request");

        Optional<ExchangeRateDto> exchangeRateDto = exchangeRateService.addNewExchangeRate(
                baseCurrencyCode.toUpperCase(Locale.ENGLISH),
                targetCurrencyCode.toUpperCase(Locale.ENGLISH),
                new BigDecimal(rate)
        );

        if (exchangeRateDto.isEmpty())
            throw new ExchangeRateNotFoundException("Exchange rate not found");

        try {
            ServletOutputStream outputStream = resp.getOutputStream();
            objectMapper.writeValue(outputStream, exchangeRateDto.get());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }
}
