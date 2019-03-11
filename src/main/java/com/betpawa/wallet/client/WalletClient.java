package com.betpawa.wallet.client;

import com.betpawa.wallet.BalanceRequest;
import com.betpawa.wallet.CURRENCY;
import com.betpawa.wallet.DepositRequest;
import com.betpawa.wallet.WalletServiceGrpc;
import com.betpawa.wallet.WalletServiceGrpc.WalletServiceBlockingStub;
import com.betpawa.wallet.WithdrawRequest;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class WalletClient {
    private static final Logger log = LoggerFactory.getLogger(WalletClient.class);

    private final WalletServiceBlockingStub blockingStub;

    public WalletClient(String host, int port) {
        ManagedChannel channel = ManagedChannelBuilder.forAddress(host, port)
                                                      .usePlaintext()
                                                      .build();

        this.blockingStub = WalletServiceGrpc.newBlockingStub(channel);
    }

    public void deposit(int userId, float amount, String currency) {
        log.info("deposit of {} {} to user with id {}", currency, amount, userId);
        DepositRequest depositRequest = DepositRequest.newBuilder()
                                                      .setUserId(userId)
                                                      .setAmount(amount)
                                                      .setCurrency(CURRENCY.valueOf(currency))
                                                      .build();
        try {
            blockingStub.deposit(depositRequest);
        } catch (StatusRuntimeException e) {
            log.error(e.getStatus() != null ? e.getStatus().getDescription() : e.getMessage());
        }

    }

    public void withdraw(int userId, float amount, String currency) {
        log.info("withdrawal of {} {} for user with id {}", currency, amount, userId);
        WithdrawRequest withdrawRequest = WithdrawRequest.newBuilder()
                                                         .setUserId(userId)
                                                         .setAmount(amount)
                                                         .setCurrency(CURRENCY.valueOf(currency))
                                                         .build();
        try {
            blockingStub.withdraw(withdrawRequest);
        } catch (StatusRuntimeException e) {
            log.error(e.getStatus() != null ? e.getStatus().getDescription() : e.getMessage());
        }
    }

    public String getBalance(int userId) {
        log.info("get balance for user with id {}", userId);
        BalanceRequest balanceRequest = BalanceRequest.newBuilder()
                                                      .setUserId(userId)
                                                      .build();

        String balanceReport = blockingStub.balance(balanceRequest)
                                           .getBalanceList()
                                           .stream()
                                           .map(b -> String.format("%s %s", b.getCurrency(), b.getAmount()))
                                           .sorted()
                                           .collect(Collectors.joining("\n"));

        log.info(balanceReport);
        return balanceReport;
    }

}
