package filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@WebFilter(value = "/currencies")
public class ExceptionHandlingFilter implements Filter {
    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletResponse httpServletResponse = (HttpServletResponse) response;
        httpServletResponse.setCharacterEncoding("UTF-8");

        try {
            chain.doFilter(request, response);
        } catch (RuntimeException runtimeException) {

            if (httpServletResponse.isCommitted())
                throw runtimeException;

            httpServletResponse.reset();
            httpServletResponse.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            httpServletResponse.setContentType("application/json");
            Map<String, String> map = new HashMap<>();
            map.put("message", "Internal server error");
            objectMapper.writeValue(httpServletResponse.getOutputStream(), map);
        }
    }
}