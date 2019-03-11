package com.betpawa.wallet.client;

import com.betpawa.wallet.CURRENCY;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.BiConsumer;
import java.util.stream.IntStream;

public class WalletClientEmulator {

    private static final Logger log = LoggerFactory.getLogger(WalletClientEmulator.class);

    private final int port;
    private final String host;
    private final int users;
    private final int concurrentThreadsPerUser;
    private final int roundsPerThread;

    public WalletClientEmulator(int port, String host, int users, int concurrentThreadsPerUser, int roundsPerThread) {
        this.port = port;
        this.host = host;
        this.users = users;
        this.concurrentThreadsPerUser = concurrentThreadsPerUser;
        this.roundsPerThread = roundsPerThread;
    }

    public void start() {
        log.info("init database for emulation");
        WalletEmulatorUtil.initDatabase(host, port, users);

        log.info("start emulation: port [{}], host [{}], users [{}], threadsPerUser [{}], rounds [{}]",
                port, host, users, concurrentThreadsPerUser, roundsPerThread);

        WalletClient client = new WalletClient(host, port);
        IntStream.rangeClosed(1, users)
                 .forEach(id -> startUserEmulation(client, id));

    }

    private void startUserEmulation(final WalletClient client, final int userId) {
        ExecutorService executor = Executors.newFixedThreadPool(concurrentThreadsPerUser);

        for (int thread = 0; thread < concurrentThreadsPerUser; thread++) {
            executor.execute(() -> {
                for (int round = 0; round < roundsPerThread; round++) {
                    int roundIndex = new Random().nextInt(rounds.size());
                    rounds.get(roundIndex).accept(client, userId);
                }
            });
        }
        executor.shutdown();
    }

    private static BiConsumer<WalletClient, Integer> roundA = (wc, id) -> {
        log.info("start roundA");
        wc.deposit(id, 100, CURRENCY.USD.name());
        wc.withdraw(id, 200, CURRENCY.USD.name());
        wc.deposit(id, 100, CURRENCY.EUR.name());
        wc.getBalance(id);
        wc.withdraw(id, 100, CURRENCY.USD.name());
        wc.getBalance(id);
        wc.withdraw(id, 100, CURRENCY.USD.name());
    };

    private static BiConsumer<WalletClient, Integer> roundB = (wc, id) -> {
        log.info("start roundB");
        wc.withdraw(id, 100, CURRENCY.GBP.name());
        wc.deposit(id, 300, CURRENCY.GBP.name());
        wc.withdraw(id, 100, CURRENCY.GBP.name());
        wc.withdraw(id, 100, CURRENCY.GBP.name());
        wc.withdraw(id, 100, CURRENCY.GBP.name());
    };

    private static BiConsumer<WalletClient, Integer> roundC = (wc, id) -> {
        log.info("start roundC");
        wc.getBalance(id);
        wc.deposit(id, 100, CURRENCY.USD.name());
        wc.deposit(id, 100, CURRENCY.USD.name());
        wc.withdraw(id, 100, CURRENCY.USD.name());
        wc.deposit(id, 100, CURRENCY.USD.name());
        wc.getBalance(id);
        wc.withdraw(id, 200, CURRENCY.USD.name());
        wc.getBalance(id);
    };

    private static List<BiConsumer<WalletClient, Integer>> rounds = Arrays.asList(roundA, roundB, roundC);

}
