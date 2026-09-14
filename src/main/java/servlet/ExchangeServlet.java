package servlet;

import com.fasterxml.jackson.databind.ObjectMapper;
import dto.ExchangeRateDto;
import exception.invalid.InvalidException;
import exception.invalid.InvalidExclusionOfRequiredParameter;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import service.ExchangeRateService;

import java.io.IOException;
import java.util.Map;


@WebServlet(value = "/exchange")
public class ExchangeServlet extends HttpServlet {
    private final ExchangeRateService exchangeRateService = ExchangeRateService.getInstance();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) {
        Map<String, String[]> parameterMap = checkRequestParameters(req);

        for (Map.Entry<String, String[]> entry: parameterMap.entrySet()) {
            String[] value = entry.getValue();
            if (value.length != 1)
                throw new InvalidException("Parameter " + entry.getKey() + " has more than one value");
        }

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

    private static Map<String, String[]> checkRequestParameters(HttpServletRequest req) {
        Map<String, String[]> parameterMap;
        try {
            parameterMap = req.getParameterMap();
        } catch (IllegalStateException e) {
            throw new InvalidException("Request parameters are malformed or incorrectly encoded.");
        }
        if (parameterMap.size() != 3)
            throw new InvalidException("There must be exactly 3 parameters in GET /exchange: from, to, amount");
        if (!parameterMap.containsKey("from"))
            throw new InvalidException("Missing from parameter in the request");
        if (!parameterMap.containsKey("to"))
            throw new InvalidException("Missing to parameter in the request");
        if (!parameterMap.containsKey("amount"))
            throw new InvalidException("Missing amount parameter in the request");
        return parameterMap;
    }
}
