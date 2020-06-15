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

import org.apache.ignite.IgniteException;
import org.apache.ignite.internal.IgniteEx;
import org.apache.ignite.plugin.IgnitePlugin;
import org.apache.ignite.plugin.PluginConfiguration;
import org.apache.ignite.plugin.PluginContext;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.server.handler.ContextHandlerCollection;

public class ViewerPlugin implements IgnitePlugin {
    private Server srv;

    public ViewerPlugin() {
    }

    public void init(PluginContext ctx) {
        IgniteViewerConfiguration cfg = configuration(ctx);

        srv = new Server(cfg.getPort());

        ContextHandler viewCtx = new ContextHandler("/views");
        viewCtx.setHandler(new ViewsHandler(((IgniteEx)ctx.grid()).context().systemView()));

        ContextHandler metricCtx = new ContextHandler("/metrics");
        metricCtx.setHandler(new MetricsHandler(((IgniteEx)ctx.grid()).context().metric(), cfg.isPrintDescription()));

        srv.setHandler(new ContextHandlerCollection(viewCtx, metricCtx));
    }

    public void start() {
        try {
            srv.start();
        }
        catch (Exception e) {
            throw new IgniteException(e);
        }
    }

    public void stop(boolean cancel) {
        try {
            srv.stop();
        }
        catch (Exception e) {
            throw new IgniteException(e);
        }
    }

    private  IgniteViewerConfiguration configuration(PluginContext ctx) {
        PluginConfiguration[] cfgs = ctx.igniteConfiguration().getPluginConfigurations();

        IgniteViewerConfiguration cfg = null;

        if (cfgs != null) {
            for (PluginConfiguration _cfg : cfgs) {
                if (_cfg instanceof IgniteViewerConfiguration)
                    cfg = (IgniteViewerConfiguration)_cfg;
            }
        }

        return cfg == null ? new IgniteViewerConfiguration() : cfg;
    }
}
