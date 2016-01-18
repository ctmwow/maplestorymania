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
import us.aaronweiss.pkgnx.NXNode;

import java.util.Iterator;
import java.util.regex.Pattern;

public class NXData extends MapleData {

    private static final Pattern SLASH_REGEX = Pattern.compile("/");
    private final NXNode nxNode;

    NXData(NXNode nxNode) {
        this.nxNode = nxNode;
    }

    @Override
    public String getName() {
        return nxNode.getName();
    }

    @Override
    public MapleData resolve(String rawPath) {
        String[] path = SLASH_REGEX.split((rawPath.startsWith("/") ? rawPath.substring(1) : rawPath));
        NXNode cursor = nxNode;
        for (String aPath : path) {
            if (cursor == null)
                break;
            cursor = cursor.getChild(aPath);
        }
        return nullOrData(cursor);
    }

    @Override
    public MapleData getChild(String name) {
        return nullOrData(nxNode.getChild(name));
    }

    @Override
    public Object getData() {
        return nxNode.get();
    }

    @Override
    public int getChildCount() {
        return nxNode.getChildCount();
    }

    @Override
    public Iterator<MapleData> iterator() {
        return new NXDataIterator(nxNode.iterator());
    }

    private static NXData nullOrData(NXNode n) {
        return n == null ? null : new NXData(n);
    }

    private static class NXDataIterator implements Iterator<MapleData> {

        private final Iterator<NXNode> nxNodeIter;

        NXDataIterator(Iterator<NXNode> nxNodeIter) {
            this.nxNodeIter = nxNodeIter;
        }

        @Override
        public boolean hasNext() {
            return nxNodeIter.hasNext();
        }

        @Override
        public MapleData next() {
            return new NXData(nxNodeIter.next());
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }
    }
}
