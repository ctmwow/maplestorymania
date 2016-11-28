package org.ascnet.leaftown.net.channel.handler;

import java.rmi.RemoteException;

import org.ascnet.leaftown.client.MapleClient;
import org.ascnet.leaftown.client.MapleFamily;
import org.ascnet.leaftown.client.MapleFamily.FCOp;
import org.ascnet.leaftown.net.AbstractMaplePacketHandler;
import org.ascnet.leaftown.tools.MaplePacketCreator;
import org.ascnet.leaftown.tools.data.input.SeekableLittleEndianAccessor;

public final class SetFamilyMessageHandler extends AbstractMaplePacketHandler
{
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(SetFamilyMessageHandler.class);
    
    @Override
    public final void handlePacket(final SeekableLittleEndianAccessor slea, final MapleClient c) 
    {
        MapleFamily fam;
		try 
		{
			fam = c.getChannelServer().getWorldInterface().getFamily(c.getPlayer().getMapleFamily().getId());
	        fam.setNotice(slea.readMapleAsciiString()); 
	        fam.writeToDB(false);
	        fam.broadcast(null, 0x000000, FCOp.UPDATE, null);
	        
	        c.sendPacket(MaplePacketCreator.getFamilyInfo(fam.getMFC(c.getPlayer().getId())));
		} 
		catch (RemoteException e)
		{
			log.error("Não foi possível processar o pacote SetFamilyMessage", e);
		}
    }
}