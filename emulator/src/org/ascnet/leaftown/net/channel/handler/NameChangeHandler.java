package org.ascnet.leaftown.net.channel.handler;

import org.ascnet.leaftown.client.MapleClient;
import org.ascnet.leaftown.net.AbstractMaplePacketHandler;
import org.ascnet.leaftown.tools.MaplePacketCreator;
import org.ascnet.leaftown.tools.data.input.SeekableLittleEndianAccessor;

public final class NameChangeHandler extends AbstractMaplePacketHandler
{
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(NameChangeHandler.class);
    
    @Override
    public final void handlePacket(final SeekableLittleEndianAccessor slea, final MapleClient c) 
    {
		try 
		{
	        c.sendPacket(MaplePacketCreator.sendNameChangeOperation());
		} 
		catch (Exception e)
		{
			log.error("Não foi possível processar o pacote NameChangeHandler", e);
		}
    }
}