package org.ascnet.leaftown.tools;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import org.mindrot.jbcrypt.BCrypt;

public class PasswordGenerator 
{
	public static void main(String[] args)
	{
		System.out.println(BCrypt.hashpw("stki5u", BCrypt.gensalt()));
	}
}
