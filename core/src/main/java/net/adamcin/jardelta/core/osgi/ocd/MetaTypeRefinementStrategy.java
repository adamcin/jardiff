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

package net.adamcin.jardelta.core.osgi.ocd;

import net.adamcin.jardelta.api.Kind;
import net.adamcin.jardelta.api.Name;
import net.adamcin.jardelta.api.diff.Diff;
import net.adamcin.jardelta.api.diff.Diffs;
import net.adamcin.jardelta.api.diff.Element;
import net.adamcin.jardelta.api.diff.Emitter;
import net.adamcin.jardelta.api.jar.OpenJar;
import net.adamcin.jardelta.core.Context;
import net.adamcin.jardelta.core.Refinement;
import net.adamcin.jardelta.core.RefinementStrategy;
import net.adamcin.jardelta.core.osgi.OsgiUtil;
import net.adamcin.streamsupport.Both;
import net.adamcin.streamsupport.Fun;
import net.adamcin.streamsupport.Result;
import org.apache.felix.metatype.MetaData;
import org.apache.felix.metatype.MetaDataReader;
import org.jetbrains.annotations.NotNull;
import org.osgi.framework.Bundle;
import org.osgi.service.metatype.MetaTypeService;

import java.io.InputStream;
import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class MetaTypeRefinementStrategy implements RefinementStrategy {
    static final Name NAME_PREFIX = Name.of("{osgi.ocd}");
    private static final Name METATYPE_PARENT = Name.of(MetaTypeService.METATYPE_DOCUMENTS_LOCATION);
    public static final Kind KIND = Kind.of("osgi.ocd");

    @Override
    public @NotNull Kind getKind() {
        return KIND;
    }

    @Override
    public @NotNull Refinement refine(@NotNull Context context,
                                      @NotNull Diffs diffs,
                                      @NotNull Element<OpenJar> openJars) {
        // no point in deep comparison of metatype unless both jars are bundles
        Optional<Both<Bundle>> bundleAdapters = OsgiUtil.requireBothBundles(openJars.values());
        if (bundleAdapters.isEmpty()) {
            return Refinement.EMPTY;
        }

        final Both<Bundle> bothBundles = bundleAdapters.get();

        final Result<Both<List<JarMetaTypeProvider>>> providersResult =
                Both.ofResults(openJars.values()
                        .zip(bothBundles)
                        .map(Fun.mapEntry(MetaTypeRefinementStrategy::readMetaTypes)));

        if (providersResult.isFailure()) {
            return new Refinement(Collections.emptyList(),
                    Diffs.of(Diff.emitterOf(KIND).forName(NAME_PREFIX).errRight(providersResult)));
        }

        final Both<List<JarMetaTypeProvider>> providers = providersResult.getOrThrow();
        final Predicate<Name> namePredicate = Fun.<Name>inferTest1(name -> name.startsWithName(METATYPE_PARENT))
                .or(getL10nResourcePredicate(providers));

        final List<Diff> superseded = diffs.stream()
                .filter(Fun.composeTest1(Diff::getName, namePredicate))
                .collect(Collectors.toList());
        if (superseded.isEmpty()) {
            return Refinement.EMPTY;
        } else {
            final AllDesignates allDesignates = new AllDesignates(providers);
            final Emitter designatesEmitter = Diff.emitterOf(KIND);
            final MetaTypeDesignateDiffer designatesDiffer = new MetaTypeDesignateDiffer();
            return new Refinement(superseded, allDesignates.stream()
                    .flatMap(element -> designatesDiffer.diff(designatesEmitter, element))
                    .collect(Diffs.collector()));
        }
    }

    public static Result<List<JarMetaTypeProvider>> readMetaTypes(@NotNull OpenJar jar, @NotNull Bundle bundle) {
        final MetaDataReader reader = new MetaDataReader();
        return jar.getEntryNames().stream()
                .filter(name -> METATYPE_PARENT.equals(name.getParent()))
                .map(Fun.result1(name -> {
                    final URL url = bundle.getResource(name.toString());
                    try (InputStream inputStream = url.openStream()) {
                        MetaData metaData = reader.parse(inputStream);
                        metaData.setSource(url);
                        return metaData;
                    }
                }))
                .filter(result -> result.map(Objects::nonNull).getOrDefault(true))
                .map(result -> result.map(metaData -> new JarMetaTypeProvider(bundle, metaData)))
                .collect(Result.tryCollect(Collectors.toList()))
                .map(Collections::unmodifiableList);
    }

    static Predicate<Name> getL10nResourcePredicate(@NotNull Both<List<JarMetaTypeProvider>> providers) {
        final Set<String> localePrefixes = providers.stream()
                .flatMap(List::stream)
                .map(JarMetaTypeProvider::getLocalePrefix)
                .filter(Fun.inferTest1(String::isEmpty).negate())
                .collect(Collectors.toSet());

        return Fun.<Name>inferTest1(name -> name.endsWith(".properties"))
                .and(localePrefixes.stream().reduce(Fun.inferTest1(name -> false),
                        (acc, prefix) -> acc.or(getSingleLocalePrefixNamePredicate(prefix)), Predicate::or));
    }

    static Predicate<Name> getSingleLocalePrefixNamePredicate(@NotNull String localePrefix) {
        return name -> {
            final Name path = Name.of(localePrefix);
            if (path.isRoot()) {
                // match nothing if getLocalePrefix() is effectively empty
                return false;
            }
            final Name parent = path.getParent();
            return (parent == null || name.startsWithName(parent))
                    && name.startsWith(path.getSegment());
        };
    }
}
