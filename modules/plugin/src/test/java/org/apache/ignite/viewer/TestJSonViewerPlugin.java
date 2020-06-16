/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.ignite.viewer;

import java.util.concurrent.ThreadLocalRandom;
import org.apache.ignite.Ignite;
import org.apache.ignite.IgniteCache;
import org.apache.ignite.cache.CacheAtomicityMode;
import org.apache.ignite.configuration.CacheConfiguration;
import org.apache.ignite.configuration.IgniteConfiguration;
import org.apache.ignite.internal.util.typedef.G;
import org.apache.ignite.transactions.Transaction;
import org.junit.Test;

public class TestJSonViewerPlugin {
    @Test
    public void testPlugin() throws Exception {
        Ignite g1 = G.start(new IgniteConfiguration()
            .setIgniteInstanceName("ignite-1")
            .setPluginConfigurations(new JSonViewerConfiguration(8080, true))
            .setPluginProviders(new JSonViewerPluginProvider()));

        Ignite g2 = G.start(new IgniteConfiguration()
            .setIgniteInstanceName("ignite-2")
            .setPluginConfigurations(new JSonViewerConfiguration(8081, true))
            .setPluginProviders(new JSonViewerPluginProvider()));

        doTxs(g1);
        doCompute(g1);

        Thread.sleep(60_000*60);
    }

    private void doCompute(Ignite g1) {
        new Thread(() -> {
            while (true) {
                int cnt = ThreadLocalRandom.current().nextInt(5);

                for (int i=0; i<cnt; i++) {
                    g1.compute().withName("test-task-" + cnt).broadcastAsync(() -> {
                        try {
                            Thread.sleep(2_000);
                        }
                        catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                    });
                }

                try {
                    Thread.sleep(2_000);
                }
                catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }).start();
    }

    private void doTxs(Ignite g1) {
        new Thread(() -> {
            IgniteCache<Object, Object> cache1 = g1.createCache(new CacheConfiguration<>("my-cache").setAtomicityMode(CacheAtomicityMode.TRANSACTIONAL));

            int num = 0;
            while (true) {
                int cnt = ThreadLocalRandom.current().nextInt(1000);

                try(Transaction tx = g1.transactions().txStart()) {
                    for (int i=0; i<cnt; i++)
                        cache1.put(num++, "Random string - " + ThreadLocalRandom.current().nextDouble());

                    Thread.sleep(2*cnt);
                }
                catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }).start();
    }
}
