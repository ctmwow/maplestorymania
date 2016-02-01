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

package org.ascnet.leaftown.server.movement;

import org.ascnet.leaftown.tools.data.output.LittleEndianWriter;

import java.awt.Point;

public class TeleportMovement extends AbsoluteLifeMovement 
{
    public TeleportMovement(int type, Point position, int newstate) 
    {
        super(type, position, 0x00, newstate);
    }

    @Override
    public void serialize(LittleEndianWriter lew) 
    {
        lew.write(getType());
        lew.writeShort(getPosition().x);
        lew.writeShort(getPosition().y);
        lew.writeShort(getPixelsPerSecond().x);
        lew.writeShort(getPixelsPerSecond().y);
        lew.write(getNewstate());
    }
}