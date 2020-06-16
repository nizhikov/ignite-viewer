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

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;
import org.apache.ignite.cluster.ClusterNode;
import org.apache.ignite.internal.IgniteEx;

public class NodesHandler extends JSonHandler {
    private final ConcurrentMap<UUID, Integer> nodes;

    private final IgniteEx grid;

    public NodesHandler(ConcurrentMap<UUID, Integer> nodes, IgniteEx grid) {
        this.nodes = nodes;
        this.grid = grid;
    }

    @Override protected <T> T data(String target) {
        return null;
    }

    @Override protected Set<Object[]> root() {
        return nodes.entrySet().stream()
            .filter(e -> grid.cluster().node(e.getKey()) != null)
            .map(e -> {
                ClusterNode node = grid.cluster().node(e.getKey());

                return new Object[] { e.getKey(), "http://" + node.hostNames().iterator().next() + ":" + e.getValue() };
        }).collect(Collectors.toSet());
    }
}
