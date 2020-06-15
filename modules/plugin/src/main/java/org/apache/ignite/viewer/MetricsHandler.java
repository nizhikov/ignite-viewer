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
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import java.io.IOException;
import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import org.apache.ignite.internal.processors.metric.GridMetricManager;
import org.apache.ignite.spi.metric.HistogramMetric;
import org.apache.ignite.spi.metric.Metric;
import org.apache.ignite.spi.metric.ReadOnlyMetricRegistry;

public class MetricsHandler extends JSonHandler {
    private final GridMetricManager mgr;

    private final boolean printDescription;

    private final ConcurrentMap<String, ReadOnlyMetricRegistry> mregs = new ConcurrentHashMap<>();

    public MetricsHandler(GridMetricManager mgr, boolean printDescription) {
        this.mgr = mgr;
        this.printDescription = printDescription;

        mgr.addMetricRegistryCreationListener(mreg -> mregs.put(mreg.name(), mreg));
        mgr.addMetricRegistryRemoveListener(mreg -> mregs.remove(mreg.name()));

        for (ReadOnlyMetricRegistry mreg : mgr)
            mregs.put(mreg.name(), mreg);
    }

    @Override protected Iterable<?> data(String target) {
        return mregs.get(target.substring(1));
    }

    @Override protected Collection<NameAndDescription> root() {
        return StreamSupport.stream(mgr.spliterator(), false)
            .map(mr -> new NameAndDescription(mr.name(), null))
            .collect(Collectors.toList());
    }

    @Override protected ObjectWriter writer() {
        ObjectMapper mapper = new ObjectMapper();

        SimpleModule module = new SimpleModule();

        module.addSerializer(HistogramMetric.class, new StdSerializer<HistogramMetric>(HistogramMetric.class) {
            @Override public void serialize(HistogramMetric m, JsonGenerator jgen, SerializerProvider provider) throws IOException {
                jgen.writeStartObject();
                jgen.writeStringField("name", m.name());

                jgen.writeArrayFieldStart("bounds");
                for (long b : m.bounds())
                    jgen.writeNumber(b);
                jgen.writeEndArray();

                jgen.writeArrayFieldStart("value");
                for (long v : m.value())
                    jgen.writeNumber(v);
                jgen.writeEndArray();

                if (printDescription)
                    jgen.writeStringField("description", m.description());
                jgen.writeEndObject();
            }
        });

        module.addSerializer(Metric.class, new StdSerializer<Metric>(Metric.class) {
            @Override public void serialize(Metric m, JsonGenerator jgen, SerializerProvider provider) throws IOException {
                jgen.writeStartObject();
                jgen.writeStringField("name", m.name());
                jgen.writeStringField("value", m.getAsString());
                if (printDescription)
                    jgen.writeStringField("description", m.description());
                jgen.writeEndObject();
            }
        });

        mapper.registerModule(module);

        return mapper.writerWithDefaultPrettyPrinter();
    }
}
