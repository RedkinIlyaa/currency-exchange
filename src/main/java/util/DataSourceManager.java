package util;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import exception.ConnectionException;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.sql.Connection;
import java.sql.SQLException;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class DataSourceManager {

    private static final HikariConfig hikariConfig = new HikariConfig();
    private static final HikariDataSource hihikariDataSource;

    static {
        hikariConfig.setJdbcUrl(PropertiesUtil.getUrl());
        hikariConfig.setUsername(PropertiesUtil.getUser());
        hikariConfig.setPassword(PropertiesUtil.getPassword());
        hikariConfig.setMaximumPoolSize(8);
        hikariConfig.setMinimumIdle(2);
        hikariConfig.setDriverClassName("org.postgresql.Driver");
        hihikariDataSource = new HikariDataSource(hikariConfig);
    }

    public static Connection getConnection() {
        try {
            return hihikariDataSource.getConnection();
        } catch (SQLException e) {
            throw new ConnectionException("Failed to obtain Connection from HikariCP pool", e);
        }
    }

}
