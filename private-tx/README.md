# Besu Plugins API Demo Plugin

## Purpose of the Demo Plugin
Compare the transactions in incoming blocks to those we have already seen in the mempool. Unseen transactions in the block may have been provided by a private builder.

### Services Used
- **PicoCLIOptions**
    * To add configuration options to change the name of the metrics
- **MetricCategoryRegistry** and **MetricsSystem**
    * To add the metrics to the metrics endpoint
- **BesuEvents**
    * To listen to transaction gossip and propagated blocks and capture relevant data

### Plugin Lifecycle
- **Register**
    * Add the configuration options and metrics category
- **Start**
    * Connect to the Besu events
- **Stop**
    * Disconnect from the Besu events


## To Execute the Demo

From the root of the project, build the plugin jar
```
./gradlew :private-tx:shadowJar
```

Install the plugin into `$BESU_HOME`

```
mkdir $BESU_HOME/plugins
cp private-tx/build/libs/*-all.jar $BESU_HOME/plugins
```

Run the Besu node w/o customized metric names
```
$BESU_HOME/bin/besu --config-file=demo-options.toml
```

Then go to http://localhost:9545/metrics to see the metrics

To change the names of the metrics use the plugin CLI options
```
$BESU_HOME/bin/besu --config-file=demo-options.toml \
  --plugin-gas-spending-metrics-name=a_name 
  --plugin-gas-spending-metrics-prefix=a_prefix_
```` 