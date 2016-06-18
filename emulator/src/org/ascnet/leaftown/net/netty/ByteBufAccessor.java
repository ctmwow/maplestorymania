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

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.ascnet.leaftown.tools.data.input.SeekableLittleEndianAccessor;

import java.nio.ByteOrder;
import java.nio.charset.Charset;

public class ByteBufAccessor implements SeekableLittleEndianAccessor {

    private final ByteBuf wrapped;

    public ByteBufAccessor(ByteBuf wrapped) {
        this.wrapped = wrapped;
    }

    @Override
    public void seek(long offset) {
        wrapped.readerIndex((int)offset);
    }

    @Override
    public long getPosition() {
        return wrapped.readerIndex();
    }

    @Override
    public byte readByte() {
        return wrapped.readByte();
    }

    @Override
    public char readChar() {
        return wrapped.readChar();
    }

    @Override
    public short readShort() {
        return wrapped.readShort();
    }

    @Override
    public int readInt() {
        return wrapped.readInt();
    }

    @Override
    public long readLong() {
        return wrapped.readLong();
    }

    @Override
    public void skip(int num) {
        wrapped.skipBytes(num);
    }

    @Override
    public byte[] read(int num) {
        byte[] ret = new byte[num];
        wrapped.readBytes(ret);
        return ret;
    }

    @Override
    public float readFloat() {
        return wrapped.readFloat();
    }

    @Override
    public double readDouble() {
        return wrapped.readDouble();
    }

    @Override
    public String readAsciiString(int n) {
        byte[] string = new byte[n];
        
        for(int x = 0; x < n; ++x)
            string[x] = readByte();
        
        return new String(string, Charset.forName("UTF-8")); 
    }
    @Override
    public byte[] readMapleAsciiStringByte() {
        return read(readShort());
    }
    @Override
    public String readNullTerminatedAsciiString() {
        ByteBuf buf = Unpooled.directBuffer().order(ByteOrder.LITTLE_ENDIAN);
        byte b;
        while ((b = readByte()) != 0)
            buf.writeByte(b);
        byte[] bytes = buf.array();
        char[] string = new char[bytes.length];
        for(int x = 0; x < string.length; ++x)
            string[x] = (char)bytes[x];
        return String.valueOf(string);
    }

    @Override
    public String readMapleAsciiString() {
        return readAsciiString(readShort());
    }

    @Override
    public long getBytesRead() {
        return wrapped.readerIndex();
    }

    @Override
    public long available() {
        return wrapped.readableBytes();
    }
}
