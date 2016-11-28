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
            c.getPlayer().dropMessage(0x01, "O nome que você digitou está incorreto ou ele/ela não está online!");
        else if (addChr.getMapleFamilyId() == c.getPlayer().getMapleFamilyId() && addChr.getMapleFamilyId() > 0x00) 
            c.getPlayer().dropMessage(0x01, "Você pertence à mesma família.");
        else if (addChr.getMapId() != c.getPlayer().getMapId()) 
            c.getPlayer().dropMessage(0x01, "Quem você deseja adicionar como junior deve estar no mesmo mapa que você!");
        else if (addChr.getMapleFamily() != null && addChr.getMapleFamily().getMFC(addChr.getId()).getSenior() != 0x00) 
            c.getPlayer().dropMessage(0x01, "O jogador já é junior de outra pessoa.");
        else if (addChr.getLevel() >= c.getPlayer().getLevel()) 
            c.getPlayer().dropMessage(0x01, "O junior que você deseja adicionar deve ter menos level que você!");
        else if (addChr.getLevel() < c.getPlayer().getLevel() - 0x14) 
            c.getPlayer().dropMessage(0x01, "A diferença entre você e o seu junior não pode ser maior que 20 leveis!");
        else if (addChr.getLevel() < 0x0A) 
            c.getPlayer().dropMessage(0x01, "O junior que você deseja adiciona deve ser pelo menos level 10!");
        else if (c.getPlayer().getMapleFamily() != null && c.getPlayer().getMapleFamily().getMFC(c.getPlayer().getId()).getJunior1() > 0x00 && c.getPlayer().getMapleFamily().getMFC(c.getPlayer().getId()).getJunior2() > 0x00) 
            c.getPlayer().dropMessage(0x01, "Você já possui 2 juniors!");
        else if (c.getPlayer().isGM() || !addChr.isGM()) 
        	addChr.getClient().sendPacket(MaplePacketCreator.sendFamilyInvite(c.getPlayer()));
        
        c.sendPacket(MaplePacketCreator.enableActions());
    }
}
