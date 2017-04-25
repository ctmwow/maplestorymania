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

import org.ascnet.leaftown.client.MapleClient;
import org.ascnet.leaftown.net.AbstractMaplePacketHandler;
import org.ascnet.leaftown.server.life.MapleMonster;
import org.ascnet.leaftown.server.maps.MapleMap;
import org.ascnet.leaftown.tools.MaplePacketCreator;
import org.ascnet.leaftown.tools.data.input.SeekableLittleEndianAccessor;

/**
 * @author iamSTEVE
 */
public class FriendlyMobDamagedHandler extends AbstractMaplePacketHandler {

    @Override
    public void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
    	int oid1 = slea.readInt(); //Id of mob that got attacked?
        @SuppressWarnings("unused")
        int randomshit = slea.readInt(); //Dunno
        int oid2 = slea.readInt(); //Oid of mob that attacked?
        MapleMap map = c.getPlayer().getMap();
        MapleMonster attacked;
        MapleMonster attacker;
        try {
            attacked = map.getMonsterByOid(oid2);
            attacker = map.getMonsterByOid(oid1);
        } catch (NullPointerException npe) {
            return;
        }
        if (attacker == null || attacked == null) return;
                if (attacker.getId() == attacked.getId()) return;
        int dmg = attacker.getLevel() * 9;
        if (attacker.getLevel() > 50) {
            dmg *= 2;
        }
        if (attacked.getId() == 9300102) {
            dmg /= 2.1;
       }
        map.addBunnyHit();
        attacked.damage(c.getPlayer(), dmg, true);
        attacked.getMap().broadcastMessage(MaplePacketCreator.MobDamageMobFriendly(attacked, dmg));
    }
}