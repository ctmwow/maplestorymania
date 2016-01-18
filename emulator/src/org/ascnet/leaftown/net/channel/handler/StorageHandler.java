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
import org.ascnet.leaftown.client.MapleStorage;
import org.ascnet.leaftown.client.MapleClient;
import org.ascnet.leaftown.client.MapleInventoryType;
import org.ascnet.leaftown.net.AbstractMaplePacketHandler;
import org.ascnet.leaftown.server.MapleInventoryManipulator;
import org.ascnet.leaftown.server.MapleItemInformationProvider;
import org.ascnet.leaftown.tools.MaplePacketCreator;
import org.ascnet.leaftown.tools.data.input.SeekableLittleEndianAccessor;

import java.util.Collections;

/**
 * @author Matze
 */
public class StorageHandler extends AbstractMaplePacketHandler 
{
    public void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) 
    {
        final MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
        final byte mode = slea.readByte();
        final MapleStorage storage = c.getPlayer().getStorage();
        
        if (mode == 0x04) // take out 
        { 
            final byte type = slea.readByte();
            byte slot = slea.readByte();
            slot = storage.getSlot(MapleInventoryType.getByType(type), slot);
            final IItem item = storage.itemAt(slot);
            if (item != null)
            {
                final boolean canHold = MapleInventoryManipulator.canHold(c, Collections.singletonList(item));
                if (canHold && (!(ii.isDropRestricted(item.getItemId()) || ii.canHaveOnlyOne(item.getItemId())) || !c.getPlayer().haveItem(item.getItemId(), 1, true, false))) 
                {
                    storage.takeOut(slot);
                    storage.sendTakenOut(c, ii.getInventoryType(item.getItemId()));
                    c.sendPacket(MaplePacketCreator.modifyInventory(true, MapleInventoryManipulator.addByItem(c, item, "Taken out from storage by " + c.getPlayer().getName(), false)));
                } 
                else if (ii.canHaveOnlyOne(item.getItemId()) && c.getPlayer().haveItem(item.getItemId(), 1, true, false)) 
                    c.sendPacket(MaplePacketCreator.serverNotice(0x01, "You can not take out this item because this item is one-of-a-kind."));
                else 
                    c.sendPacket(MaplePacketCreator.serverNotice(0x01, "Your inventory is full"));
            } 
            else
                c.disconnect();
        } 
        else if (mode == 0x05)// store 
        { 
            final short slot = slea.readShort();
            final int itemId = slea.readInt();
            short quantity = slea.readShort();
            
            if (quantity < 0x01)
            {
                c.disconnect();
                return;
            }
            if (storage.isFull())
            {
                c.sendPacket(MaplePacketCreator.getStorageFull());
                return;
            }
            if (c.getPlayer().getMeso() < 100) 
                c.sendPacket(MaplePacketCreator.serverNotice(1, "You don't have enough mesos to store the item"));
            else if (itemId == 4001168) 
                c.sendPacket(MaplePacketCreator.serverNotice(1, "You cannot store this item."));
            else 
            {
                final MapleInventoryType type = ii.getInventoryType(itemId);
                final IItem item = c.getPlayer().getInventory(type).getItem(slot).copy();
                
                if (item.getItemId() == itemId && (item.getQuantity() >= quantity || ii.isThrowingStar(itemId) || ii.isShootingBullet(itemId)))
                {
                    if (ii.isThrowingStar(itemId) || ii.isShootingBullet(itemId))
                        quantity = item.getQuantity();
                    if (!c.getPlayer().haveItem(itemId, quantity, true, false))
                        return;
                    
                    item.log("Stored by " + c.getPlayer().getName(), false);
                    c.getPlayer().gainMeso(-100, false, true, false);
                    MapleInventoryManipulator.removeFromSlot(c, type, slot, quantity, false);
                    item.setQuantity(quantity);
                    storage.store(item);
                    storage.sendStored(c, ii.getInventoryType(itemId));
                } 
                else 
                    c.disconnect();
            }
        } /* else if (mode == 6) { // TODO Storage Arrange Items }*/
        else if (mode == 0x07) // meso
        { 
            int meso = slea.readInt();
            final int storageMesos = storage.getMeso();
            final int playerMesos = c.getPlayer().getMeso();
            if (meso > 0 && storageMesos >= meso || meso < 0x00 && playerMesos >= -meso) 
            {
                if (meso < 0 && storageMesos - meso < 0x00) // storing with overflow
                { 
                    meso = -(Integer.MAX_VALUE - storageMesos);
                    if (-meso > playerMesos) // should never happen just a failsafe
                        throw new RuntimeException("everything sucks");
                }
                else if (meso > 0 && playerMesos + meso < 0) // taking out with overflow
                { 
                    meso = Integer.MAX_VALUE - playerMesos;
                    if (meso > storageMesos)  // should never happen just a failsafe
                        throw new RuntimeException("everything sucks");
                }
                storage.setMeso(storageMesos - meso);
                c.getPlayer().gainMeso(meso, false, true, false);
            } 
            else 
            {
                c.disconnect();
                return;
            }
            storage.sendMeso(c);
        }
        else if (mode == 0x08)// close
            storage.close();
    }
}