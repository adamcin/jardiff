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

package net.adamcin.jardiff.core;

import net.adamcin.streamsupport.Both;
import org.jetbrains.annotations.NotNull;

/**
 * Represents a container of diffed values.
 *
 * @param <V> a generic value type that should be reifed by each concrete implementation
 */
public interface Diffed<V> {
    /**
     * Get the name of the resource within the jars being compared.
     *
     * @return the resource name
     */
    @NotNull
    Name getName();

    /**
     * The diffed values.
     *
     * @return a {@link net.adamcin.streamsupport.Both} containing the diffed values
     */
    @NotNull
    Both<V> both();
}
