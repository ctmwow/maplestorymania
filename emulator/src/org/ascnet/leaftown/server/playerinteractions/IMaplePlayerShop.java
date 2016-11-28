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

package org.ascnet.leaftown.server.playerinteractions;

import org.ascnet.leaftown.client.MapleCharacter;
import org.ascnet.leaftown.client.MapleClient;
import org.ascnet.leaftown.net.MaplePacket;
import org.ascnet.leaftown.tools.Pair;

import java.sql.SQLException;
import java.util.List;

/**
 * @author XoticStory
 */
public interface IMaplePlayerShop 
{
    public final byte HIRED_MERCHANT = 1;
    public final byte PLAYER_SHOP = 2;

    public void broadcastToVisitors(MaplePacket packet);

    public void addVisitor(MapleCharacter visitor);

    public void removeVisitor(MapleCharacter visitor);

    public int getVisitorSlot(MapleCharacter visitor);

    public void removeAllVisitors(int error, int type);

    public void buy(MapleClient c, int item, short quantity);

    public void closeShop();

    public String getOwnerName();

    public int getOwnerId();

    public String getDescription();

    public void setDescription(String desc);

    public List<Pair<Byte, MapleCharacter>> getVisitors();

    public List<MaplePlayerShopItem> getItems();

    public void addItem(MaplePlayerShopItem item) throws SQLException;

    public boolean removeItem(int item) throws SQLException;

    public void updateItem(MaplePlayerShopItem item) throws SQLException;

    public void removeFromSlot(int slot);

    public int getFreeSlot();

    public int getItemId();

    public boolean isOwner(MapleCharacter chr);

    public byte getShopType();

    public void spawned();

    public boolean isSpawned();

    public boolean isSoldOut();

    public void makeAvailableAtFred();
}