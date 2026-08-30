package servlet;

import com.fasterxml.jackson.databind.ObjectMapper;
import dto.ExchangeRateDto;
import exception.invalid.InvalidExclusionOfRequiredParameter;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import service.ExchangeRateService;

import java.io.IOException;
import java.math.BigDecimal;


@WebServlet(value = "/exchange")
public class ExchangeServlet extends HttpServlet {
    private final ExchangeRateService exchangeRateService = ExchangeRateService.getInstance();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) {
        String fromCurrency = req.getParameter("from");
        String toCurrency = req.getParameter("to");
        String amount = req.getParameter("amount");
        BigDecimal bigDecimal = new BigDecimal(amount);

        if (fromCurrency == null || toCurrency == null || amount == null)
            throw new InvalidExclusionOfRequiredParameter("Missing parameter(fromCurrency/toCurrency/amount) in request");

        ExchangeRateDto exchangeRateDto = exchangeRateService.transferFromOneCurrencyToAnother(fromCurrency, toCurrency, bigDecimal);
        try {
            resp.setStatus(HttpServletResponse.SC_OK);
            objectMapper.writeValue(resp.getOutputStream(), exchangeRateDto);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
