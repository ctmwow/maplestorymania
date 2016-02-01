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
import org.ascnet.leaftown.net.AbstractMaplePacketHandler;
import org.ascnet.leaftown.server.MaplePortal;
import org.ascnet.leaftown.server.maps.MapleMap;
import org.ascnet.leaftown.tools.MaplePacketCreator;
import org.ascnet.leaftown.tools.data.input.SeekableLittleEndianAccessor;

public class ChangeMapSpecialHandler extends AbstractMaplePacketHandler 
{
    @Override
    public void handlePacket(SeekableLittleEndianAccessor slea, final MapleClient c) 
    {
        if (System.currentTimeMillis() - c.getPlayer().getLastWarpTime() < 2000) 
        {
            c.sendPacket(MaplePacketCreator.enableActions());
            return;
        }
        
        slea.readByte();
        String startwp = slea.readMapleAsciiString();
        slea.readShort();

        MaplePortal portal = c.getPlayer().getMap().getPortal(startwp);
        
        c.getPlayer().setLastWarpTime(System.currentTimeMillis());
        
        if (c.getPlayer().getMap().isDojoMap() && c.getPlayer().getMap().getDojoStage() != 38) 
        {
            if (portal != null) 
            {
                if (c.getPlayer().getMap().countMobOnMap(9300216) > 0x00) 
                {
                    c.getPlayer().getClient().sendPacket(MaplePacketCreator.showOwnBuffEffect(0x00, 0x07, (byte) c.getPlayer().getLevel()));
                    MapleMap map = c.getPlayer().getEventInstance().getMapInstance(c.getPlayer().getMap().getNextDojoMap());
                    c.getPlayer().changeMap(map, map.getPortal(0x00));
                }
                else 
                {
                    c.sendPacket(MaplePacketCreator.serverNotice(0x05, "You haven't killed the boss yet. Please kill it before continuing."));
                    c.sendPacket(MaplePacketCreator.enableActions());
                }
            } 
            else 
                c.sendPacket(MaplePacketCreator.enableActions());
        } 
        else 
        {
            if (portal != null) 
                portal.enterPortal(c);
            else 
                c.sendPacket(MaplePacketCreator.enableActions());
        }
    }
}