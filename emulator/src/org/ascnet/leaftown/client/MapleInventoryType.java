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

package org.ascnet.leaftown.client;

/**
 * @author Matze
 */
public enum MapleInventoryType 
{
    UNDEFINED(0x00000000),
    EQUIP(0x00000001),
    USE(0x00000002),
    SETUP(0x00000003),
    ETC(0x00000004),
    CASH(0x00000005),
    EQUIPPED(-0x00000001);

    final byte type;

    private MapleInventoryType(int type) 
    {
        this.type = (byte) type;
    }

    public byte getType()
    {
        return type;
    }

    public short getBitfieldEncoding()
    {
        return (short) (0x02 << type);
    }

    public static MapleInventoryType getByType(byte type) 
    {
        for (MapleInventoryType l : MapleInventoryType.values())
        {
            if (l.type == type) 
                return l;
        }
        return null;
    }

    public static MapleInventoryType getByWZName(String name) 
    {
        switch (name) 
        {
            case "Install":
                return SETUP;
            case "Consume":
                return USE;
            case "Etc":
                return ETC;
            case "Cash":
                return CASH;
            case "Pet":
                return CASH;
        }
        return UNDEFINED;
    }
}