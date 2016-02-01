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
import org.ascnet.leaftown.server.movement.LifeMovementFragment;
import org.ascnet.leaftown.tools.MaplePacketCreator;
import org.ascnet.leaftown.tools.data.input.SeekableLittleEndianAccessor;

import java.util.List;

public class MovePlayerHandler extends AbstractMovementPacketHandler 
{
    @Override
    public void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) 
    {
        slea.skip(0x05);
        
        final int oid = slea.readInt(); 
        final List<LifeMovementFragment> res = parseMovement(slea);
        
        if (res != null) 
        {
            if (c.getPlayer().isHidden()) 
                c.getPlayer().getMap().broadcastGMMessage(c.getPlayer(), MaplePacketCreator.movePlayer(c.getPlayer().getId(), oid, res), false);
            else 
                c.getPlayer().getMap().broadcastMessage(c.getPlayer(), MaplePacketCreator.movePlayer(c.getPlayer().getId(), oid, res), false);
        }
        
        updatePosition(res, c.getPlayer(), 0x00);
        c.getPlayer().getMap().movePlayer(c.getPlayer(), c.getPlayer().getPosition());
    }
}