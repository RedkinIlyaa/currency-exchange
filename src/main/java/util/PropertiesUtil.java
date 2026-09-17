package util;

import exception.ConfigurationException;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.Properties;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class PropertiesUtil {

    private static final Properties PROPERTIES = new Properties();
    private static final Logger logger = LoggerFactory.getLogger(PropertiesUtil.class);

    static  {
        try (InputStream inputStream = PropertiesUtil.class
                .getResourceAsStream("/application.properties")
        ) {
            if (inputStream == null) {
                throw new ConfigurationException("application.properties was not found");
            }
            PROPERTIES.load(inputStream);
            logger.debug("application.properties loaded successfully");

            } catch (IOException e) {
                throw new ConfigurationException("Failed to load application.properties", e);
            }
    }

    public static String getUrl() {
        return getRequiredProperty("db.url");
    }

    public static String getUser() {
        return getRequiredProperty("db.user");
    }

    public static String getPassword() {
        return getRequiredProperty("db.password");
    }

    private static String getRequiredProperty(String key) {
        String value = PROPERTIES.getProperty(key);

        if (value == null || value.isBlank()) {
            throw new ConfigurationException(
                    "Required property '" + key + "' is missing"
            );
        }

        return value;
    }
}