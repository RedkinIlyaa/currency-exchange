package servlet;


import dto.CurrencyDto;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletOutputStream;
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

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json");
        List<CurrencyDto> allCurrencies = currencyService.getAllCurrencies();
        ObjectMapper objectMapper = new ObjectMapper();

        ServletOutputStream respOutputStream = resp.getOutputStream();
        objectMapper.writeValue(respOutputStream, allCurrencies);
    }
}
