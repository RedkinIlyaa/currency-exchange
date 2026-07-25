package filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import exception.CurrencyNotFoundException;
import exception.InvalidCurrencyCodeException;
import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

@WebFilter(value = "/*")
public class ExceptionHandlingFilter implements Filter {
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final Logger logger = LoggerFactory.getLogger(ExceptionHandlingFilter.class);

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletResponse httpServletResponse = (HttpServletResponse) response;
        httpServletResponse.setContentType("application/json");
        httpServletResponse.setCharacterEncoding(StandardCharsets.UTF_8.name());

        try {
            chain.doFilter(request, response);
        } catch (InvalidCurrencyCodeException invalidCurrencyCodeException) {
            httpServletResponse.reset();
            httpServletResponse.setCharacterEncoding(StandardCharsets.UTF_8.name());
            httpServletResponse.setContentType("application/json");
            httpServletResponse.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            Map<String, String> map = new HashMap<>();
            map.put("message", invalidCurrencyCodeException.getMessage());
            objectMapper.writeValue(httpServletResponse.getOutputStream(), map);
        } catch (CurrencyNotFoundException currencyNotFoundException) {
            httpServletResponse.reset();
            httpServletResponse.setCharacterEncoding(StandardCharsets.UTF_8.name());
            httpServletResponse.setContentType("application/json");
            httpServletResponse.setStatus(HttpServletResponse.SC_NOT_FOUND);
            Map<String, String> map = new HashMap<>();
            map.put("message", currencyNotFoundException.getMessage());
            objectMapper.writeValue(httpServletResponse.getOutputStream(), map);
        } catch (RuntimeException runtimeException) {

            if (httpServletResponse.isCommitted())
                throw runtimeException;

            httpServletResponse.reset();
            httpServletResponse.setCharacterEncoding(StandardCharsets.UTF_8.name());
            httpServletResponse.setContentType("application/json");
            httpServletResponse.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            Map<String, String> map = new HashMap<>();
            map.put("message", "Internal server error");
            objectMapper.writeValue(httpServletResponse.getOutputStream(), map);
            logger.error("Unhandled exception during request processing", runtimeException);
        }
    }
}