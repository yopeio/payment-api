package io.yope.payment.services;

public interface ExternalServiceAuthenticator {

    AuthenticationWithToken authenticate(String username, String password);
}
