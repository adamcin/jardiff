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

package net.adamcin.jardiff.core.entry;

import aQute.bnd.osgi.Resource;
import aQute.libg.cryptography.SHA256;
import net.adamcin.streamsupport.Fun;
import net.adamcin.streamsupport.Result;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.InputStream;

public class JarEntryMetadata {
    private final long lastModified;
    private final long size;
    private final String extra;
    private final String sha256;

    public JarEntryMetadata(long lastModified, long size, @Nullable String extra, @NotNull String sha256) {
        this.lastModified = lastModified;
        this.size = size;
        this.extra = extra;
        this.sha256 = sha256;
    }

    public long getLastModified() {
        return lastModified;
    }

    public long getSize() {
        return size;
    }

    @Nullable
    public String getExtra() {
        return extra;
    }

    @NotNull
    public String getSha256() {
        return sha256;
    }

    public static Result<JarEntryMetadata> fromResource(@NotNull Resource resource) {
        return Fun.result0(() -> {
            try (InputStream inputStream = resource.openInputStream()) {
                return new JarEntryMetadata(resource.lastModified(), resource.size(), resource.getExtra(),
                        SHA256.digest(inputStream).asHex());
            }
        }).get();
    }
}
