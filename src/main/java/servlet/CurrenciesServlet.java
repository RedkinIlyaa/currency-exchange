package servlet;


import dto.CurrencyDto;
import exception.CurrenciesServletException;
import exception.InvalidExclusionOfRequiredParameter;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import service.CurrencyService;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.List;


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
        req.setCharacterEncoding("UTF-8");
        String name = req.getParameter("name");
        String code = req.getParameter("code");
        String sign = req.getParameter("sign");

        if (name == null || code == null || sign == null)
            throw new InvalidExclusionOfRequiredParameter("Missing parameter(name/code/sign) in request");

        CurrencyDto currencyDto = currencyService.addNewCurrency(name, code, sign);
        try {
            resp.setStatus(HttpServletResponse.SC_CREATED);
            objectMapper.writeValue(resp.getOutputStream(), currencyDto);
        } catch (IOException e) {
            throw new CurrenciesServletException("Failed to write JSON response", e);
        }
    }
}
