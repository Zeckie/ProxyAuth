package proxyauth.conf;

import java.io.Console;

/**
 * @author Zeckie
 * Copyright and licence details in Main.java
 */
public class Setting<A> {
    final A defaultValue;
    final Converter<A> converter;
    final String description;
    final Comparable<A> min;
    final Comparable<A> max;
    final Validator validator;
    final boolean special;
    protected A currentValue;

    /**
     * Configuration settings.
     *
     * @param defaultValue (Optional) Value that is used unless overridden by user. If null, user will be prompted for a value.
     * @param converter    (Required) Converter to convert between Strings and the configuration's class
     * @param special      (Required) Is this setting special (different handling for wizard, load, save)
     * @param description  (Required) Description that is displayed to the user when prompting for a value
     * @param validator    (Optional) Validator to check that the (user) supplied string is valid.
     * @param min          (Optional) Minimum value - values less than this will be rejected
     * @param max          (Optional) Maximum value - values greater than this will be rejected
     */
    protected Setting(A defaultValue, Converter<A> converter, boolean special, String description, Validator validator, Comparable<A> min, Comparable<A> max) {
        this.min = min;
        this.max = max;
        this.validator = validator;
        this.defaultValue = defaultValue;
        this.converter = converter;
        this.description = description;
        this.special = special;

        setValue(defaultValue);
    }

    /**
     * Ask the user to supply value for the setting
     */
    void prompt(String name, Console con) {
        boolean isValid;
        do {
            String def = (currentValue != null) ? (" (press ENTER for " + converter.toString(currentValue) + ")") : "";
            System.out.println(
                    "\n\n" + name + ": " + description +
                            "\n\nEnter " + name + def + ":"
            );
            String read = con.readLine();

            // Accepted current value
            if (read.equals("") && currentValue != null) return;

            isValid = true;
            try {
                setString(read);
            } catch (Exception ex) {
                System.err.println(ex);
                isValid = false;
            }

        } while (!isValid);
    }

    void setString(String s) throws InvalidSettingException {
        if (validator != null) validator.validate(s);
        setValue(converter.fromString(s));
    }

    /**
     * @return the current value of this setting, or null if it has not been set
     */
    public A getValue() {
        return currentValue;
    }

    public void setValue(A newValue) {
        if (min != null && min.compareTo(newValue) > 0) {
            throw new InvalidSettingException("less than minimum (" + min + ")");
        }
        if (max != null && max.compareTo(newValue) < 0) {
            throw new InvalidSettingException("greater than minimum (" + max + ")");
        }
        this.currentValue = newValue;
    }

    public String toString() {
        return "[Configuration val=" + this.currentValue + ", default=" + this.defaultValue + ", description=" + this.description + "]";
    }

    public String toUserString() {
        return converter.toString(currentValue);
    }

}

interface Validator {
    void validate(String val);
}

class InvalidSettingException extends RuntimeException {
    public InvalidSettingException(String message) {
        super(message);
    }

    public InvalidSettingException(String message, Exception cause) {
        super(message, cause);
    }
}