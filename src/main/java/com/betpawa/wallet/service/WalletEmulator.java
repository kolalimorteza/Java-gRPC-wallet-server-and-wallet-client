package com.betpawa.wallet.service;

import com.betpawa.wallet.Empty;
import com.betpawa.wallet.InitDatabaseRequest;
import com.betpawa.wallet.WalletEmulatorGrpc.WalletEmulatorImplBase;
import com.betpawa.wallet.dao.repository.BalanceRepository;
import io.grpc.stub.StreamObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WalletEmulator extends WalletEmulatorImplBase {

    private static final Logger log = LoggerFactory.getLogger(WalletEmulator.class);
    private final BalanceRepository balanceRepository;

    public WalletEmulator(BalanceRepository balanceRepository) {
        this.balanceRepository = balanceRepository;
    }

    public void initDatabase(InitDatabaseRequest request, StreamObserver<Empty> responseObserver) {
        try {
            log.info("prepare database for emulation, generate balances for {} users",
                    request.getNumOfUsers());

            balanceRepository.init(request.getNumOfUsers());
            responseObserver.onNext(Empty.getDefaultInstance());
            responseObserver.onCompleted();
        } catch (Exception e) {
            log.error("cannot generate users balances", e);
            responseObserver.onError(e);
        }
    }
}
