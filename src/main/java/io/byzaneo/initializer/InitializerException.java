package io.byzaneo.initializer;

import static java.lang.String.format;

public class InitializerException extends RuntimeException {
    private static final long serialVersionUID = 671631366410845593L;

    public InitializerException(String message, Object... args) {
        super(format(message, args));
    }

    public InitializerException(Throwable cause, String message, Object... args) {
        super(format(message, args), cause);
    }
}
