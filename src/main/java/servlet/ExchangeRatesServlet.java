package servlet;

import com.fasterxml.jackson.databind.ObjectMapper;
import dto.ExchangeRateDto;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import service.ExchangeRateService;

import java.io.IOException;
import java.util.List;

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
}
