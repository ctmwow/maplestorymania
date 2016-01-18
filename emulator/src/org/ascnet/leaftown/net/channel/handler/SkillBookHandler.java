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

import org.ascnet.leaftown.client.IItem;
import org.ascnet.leaftown.client.ISkill;
import org.ascnet.leaftown.client.MapleCharacter;
import org.ascnet.leaftown.client.MapleClient;
import org.ascnet.leaftown.client.MapleInventoryType;
import org.ascnet.leaftown.client.SkillFactory;
import org.ascnet.leaftown.net.AbstractMaplePacketHandler;
import org.ascnet.leaftown.server.MapleInventoryManipulator;
import org.ascnet.leaftown.server.MapleItemInformationProvider;
import org.ascnet.leaftown.tools.MaplePacketCreator;
import org.ascnet.leaftown.tools.data.input.SeekableLittleEndianAccessor;

import java.util.Map;

public class SkillBookHandler extends AbstractMaplePacketHandler {

    public void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        if (!c.getPlayer().isAlive()) {
            c.sendPacket(MaplePacketCreator.enableActions());
            return;
        }
        slea.readInt();
        final short slot = slea.readShort();
        final int itemId = slea.readInt();
        final MapleCharacter player = c.getPlayer();
        final IItem toUse = c.getPlayer().getInventory(MapleInventoryType.USE).getItem(slot);

        if (toUse != null && toUse.getQuantity() == 1) {
            if (toUse.getItemId() != itemId) {
                return;
            }
            final MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
            final Map<String, Integer> skilldata = ii.getSkillStats(toUse.getItemId(), c.getPlayer().getJob().getId());

            boolean canuse = false;
            boolean success = false;
            final int skill = 0;
            final int maxlevel = 0;
            if (skilldata.isEmpty()) { // Hacking or used an unknown item
                return;
            }
            if (skilldata.get("skillid") == 0) { // Wrong Job
                canuse = false;
            } else if (player.getSkillLevel(SkillFactory.getSkill(skilldata.get("skillid"))) >= skilldata.get("reqSkillLevel") && player.getMasterLevel(SkillFactory.getSkill(skilldata.get("skillid"))) < skilldata.get("masterLevel")) {
                canuse = true;
                final int random = (int) Math.floor(Math.random() * 100) + 1;
                if (random <= skilldata.get("success") && skilldata.get("success") != 0) {
                    success = true;
                    final ISkill skill2 = SkillFactory.getSkill(skilldata.get("skillid"));
                    final int curlevel = player.getSkillLevel(skill2);
                    final int masterLevel = skilldata.get("masterLevel");
                    player.changeSkillLevel(skill2, curlevel, masterLevel);
                    if (masterLevel == 20) {
                        player.finishAchievement(46);
                    } else if (masterLevel == 30) {
                        player.finishAchievement(47);
                    }
                    if (itemId == 2290096) { // MW20
                        player.finishAchievement(48);
                    }
                } else {
                    success = false;
                }
                MapleInventoryManipulator.removeFromSlot(c, MapleInventoryType.USE, slot, (short) 1, false);
            } else { // Failed to meet skill requirements
                canuse = false;
            }
            c.getPlayer().getMap().broadcastMessage(MaplePacketCreator.skillBookSuccess(player, skill, maxlevel, canuse, success));
        }
    }
}