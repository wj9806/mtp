package com.mtp.core.mtp;

public class MtpException extends RuntimeException {

    public MtpException(String message, Throwable cause) {
        super(message, cause);
    }

    public MtpException(String message) {
        super(message);
    }

    public MtpException(Throwable cause) {
        super(cause);
    }

    public MtpException() {
        super();
    }
}
