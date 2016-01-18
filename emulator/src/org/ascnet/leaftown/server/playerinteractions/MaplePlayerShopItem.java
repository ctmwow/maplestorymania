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

import org.ascnet.leaftown.client.IItem;

/**
 * @author Matze
 */
public class MaplePlayerShopItem {

    private final IItem item;
    private short bundles;
    private final int price;
    private int id;

    public MaplePlayerShopItem(IItem item, short bundles, int price) {
        this.item = item;
        this.bundles = bundles;
        this.price = price;
    }

    public IItem getItem() {
        return item;
    }

    public short getBundles() {
        return bundles;
    }

    public int getPrice() {
        return price;
    }

    public void setBundles(short bundles) {
        this.bundles = bundles;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }
}