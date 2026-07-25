package servlet;

import com.fasterxml.jackson.databind.ObjectMapper;
import dto.CurrencyDto;
import exception.CurrencyNotFoundException;
import exception.InvalidCurrencyCodeException;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import service.CurrencyService;

import java.io.IOException;
import java.util.Optional;


@WebServlet(value = "/currency/*")
    public class CurrencyByCodeServlet extends HttpServlet {
    private final CurrencyService currencyService = CurrencyService.getInstance();
    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)  {
        String pathInfo = req.getPathInfo();

        if (pathInfo == null || pathInfo.equals("/")) {
            // Код валюты отсутствует в адресе - 400
            throw new InvalidCurrencyCodeException("Currency code is missing");
        }

        Optional<CurrencyDto> currencyByCode = currencyService.getCurrencyByCode(pathInfo.substring(1));

        if (!currencyByCode.isPresent()) {
            // Валюта не найдена - 404
            throw new CurrencyNotFoundException("Currency not found: " + pathInfo.substring(1));
        }

        CurrencyDto currencyDto = currencyByCode.get();

        try {
            ServletOutputStream outputStream = resp.getOutputStream();
            objectMapper.writeValue(outputStream, currencyDto);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
