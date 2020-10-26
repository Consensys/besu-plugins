# Quorum Besu Plugins

[![CircleCI](https://circleci.com/gh/ConsenSys/besu-plugins/tree/master.svg?style=svg)](https://circleci.com/gh/ConsenSys/besu-plugins/tree/master)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://github.com/ConsenSys/besu-plugins/blob/master/LICENSE)
[![Discord](https://img.shields.io/badge/Chat-on%20Discord-blue)](https://discord.com/invite/TCtK3YM)

**Quorum Besu Plugins** extends the Hyperledger Besu functionality. 
It uses the Plugin API to retrieve data from any Besu network, public or permissioned and feed it into an application or system.

This API exposes data about the following components:

- Blocks
- Balances
- Transactions
- Smart contracts
- Execution results
- Logs
- Syncing state.

**The current Besu recommended version is 20.10.0**

We recommend using plugins with the last minor (i.e. 20.X) Besu version in production. That version will have undergone the most extensive testing with plugins. While patch releases of Besu should work with plugins, they are not put through the same QA cycle and are only tested by automatic tests. If you have a problem using plugins with a Besu patch release, please open an issue.

## Useful Links

* [Besu User Documentation](https://besu.hyperledger.org)
* [Plugins User Documentation](https://doc.quorumplugins.consensys.net)
* [Plugins Issues](https://github.com/ConsenSys/besu-plugins/issues)
* [Contribution guidelines](CONTRIBUTING.md)
* [Plugins Changelog](CHANGELOG.md)

## Plugins 

 ### Event streams
 
This plugin will listen to events occurring on the Ethereum network and will broadcast them to Kafka. Core broadcasting logic has been extracted to a [common folder](https://github.com/ConsenSys/besu-plugins/tree/master/event-stream/common) to facilitate the addition of support for other message brokers.

**List of compatible message brokers:**
- [Kafka](https://github.com/ConsenSys/besu-plugins/tree/master/event-stream/kafka)

**List of events that can be sent by the plugin:**
- Block Propagated
- Block Added
- Block Reorg
- Transaction Added
- Transaction Dropped
- Transaction Reverted
- Sync Status Changed
- Log Emitted

## Quorum Besu Plugins users 

See our [user documentation](https://doc.quorumplugins.consensys.net/en/latest/Concepts/Besu-Plugins/Event-Streams/). 

## Quorum Besu Plugins developers 

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

