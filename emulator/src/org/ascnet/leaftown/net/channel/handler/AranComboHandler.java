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
import org.ascnet.leaftown.client.SkillFactory;
import org.ascnet.leaftown.client.anticheat.CheatingOffense;
import org.ascnet.leaftown.net.AbstractMaplePacketHandler;
import org.ascnet.leaftown.tools.MaplePacketCreator;
import org.ascnet.leaftown.tools.data.input.SeekableLittleEndianAccessor;

/**
 * @author Xterminator
 */
public final class AranComboHandler extends AbstractMaplePacketHandler {

    public final void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        MapleCharacter player = c.getPlayer();
        final int jobId = player.getJob().getId();
        if (jobId == 2000 || jobId >= 2100 && jobId < 2200) {
            final long cur = System.currentTimeMillis();
            short combo = player.getComboCounter();
            if (combo > 0 && cur - player.getLastComboAttack() > 5000) {
                player.getCheatTracker().registerOffense(CheatingOffense.ARAN_COMBO_HACK);
            }
            //MapleQuest quest = MapleQuest.getInstance(10335);
            if (player.getLastComboAttack() < System.currentTimeMillis() - 3000) {
                player.setComboCounter((short) 1);
            } else {
                combo++;
                player.setComboCounter(combo);
            }
            player.setLastComboAttack(cur);
            /*String comboRecord = player.getQuest(quest).getQuestRecord();
			if (comboRecord.length() == 0 || player.getComboCounter() > Integer.parseInt(comboRecord))
				quest.setQuestInfo(player, "" + player.getComboCounter(), true, false);*/
            c.sendPacket(MaplePacketCreator.showAranComboCounter(player.getComboCounter()));
            if (combo >= 10 && combo % 10 == 0 && combo <= 100)
                SkillFactory.getSkill(21000000).getEffect(combo / 10).applyComboBuff(player, combo);
        }
    }
}