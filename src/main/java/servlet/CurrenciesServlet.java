package servlet;


import dto.CurrencyDto;
import exception.CurrenciesServletException;
import exception.invalid.InvalidException;
import exception.invalid.InvalidExclusionOfRequiredParameter;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import service.CurrencyService;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Map;


@WebServlet(value = "/currencies")
public class CurrenciesServlet extends HttpServlet {

    private final CurrencyService currencyService = CurrencyService.getInstance();
    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)  {
        List<CurrencyDto> allCurrencies = currencyService.getAllCurrencies();

        try {
            objectMapper.writeValue(resp.getOutputStream(), allCurrencies);
        } catch (IOException e) {
            throw new CurrenciesServletException("Failed to write JSON response", e);
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
            throw new InvalidException("Parameters in POST request to /currencies must be in body and have 'application/x-www-form-urlencoded' media type");

        Map<String, String[]> parameterMap = checkRequestParameters(req);

        for (Map.Entry<String, String[]> entry: parameterMap.entrySet()) {
            String[] value = entry.getValue();
            if (value.length != 1)
                throw new InvalidException("Parameter " + entry.getKey() + " has more than one value");
        }

        String name = req.getParameter("name");
        String code = req.getParameter("code");
        String sign = req.getParameter("sign");

        if (name  == null || name.isBlank())
            throw new InvalidExclusionOfRequiredParameter("Omitted parameter - name in the request");

        if (code  == null || code.isBlank())
            throw new InvalidExclusionOfRequiredParameter("Omitted parameter - code in the request");

        if (sign == null || sign.isBlank())
            throw new InvalidExclusionOfRequiredParameter("Omitted parameter - sign in the request");

        CurrencyDto currencyDto = currencyService.addNewCurrency(name, code, sign);
        try {
            resp.setStatus(HttpServletResponse.SC_CREATED);
            objectMapper.writeValue(resp.getOutputStream(), currencyDto);
        } catch (IOException e) {
            throw new CurrenciesServletException("Failed to write JSON response", e);
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
            throw new InvalidException("There must be exactly 3 parameters in POST /currencies: name, code, sign");
        if (!parameterMap.containsKey("name"))
            throw new InvalidException("Missing name parameter in the request");    
        if (!parameterMap.containsKey("code"))
            throw new InvalidException("Missing code parameter in the request");
        if (!parameterMap.containsKey("sign"))
            throw new InvalidException("Missing sign parameter in the request");
        return parameterMap;
    }

    private boolean isParameterFromUrl(HttpServletRequest httpServletRequest) {
        String queryString = httpServletRequest.getQueryString();
        return queryString != null && !queryString.isBlank();
    }
}
