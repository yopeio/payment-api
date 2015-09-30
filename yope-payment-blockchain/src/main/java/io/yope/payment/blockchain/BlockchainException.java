package io.yope.payment.blockchain;


public class BlockchainException extends Exception {
    public BlockchainException(String message) {
        super(message);
    }

    public BlockchainException(Throwable cause) {
        super(cause);
    }
}
