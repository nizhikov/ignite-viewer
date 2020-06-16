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
import java.util.Iterator;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import org.apache.ignite.IgniteException;
import org.apache.ignite.internal.managers.systemview.GridSystemViewManager;
import org.apache.ignite.spi.systemview.view.SystemView;
import org.apache.ignite.spi.systemview.view.SystemViewRowAttributeWalker;

public class ViewsHandler extends JSonHandler {
    private final GridSystemViewManager mgr;

    public ViewsHandler(GridSystemViewManager mgr) {
        this.mgr = mgr;
    }

    @Override protected Iterable<?> data(String target) {
        SystemView<?> view = mgr.view(target.substring(1));

        if (view == null)
            return null;

        return view;
    }

    @Override protected Collection<NameAndDescription> root() {
        return StreamSupport.stream(mgr.spliterator(), false)
            .map(sv -> new NameAndDescription(sv.name(), sv.description()))
            .collect(Collectors.toList());
    }

    @Override protected ObjectWriter writer() {
        ObjectMapper mapper = new ObjectMapper();

        SimpleModule module = new SimpleModule();

        for (SystemView view : mgr) {
            module.addSerializer(view.rowClass(),
                new SystemViewRowAttributeWalkerSerializer(view.rowClass(), view.walker()));
        }

        mapper.registerModule(module);

        return mapper.writerWithDefaultPrettyPrinter();
    }

    private static class SystemViewRowAttributeWalkerSerializer<T> extends StdSerializer<T> {
        private SystemViewRowAttributeWalker<T> walker;

        protected SystemViewRowAttributeWalkerSerializer(Class<T> t, SystemViewRowAttributeWalker<T> walker) {
            super(t);
            this.walker = walker;
        }

        @Override public void serialize(T t, JsonGenerator jgen, SerializerProvider provider) throws IOException {
            Consumer<RunnableX> runner = RunnableX::run;

            jgen.writeStartObject();

            walker.visitAll(t, new SystemViewRowAttributeWalker.AttributeWithValueVisitor() {
                @Override public <K> void accept(int idx, String name, Class<K> clazz, K val) {
                    runner.accept(() -> {
                        if (val instanceof Collection) {
                            Collection<?> coll = (Collection<?>)val;

                            jgen.writeArrayFieldStart(name);

                            for (Object item : coll) {
                                jgen.writeString(Objects.toString(item));
                            }
                            jgen.writeEndArray();
                        }
                        else
                            jgen.writeObjectField(name, val);
                    });
                }

                @Override public void acceptBoolean(int idx, String name, boolean val) {
                    runner.accept(() -> jgen.writeBooleanField(name, val));
                }

                @Override public void acceptChar(int idx, String name, char val) {
                    runner.accept(() -> jgen.writeNumberField(name, val));
                }

                @Override public void acceptByte(int idx, String name, byte val) {
                    runner.accept(() -> jgen.writeNumberField(name, val));
                }

                @Override public void acceptShort(int idx, String name, short val) {
                    runner.accept(() -> jgen.writeNumberField(name, val));
                }

                @Override public void acceptInt(int idx, String name, int val) {
                    runner.accept(() -> jgen.writeNumberField(name, val));
                }

                @Override public void acceptLong(int idx, String name, long val) {
                    runner.accept(() -> jgen.writeNumberField(name, val));
                }

                @Override public void acceptFloat(int idx, String name, float val) {
                    runner.accept(() -> jgen.writeNumberField(name, val));
                }

                @Override public void acceptDouble(int idx, String name, double val) {
                    runner.accept(() -> jgen.writeNumberField(name, val));
                }
            });

            jgen.writeEndObject();
        }
    }

    @FunctionalInterface
    private interface RunnableX extends Runnable {
        @Override public default void run() {
            try {
                runx();
            }
            catch (IOException e) {
                throw new IgniteException(e);
            }
        }

        void runx() throws IOException;
    }
}
