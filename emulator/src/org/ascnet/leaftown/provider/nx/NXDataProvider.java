/*
 * This file is part of AscNet Leaftown.
 * Copyright (C) 2014 Ascension Network
 *
 * AscNet Leaftown is a fork of the OdinMS MapleStory Server.
 *
 * AscNet Leaftown is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License version 3
 * as published by the Free Software Foundation. You may not use, modify
 * or distribute this program under any other version of the
 * GNU Affero General Public License.
 *
 * AscNet Leaftown is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with AscNet Leaftown.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.ascnet.leaftown.provider.nx;

import org.ascnet.leaftown.provider.MapleData;
import org.ascnet.leaftown.provider.MapleDataProvider;
import org.ascnet.leaftown.provider.MapleDataProviderFactory;
import us.aaronweiss.pkgnx.LazyNXFile;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class NXDataProvider implements MapleDataProvider {

    private final static Map<Path, NXDataProvider> openedFiles = new ConcurrentHashMap<>();

    private final LazyNXFile nxfile;
    private final NXData rootData;

    private NXDataProvider(Path path) throws IOException {
        nxfile = new LazyNXFile(path);
        rootData = new NXData(nxfile.getRoot());
    }

    /**
     * If an {@code NXDataProvider} for the given {@code path} does not
     * already exist, this creates one. Otherwise, the existing provider
     * is returned.
     *
     * @param path A path either absolute, or relative to the NX directory.
     * @return An {@code NXDataProvider} serving the given path.
     */
    public static NXDataProvider openFile(String path) throws IOException {
        return openFile(MapleDataProviderFactory.resolvePath(Paths.get(path)));
    }

    /**
     * If an {@code NXDataProvider} for the given {@code path} does not
     * already exist, this creates one. Otherwise, the existing provider
     * is returned.
     *
     * @param path An absolute path.
     * @return An {@code NXDataProvider} serving the given path.
     *
     * @throws java.lang.IllegalArgumentException Thrown if the given path is not absolute.
     */
    public static NXDataProvider openFile(Path path) throws IOException {
        if (!path.isAbsolute()) throw new IllegalArgumentException("Given path must be absolute.");

        NXDataProvider ret = openedFiles.get(path);
        if (ret == null) {
            ret = new NXDataProvider(path);
            openedFiles.put(path, ret);
            return ret;
        }
        return ret;
    }

    @Override
    public MapleData getData(String path) {
        if (path.equals("/")) return getRoot();
        return new NXData(nxfile.resolve(path.startsWith("/") ? path.substring(1) : path));
    }

    @Override
    public MapleData getRoot() {
        return rootData;
    }
}
