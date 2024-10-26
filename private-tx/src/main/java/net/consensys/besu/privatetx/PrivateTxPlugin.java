/*
 * Copyright contributors to Hyperledger Besu.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package net.consensys.besu.privatetx;

import com.google.auto.service.AutoService;
import org.hyperledger.besu.datatypes.Hash;
import org.hyperledger.besu.datatypes.Transaction;
import org.hyperledger.besu.plugin.BesuContext;
import org.hyperledger.besu.plugin.BesuPlugin;
import org.hyperledger.besu.plugin.data.AddedBlockContext;
import org.hyperledger.besu.plugin.data.SyncStatus;
import org.hyperledger.besu.plugin.services.BesuEvents;
import org.hyperledger.besu.plugin.services.MetricsSystem;
import org.hyperledger.besu.plugin.services.PicoCLIOptions;
import org.hyperledger.besu.plugin.services.metrics.Counter;
import org.hyperledger.besu.plugin.services.metrics.LabelledGauge;
import org.hyperledger.besu.plugin.services.metrics.LabelledMetric;
import org.hyperledger.besu.plugin.services.metrics.MetricCategory;
import org.hyperledger.besu.plugin.services.metrics.MetricCategoryRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.Charset;
import java.util.HashSet;
import java.util.Locale;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import picocli.CommandLine;

@AutoService(BesuPlugin.class)
public class PrivateTxPlugin implements BesuPlugin, BesuEvents.BlockAddedListener, BesuEvents.TransactionAddedListener, BesuEvents.SyncStatusListener {

    private static String PLUGIN_NAME = "private-tx-detection";
    private BesuContext context;
    private Cache<Hash, Transaction> miniMemPool;
    private long blockListenerId;
    private long txListenerId;
    private Logger LOG = LoggerFactory.getLogger(PrivateTxPlugin.class);
    private long syncStatusId;
    private Optional<SyncStatus> syncStatus = Optional.empty();

    @Override
    public Optional<String> getName() {
        return Optional.of(PLUGIN_NAME);
    }

    @Override
    public String getVersion() {
        return "24.10.0";
    }

    @Override
    public void register(final BesuContext besuContext) {
        this.context = besuContext;
        this.miniMemPool =
                Caffeine.newBuilder()
                        .expireAfterWrite(
                                3 * 32 * 12L, TimeUnit.SECONDS)
                        .build();
        this.context.getService(MetricCategoryRegistry.class).ifPresentOrElse(
                this::registerMetrics,
                () -> LOG.error("No MetricsCategoryRegistry service found"));
        this.context.getService(PicoCLIOptions.class).ifPresentOrElse(
                this::registerPicoCLIOptions,
                () -> LOG.error("No PicoCLIOptions service found"));
    }

    @Override
    public void start() {
        LOG.info("PrivateTxPlugin started");
        context.getService(BesuEvents.class).ifPresentOrElse(this::startEvents, () -> LOG.error("No BesuEvents service found"));
        context.getService(MetricsSystem.class)
                .ifPresentOrElse(this::startMetrics, () -> LOG.error("Could not obtain MetricsSystem"));
    }

    @Override
    public void stop() {
        LOG.info("PrivateTxPlugin stopped");
        context.getService(BesuEvents.class).ifPresentOrElse(this::stopEvents, () -> LOG.error("No BesuEvents service found"));
    }

    @Override
    public void onBlockAdded(final AddedBlockContext addedBlockContext) {
        if(this.syncStatus.isPresent() ) {
            HashSet<Transaction> possiblyPrivate = new HashSet<>(addedBlockContext.getBlockBody().getTransactions());
            possiblyPrivate.removeAll(miniMemPool.asMap().values());
            if (possiblyPrivate.size() > 0) {
                LOG.info(possiblyPrivate.size() + " txs from block " + addedBlockContext.getBlockHeader().getNumber() + " with hash " + addedBlockContext.getBlockHeader().getBlockHash());
                privateTxInBlock.inc(possiblyPrivate.size());
                privateTxCountLastBlock = possiblyPrivate.size();
            }
        }
    }

    private final MetricCategory metricCategory = new MetricCategory() {
        @Override
        public String getName() {
            return metricName;
        }

        @Override
        public Optional<String> getApplicationPrefix() {
            return Optional.ofNullable(metricPrefix);
        }
    };

    private void registerMetrics(final MetricCategoryRegistry metricCategoryRegistry) {
        metricCategoryRegistry.addMetricCategory(metricCategory);
    }

    Counter privateTxInBlock;
    private int privateTxCountLastBlock = 0;

    private void startMetrics(MetricsSystem metricsSystem) {
        LabelledMetric<Counter> lbldCounter = metricsSystem.createLabelledCounter(
                metricCategory,
                "private_txs",
                "Number of private transactions in the last block",
                "count");
        privateTxInBlock = lbldCounter.labels("count");
        metricsSystem.createLongGauge(
                metricCategory,
                "private_txs_last_block",
                "Number of private transactions in the last block",
                () -> privateTxCountLastBlock);
    }

    @CommandLine.Option(names = "--plugin-private-tx-detection-metric-name")
    public String metricName = "private_tx_metrics";

    @CommandLine.Option(names = "--plugin-private-tx-detection-prefix")
    public String metricPrefix = "demo_";

    private void registerPicoCLIOptions(PicoCLIOptions picoCLIOptions) {
        picoCLIOptions.addPicoCLIOptions(PLUGIN_NAME, this);
    }

    private void startEvents(final BesuEvents besuEvents) {
        this.blockListenerId = besuEvents.addBlockAddedListener(this);
        this.txListenerId = besuEvents.addTransactionAddedListener(this);
        this.syncStatusId = besuEvents.addSyncStatusListener(this);
    }

    private void stopEvents(final BesuEvents besuEvents) {
        besuEvents.removeBlockAddedListener(this.blockListenerId);
        besuEvents.removeTransactionAddedListener(this.txListenerId);
        besuEvents.removeSyncStatusListener(this.syncStatusId);
    }

    @Override
    public void onTransactionAdded(final Transaction transaction) {
        miniMemPool.put(transaction.getHash(), transaction);
    }

    @Override
    public void onSyncStatusChanged(final Optional<SyncStatus> syncStatus) {
        this.syncStatus = syncStatus;
    }
}
