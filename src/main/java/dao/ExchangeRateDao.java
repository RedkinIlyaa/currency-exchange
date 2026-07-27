package dao;


import entity.Currency;
import entity.ExchangeRate;
import exception.CurrencyDaoException;
import exception.ExchangeRateDaoException;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import util.DataSourceManager;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ExchangeRateDao {

    private static final ExchangeRateDao exchangeRateDao = new ExchangeRateDao();

    private static final String GET_ALL_EXCHANGE_RATES = """
            SELECT er.id, base.id, base.code, base.full_name, base.sign, target.id, target.code, target.full_name, target.sign, er.rate
            FROM exchange_rates er
            INNER JOIN currencies base
            ON er.base_currency_id = base.id
            INNER JOIN currencies target
            ON er.target_currency_id = target.id
            """;

    private static final String GET_EXCHANGE_RATE_BY_ID = """
            SELECT er.id, er.base_currency_id, er.target_currency_id, er.rate
            FROM exchange_rates er
            WHERE base_currency_id = ? AND target_currency_id = ?
            """;

    private static final String UPDATE_EXCHANGE_RATE = """
            UPDATE exchange_rates
            SET rate = ?
            WHERE base_currency_id = ? AND target_currency_id = ?
            """;

    private static final String ADD_NEW_EXCHANGE_RATE = """
            INSERT INTO exchange_rates (base_currency_id, target_currency_id, rate)
            VALUES (
                ?,
                ?,
                ?
            )
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
                                .baseCurrency(Currency.builder()
                                        .id(resultSet.getInt("id"))
                                        .code(resultSet.getString("code"))
                                        .fullName(resultSet.getString("full_name"))
                                        .sign(resultSet.getString("sign"))
                                        .build()
                                )
                                .targetCurrency(Currency.builder()
                                        .id(resultSet.getInt("id"))
                                        .code(resultSet.getString("code"))
                                        .fullName(resultSet.getString("full_name"))
                                        .sign(resultSet.getString("sign"))
                                        .build()
                                )
                                .rate(resultSet.getBigDecimal("rate"))
                                .build()
                );
            }

            return exchangeRates;

        } catch (SQLException e) {
            throw new ExchangeRateDaoException("Failed to get List<ExchangeRate> from db", e);
        }
    }

    public Optional<ExchangeRate> getExchangeRate(Integer baseCurrencyId, Integer targetCurrencyId) {
        try (Connection connection = DataSourceManager.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(GET_EXCHANGE_RATE_BY_ID);) {

            preparedStatement.setInt(1, baseCurrencyId);
            preparedStatement.setInt(2, targetCurrencyId);

            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next())
                return Optional.of(
                        ExchangeRate.builder()
                        .id(resultSet.getInt("er.id"))
                        .baseCurrency(Currency.builder()
                                .id(resultSet.getInt("base.id"))
                                .code(resultSet.getString("base.code"))
                                .fullName(resultSet.getString("base.full_name"))
                                .sign(resultSet.getString("base.sign"))
                                .build()
                        )
                        .targetCurrency(Currency.builder()
                                .id(resultSet.getInt("target.id"))
                                .code(resultSet.getString("target.code"))
                                .fullName(resultSet.getString("target.full_name"))
                                .sign(resultSet.getString("target.sign"))
                                .build()
                        )
                        .rate(resultSet.getBigDecimal("rate"))
                        .build()
                );

            return Optional.empty();

        } catch (SQLException e) {
            throw new ExchangeRateDaoException("Failed to get ExchangeRate from db", e);
        }
    }

    public int updateExchangeRate(Integer baseCurrencyId, Integer targetCurrencyId, BigDecimal rate) {

        try (Connection connection = DataSourceManager.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(UPDATE_EXCHANGE_RATE);) {

            preparedStatement.setBigDecimal(1, rate);
            preparedStatement.setInt(2, baseCurrencyId);
            preparedStatement.setInt(3, targetCurrencyId);

            return preparedStatement.executeUpdate();

        } catch (SQLException e) {
            throw new ExchangeRateDaoException("Failed to update ExchangeRate in db", e);
        }
    }

    public Integer addExchangeRate(Integer baseCurrencyId, Integer targetCurrencyId, BigDecimal rate) {

        try (Connection connection = DataSourceManager.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(ADD_NEW_EXCHANGE_RATE,
                     Statement.RETURN_GENERATED_KEYS);) {

            preparedStatement.setInt(1, baseCurrencyId);
            preparedStatement.setInt(2, targetCurrencyId);
            preparedStatement.setBigDecimal(3, rate);

            int executedUpdate = preparedStatement.executeUpdate();
            if (executedUpdate == 0)
                throw new CurrencyDaoException("Add 0 exchangeRate to db");

            ResultSet generatedKeys = preparedStatement.getGeneratedKeys();
            if (!generatedKeys.next())
                throw new CurrencyDaoException("Failed to get generatedKey(id of ExchangeRate");

            return generatedKeys.getInt("id");

        } catch (SQLException e) {
            throw new CurrencyDaoException("Failed to add new ExchangeRate   to db", e);
        }

    }

    public static ExchangeRateDao getInstance() {
        return exchangeRateDao;
    }
}
