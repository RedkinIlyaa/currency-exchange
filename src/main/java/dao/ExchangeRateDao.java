package dao;


import entity.ExchangeRate;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import util.DataSourceManager;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ExchangeRateDao {

    private static final ExchangeRateDao exchangeRateDao = new ExchangeRateDao();

    private static final String GET_ALL_EXCHANGE_RATES = """
            SELECT er.id, er.base_currency_id, er.target_currency_id, er.rate
            FROM exchange_rates er
            """;

    private static final String GET_EXCHANGE_RATE_BY_ID = """
            SELECT er.id, er.base_currency_id, er.target_currency_id, er.rate
            FROM exchange_rates er
            WHERE base_currency_id = ? AND target_currency_id = ?
            """;

    private static final String UPDATE_EXCHANGE_RATE = """
            UPDATE exchange_rate
            SET rate = ?
            WHERE base_currency_id = ? AND target_currency_id = ?
            """;

    public List<ExchangeRate> getExchangeRatesList() {
        try (Connection connection = DataSourceManager.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(GET_ALL_EXCHANGE_RATES);) {

            List<ExchangeRate> exchangeRates = new ArrayList<>();
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                exchangeRates.add(
                        ExchangeRate.builder()
                                .id(resultSet.getInt("id"))
                                .baseCurrencyId(resultSet.getInt("base_currency_id"))
                                .targetCurrencyId(resultSet.getInt("target_currency_id"))
                                .rate(resultSet.getBigDecimal("rate"))
                                .build()
                );
            }

            return exchangeRates;

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public Optional<ExchangeRate> getExchangeRate(Integer base_currency_id, Integer target_currency_id) {
        try (Connection connection = DataSourceManager.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(GET_EXCHANGE_RATE_BY_ID);) {

            preparedStatement.setInt(1, base_currency_id);
            preparedStatement.setInt(2, target_currency_id);

            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next())
                return Optional.of(ExchangeRate.builder()
                        .id(resultSet.getInt("id"))
                        .baseCurrencyId(resultSet.getInt("base_currency_id"))
                        .targetCurrencyId(resultSet.getInt("target_currency_id"))
                        .rate(resultSet.getBigDecimal("rate"))
                        .build()
                );

            return Optional.empty();

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public int updateExchangeRate(Integer base_currency_id, Integer target_currency_id, BigDecimal rate) {

        try (Connection connection = DataSourceManager.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(UPDATE_EXCHANGE_RATE);) {

            preparedStatement.setBigDecimal(1, rate);
            preparedStatement.setInt(2, base_currency_id);
            preparedStatement.setInt(3, target_currency_id);

            int executedUpdate = preparedStatement.executeUpdate();
            if (executedUpdate == 0)
                throw new SQLException();

            return executedUpdate;

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

}
