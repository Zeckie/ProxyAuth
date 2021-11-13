package proxyauth.conf;

import java.util.Set;

/**
 * Convert between user readable / supplied string, and another type
 *
 * @author Zeckie
 * Copyright and licence details in Main.java
 */
abstract class Converter<A> {

    abstract A fromString(String val) throws InvalidSettingException;

    String toString(A val) {
        return val.toString();
    }

    public static final Converter<Boolean> YES_NO = new Converter<>() {
        static final Set<String> YES = Set.of("yes", "y", "true");
        static final Set<String> NO = Set.of("no", "n", "false");

        @Override
        Boolean fromString(String val) throws InvalidSettingException {
            String lower = val.toLowerCase();
            if (YES.contains(lower)) {
                return true;
            }
            if (NO.contains(lower)) {
                return false;
            }
            throw new InvalidSettingException("Value should be 'Yes' or 'No'");
        }

        @Override
        String toString(Boolean val) {
            return val ? "Yes" : "No";
        }
    };
    static final Converter<Integer> INTEGER = new Converter<>() {
        @Override
        Integer fromString(String val) {
            try {
                return Integer.parseInt(val);
            } catch (NumberFormatException nfe) {
                throw new InvalidSettingException("Value should be an integer", nfe);
            }
        }
    };
    static final Converter<String> STRING = new Converter<>() {
        @Override
        String fromString(String val) {
            return val;
        }
    };

    private Converter() {
    }
}
