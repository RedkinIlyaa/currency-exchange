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

    private static HikariDataSource hikariDataSource;

    public static void createHikariCP() {
        if (hikariDataSource != null && !hikariDataSource.isClosed())
            throw new IllegalStateException("HikariCP pool is already initialized");

        HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setJdbcUrl(PropertiesUtil.getUrl());
        hikariConfig.setUsername(PropertiesUtil.getUser());
        hikariConfig.setPassword(PropertiesUtil.getPassword());
        hikariConfig.setMaximumPoolSize(8);
        hikariConfig.setMinimumIdle(2);
        hikariConfig.setDriverClassName("org.postgresql.Driver");
        hikariConfig.setConnectionTimeout(2000);
        hikariDataSource = new HikariDataSource(hikariConfig);
    }

    public static Connection getConnection() {
        if (hikariDataSource == null)
            throw new IllegalStateException("HikariCP pool is not initialized");

        if (hikariDataSource.isClosed())
            throw new IllegalStateException("HikariCP pool is already closed");

        try {
            return hikariDataSource.getConnection();
        } catch (SQLException e) {
            throw new ConnectionException("Failed to obtain Connection from HikariCP pool", e);
        }
    }

    public static synchronized void closeHikariCP() {
        if (hikariDataSource != null) {
            hikariDataSource.close();
            hikariDataSource = null;
        }
    }
}
