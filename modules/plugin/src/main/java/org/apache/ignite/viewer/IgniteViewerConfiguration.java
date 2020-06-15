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

import org.apache.ignite.plugin.PluginConfiguration;

public class IgniteViewerConfiguration implements PluginConfiguration {
    private int port = 8080;

    private boolean prettyPrint;

    private boolean printDescription;

    public IgniteViewerConfiguration() {
    }

    public IgniteViewerConfiguration(int port, boolean prettyPrint) {
        this.port = port;
        this.prettyPrint = prettyPrint;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public boolean isPrettyPrint() {
        return prettyPrint;
    }

    public void setPrettyPrint(boolean prettyPrint) {
        this.prettyPrint = prettyPrint;
    }

    public boolean isPrintDescription() {
        return printDescription;
    }

    public void setPrintDescription(boolean printDescription) {
        this.printDescription = printDescription;
    }
}
