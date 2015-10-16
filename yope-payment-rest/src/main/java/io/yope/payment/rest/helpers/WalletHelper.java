/**
 *
 */
package io.yope.payment.rest.helpers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import groovy.util.logging.Slf4j;
import io.yope.payment.services.WalletService;

/**
 * @author massi
 *
 */
@Slf4j
@Service
public class WalletHelper {

    @Autowired
    private WalletService walletService;

}