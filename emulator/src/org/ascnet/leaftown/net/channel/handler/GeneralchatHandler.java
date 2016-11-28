/*
 * This file is part of AscNet Leaftown.
 * Copyright (C) 2014 Ascension Network
 *
 * AscNet Leaftown is a fork of the OdinMS MapleStory Server.
 * The following is the original copyright notice:
 *
 *     This file is part of the OdinMS Maple Story Server
 *     Copyright (C) 2008 Patrick Huy <patrick.huy@frz.cc>
 *                        Matthias Butz <matze@odinms.de>
 *                        Jan Christian Meyer <vimes@odinms.de>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License version 3
 * as published by the Free Software Foundation. You may not use, modify
 * or distribute this program under any other version of the
 * GNU Affero General Public License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.ascnet.leaftown.net.channel.handler;

import java.nio.charset.Charset;

import org.ascnet.leaftown.client.MapleClient;
import org.ascnet.leaftown.client.messages.CommandProcessor;
import org.ascnet.leaftown.net.AbstractMaplePacketHandler;
import org.ascnet.leaftown.tools.MaplePacketCreator;
import org.ascnet.leaftown.tools.data.input.SeekableLittleEndianAccessor;

public class GeneralchatHandler extends AbstractMaplePacketHandler 
{
    @Override
    public void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) 
    {
        final String text = slea.readMapleAsciiString();
        final int show = slea.readByte();

        if (show != 0x00000001 && c.getPlayer().getCheatTracker().textSpam(text) && !c.getPlayer().isGM()) 
        {
            c.sendPacket(MaplePacketCreator.serverNotice(0x00000005, "Você está escrevendo muito rápido. Se acalme um pouco!"));
            return;
        }
        if (text.length() > 0x00000046 && !c.getPlayer().isGM())
            return;
        if (!CommandProcessor.getInstance().processCommand(c, text)) 
        {
            if (c.getPlayer().isMuted() || c.getPlayer().getMap().getMuted() && !c.getPlayer().isGM()) 
            {
                c.getPlayer().dropMessage(0x00000005, c.getPlayer().isMuted() ? "Você está " : "O mapa está " + "silênciado, portanto, você é incapaz de falar");
                return;
            }
            
            c.getPlayer().resetAfkTimer(); 
            c.getPlayer().getMap().broadcastMessage(MaplePacketCreator.getChatText(c.getPlayer().getId(), text, c.getPlayer().isGM() && c.getChannelServer().allowGmWhiteText() && c.getPlayer().getWhiteText(), show));
            
            if (text.equalsIgnoreCase("cc plz")) 
                c.getPlayer().finishAchievement(0x0000000E);
        }
    }
}