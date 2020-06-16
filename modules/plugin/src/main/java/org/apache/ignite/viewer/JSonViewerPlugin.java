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

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.apache.ignite.IgniteException;
import org.apache.ignite.internal.IgniteEx;
import org.apache.ignite.internal.util.typedef.T2;
import org.apache.ignite.plugin.IgnitePlugin;
import org.apache.ignite.plugin.PluginConfiguration;
import org.apache.ignite.plugin.PluginContext;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.server.handler.ContextHandlerCollection;

public class JSonViewerPlugin implements IgnitePlugin {
    private Server srv;

    private List<T2<UUID, String>> nodes = new ArrayList<>();

    public JSonViewerPlugin() {
    }

    public void init(PluginContext ctx) {
        IgniteEx grid = (IgniteEx)ctx.grid();

        JSonViewerConfiguration cfg = configuration(ctx);

        srv = new Server(cfg.getPort());

        ContextHandler viewCtx = new ContextHandler("/views");
        viewCtx.setHandler(new ViewsHandler(grid.context().systemView()));

        ContextHandler metricCtx = new ContextHandler("/metrics");
        metricCtx.setHandler(new MetricsHandler(grid.context().metric(), cfg.isPrintDescription()));

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

    private JSonViewerConfiguration configuration(PluginContext ctx) {
        PluginConfiguration[] cfgs = ctx.igniteConfiguration().getPluginConfigurations();

        JSonViewerConfiguration cfg = null;

        if (cfgs != null) {
            for (PluginConfiguration _cfg : cfgs) {
                if (_cfg instanceof JSonViewerConfiguration)
                    cfg = (JSonViewerConfiguration)_cfg;
            }
        }

        return cfg == null ? new JSonViewerConfiguration() : cfg;
    }

    public URI uri() {
        return srv == null ? null : srv.getURI();
    }
}
