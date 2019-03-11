package com.betpawa.wallet.server;

import com.betpawa.wallet.dao.repository.BalanceRepository;
import com.betpawa.wallet.service.WalletEmulator;
import com.betpawa.wallet.service.WalletService;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class WalletServer {

    private static final Logger log = LoggerFactory.getLogger(WalletServer.class);

    private static final int DEFAULT_PORT = 1234;
    private static final String WALLET_SERVER_PORT = "WALLET_SERVER_PORT";

    private final int port;
    private final Server server;

    public WalletServer(int port) {
        BalanceRepository balanceRepository = new BalanceRepository();
        WalletService walletService = new WalletService(balanceRepository);
        WalletEmulator walletEmulator = new WalletEmulator(balanceRepository);

        this.server = ServerBuilder.forPort(port)
                                   .addService(walletService)
                                   .addService(walletEmulator)
                                   .build();
        this.port = port;
    }

    public void start() throws IOException, InterruptedException {
        server.start();
        log.info("Server started, listening on port {}", port);
        server.awaitTermination();
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        String port = System.getenv(WALLET_SERVER_PORT);
        if (port == null) {
            log.info("{} is not set, use default server port {}", WALLET_SERVER_PORT, DEFAULT_PORT);
            new WalletServer(DEFAULT_PORT).start();
        } else {
            new WalletServer(Integer.valueOf(port)).start();
        }
    }

}
