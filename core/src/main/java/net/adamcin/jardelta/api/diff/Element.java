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

package net.adamcin.jardelta.api.diff;

import net.adamcin.jardelta.api.Name;
import net.adamcin.streamsupport.Both;
import org.jetbrains.annotations.NotNull;
import org.osgi.annotation.versioning.ConsumerType;

import java.util.function.Function;

/**
 * Represents a named pair of comparable values.
 *
 * @param <V> a generic value type that should be reifed by each concrete implementation.
 */
@ConsumerType
public interface Element<V> {

    /**
     * Get the name of the resource within the jars being compared.
     *
     * @return the resource name
     */
    @NotNull
    Name name();

    /**
     * The diffed values.
     *
     * @return a {@link net.adamcin.streamsupport.Both} containing the diffed values
     */
    @NotNull
    Both<V> values();

    /**
     * Create a new generic element with {@code name} and {@code values}.
     *
     * @param name   the element name
     * @param values both values
     * @param <T>    the value type
     * @return a new {@link net.adamcin.jardelta.api.diff.Element}
     */
    static <T> Element<T> of(@NotNull Name name, @NotNull Both<T> values) {
        return new Element<>() {
            @Override
            public @NotNull Name name() {
                return name;
            }

            @Override
            public @NotNull Both<T> values() {
                return values;
            }
        };
    }

    /**
     * Project this element's values into a descendant element.
     *
     * @param relName   the name suffix to append to this name
     * @param newValues both of the new element's values
     * @param <T>       the element's value type
     * @return a new element
     */
    @NotNull
    default <T> Element<T> project(@NotNull Name relName, @NotNull Both<T> newValues) {
        final Name newName = name().append(relName);
        return of(newName, newValues);
    }

    /**
     * Project this element's values into a descendant element.
     *
     * @param relName  the name suffix to append to this name
     * @param mapperFn a function to map this element's values to produce the new element's values
     * @param <T>      the element's value type
     * @return a new element
     */
    @NotNull
    default <T> Element<T> project(@NotNull Name relName, @NotNull Function<? super V, ? extends T> mapperFn) {
        return project(relName, values().map(mapperFn::apply));
    }

}
