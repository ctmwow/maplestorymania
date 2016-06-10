package org.ascnet.leaftown.net.channel.handler;

import org.ascnet.leaftown.client.MapleCharacter;
import org.ascnet.leaftown.client.MapleClient;
import org.ascnet.leaftown.net.AbstractMaplePacketHandler;
import org.ascnet.leaftown.net.channel.ChannelServer;
import org.ascnet.leaftown.tools.MaplePacketCreator;
import org.ascnet.leaftown.tools.data.input.SeekableLittleEndianAccessor;

public final class RequestFamilyHandler extends AbstractMaplePacketHandler
{
    @Override
    public final void handlePacket(final SeekableLittleEndianAccessor slea, final MapleClient c) 
    {
    	final String characterName = slea.readMapleAsciiString();

        for (ChannelServer cserv : ChannelServer.getAllInstances()) 
        {
        	final MapleCharacter inviter = cserv.getPlayerStorage().getCharacterByName(characterName);
            
            if (inviter != null)
            {
            	//inviter.getClient().sendPacket(MaplePacketCreator.sendFamilyJoinResponse(true, c.getPlayer().getName()));
            	break;
            }
        }
        System.out.println("what to do " + slea.toString());
        //c.sendPacket(MaplePacketCreator.sendFamilyMessage(0x00, 0x00));
    }
}