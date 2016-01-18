package org.ascnet.leaftown.provider.text;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Enumeration;
import java.util.Locale;
import java.util.ResourceBundle;

public class ServerMessages extends ResourceBundle 
{	
	private static ServerMessages instance = new ServerMessages();
	
	public static ServerMessages getInstance()
	{
		return instance;
	}
	
	public void load(Locale locale)
	{
		File file = null;
		
		if(locale.getCountry() == "BR")
			file = new File("langs/");	
		else if(locale == Locale.ENGLISH)
			file = new File("langs/");
		
		try 
		{
			URL[] urls = {file.toURI().toURL()};
			
            setParent(getBundle("ServerMessages", locale, new URLClassLoader(urls), new UTF8Control()));
		} 
		catch (MalformedURLException e)
		{
			e.printStackTrace();
		} 
	}
	
	@Override
	public Enumeration<String> getKeys() 
	{
		return null;
	}

	@Override
	protected Object handleGetObject(String key) 
	{
		return null;
	}
}
