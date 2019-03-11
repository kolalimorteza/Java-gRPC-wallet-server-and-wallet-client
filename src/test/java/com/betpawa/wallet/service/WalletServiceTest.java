package com.betpawa.wallet.service;


import com.betpawa.wallet.CURRENCY;
import com.betpawa.wallet.DepositRequest;
import com.betpawa.wallet.WalletServiceGrpc;
import com.betpawa.wallet.WalletServiceGrpc.WalletServiceBlockingStub;
import com.betpawa.wallet.WithdrawRequest;
import com.betpawa.wallet.dao.entity.Balance;
import com.betpawa.wallet.dao.repository.BalanceRepository;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.inprocess.InProcessChannelBuilder;
import io.grpc.inprocess.InProcessServerBuilder;
import io.grpc.testing.GrpcCleanupRule;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.io.IOException;
import java.math.BigDecimal;

import static junit.framework.TestCase.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class WalletServiceTest {

    /**
     * This rule manages automatic graceful shutdown for the registered servers and channels at the
     * end of test.
     */
    @Rule
    public final GrpcCleanupRule grpcCleanup = new GrpcCleanupRule();

    WalletServiceBlockingStub blockingStub;
    BalanceRepository balanceRepository = mock(BalanceRepository.class);
    final int userId = 1;
    Balance balance;

    @Before
    public void test() throws IOException {
        // Generate a unique in-process server name.
        String serverName = InProcessServerBuilder.generateName();

        WalletService walletService = new WalletService(balanceRepository);

        // Create a server, add service, start, and register for automatic graceful shutdown.
        grpcCleanup.register(InProcessServerBuilder
                .forName(serverName).directExecutor().addService(walletService).build().start());

        blockingStub = WalletServiceGrpc.newBlockingStub(
                // Create a client channel and register for automatic graceful shutdown.
                grpcCleanup.register(InProcessChannelBuilder.forName(serverName).directExecutor().build()));

        balance = new Balance();
        balance.setAmount(BigDecimal.valueOf(100));
        balance.setUserId(userId);
        balance.setCurrency("USD");

        when(balanceRepository.findByUserIdAndCurrency(userId, "USD")).thenReturn(balance);
    }

    @Test
    public void exceptionIfAmountIsLessThenZero() {
        try {
            blockingStub.withdraw(WithdrawRequest.newBuilder().setAmount(-1).build());
        } catch (StatusRuntimeException e) {
            assertEquals(Status.FAILED_PRECONDITION.getCode(), e.getStatus().getCode());
        }
    }

    @Test
    public void shouldWithdrawSuccess() {
        blockingStub.withdraw(WithdrawRequest.newBuilder()
                                             .setCurrency(CURRENCY.USD)
                                             .setAmount(100)
                                             .setUserId(userId)
                                             .build());

        assertEquals(BigDecimal.ZERO, balance.getAmount());
        verify(balanceRepository).save(balance);
    }

    @Test
    public void shouldDepositSuccess() {
        blockingStub.deposit(DepositRequest.newBuilder()
                                                    .setCurrency(CURRENCY.USD)
                                                    .setAmount(100)
                                                    .setUserId(userId)
                                                    .build());

        assertEquals(BigDecimal.valueOf(200), balance.getAmount());
        verify(balanceRepository).save(balance);
    }

    @Test(expected = StatusRuntimeException.class)
    public void exceptionIfInsufficientFounds() {
        blockingStub.withdraw(WithdrawRequest.newBuilder()
                                             .setCurrency(CURRENCY.USD)
                                             .setAmount(200)
                                             .setUserId(userId)
                                             .build());
    }

}