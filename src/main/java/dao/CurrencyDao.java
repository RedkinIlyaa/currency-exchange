package dao;

import entity.Currency;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import util.DataSourceManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CurrencyDao {

    @Getter
    private static final CurrencyDao currencyDao = new CurrencyDao();

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

    public List<Currency> getCurrenciesList() {

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
            throw new RuntimeException(e);
        }
    }

    public Optional<Currency> getCurrencyByCode(String code) {

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
            throw new RuntimeException(e);
        }
    }

    public Integer addNewCurrency(Currency currency) {

        try (Connection connection = DataSourceManager.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(ADD_NEW_CURRENCY)) {

            preparedStatement.setString(1, currency.getCode());
            preparedStatement.setString(2, currency.getFullName());
            preparedStatement.setString(3, currency.getSign());

            int executed = preparedStatement.executeUpdate();
            if (executed == 0)
                throw  new SQLException();

            ResultSet generatedKeys = preparedStatement.getGeneratedKeys();
            generatedKeys.next();
            return generatedKeys.getInt("id");

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
