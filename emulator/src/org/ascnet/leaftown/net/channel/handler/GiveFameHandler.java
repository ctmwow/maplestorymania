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
import org.ascnet.leaftown.client.MapleStat;
import org.ascnet.leaftown.client.anticheat.CheatingOffense;
import org.ascnet.leaftown.net.AbstractMaplePacketHandler;
import org.ascnet.leaftown.tools.MaplePacketCreator;
import org.ascnet.leaftown.tools.data.input.SeekableLittleEndianAccessor;

public class GiveFameHandler extends AbstractMaplePacketHandler {

    @Override
    public void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        int who = slea.readInt();
        int mode = slea.readByte();

        int famechange = mode == 0 ? -1 : 1;
        MapleCharacter target = (MapleCharacter) c.getPlayer().getMap().getMapObject(who);

        if (target == c.getPlayer()) { // faming self
            c.getPlayer().getCheatTracker().registerOffense(CheatingOffense.FAMING_SELF);
            return;
        } else if (target == null || target.hasGMLevel(2) && !c.getPlayer().hasGMLevel(2)) {
            return;
        } else if (c.getPlayer().getLevel() < 15) {
            c.getPlayer().getCheatTracker().registerOffense(CheatingOffense.FAMING_UNDER_15);
            return;
        }
        switch (c.getPlayer().canGiveFame(target)) {
            case OK:
                if (Math.abs(target.getFame() + famechange) < 30001) {
                    target.addFame(famechange);
                    target.updateSingleStat(MapleStat.FAME, target.getFame());
                }
                if (target.getFame() >= 50) {
                    target.finishAchievement(9);
                }
                if (!c.getPlayer().hasGMLevel(2))
                    c.getPlayer().hasGivenFame(target);
                c.sendPacket(MaplePacketCreator.giveFameResponse(mode, target.getName(), target.getFame()));
                target.getClient().sendPacket(MaplePacketCreator.receiveFame(mode, c.getPlayer().getName()));
                break;
            case NOT_TODAY:
                c.sendPacket(MaplePacketCreator.giveFameErrorResponse(3));
                break;
            case NOT_THIS_MONTH:
                c.sendPacket(MaplePacketCreator.giveFameErrorResponse(4));
                break;
        }
    }
}