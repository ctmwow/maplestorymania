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
import org.ascnet.leaftown.client.MapleClient;
import org.ascnet.leaftown.client.MapleInventoryType;
import org.ascnet.leaftown.client.anticheat.CheatingOffense;
import org.ascnet.leaftown.net.AbstractMaplePacketHandler;
import org.ascnet.leaftown.tools.MaplePacketCreator;
import org.ascnet.leaftown.tools.data.input.SeekableLittleEndianAccessor;

public class UseItemEffectHandler extends AbstractMaplePacketHandler {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(UseItemEffectHandler.class);

    public void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        final int itemId = slea.readInt();

        if (itemId >= 5000000 && itemId <= 5000053) {
            log.warn(slea.toString());
        }

        if (itemId != 0) {
            final IItem toUse = c.getPlayer().getInventory(MapleInventoryType.CASH).findById(itemId);
            if (toUse == null || toUse.getItemId() != itemId) {
                c.getPlayer().getCheatTracker().registerOffense(CheatingOffense.USING_UNAVAILABLE_ITEM, Integer.toString(itemId));
                return;
            }
        }

        c.getPlayer().setItemEffect(itemId);
        c.getPlayer().getMap().broadcastMessage(c.getPlayer(), MaplePacketCreator.itemEffect(c.getPlayer().getId(), itemId), false);
    }
}