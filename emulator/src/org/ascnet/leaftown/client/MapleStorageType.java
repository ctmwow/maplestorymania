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

public enum MapleStorageType 
{
    EQUIP(0x01),
    USE(0x02),
    SETUP(0x03),
    ETC(0x04);

    final byte type;

    private MapleStorageType(int type) 
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

    public static MapleStorageType getByType(byte type) 
    {
        for (MapleStorageType l : MapleStorageType.values())
        {
            if (l.type == type) 
                return l;
        }
        return null;
    }
}