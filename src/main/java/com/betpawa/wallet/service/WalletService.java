package com.betpawa.wallet.service;


import com.betpawa.wallet.BalanceRequest;
import com.betpawa.wallet.BalanceResponse;
import com.betpawa.wallet.CURRENCY;
import com.betpawa.wallet.DepositRequest;
import com.betpawa.wallet.Empty;
import com.betpawa.wallet.WalletServiceGrpc.WalletServiceImplBase;
import com.betpawa.wallet.WithdrawRequest;
import com.betpawa.wallet.dao.entity.Balance;
import com.betpawa.wallet.dao.repository.BalanceRepository;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class WalletService extends WalletServiceImplBase {

    private static final Logger log = LoggerFactory.getLogger(WalletService.class);

    private final BalanceRepository repository;
    private final ConcurrentHashMap<String, Lock> locks = new ConcurrentHashMap<>();

    public WalletService(BalanceRepository repository) {
        this.repository = repository;
    }

    @Override
    public void deposit(DepositRequest request, StreamObserver<Empty> responseObserver) {
        log.info("deposit of {} {} to user with id {}",
                request.getCurrency(), request.getAmount(), request.getUserId());

        Lock balanceLock = null;
        try {
            checkDepositRequest(request);
            balanceLock = getLock(request.getUserId(), request.getCurrency());
            balanceLock.lock();

            Balance balance = repository.findByUserIdAndCurrency(request.getUserId(), request.getCurrency().name());
            balance.setAmount(balance.getAmount().add(new BigDecimal(request.getAmount())));
            repository.save(balance);

            responseObserver.onNext(Empty.getDefaultInstance());
            responseObserver.onCompleted();
        } catch (Exception e) {
            log.error("cannot deposit money an error occurred", e);
            responseObserver.onError(e);
        } finally {
            if (balanceLock != null) {
                balanceLock.unlock();
            }
        }
    }

    private Lock getLock(int userId, CURRENCY currency) {
        String lockKey = String.format("%s_%s", userId, currency);
        return locks.computeIfAbsent(lockKey, k -> new ReentrantLock());
    }

    private void checkDepositRequest(DepositRequest request) {
        if (request.getCurrency() == CURRENCY.UNRECOGNIZED) {
            throw new StatusRuntimeException(Status.FAILED_PRECONDITION.withDescription("Unknown currency"));
        }

        if (request.getAmount() <= 0) {
            throw new StatusRuntimeException(Status.FAILED_PRECONDITION.withDescription("Deposit amount <= 0"));
        }
    }

    @Override
    public void withdraw(WithdrawRequest request, StreamObserver<Empty> responseObserver) {
        log.info("withdrawal of {} {} for user with id {}",
                request.getCurrency(), request.getAmount(), request.getUserId());

        Lock balanceLock = null;
        try {
            checkWithdrawRequest(request);
            balanceLock = getLock(request.getUserId(), request.getCurrency());
            balanceLock.lock();

            Balance balance = repository.findByUserIdAndCurrency(request.getUserId(), request.getCurrency().name());
            BigDecimal withdrawAmount = new BigDecimal(request.getAmount());

            if (withdrawAmount.compareTo(balance.getAmount()) > 0) {
                throw new StatusRuntimeException(Status.FAILED_PRECONDITION.withDescription("insufficient funds"));
            }

            balance.setAmount(balance.getAmount().subtract(new BigDecimal(request.getAmount())));
            repository.save(balance);

            responseObserver.onNext(Empty.getDefaultInstance());
            responseObserver.onCompleted();
        } catch (Exception e) {
            log.error("cannot withdraw money an error occurred", e);
            responseObserver.onError(e);
        } finally {
            if (balanceLock != null) {
                balanceLock.unlock();
            }
        }

    }

    private void checkWithdrawRequest(WithdrawRequest request) {
        if (request.getCurrency() == CURRENCY.UNRECOGNIZED) {
            throw new StatusRuntimeException(Status.FAILED_PRECONDITION.withDescription("Unknown currency"));
        }

        if (request.getAmount() <= 0) {
            throw new StatusRuntimeException(Status.FAILED_PRECONDITION.withDescription("Withdraw amount <= 0"));
        }
    }

    @Override
    public void balance(BalanceRequest request, StreamObserver<BalanceResponse> responseObserver) {
        log.info("get balance for user with id {}", request.getUserId());

        try {
            BalanceResponse.Builder responseBuilder = BalanceResponse.newBuilder();
            repository.findByUserId(request.getUserId())
                      .stream()
                      .map(this::mapBalance)
                      .forEach(responseBuilder::addBalance);

            responseObserver.onNext(responseBuilder.build());
            responseObserver.onCompleted();

        } catch (Exception e) {
            log.error("cannot get balance for userId {}", request.getUserId(), e);
            responseObserver.onError(e);
        }
    }

    private com.betpawa.wallet.Balance mapBalance(Balance balance) {
        return com.betpawa.wallet.Balance.newBuilder()
                                         .setAmount(balance.getAmount().floatValue())
                                         .setCurrency(CURRENCY.valueOf(balance.getCurrency()))
                                         .build();
    }


}
