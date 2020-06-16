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

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import java.io.IOException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.ignite.lang.IgniteUuid;
import org.apache.ignite.spi.metric.HistogramMetric;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;

public abstract class JSonHandler extends AbstractHandler {
    private ObjectWriter writer;

    @Override protected void doStart() throws Exception {
        writer = writer();
    }

    @Override public void handle(String target, Request baseRequest, HttpServletRequest request,
        HttpServletResponse response) throws IOException {
        baseRequest.setHandled(true);

        response.addHeader("Access-Control-Allow-Origin", "*");

        Object d = "/".equals(target) ? root() : data(target);

        if (d == null) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);

            return;
        }

        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType("application/json");

        writer.writeValue(response.getWriter(), d);
    }

    protected abstract <T> T data(String target);

    protected abstract <T> T root();

    protected ObjectWriter writer() {
        return new ObjectMapper()
            .writerWithDefaultPrettyPrinter();
    }

    public static class NameAndDescription {
        private final String name;
        private final String description;

        public NameAndDescription(String name, String description) {
            this.name = name;
            this.description = description;
        }

        public String getName() { return name; }

        public String getDescription() { return description; }
    }

    public static class IgniteUuidSerializer  extends StdSerializer<IgniteUuid> {
        protected IgniteUuidSerializer() {
            super(IgniteUuid.class);
        }

        @Override public void serialize(IgniteUuid m, JsonGenerator jgen, SerializerProvider provider) throws IOException {
            jgen.writeString(m.toString());
        }
    }
}
