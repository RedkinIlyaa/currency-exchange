package servlet;

import com.fasterxml.jackson.databind.ObjectMapper;
import dto.ExchangeRateDto;
import exception.invalid.*;
import exception.notfound.ExchangeRateNotFoundException;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import service.ExchangeRateService;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.Optional;

@WebServlet(value = "/exchangeRate/*")
public class ExchangeRatePairServlet extends HttpServlet {
    private final ExchangeRateService exchangeRateService = ExchangeRateService.getInstance();
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final int MAX_PATCH_BODY_LENGTH = 64;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) {
        String pathInfo = req.getPathInfo();

        if (pathInfo == null || pathInfo.equals("/")) {
            // Коды валют пары отсутствуют в адресе - 400
            throw new InvalidExchangeRatePairException("Currency codes are missing");
        }

        String pathInfoWithoutSlash = pathInfo.substring(1);
        if (pathInfoWithoutSlash.length() != 6 || !pathInfoWithoutSlash.matches("[a-zA-Z]{6}")) {
            throw new InvalidExchangeRatePairException("Currency pair must contain exactly 6 Latin letters");
        }

        String firstCurrencyCode = pathInfoWithoutSlash.substring(0, 3);
        String secondCurrencyCode = pathInfoWithoutSlash.substring(3);
        Optional<ExchangeRateDto> exchangeRateDto = exchangeRateService
                .exchangeRateDtoByCurrenciesCodes(firstCurrencyCode, secondCurrencyCode);

        if (exchangeRateDto.isEmpty())
            // Обменный курс для пары не найден - 404
            throw new ExchangeRateNotFoundException("Exchange rate not found");

        try {
            ServletOutputStream outputStream = resp.getOutputStream();
            objectMapper.writeValue(outputStream, exchangeRateDto.get());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected void doPatch(HttpServletRequest req, HttpServletResponse resp) throws UnsupportedEncodingException {
        String contentType = req.getContentType();

        String mediaType = contentType == null
                ? null
                : contentType.split(";", 2)[0].trim();

        if (!"application/x-www-form-urlencoded".equalsIgnoreCase(mediaType)) {
            throw new InvalidException("This request might have header Content-Type - application/x-www-form-urlencoded");
        }

        req.setCharacterEncoding("UTF-8");
        if (isParameterFromUrl(req))
            throw new InvalidException("Parameters in PATCH request to /exchangeRate/ must be in body and have 'application/x-www-form-urlencoded' media type");

        String pathInfo = req.getPathInfo();
        if (pathInfo == null || pathInfo.equals("/")) {
            throw new InvalidExchangeRatePairException("Currencies codes are missing");
        }

        String pathInfoWithoutSlash = pathInfo.substring(1);
        if (pathInfoWithoutSlash.length() != 6 || !pathInfoWithoutSlash.matches("[a-zA-Z]{6}")) {
            throw new InvalidExchangeRatePairException("Currency pair must contain exactly 6 latin letters");
        }
        String firstCurrency = pathInfoWithoutSlash.substring(0, 3);
        String secondCurrency = pathInfoWithoutSlash.substring(3);

        String rate = getFormParameter(req);

        Optional<ExchangeRateDto> exchangeRateDto = exchangeRateService.patchToExchangeRate(firstCurrency.toUpperCase(Locale.ENGLISH), secondCurrency.toUpperCase(Locale.ENGLISH), rate);

        if (exchangeRateDto.isEmpty()) {
            throw new ExchangeRateNotFoundException("Exchange rate not found");
        }

        try {
            ServletOutputStream outputStream = resp.getOutputStream();
            objectMapper.writeValue(outputStream, exchangeRateDto.get());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private String getFormParameter(HttpServletRequest req) {
        String body = readRequestBody(req);

        if (body.isEmpty()
            || body.indexOf('\n') >= 0
            || body.indexOf('\r') >= 0) {
            throw new InvalidCountOfBodyLinesException(
                    "Body should contain exactly one line"
            );
        }

        String[] parameters = body.split("&", -1);

        if (parameters.length != 1) {
            throw new InvalidCountOfBodyParametersException(
                    "Body should contain exactly one key-value pair"
            );
        }

        String[] keyAndValue = parameters[0].split("=", 2);

        if (keyAndValue.length != 2) {
            throw new InvalidException(
                    "Body parameter doesn't have a value"
            );
        }

        try {
            String decodedKey = URLDecoder.decode(
                    keyAndValue[0],
                    StandardCharsets.UTF_8
            );

            String decodedValue = URLDecoder.decode(
                    keyAndValue[1],
                    StandardCharsets.UTF_8
            );

            if (!decodedKey.equals("rate")) {
                throw new InvalidNameOfBodyParameterException(
                        "The only allowed body parameter is 'rate'"
                );
            }

            return decodedValue;
        } catch (IllegalArgumentException e) {
            throw new InvalidException(
                    "Body contains invalid URL encoding"
            );
        }
    }

    private String readRequestBody(HttpServletRequest req) {
        if (req.getContentLengthLong() > MAX_PATCH_BODY_LENGTH) {
            throw new InvalidException("Request body is too large");
        }

        try {
            BufferedReader reader = req.getReader();
            StringBuilder body = new StringBuilder();
            char[] buffer = new char[32];

            int read;

            while ((read = reader.read(buffer)) != -1) {
                if (body.length() + read > MAX_PATCH_BODY_LENGTH) {
                    throw new InvalidException("Request body is too large");
                }

                body.append(buffer, 0, read);
            }

            return body.toString();
        } catch (IOException e) {
            throw new RuntimeException("Failed to read request body", e);
        }
    }

    private boolean isParameterFromUrl(HttpServletRequest httpServletRequest) {
        String queryString = httpServletRequest.getQueryString();
        return queryString != null && !queryString.isBlank();
    }
}
