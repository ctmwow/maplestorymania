package org.ascnet.leaftown.net.channel.handler;

import java.rmi.RemoteException;

import org.ascnet.leaftown.client.MapleClient;
import org.ascnet.leaftown.client.MapleFamily;
import org.ascnet.leaftown.net.AbstractMaplePacketHandler;
import org.ascnet.leaftown.tools.MaplePacketCreator;
import org.ascnet.leaftown.tools.data.input.SeekableLittleEndianAccessor;

public final class OpenFamilyHandler extends AbstractMaplePacketHandler 
{
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(OpenFamilyHandler.class);
    
    public final void handlePacket(final SeekableLittleEndianAccessor slea, final MapleClient c) 
    {
    	try 
    	{
    		MapleFamily characterFamily = c.getPlayer().getMapleFamily();
    		
    		if(characterFamily != null)
    		{
    			characterFamily = c.getChannelServer().getWorldInterface().getFamily(characterFamily.getId());
            	c.sendPacket(MaplePacketCreator.getFamilyInfo(characterFamily.getMFC(c.getPlayer().getId())));
    		}
    	}
    	catch(RemoteException rme)
    	{
    		log.error("Remote Connection to the World cannot be estabilized!", rme);
    		c.getPlayer().dropMessage("Não foi possível carregar as informações sobre a sua Família. Por favor, tente novamente mais tarde!");
    	}
    }
}