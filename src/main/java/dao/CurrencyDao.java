package dao;

import entity.Currency;
import exception.exist.CurrencyAlreadyExistsException;
import org.postgresql.util.PSQLException;
import exception.CurrencyDaoException;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.postgresql.util.ServerErrorMessage;
import util.DataSourceManager;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CurrencyDao {

    private static final CurrencyDao currencyDao = new CurrencyDao();
    private static final String UNIQUE_VIOLATION_SQL_STATE = "23505";
    private static final String CODE_UNIQUE_CONSTRAINT =  "currencies_code_key";
    private static final String FULL_NAME_UNIQUE_CONSTRAINT = "currencies_full_name_key";

    private static final String GET_ALL_CURRENCIES = """
            SELECT c.id, c.code, c.full_name, c.sign
            FROM currencies c
            """;

    private static final String GET_CURRENCY_BY_CODE = """
            SELECT c.id, c.code, c.full_name, c.sign
            FROM currencies c
            WHERE c.code = ?
            """;

    private static final String ADD_NEW_CURRENCY = """
            INSERT INTO currencies (code, full_name, sign)
            VALUES (?, ?, ?);
            """;

    public List<Currency> findAll() {

        try (Connection connection = DataSourceManager.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(GET_ALL_CURRENCIES)) {

            List<Currency> currencyList = new ArrayList<>();
            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                currencyList.add(
                        Currency.builder()
                                .id(resultSet.getInt("id"))
                                .code(resultSet.getString("code"))
                                .fullName(resultSet.getString("full_name"))
                                .sign(resultSet.getString("sign"))
                                .build()
                );
            }

            return currencyList;

        } catch (SQLException e) {
            throw new CurrencyDaoException("Failed to get List<Currency> from db", e);
        }
    }

    public Optional<Currency> findByCode(String code) {

        try (Connection connection = DataSourceManager.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(GET_CURRENCY_BY_CODE)) {

            preparedStatement.setString(1, code);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next())
                return Optional.of(
                        Currency.builder()
                                .id(resultSet.getInt("id"))
                                .code(resultSet.getString("code"))
                                .fullName(resultSet.getString("full_name"))
                                .sign(resultSet.getString("sign"))
                                .build()
                );

            return Optional.empty();

        } catch (SQLException e) {
            throw new CurrencyDaoException("Failed to get Currency from db", e);
        }
    }

    public Integer save(Currency currency) {

        try (Connection connection = DataSourceManager.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(ADD_NEW_CURRENCY,
                     Statement.RETURN_GENERATED_KEYS)) {

            preparedStatement.setString(1, currency.getCode());
            preparedStatement.setString(2, currency.getFullName());
            preparedStatement.setString(3, currency.getSign());

            int executed = preparedStatement.executeUpdate();
            if (executed == 0)
                throw new CurrencyDaoException("Add 0 currency to db");

            ResultSet generatedKeys = preparedStatement.getGeneratedKeys();
            if (!generatedKeys.next())
                throw new CurrencyDaoException("Failed to obtain generated id of Currency");

            return generatedKeys.getInt("id");

        } catch (SQLException e) {
            if (!UNIQUE_VIOLATION_SQL_STATE.equals(e.getSQLState())) {
                throw new CurrencyDaoException("Failed to add new Currency to db", e);
            }

            String constraint = null;

            if (e instanceof PSQLException psqlException) {
                ServerErrorMessage serverErrorMessage = psqlException.getServerErrorMessage();
                if (serverErrorMessage != null) {
                    constraint = serverErrorMessage.getConstraint();
                }
            }

            if (CODE_UNIQUE_CONSTRAINT.equals(constraint)) {
                throw new CurrencyAlreadyExistsException("Currency with code = " + currency.getCode() + " already exists", e);
            }

            if (FULL_NAME_UNIQUE_CONSTRAINT.equals(constraint)){
                throw new CurrencyAlreadyExistsException("Currency with name = " + currency.getFullName() + " already exists", e);
            }

            throw new CurrencyAlreadyExistsException("Currency already exists", e);
        }
    }

    public static CurrencyDao getInstance() {
        return currencyDao;
    }
}
