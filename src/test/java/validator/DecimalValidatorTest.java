package validator;


import exception.invalid.InvalidException;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.*;

import static validator.DecimalValidator.parseAmount;
import static validator.DecimalValidator.parseRate;


public class DecimalValidatorTest {
    private static final String INVALID_RATE_FORMAT_MESSAGE =
            "Rate must contain from 1 to 6 integer digits and up to 6 fractional digits, without exponent";

    private static final String INVALID_AMOUNT_FORMAT_MESSAGE =
            "Amount must contain from 1 to 18 integer digits and up to 6 fractional digits, without exponent";

    @ParameterizedTest
    @NullAndEmptySource
    void parseRateNullOrEmpty(String value) {
        assertThatThrownBy(() -> parseRate(value))
                .isInstanceOf(InvalidException.class)
                .hasMessage(INVALID_RATE_FORMAT_MESSAGE)
                .hasNoCause();
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "1234567.123456",
            "123456.1234567",
            "-0000001.798",
            "1000000.000000",
            "0.0000001"
    })
    void parseRateWithSevenDigitsBeforeAndAfterDot(String value) {
        assertThatThrownBy(() -> parseRate(value))
                .isInstanceOf(InvalidException.class)
                .hasMessage(INVALID_RATE_FORMAT_MESSAGE)
                .hasNoCause();
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "12345o.123456",
            "123456.12345e",
            "4.5e4",
            "1234..56",
            "1.982/2",
            "1234,5678",
            "1.",
            ".5",
            "+5",
            " 5",
            "5 "
    })
    void parseRateWithLettersAndOtherSymbols(String value) {
        assertThatThrownBy(() -> parseRate(value))
                .isInstanceOf(InvalidException.class)
                .hasMessage(INVALID_RATE_FORMAT_MESSAGE)
                .hasNoCause();
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "-00000.798",
            "0.000000",
            "-9.12131",
            "-0.000001",
            "000000.000000",
            "0"
    })
    void parseRateEqualToZeroOrLess(String value) {
        assertThatThrownBy(() -> parseRate(value))
                .isInstanceOf(InvalidException.class)
                .hasMessage("Number must be greater than zero")
                .hasNoCause();
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "000000.798",
            "982732.121",
            "0.191",
            "23842.278",
            "975.253",
            "999999.999999",
            "000000.000001",
            "0.000001",
            "999999",
            "5.987652"
    })
    void shouldParseValidRate(String value) {
        assertThat(parseRate(value))
                .isEqualByComparingTo(new BigDecimal(value))
                .isNotNegative();
    }

    @ParameterizedTest
    @NullAndEmptySource
    void parseAmountNullOrEmpty(String value) {
        assertThatThrownBy(() -> parseAmount(value))
                .isInstanceOf(InvalidException.class)
                .hasMessage(INVALID_AMOUNT_FORMAT_MESSAGE)
                .hasNoCause();
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "1234567891234567890.123456",
            "123456.1234567",
            "0.0000001",
            "0000000000000000000.1",
            "1.1234567",
            "9999999999999999990.999999",
            "999999999999999999.9999990"
    })
    void parseAmountWithNineteenDigitsBeforeAndSevenAfterDot(String value) {
        assertThatThrownBy(() -> parseAmount(value))
                .isInstanceOf(InvalidException.class)
                .hasMessage(INVALID_AMOUNT_FORMAT_MESSAGE)
                .hasNoCause();
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "1e7",
            "123456,123456",
            "123/65346",
            "87676..12",
            "12345678901234567.31,"
    })
    void parseAmountWithLettersAndOtherSymbols(String value) {
        assertThatThrownBy(() -> parseAmount(value))
                .isInstanceOf(InvalidException.class)
                .hasMessage(INVALID_AMOUNT_FORMAT_MESSAGE)
                .hasNoCause();
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "000000000000000000.000000",
            "-9.12131",
            "-0.000001",
            "-12345.123456",
            "0"
    })
    void parseAmountEqualToZeroOrLess(String value) {
        assertThatThrownBy(() -> parseAmount(value))
                .isInstanceOf(InvalidException.class)
                .hasMessage("Number must be greater than zero")
                .hasNoCause();
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "999999999999999999.999999",
            "000000000000000000.000001",
            "123456.123456",
            "456"
    })
    void shouldParseValidAmount(String value) {
        assertThat(parseAmount(value))
                .isEqualByComparingTo(new BigDecimal(value))
                .isNotNegative();
    }
}
