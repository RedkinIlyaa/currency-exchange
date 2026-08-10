package dao;


import entity.Currency;
import entity.ExchangeRate;
import exception.exist.ExchangeRateAlreadyExistsException;
import exception.ExchangeRateDaoException;
import exception.notfound.ExchangeRateNotFoundException;
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

    private static final String GET_EXCHANGE_RATE_BY_CURRENCIES_CODES = """
            SELECT er.id, base.id, base.code, base.full_name, base.sign, target.id, target.code, target.full_name, target.sign, er.rate
            FROM exchange_rates er
            INNER JOIN currencies base
            ON er.base_currency_id = base.id
            INNER JOIN currencies target
            ON er.target_currency_id = target.id
            WHERE base.code = ? AND target.code = ?
            """;

    private static final String UPDATE_EXCHANGE_RATE = """
            WITH updated_rate AS (
                UPDATE exchange_rates
                SET rate = ?
                WHERE base_currency_id = (SELECT id FROM currencies WHERE code = ?) AND
                      target_currency_id = (SELECT id FROM currencies WHERE code = ?)
                RETURNING id, base_currency_id, target_currency_id, rate
            )
            SELECT updated_rate.id,
                   base.id,
                   base.code,
                   base.full_name,
                   base.sign,
                   target.id,
                   target.code,
                   target.full_name,
                   target.sign,
                   updated_rate.rate
            FROM updated_rate
            INNER JOIN currencies base
            ON base.id = updated_rate.base_currency_id
            INNER JOIN currencies target
            ON target.id = updated_rate.target_currency_id;
            """;

    private static final String ADD_NEW_EXCHANGE_RATE = """
    WITH inserted_rate AS (
        INSERT INTO exchange_rates (base_currency_id, target_currency_id, rate)
        SELECT base.id, target.id, ?
        FROM currencies base
        CROSS JOIN currencies target
        WHERE base.code = ? AND target.code = ?
        RETURNING id, base_currency_id, target_currency_id, rate
    )
    SELECT ir.id,
           base.id,
           base.code,
           base.full_name,
           base.sign,
           target.id,
           target.code,
           target.full_name,
           target.sign,
           ir.rate
    FROM inserted_rate ir
             JOIN currencies base
                  ON base.id = ir.base_currency_id
             JOIN currencies target
                  ON target.id = ir.target_currency_id
    """;

    public List<ExchangeRate> getExchangeRatesList() {
        try (Connection connection = DataSourceManager.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(GET_ALL_EXCHANGE_RATES)) {

            List<ExchangeRate> exchangeRates = new ArrayList<>();
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                exchangeRates.add(
                        ExchangeRate.builder()
                                .id(resultSet.getInt("id"))
                                .baseCurrency(Currency.builder()
                                        .id(resultSet.getInt(2))
                                        .code(resultSet.getString(3))
                                        .fullName(resultSet.getString(4))
                                        .sign(resultSet.getString(5))
                                        .build()
                                )
                                .targetCurrency(Currency.builder()
                                        .id(resultSet.getInt(6))
                                        .code(resultSet.getString(7))
                                        .fullName(resultSet.getString(8))
                                        .sign(resultSet.getString(9))
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

    public Optional<ExchangeRate> getExchangeRateByCurrencyCodes(String baseCurrencyCode, String targetCurrencyCode) {
        try (Connection connection = DataSourceManager.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(GET_EXCHANGE_RATE_BY_CURRENCIES_CODES)) {

            preparedStatement.setString(1, baseCurrencyCode);
            preparedStatement.setString(2, targetCurrencyCode);

            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next())
                return Optional.of(
                        ExchangeRate.builder()
                                .id(resultSet.getInt("id"))
                                .baseCurrency(Currency.builder()
                                        .id(resultSet.getInt(2))
                                        .code(resultSet.getString(3))
                                        .fullName(resultSet.getString(4))
                                        .sign(resultSet.getString(5))
                                        .build()
                                )
                                .targetCurrency(Currency.builder()
                                        .id(resultSet.getInt(6))
                                        .code(resultSet.getString(7))
                                        .fullName(resultSet.getString(8))
                                        .sign(resultSet.getString(9))
                                        .build()
                                )
                                .rate(resultSet.getBigDecimal("rate"))
                                .build()
                );

            return Optional.empty();

        } catch (SQLException e) {
            throw new ExchangeRateDaoException("Failed to get ExchangeRate from db with: " + baseCurrencyCode + " and " + targetCurrencyCode, e);
        }
    }

    public Optional<ExchangeRate> updateExchangeRate(String baseCurrencyCode, String targetCurrencyCode, BigDecimal rate) {

        try (Connection connection = DataSourceManager.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(UPDATE_EXCHANGE_RATE)) {

            preparedStatement.setBigDecimal(1, rate);
            preparedStatement.setString(2, baseCurrencyCode);
            preparedStatement.setString(3, targetCurrencyCode);

            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                return Optional.ofNullable(ExchangeRate.builder()
                        .id(resultSet.getInt(1))
                        .baseCurrency(Currency.builder()
                                .id(resultSet.getInt(2))
                                .code(resultSet.getString(3))
                                .fullName(resultSet.getString(4))
                                .sign(resultSet.getString(5))
                                .build()
                        )
                        .targetCurrency(Currency.builder()
                                .id(resultSet.getInt(6))
                                .code(resultSet.getString(7))
                                .fullName(resultSet.getString(8))
                                .sign(resultSet.getString(9))
                                .build()
                        )
                        .rate(resultSet.getBigDecimal(10))
                        .build());
            } else {
                throw new ExchangeRateNotFoundException("One (or both) of the currencies in the currency pair does not exist in the database: " + baseCurrencyCode + " " + targetCurrencyCode);
            }
        } catch (SQLException e) {
            throw new ExchangeRateDaoException("Failed to update ExchangeRate in db", e);
        }
    }

    public Optional<ExchangeRate> addExchangeRate(String baseCurrencyCode, String targetCurrencyCode, BigDecimal rate) {

        try (Connection connection = DataSourceManager.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(ADD_NEW_EXCHANGE_RATE)) {

            preparedStatement.setBigDecimal(1, rate);
            preparedStatement.setString(2, baseCurrencyCode);
            preparedStatement.setString(3, targetCurrencyCode);

            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                return Optional.ofNullable(ExchangeRate.builder()
                        .id(resultSet.getInt(1))
                        .baseCurrency(Currency.builder()
                                .id(resultSet.getInt(2))
                                .code(resultSet.getString(3))
                                .fullName(resultSet.getString(4))
                                .sign(resultSet.getString(5))
                                .build()
                        )
                        .targetCurrency(Currency.builder()
                                .id(resultSet.getInt(6))
                                .code(resultSet.getString(7))
                                .fullName(resultSet.getString(8))
                                .sign(resultSet.getString(9))
                                .build()
                        )
                        .rate(resultSet.getBigDecimal(10))
                        .build());
            } else {
                throw new ExchangeRateNotFoundException("One (or both) of the currencies in the currency pair does not exist in the database: " + baseCurrencyCode + " " + targetCurrencyCode);
            }
        } catch (SQLException e) {

            if (e.getSQLState().equals("23505")) {
                throw new ExchangeRateAlreadyExistsException("Exchange rate with baseCurrencyCode = " + baseCurrencyCode + " and targetCurrencyCode = " + targetCurrencyCode + " is already exists.", e);
            }

            throw new ExchangeRateDaoException("Failed to add new ExchangeRate to db", e);
        }

    }

    public static ExchangeRateDao getInstance() {
        return exchangeRateDao;
    }
}
