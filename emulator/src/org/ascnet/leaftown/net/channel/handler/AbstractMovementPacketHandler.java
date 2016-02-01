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

import org.ascnet.leaftown.net.AbstractMaplePacketHandler;
import org.ascnet.leaftown.server.maps.AnimatedMapleMapObject;
import org.ascnet.leaftown.server.movement.AbsoluteLifeMovement;
import org.ascnet.leaftown.server.movement.ChangeEquipSpecialAwesome;
import org.ascnet.leaftown.server.movement.JumpDownMovement;
import org.ascnet.leaftown.server.movement.LifeMovement;
import org.ascnet.leaftown.server.movement.LifeMovementFragment;
import org.ascnet.leaftown.server.movement.RelativeLifeMovement;
import org.ascnet.leaftown.server.movement.TeleportMovement;
import org.ascnet.leaftown.tools.data.input.LittleEndianAccessor;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

public abstract class AbstractMovementPacketHandler extends AbstractMaplePacketHandler 
{
    protected List<LifeMovementFragment> parseMovement(LittleEndianAccessor lea) 
    {
        final List<LifeMovementFragment> res = new ArrayList<>();
        
        final int numCommands = lea.readByte();
        
        for (byte i = 0x00; i < numCommands; i++) 
        {
            final short command = lea.readByte();
            
            switch (command) 
            {
                case 0x00:
                case 0x05:
                case 0x11: 
                { 
                    final short xpos = lea.readShort();
                    final short ypos = lea.readShort();
                    final short xwobble = lea.readShort();
                    final short ywobble = lea.readShort();
                    final short unk = lea.readShort();
                    final byte newstate = lea.readByte();
                    final short duration = lea.readShort();
                    AbsoluteLifeMovement alm = new AbsoluteLifeMovement(command, new Point(xpos, ypos), duration, newstate);
                    alm.setUnk(unk);
                    alm.setPixelsPerSecond(new Point(xwobble, ywobble));
                    res.add(alm);
                    break;
                }
                case 0x01:
                case 0x02:
                case 0x06:
                case 0x0C:
                case 0x0D:
                case 0x10:
                case 0x12:
                case 0x13:
                case 0x14:
                case 0x16: 
                {
                    final short xpos = lea.readShort();
                    final short ypos = lea.readShort();
                    final byte newstate = lea.readByte();
                    final short duration = lea.readShort();
                    res.add(new RelativeLifeMovement(command, new Point(xpos, ypos), duration, newstate));
                    break;
                }
                case 0x03:
                case 0x04:
                case 0x07:
                case 0x08:
                case 0x09:
                case 0x0B:
                {
                	short xpos = lea.readShort();
                    short ypos = lea.readShort();
                    short xwobble = lea.readShort();
                    short ywobble = lea.readShort();
                    byte newstate = lea.readByte();
                    TeleportMovement tm = new TeleportMovement(command, new Point(xpos, ypos), newstate);
                    tm.setPixelsPerSecond(new Point(xwobble, ywobble));
                    res.add(tm);
                    break;
                }
                case 0x0A:
                { 
                    res.add(new ChangeEquipSpecialAwesome(lea.readByte()));
                    break;
                }
                case 0x0E: 
                {
                    lea.skip(0x09);
                    break;
                }
                case 0x0F:
                { 
                    short xpos = lea.readShort();
                    short ypos = lea.readShort();
                    short xwobble = lea.readShort();
                    short ywobble = lea.readShort();
                    short unk = lea.readShort();
                    short fh = lea.readShort();
                    byte newstate = lea.readByte();
                    short duration = lea.readShort();
                    JumpDownMovement jdm = new JumpDownMovement(command, new Point(xpos, ypos), duration, newstate);
                    jdm.setUnk(unk);
                    jdm.setPixelsPerSecond(new Point(xwobble, ywobble));
                    jdm.setFH(fh);
                    res.add(jdm);
                    break;
                }
                case 0x15:
                {
                    lea.skip(0x03);
                    break;
                }
                default:
                {
                	System.err.println("Unhandled AM Packet Command " + command);
                	return null;
                }
            }
        }

        return res;
    }

    protected void updatePosition(List<LifeMovementFragment> movement, AnimatedMapleMapObject target, int yoffset) 
    {
        for (LifeMovementFragment move : movement) 
        {
            if (move instanceof LifeMovement) 
            {
                if (move instanceof AbsoluteLifeMovement) 
                {
                    final Point position = move.getPosition();
                    position.y += yoffset;
                    target.setPosition(position);
                }
                target.setStance((byte) ((LifeMovement) move).getNewstate());
            }
        }
    }
}