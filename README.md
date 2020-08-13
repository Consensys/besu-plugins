# Quorum plugins for Besu

[![CircleCI](https://circleci.com/gh/ConsenSys/besu-plugins/tree/master.svg?style=svg)](https://circleci.com/gh/ConsenSys/besu-plugins/tree/master)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://github.com/PegasysEng/besu/blob/master/LICENSE)
[![Discord](https://img.shields.io/badge/Chat-on%20Discord-blue)](https://discord.gg/B4yx9SQ)

List of plugins allowing to extend the Hyperledger Besu functionality. These use the Plugin API to retrieve data from any Besu network, public or permissioned and feed it into an application or system.

These plugins can help add more monitoring functionality or stream event data to a third party application. The API exposes data about the following components:

- Blocks
- Balances
- Transactions
- Smart contracts
- Execution results
- Logs
- Syncing state.

**The Besu recommended version is `1.5`**

## Useful Links

* [Besu User Documentation](https://besu.hyperledger.org)
* [Quorum Plugins User Documentation](https://consensys.net/quorum/docs/plugins)
* [Quorum Plugins Issues](https://github.com/ConsenSys/besu-plugins/issues)
* [Contribution guidelines](CONTRIBUTING.md)
* [Quorum Plugins Changelog](CHANGELOG.md)

## Plugins 

 ### Event stream 
 
These plugins will listen to events occurring on the Ethereum network and will broadcast these events to your message broker.

**List of compatible message brokers:**
- [Kafka](https://github.com/ConsenSys/besu-plugins/tree/master/event-stream/kafka)
- [Kinesis](https://github.com/ConsenSys/besu-plugins/tree/master/event-stream/kinesis)
- [Nats](https://github.com/ConsenSys/besu-plugins/tree/master/event-stream/nats)
- [RabbitMQ](https://github.com/ConsenSys/besu-plugins/tree/master/event-stream/rabbitmq)

**List of events that can be sent by the plugin:**
- Block Propagated
- Block Added
- Block Reorg
- Transaction Added
- Transaction Dropped
- Transaction Reverted
- Sync Status Changed
- Log Emitted

## Quorum Plugins users 

See our [user documentation](https://consensys.net/quorum/docs/plugins). 

## Quorum Plugins developers 

* [Contribution Guidelines](CONTRIBUTING.md)
* [Coding Conventions](https://github.com/hyperledger/besu/blob/master/CODING-CONVENTIONS.md)

## Build Instructions

### Install Prerequisites

* Java 11

### Build and Dist

### Install Plugins

To allow Besu to access and use the plugin, copy the plugin (.jar) to the plugins directory.

```shell script
git clone https://github.com/ConsenSys/besu-plugins.git
cd besu-plugins && ./gradlew distTar installDist
```

This produces:
- Fully packaged distribution in `build/distributions` 


You can find the `jar` of each plugin in `build/libs` of the module 


### Build and Test

To build, clone this repo and run with `gradle`:

```shell script
git clone https://github.com/ConsenSys/besu-plugins.git
cd besu-plugins && ./gradlew
```

After a successful build, distribution packages are available in `build/distributions`.

## Code Style

We use Google's Java coding conventions for the project. To reformat code, run: 

```shell script 
./gradlew spotlessApply
```

Code style is checked automatically during a build.

## Testing

All the unit tests are run as part of the build, but can be explicitly triggered with:

```shell script 
./gradlew test
```

