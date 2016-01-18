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
import org.ascnet.leaftown.client.MapleInventory;
import org.ascnet.leaftown.client.MapleInventoryType;
import org.ascnet.leaftown.net.AbstractMaplePacketHandler;
import org.ascnet.leaftown.server.MapleInventoryManipulator;
import org.ascnet.leaftown.tools.MaplePacketCreator;
import org.ascnet.leaftown.tools.Pair;
import org.ascnet.leaftown.tools.data.input.SeekableLittleEndianAccessor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Xterminator
 */
public class ItemSortHandler extends AbstractMaplePacketHandler {

    @Override
    public void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        final int action = slea.readInt();
        final byte invTypeId = slea.readByte();
        if (invTypeId < 1 || invTypeId > 5)
            return;
        if (action <= c.getLastAction()) {
            c.sendPacket(MaplePacketCreator.itemSortComplete(false, invTypeId));
            return;
        }
        c.setLastAction(action);
        final MapleInventoryType invType = MapleInventoryType.getByType(invTypeId);
        final MapleInventory inv = c.getPlayer().getInventory(invType);
        final List<Integer> items = new ArrayList<>();
        for (IItem item : inv.list()) {
            if (!items.contains(item.getItemId()))
                items.add(item.getItemId());
        }
        if (items.size() > 1) {
            Collections.sort(items);
        }
        short currentSlot = 1;
        final List<Pair<Short, IItem>> allChanges = new ArrayList<>();
        for (int itemId : items) {
            for (IItem item : inv.listById(itemId)) {
                if (item.getPosition() != currentSlot) {
                    allChanges.addAll(MapleInventoryManipulator.move(c, invType, item.getPosition(), currentSlot));
                }
                currentSlot++;
            }
        }
        c.sendPacket(MaplePacketCreator.modifyInventory(true, allChanges));
        c.sendPacket(MaplePacketCreator.itemSortComplete(!allChanges.isEmpty(), invType.getType()));
    }
}