package org.ascnet.leaftown.net.channel.handler;

import org.ascnet.leaftown.client.MapleCharacter;
import org.ascnet.leaftown.client.MapleClient;
import org.ascnet.leaftown.net.AbstractMaplePacketHandler;
import org.ascnet.leaftown.tools.MaplePacketCreator;
import org.ascnet.leaftown.tools.data.input.SeekableLittleEndianAccessor;

public final class FamilyAddHandler extends AbstractMaplePacketHandler 
{
    public final void handlePacket(final SeekableLittleEndianAccessor slea, final MapleClient c) 
    {
        final String toAdd = slea.readMapleAsciiString();
        final MapleCharacter addChr = c.getChannelServer().getPlayerStorage().getCharacterByName(toAdd);
        
        if (addChr == null) 
            c.getPlayer().dropMessage(0x01, "The name you requested is incorrect or he/she is currently not logged in.");
        else if (addChr.getMapleFamilyId() == c.getPlayer().getMapleFamilyId() && addChr.getMapleFamilyId() > 0x00) 
            c.getPlayer().dropMessage(0x01, "You belong to the same family.");
        else if (addChr.getMapId() != c.getPlayer().getMapId()) 
            c.getPlayer().dropMessage(0x01, "The one you wish to add as a junior must be in the same map.");
        else if (addChr.getMapleFamily() != null && addChr.getMapleFamily().getMFC(addChr.getId()).getSenior() != 0x00) 
            c.getPlayer().dropMessage(0x01, "The character is already a junior of another character.");
        else if (addChr.getLevel() >= c.getPlayer().getLevel()) 
            c.getPlayer().dropMessage(0x01, "The junior you wish to add must be at a lower rank.");
        else if (addChr.getLevel() < c.getPlayer().getLevel() - 0x14) 
            c.getPlayer().dropMessage(0x01, "The gap between you and your junior must be within 20 levels.");
        else if (addChr.getLevel() < 0x0A) 
            c.getPlayer().dropMessage(0x01, "The junior you wish to add must be over Level 10.");
        else if (c.getPlayer().getMapleFamily() != null && c.getPlayer().getMapleFamily().getMFC(c.getPlayer().getId()).getJunior1() > 0x00 && c.getPlayer().getMapleFamily().getMFC(c.getPlayer().getId()).getJunior2() > 0x00) 
            c.getPlayer().dropMessage(0x01, "You have 2 juniors already.");
        else if (c.getPlayer().isGM() || !addChr.isGM()) 
        	addChr.getClient().sendPacket(MaplePacketCreator.sendFamilyInvite(c.getPlayer()));
        
        c.sendPacket(MaplePacketCreator.enableActions());
    }
}
