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


@WebServlet(value = "/exchange")
public class ExchangeServlet extends HttpServlet {
    private final ExchangeRateService exchangeRateService = ExchangeRateService.getInstance();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) {
        String fromCurrency = req.getParameter("from");
        String toCurrency = req.getParameter("to");
        String amount = req.getParameter("amount");

        if (fromCurrency == null || fromCurrency.isBlank())
            throw new InvalidExclusionOfRequiredParameter("Missing parameter 'from' in request");

        if (toCurrency == null || toCurrency.isBlank())
            throw new InvalidExclusionOfRequiredParameter("Missing parameter 'to' in request");

        if (amount == null || amount.isBlank())
            throw new InvalidExclusionOfRequiredParameter("Missing parameter 'amount' in request");

        ExchangeRateDto exchangeRateDto = exchangeRateService.transferFromOneCurrencyToAnother(fromCurrency, toCurrency, amount);
        try {
            resp.setStatus(HttpServletResponse.SC_OK);
            objectMapper.writeValue(resp.getOutputStream(), exchangeRateDto);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
