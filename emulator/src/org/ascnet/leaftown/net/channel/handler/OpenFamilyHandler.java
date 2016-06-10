package org.ascnet.leaftown.net.channel.handler;

import java.rmi.RemoteException;

import org.ascnet.leaftown.client.MapleClient;
import org.ascnet.leaftown.client.MapleFamilyCharacterInfo;
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
    		MapleFamilyCharacterInfo characterInfo = c.getChannelServer().getWorldInterface().getFamily(c.getPlayer().getMapleFamilyId()).getMFC(c.getPlayer().getId());
    		
    		if(characterInfo == null)
    			characterInfo = new MapleFamilyCharacterInfo(c.getPlayer());
    		
        	c.sendPacket(MaplePacketCreator.getFamilyInfo(characterInfo));	
    	}
    	catch(RemoteException rme)
    	{
    		log.error("Remote Connection to the World cannot be estabilized!", rme);
    		c.getPlayer().dropMessage("Não foi possível carregar as informações sobre a sua Família. Por favor, tente novamente mais tarde!");
    	}
    }
}