package com.betpawa.wallet.client;

public class WalletClientCli {

    public static final String USAGE = "Illegal options \n" +
            "Usage: --users 10 --concurrent_threads_per_user 2 --rounds_per_thread 1 --port 1234 --host localhost";

    public static void main(String[] args) {
        int users = 1;
        int concurrentThreadsPerUser = 1;
        int roundsPerThread = 1;
        int port = 1234;
        String host = "localhost";

        for (int i = 0; i < args.length; i++) {
            switch (args[i]) {
                case "--users":
                    users = Integer.valueOf(args[++i]);
                    break;
                case "--concurrent_threads_per_user":
                    concurrentThreadsPerUser = Integer.valueOf(args[++i]);
                    break;
                case "--rounds_per_thread":
                    roundsPerThread = Integer.valueOf(args[++i]);
                    break;
                case "--port" :
                    port = Integer.valueOf(args[++i]);
                    break;
                case "--host":
                    host = args[++i];
                    break;
                default:
                    System.out.println(USAGE);
                    System.exit(0);
            }
        }

        new WalletClientEmulator(port, host, users, concurrentThreadsPerUser, roundsPerThread)
                .start();

    }
}
