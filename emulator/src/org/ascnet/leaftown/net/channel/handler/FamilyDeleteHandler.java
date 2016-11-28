package org.ascnet.leaftown.net.channel.handler;

import org.ascnet.leaftown.client.MapleClient;
import org.ascnet.leaftown.client.MapleFamily;
import org.ascnet.leaftown.client.MapleFamily.FCOp;
import org.ascnet.leaftown.net.AbstractMaplePacketHandler;
import org.ascnet.leaftown.net.world.WorldRegistryImpl;
import org.ascnet.leaftown.tools.MaplePacketCreator;
import org.ascnet.leaftown.tools.data.input.SeekableLittleEndianAccessor;

public final class FamilyDeleteHandler extends AbstractMaplePacketHandler 
{
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(FamilyDeleteHandler.class);
    
    public final void handlePacket(final SeekableLittleEndianAccessor slea, final MapleClient c) 
    { 
    	final int charId = (int) slea.readShort();
    	
    	try 
    	{
    		final MapleFamily family = WorldRegistryImpl.getInstance().getFamily(c.getPlayer().getMapleFamily().getId());
    		
    		if(family.getMFC(charId) == null)
    		{ 
    			WorldRegistryImpl.getInstance().updateFamily(family);
    			family.broadcast(null, 0x00000, FCOp.UPDATE, null); 
    		}
    		else
    		{
        		family.splitFamily(charId, family.getMFC(charId));
    		}
    		
        	c.sendPacket(MaplePacketCreator.getFamilyInfo(family.getMFC(c.getPlayer().getId())));
        	c.sendPacket(MaplePacketCreator.getFamilyPedigree(family.getMFC(c.getPlayer().getId())));
		}
    	catch (Exception e) 
    	{
			log.error("Não foi possível processar o pacote FamilyDelete", e);
		}
    }
}
