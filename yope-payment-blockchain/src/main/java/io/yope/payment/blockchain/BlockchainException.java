package io.yope.payment.blockchain;


public class BlockchainException extends Exception {
    private String errorCode;
    public BlockchainException(String message) {
        super(message);
    }

    public BlockchainException(Throwable cause) {
        super(cause);
    }

    public BlockchainException(Throwable cause, String errorCode) {
        super(cause);
    }


    public String getErrorCode() {
        return errorCode;
    }
}
