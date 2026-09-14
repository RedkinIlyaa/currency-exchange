package servlet;

import com.fasterxml.jackson.databind.ObjectMapper;
import dto.ExchangeRateDto;
import exception.invalid.InvalidException;
import exception.notfound.ExchangeRateNotFoundException;
import exception.invalid.InvalidExclusionOfRequiredParameter;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import service.ExchangeRateService;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Map;
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
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws UnsupportedEncodingException {
        String contentType = req.getContentType();

        String mediaType = contentType == null
                ? null
                : contentType.split(";", 2)[0].trim();

        if (!"application/x-www-form-urlencoded".equalsIgnoreCase(mediaType)) {
            throw new InvalidException("This request might have header Content-Type - application/x-www-form-urlencoded");
        }

        req.setCharacterEncoding("UTF-8");
        if (isParameterFromUrl(req))
            throw new InvalidException("Parameters in POST request to /exchangeRates must be in body and have 'application/x-www-form-urlencoded' media type");

        Map<String, String[]> parameterMap = checkRequestParameters(req);

        for (Map.Entry<String, String[]> entry: parameterMap.entrySet()) {
            String[] value = entry.getValue();
            if (value.length != 1)
                throw new InvalidException("Parameter " + entry.getKey() + " has more than one value");
        }

        String baseCurrencyCode = req.getParameter("baseCurrencyCode");
        String targetCurrencyCode = req.getParameter("targetCurrencyCode");
        String rate = req.getParameter("rate");

        if (baseCurrencyCode == null || baseCurrencyCode.isBlank())
            throw new InvalidExclusionOfRequiredParameter("Omitted parameter - baseCurrencyCode in the request");

        if (targetCurrencyCode == null || targetCurrencyCode.isBlank())
            throw new InvalidExclusionOfRequiredParameter("Omitted parameter - targetCurrencyCode in the request");

        if (rate == null || rate.isBlank())
            throw new InvalidExclusionOfRequiredParameter("Omitted parameter - rate in the request");

        Optional<ExchangeRateDto> exchangeRateDto = exchangeRateService.addNewExchangeRate(
                baseCurrencyCode,
                targetCurrencyCode,
                rate
        );

        if (exchangeRateDto.isEmpty())
            throw new ExchangeRateNotFoundException("Exchange rate not found");

        try {
            ServletOutputStream outputStream = resp.getOutputStream();
            resp.setStatus(HttpServletResponse.SC_CREATED);
            objectMapper.writeValue(outputStream, exchangeRateDto.get());
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
            throw new InvalidException("There must be exactly 3 parameters in POST /exchangeRates: baseCurrencyCode, targetCurrencyCode, rate");
        if (!parameterMap.containsKey("baseCurrencyCode"))
            throw new InvalidException("Missing baseCurrencyCode parameter in the request");
        if (!parameterMap.containsKey("targetCurrencyCode"))
            throw new InvalidException("Missing targetCurrencyCode parameter in the request");
        if (!parameterMap.containsKey("rate"))
            throw new InvalidException("Missing rate parameter in the request");
        return parameterMap;
    }

    private boolean isParameterFromUrl(HttpServletRequest httpServletRequest) {
        String queryString = httpServletRequest.getQueryString();
        return queryString != null && !queryString.isBlank();
    }
}
