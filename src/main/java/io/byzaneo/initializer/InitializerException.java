package io.byzaneo.initializer;

public class InitializerException extends RuntimeException {
    private static final long serialVersionUID = 671631366410845593L;

    public InitializerException(String message) {
        super(message);
    }

    public InitializerException(String message, Throwable cause) {
        super(message, cause);
    }
}
