package io.byzaneo.initializer;

public class RepositoryException extends RuntimeException {
    private static final long serialVersionUID = 671631366410845593L;

    public RepositoryException(String message) {
        super(message);
    }

    public RepositoryException(String message, Throwable cause) {
        super(message, cause);
    }
}
