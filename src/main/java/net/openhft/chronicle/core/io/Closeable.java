/*
 * Copyright 2016 higherfrequencytrading.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.openhft.chronicle.core.io;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Collection;

@FunctionalInterface
public interface Closeable extends java.io.Closeable {

    static void closeQuietly(@Nullable Object o, @NotNull Object... a) {
        closeQuietly(o);
        closeQuietly(a);
    }

    static void closeQuietly(@Nullable Object o) {
        // this if-else run assumes that the collection itself isn't closeable
        if (o instanceof Collection) {
            closeQuietly((Collection) o);
        } else if (o instanceof Object[]) {
            closeQuietly((Object[]) o);
        } else if (o instanceof java.io.Closeable) {
            try {
                ((java.io.Closeable) o).close();
            } catch (IOException | IllegalStateException e) {
                LoggerFactory.getLogger(Closeable.class).debug("", e);
            }
        }
    }

    /**
     * Quietly closes each element of the specified array.
     *
     * @param a the array whose elements are to be closed
     * @param <T> the type of array's element
     */
    static <T> void closeQuietly(@NotNull T[] a) {
        for (Object o2 : a) {
            closeQuietly(o2);
        }
    }

    /**
     * Quietly closes each item in the specified collection. Leaves the
     * collection's contents intact; doesn't close the collection object.
     *
     * @param c the collection whose elements are to be closed
     */
    static void closeQuietly(@NotNull Collection<?> c) {
        c.forEach(Closeable::closeQuietly);
    }

    /**
     * Doesn't throw a checked exception.
     */
    @Override
    void close();

    default void notifyClosing() {
        // take an action before everything else closes.
    }

    default boolean isClosed() {
        throw new UnsupportedOperationException("todo");
    }
}
