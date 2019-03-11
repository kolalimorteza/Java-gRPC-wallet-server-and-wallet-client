package com.betpawa.wallet.client;

import com.betpawa.wallet.InitDatabaseRequest;
import com.betpawa.wallet.WalletEmulatorGrpc;
import com.betpawa.wallet.WalletEmulatorGrpc.WalletEmulatorBlockingStub;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

public class WalletEmulatorUtil {

    public static void initDatabase(String host, int port, int numOfUsers) {
        ManagedChannel channel = ManagedChannelBuilder.forAddress(host, port)
                                                      .usePlaintext()
                                                      .build();

        WalletEmulatorBlockingStub emulatorBlockingStub = WalletEmulatorGrpc.newBlockingStub(channel);
        InitDatabaseRequest initDatabaseRequest = InitDatabaseRequest.newBuilder()
                                                                     .setNumOfUsers(numOfUsers)
                                                                     .build();
        emulatorBlockingStub.initDatabase(initDatabaseRequest);
    }
}
