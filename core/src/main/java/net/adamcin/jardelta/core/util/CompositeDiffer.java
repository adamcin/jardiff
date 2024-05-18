/*
 * Copyright 2024 Adobe
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

package net.adamcin.jardelta.core.util;

import net.adamcin.jardelta.api.Name;
import net.adamcin.jardelta.api.diff.Diff;
import net.adamcin.jardelta.api.diff.Differ;
import net.adamcin.jardelta.api.diff.Element;
import net.adamcin.jardelta.api.diff.Emitter;
import net.adamcin.streamsupport.Both;
import net.adamcin.streamsupport.Fun;
import org.jetbrains.annotations.NotNull;
import org.osgi.annotation.versioning.ConsumerType;

import java.util.Map;
import java.util.TreeMap;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;

/**
 * Aggregates zero-to-many differs of elements and element projections when element structures are more complex.
 *
 * @param <V> the type parameter of the element being diffed
 */
public final class CompositeDiffer<V> implements Differ<Element<V>> {

    /**
     * A builder of a map of child names to differs, which constitutes the structure of a new
     * {@link net.adamcin.jardelta.core.util.CompositeDiffer}.
     *
     * @param <T> the element value type
     */
    @ConsumerType
    @FunctionalInterface
    public interface Builder<T> {

        /**
         * Provide another differ identified by the given {@code childName}.
         *
         * @param childName the child segment to append to the base {@link net.adamcin.jardelta.api.diff.Emitter},
         *                  or {@code ""} to emit for the base name
         * @param differ    a differ
         */
        void put(String childName, Differ<Element<T>> differ);
    }

    /**
     * Builds a {@link net.adamcin.jardelta.core.util.CompositeDiffer} using the provided {@code builderConsumer}
     * function, which is given a {@link net.adamcin.jardelta.core.util.CompositeDiffer.Builder}.
     *
     * @param builderConsumer the builder consumer function
     * @param <T>             the {@link net.adamcin.jardelta.api.diff.Element} type parameter
     * @return a new {@link net.adamcin.jardelta.core.util.CompositeDiffer}
     */
    @NotNull
    public static <T> CompositeDiffer<T> of(@NotNull Consumer<Builder<T>> builderConsumer) {
        Map<String, Differ<Element<T>>> differs = new TreeMap<>();
        builderConsumer.accept(differs::put);
        return new CompositeDiffer<>(differs);
    }

    private final Map<String, Differ<Element<V>>> differs;

    private CompositeDiffer(@NotNull Map<String, Differ<Element<V>>> differs) {
        this.differs = differs;
    }

    @Override
    @NotNull
    public Stream<Diff> diff(@NotNull Emitter baseEmitter, @NotNull Element<V> values) {
        final Emitter subEmitter = baseEmitter.forSubElement(values);
        return differs.entrySet().stream()
                .flatMap(Fun.mapEntry((key, func) -> func.diff(subEmitter.forChild(key), values)));
    }


    @NotNull
    public static <T, U> Map.Entry<Emitter, Element<U>> projectChild(@NotNull Emitter parentEmitter,
                                                                     @NotNull Element<T> element,
                                                                     @NotNull String childName,
                                                                     @NotNull Both<U> newValues) {
        final Name segment = Name.ofSegment(childName);
        return Fun.toEntry(parentEmitter.forChild(childName), element.project(segment, newValues));
    }

    @NotNull
    public static <T, U> Map.Entry<Emitter, Element<U>> projectChild(@NotNull Emitter parentEmitter,
                                                                     @NotNull Element<T> element,
                                                                     @NotNull String childName,
                                                                     @NotNull Function<? super T, ? extends U> mapperFn) {
        final Both<U> newValues = element.values().map(mapperFn::apply);
        return projectChild(parentEmitter, element, childName, newValues);
    }
}
