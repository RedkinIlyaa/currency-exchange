package servlet;


import dto.CurrencyDto;
import exception.CurrenciesServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import service.CurrencyService;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.List;


@WebServlet(value = "/currencies")
public class CurrenciesServlet extends HttpServlet {

    private final CurrencyService currencyService = CurrencyService.getInstance();
    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)  {
        resp.setContentType("application/json");
        List<CurrencyDto> allCurrencies = currencyService.getAllCurrencies();

        try {
            objectMapper.writeValue(resp.getOutputStream(), allCurrencies);
        } catch (IOException e) {
            throw new CurrenciesServletException("Failed to write JSON response", e);
        }
    }
}
