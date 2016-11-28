package org.ascnet.leaftown.tools;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import org.mindrot.jbcrypt.BCrypt;

public class PasswordGenerator 
{
	public static void main(String[] args)
	{
		ByteBuffer bb = ByteBuffer.allocate(2);
		bb.order(ByteOrder.LITTLE_ENDIAN);
		bb.put((byte)15);
		bb.put((byte)00);
		short shortVal = bb.getShort(0);
		System.out.println(shortVal);
		//System.out.println(BCrypt.hashpw("mateus123", BCrypt.gensalt()));
	}
}
