# grpc-wallet 
Based on Google RPC and Protocool Buffer - a multi threaded App that Allows Deposit, Withdrawal and Balance

## Requirements
The task consists of a wallet server and a wallet client. The wallet server will keep track of a users monetary balance in the system. The client will emulate users depositing and withdrawing funds.

## Wallet Server
The wallet server must expose the interface described below via gRPC.

## Interfaces
### Deposit
Deposit funds to the users wallet.

#### Input
User id
Amount
Currency (allowed values are EUR, USD, GBP)
#### Output
No output needed
#### Errors
Unknown currency

### Withdraw
Withdraw funds from the users wallet.

#### Input
User id
Amount
Currency (allowed values are EUR, USD, GBP)
#### Output
No output needed
#### Errors
Unknown currency, insufficient funds
### Balance
Get the users current balance.

#### Input
User id
#### Output
The balance of the users account for each currency

### Database
Include the database schema that is needed for the server application.

### Integration Test

    1.  Make a withdrawal of USD 200 for user with id 1. Must return "insufficient_funds".
    2.  Make a deposit of USD 100 to user with id 1.
    3.  Check that all balances are correct
    4.  Make a withdrawal of USD 200 for user with id 1. Must return "insufficient_funds".
    5.  Make a deposit of EUR 100 to user with id 1.
    6.  Check that all balances are correct
    7.  Make a withdrawal of USD 200 for user with id 1. Must return "insufficient_funds".
    8.  Make a deposit of USD 100 to user with id 1.
    9.  Check that all balances are correct
    10. Make a withdrawal of USD 200 for user with id 1. Must return "ok".
    11. Check that all balances are correct
    12. Make a withdrawal of USD 200 for user with id 1. Must return "insufficient_funds".

### Wallet Client
* The wallet client will emulate a number of users concurrently using the wallet. 
* The wallet client must connect to the wallet server over gRPC. 
* The client eliminating users doing rounds (a sequence of events). 
* Whenever a round is needed it is picked at random from the following list of available rounds

#### Round A
* Deposit 100 USD
* Withdraw 200 USD
* Deposit 100 EUR
* Get Balance
* Withdraw 100 USD
* Get Balance
* Withdraw 100 USD
#### Round B
* Withdraw 100 GBP
* Deposit 300 GPB
* Withdraw 100 GBP
* Withdraw 100 GBP
* Withdraw 100 GBP
* Withdraw 100 GBP
* Withdraw 100 GBP
#### Round C
* Get Balance
* Deposit 100 USD
* Deposit 100 USD
* Withdraw 100 USD
* Depsoit 100 USD
* Get Balance
* Withdraw 200 USD
* Get Balance

#### The wallet client should have the following CLI parameters:

* users (number of concurrent users emulated)
* concurrent_threads_per_user (number of concurrent requests a user will make)
* rounds_per_thread (number of rounds each thread is executing)
* Make sure the client exits when all rounds has been executed.

### Technologies
The following technologies MUST be used

* Java
* gRPC
* MySQL or PostgreSQL
* Gradle
* JUnit
* SLF4J
* Docker
* Hibernate

# Requirements
- Java 8 or above
- Docker

## Build and run docker image

```
cd src/docker 
docker build -t wallet/mysql .
docker run --rm --name wallet_db -p 3306:3306 wallet/mysql
docker run --rm --name wallet_db -p 3306:3306 -d wallet/mysql:latest
docker run --name wallet_db -d --link wallet_db:db -p 1234:80 phpmyadmin/phpmyadmin 
```
## Build wallet-server and wallet-client-cli

Run in project root directory:

```
$ ./gradlew installDist

```

For example, to try wallet emulator first run:

```
$ ./build/install/wallet/bin/wallet-server

```

And in a different terminal window run:

```
$ ./build/install/wallet/bin/wallet-client-cli
```

## Run with custom parameters

You can configure emulator via CLI parameters

````
$ ./wallet-client-cli --users 10 --concurrent_threads_per_user 2 --rounds_per_thread 3 --port 1234 --host localhost
````

Use env variable WALLET_SERVER_PORT in order to specify wallet-server port

```
export WALLET_SERVER_PORT=1122
$ ./wallet-server
```

## Performance Estimation

- on developer machine (Core i5 3.1GHz, 16GB)  ~1000 RPS
- avg transaction time ~100 ms. 
- max 100 concurrent transactions (limited by mysql connection pool)
