package validator;

import exception.invalid.InvalidException;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.regex.Pattern;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class DecimalValidator {

    private static final Pattern RATE_PATTERN = Pattern.compile("[0-9]{1,6}(?:\\.[0-9]{1,6})?");

    private static final Pattern AMOUNT_PATTERN = Pattern.compile("[0-9]{1,18}(?:\\.[0-9]{1,6})?");

    public static BigDecimal parseRate(String value) {
        return parsePositiveDecimal(
                value,
                RATE_PATTERN,
                "Rate must contain from 1 to 6 integer digits and up to 6 fractional digits, without exponent"
        );
    }

    public static BigDecimal parseAmount(String value) {
        return parsePositiveDecimal(
                value,
                AMOUNT_PATTERN,
                "Amount must contain from 1 to 18 integer digits and up to 6 fractional digits, without exponent"
        );
    }

    private static BigDecimal parsePositiveDecimal(String value, Pattern pattern, String formatErrorMessage) {
        if (value == null || !pattern.matcher(value).matches()) {
            throw new InvalidException(formatErrorMessage);
        }

        BigDecimal number = new BigDecimal(value);

        if (number.signum() <= 0) {
            throw new InvalidException("Number must be greater than zero");
        }

        return number;
    }
}
