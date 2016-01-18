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

package org.ascnet.leaftown.net.netty;

import io.netty.buffer.AbstractByteBufAllocator;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;

import java.nio.ByteOrder;

public class LittleEndianByteBufAllocator extends AbstractByteBufAllocator {

    private final ByteBufAllocator wrapped;

    public LittleEndianByteBufAllocator(ByteBufAllocator wrapped) {
        this.wrapped = wrapped;
    }

    @Override
    protected ByteBuf newHeapBuffer(int initialCapacity, int maxCapacity) {
        return wrapped.heapBuffer(initialCapacity, maxCapacity).order(ByteOrder.LITTLE_ENDIAN);
    }

    @Override
    protected ByteBuf newDirectBuffer(int initialCapacity, int maxCapacity) {
        return wrapped.directBuffer(initialCapacity, maxCapacity).order(ByteOrder.LITTLE_ENDIAN);
    }

    @Override
    public boolean isDirectBufferPooled() {
        return false;
    }
}
