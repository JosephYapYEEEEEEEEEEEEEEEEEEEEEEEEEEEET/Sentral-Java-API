package io.github.josephyapyeeeeeeeeeeeeeeeeeeeeeeeeeeeet.sdiofhfh79;

public class StatusException extends RuntimeException {
    private final int code;
    public StatusException(int code) {
        this.code = code;
    }

    public StatusException(String message, int code) {
        super(message);
        this.code = code;
    }

    public StatusException(String message, Throwable cause, int code) {
        super(message, cause);
        this.code = code;
    }

    public StatusException(Throwable cause, int code) {
        super(cause);
        this.code = code;
    }

    public StatusException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace, int code) {
        super(message, cause, enableSuppression, writableStackTrace);
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}
