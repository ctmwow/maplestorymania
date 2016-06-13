/*
 * This file is part of AscNet Leaftown.
 * Copyright (C) 2014 Ascension Network
 *
 * AscNet Leaftown is a fork of the OdinMS MapleStory Server.
 * The following is the original copyright notice:
 *
 *     This file is part of the OdinMS Maple Story Server
 *     Copyright (C) 2008 Patrick Huy <patrick.huy@frz.cc>
 *                        Matthias Butz <matze@odinms.de>
 *                        Jan Christian Meyer <vimes@odinms.de>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License version 3
 * as published by the Free Software Foundation. You may not use, modify
 * or distribute this program under any other version of the
 * GNU Affero General Public License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.ascnet.leaftown.tools.data.output;

import java.awt.Point;
import java.nio.charset.Charset;

/**
 * Provides a generic writer of a little-endian sequence of bytes.
 *
 * @author Frz
 * @version 1.0
 * @since Revision 323
 */
public class GenericLittleEndianWriter implements LittleEndianWriter 
{
    @SuppressWarnings("unused")
	private static final Charset ASCII = Charset.forName("US-ASCII");
    private static final Charset UTF8 = Charset.forName("UTF-8");
    private ByteOutputStream bos;

    protected GenericLittleEndianWriter() 
    {
    }

    /**
     * Sets the byte-output stream for this instance of the object.
     *
     * @param bos The new output stream to set.
     */
    protected void setByteOutputStream(ByteOutputStream bos) 
    {
        this.bos = bos;
    }

    /**
     * Class constructor - only this one can be used.
     *
     * @param bos The stream to wrap this objecr around.
     */
    public GenericLittleEndianWriter(ByteOutputStream bos) 
    {
        this.bos = bos;
    }

    /**
     * Write an array of bytes to the stream.
     *
     * @param b The bytes to write.
     */
    @Override
    public void write(byte[] b) 
    {
        for (byte element : b) 
        {
            bos.writeByte(element);
        }
    }

    /**
     * Write a byte to the stream.
     *
     * @param b The byte to write.
     */
    @Override
    public void write(byte b) 
    {
        bos.writeByte(b);
    }

    /**
     * Write a byte in integer form to the stream.
     *
     * @param b The byte as an <code>Integer</code> to write.
     */
    @Override
    public void write(int b) 
    {
        bos.writeByte((byte) b);
    }

    /**
     * Write a short integer to the stream.
     *
     * @param i The short integer to write.
     */
    @Override
    public void writeShort(int i) 
    {
        bos.writeByte((byte) (i & 0xFF));
        bos.writeByte((byte) (i >>> 0x08 & 0xFF));
    }

    /**
     * Writes an integer to the stream.
     *
     * @param i The integer to write.
     */
    @Override
    public void writeInt(int i) 
    {
        bos.writeByte((byte) (i & 0xFF));
        bos.writeByte((byte) (i >>> 0x08 & 0xFF));
        bos.writeByte((byte) (i >>> 0x10 & 0xFF));
        bos.writeByte((byte) (i >>> 0x18 & 0xFF));
    }

    /**
     * Writes an ASCII string the the stream.
     *
     * @param s The ASCII string to write.
     */
    @Override
    public void writeAsciiString(String s)
    {
        write(s.getBytes(UTF8));
    }

    /**
     * Writes a maple-convention ASCII string to the stream.
     *
     * @param s The ASCII string to use maple-convention to write.
     */
    @Override
    public void writeMapleAsciiString(String s)
    {
        writeShort((short) s.length());
        writeAsciiString(s);
    }

    public void writeMapleNameString(String s)
    {
        if (s.length() > 0x0C) 
            s = s.substring(0, 0x0C);

        writeAsciiString(s);
        
        for (int x = s.length(); x < 0x0C; x++)
            write(0x00);
    }

    /**
     * Writes a null-terminated ASCII string to the stream.
     *
     * @param s The ASCII string to write.
     */
    @Override
    public void writeNullTerminatedAsciiString(String s) 
    {
        writeAsciiString(s);
        write(0x00);
    }

    /**
     * Write a long integer to the stream.
     *
     * @param l The long integer to write.
     */
    @Override
    public void writeLong(long l) 
    {
        bos.writeByte((byte) (l & 0xFF));
        bos.writeByte((byte) (l >>> 0x08 & 0xFF));
        bos.writeByte((byte) (l >>> 0x10 & 0xFF));
        bos.writeByte((byte) (l >>> 0x18 & 0xFF));
        bos.writeByte((byte) (l >>> 0x20 & 0xFF));
        bos.writeByte((byte) (l >>> 0x28 & 0xFF));
        bos.writeByte((byte) (l >>> 0x30 & 0xFF));
        bos.writeByte((byte) (l >>> 0x38 & 0xFF));
    }
    
    public void skip(int b) 
    {
        write(new byte[b]);
    }
    
    public void writePos(Point s) 
    {
        writeShort(s.x);
        writeShort(s.y);
    }
}