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

import org.ascnet.leaftown.client.ISkill;
import org.ascnet.leaftown.client.MapleClient;
import org.ascnet.leaftown.client.SkillFactory;
import org.ascnet.leaftown.client.anticheat.CheatingOffense;
import org.ascnet.leaftown.net.AbstractMaplePacketHandler;
import org.ascnet.leaftown.tools.MaplePacketCreator;
import org.ascnet.leaftown.tools.data.input.SeekableLittleEndianAccessor;

public class CancelBuffHandler extends AbstractMaplePacketHandler {

    @Override
    public void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        final int sourceid = slea.readInt();
        final ISkill skill = SkillFactory.getSkill(sourceid);
        if (skill != null) {
            if (skill.hasCharge()) {
                c.getPlayer().getMap().broadcastMessage(c.getPlayer(), MaplePacketCreator.skillCancel(c.getPlayer(), sourceid), false);
                if (skill.isKeyDownAttack()) {
                    c.getPlayer().addAttackCount();
                    if (c.getPlayer().getAttackCount() > 100) {
                        c.getPlayer().getCheatTracker().registerOffense(CheatingOffense.UNLIMITED_ATTACK);
                    }
                }
            }
            c.getPlayer().cancelEffect(skill.getEffect(1), false, -1);
        }
    }
}