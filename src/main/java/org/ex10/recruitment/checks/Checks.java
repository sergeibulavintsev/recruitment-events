package org.ex10.recruitment.checks;

import static java.util.Objects.requireNonNull;

public final class Checks {

    public static void checkThat(boolean condition, String message, Object... args) {
        if (!condition)
            throw new IllegalArgumentException(message.formatted(args));
    }

    public static void checkNotEmpty(String value, String message, Object... args) {
        checkThat(!requireNonNull(value).isEmpty(), message, args);
    }
}
