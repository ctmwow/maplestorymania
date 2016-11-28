package org.ascnet.leaftown.net.netty;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.ascnet.leaftown.tools.data.input.SeekableLittleEndianAccessor;

import java.nio.ByteOrder;
import java.nio.charset.Charset;

public class ByteBufAccessor implements SeekableLittleEndianAccessor 
{
    private static final Charset WINDOWS_1252 = Charset.forName("WINDOWS-1252");
    private final ByteBuf wrapped;

    public ByteBufAccessor(ByteBuf wrapped) 
    {
        this.wrapped = wrapped;
    }

    @Override
    public void seek(long offset) 
    {
        wrapped.readerIndex((int)offset);
    }

    @Override
    public long getPosition() 
    {
        return wrapped.readerIndex();
    }

    @Override
    public byte readByte() 
    {
        return wrapped.readByte();
    }

    @Override
    public char readChar() 
    {
        return wrapped.readChar();
    }

    @Override
    public short readShort() 
    {
        return wrapped.readShort();
    }

    @Override
    public int readInt() 
    {
        return wrapped.readInt();
    }

    @Override
    public long readLong() 
    {
        return wrapped.readLong();
    }

    @Override
    public void skip(int num) 
    {
        wrapped.skipBytes(num);
    }

    @Override
    public byte[] read(int num) 
    {
        byte[] ret = new byte[num];
        wrapped.readBytes(ret);
        return ret;
    }

    @Override
    public float readFloat() 
    {
        return wrapped.readFloat();
    }

    @Override
    public double readDouble() 
    {
        return wrapped.readDouble();
    }

    @Override
    public String readAsciiString(int n) 
    {
        return new String(read(n), WINDOWS_1252); 
    }

    @Override
    public String readNullTerminatedAsciiString() 
    {
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
    public String readMapleAsciiString() 
    {
        return readAsciiString(readShort());
    }

    @Override
    public long getBytesRead() 
    {
        return wrapped.readerIndex();
    }

    @Override
    public long available() 
    {
        return wrapped.readableBytes();
    }
}
