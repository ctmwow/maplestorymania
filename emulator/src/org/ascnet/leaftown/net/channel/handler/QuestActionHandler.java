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

import org.ascnet.leaftown.client.MapleCharacter;
import org.ascnet.leaftown.client.MapleClient;
import org.ascnet.leaftown.client.MapleQuestStatus.Status;
import org.ascnet.leaftown.net.AbstractMaplePacketHandler;
import org.ascnet.leaftown.scripting.quest.QuestScriptManager;
import org.ascnet.leaftown.server.quest.MapleQuest;
import org.ascnet.leaftown.tools.data.input.SeekableLittleEndianAccessor;

/**
 * @author Matze
 */
public class QuestActionHandler extends AbstractMaplePacketHandler 
{
    public void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) 
    {
        final byte action = slea.readByte();
        final short quest = slea.readShort();
        final MapleCharacter player = c.getPlayer();
        if (action == 0) 
        {
            slea.skip(4);
            final int itemId = slea.readInt();
            if (player.getQuest(quest).getStatus().equals(Status.STARTED))
                MapleQuest.getInstance(quest).recoverItem(player, itemId);
        } 
        else if (action == 1) 
        { // start quest
            final int npc = slea.readInt(); 
            MapleQuest.getInstance(quest).start(player, npc);
        } 
        else if (action == 2) 
        { // complete quest
            if (player.getQuest(quest).getStatus().equals(Status.STARTED)) 
            {
                final int npc = slea.readInt();
                slea.readInt();
                if (slea.available() >= 0x02) 
                { 
                    final int selection = slea.readShort();
                    MapleQuest.getInstance(quest).complete(player, npc, selection, true, true); //first true to show quest completion effect
                }
                else 
                {
                    MapleQuest.getInstance(quest).complete(player, npc);
                }
            }
        } 
        else if (action == 3) 
        { // forfeit quest
            MapleQuest.getInstance(quest).forfeit(player);
        }
        else if (action == 4) 
        { // scripted start quest
            final int npc = slea.readInt();
            //slea.readInt();
            if (player.getQuest(quest).getStatus().equals(Status.NOT_STARTED))
                QuestScriptManager.getInstance().start(c, npc, quest);
        }
        else if (action == 5)
        { // scripted end quests
            final int npc = slea.readInt();
            //slea.readInt();
            if (player.getQuest(quest).getStatus().equals(Status.STARTED))
                QuestScriptManager.getInstance().end(c, npc, quest);
        }
    }
}