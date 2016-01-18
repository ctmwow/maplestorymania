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
import org.ascnet.leaftown.server.AutobanManager;
import org.ascnet.leaftown.tools.MaplePacketCreator;
import org.ascnet.leaftown.tools.data.input.SeekableLittleEndianAccessor;

import java.rmi.RemoteException;

/**
 * @author Xterminator
 */
public class AutoBanHandler extends AbstractMaplePacketHandler {

    @Override
    public void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        int type = slea.readInt();

        switch (type) {
            case 2:
                /*try {
					c.getChannelServer().getWorldInterface().broadcastGMMessage(c.getPlayer().getName(), MaplePacketCreator.serverNotice(0, c.getPlayer().getName() + " is suspected of item vaccing. ").getBytes());
				} catch (RemoteException ex) {
				}*/
                if (!c.isGM()) {
                    AutobanManager.getInstance().broadcastMessage(c, c.getPlayer().getName() + " was auto banned for item vaccing.");
                    c.getPlayer().ban(c.getPlayer().getName() + " was auto banned for item vaccing (IP: " + c.getIP() + ")");
                }
                break;
            default:
                try {
                    c.getChannelServer().getWorldInterface().broadcastGMMessage(c.getPlayer().getName(), MaplePacketCreator.serverNotice(0, c.getPlayer().getName() + " is suspected of hacking. Reason ID: " + type).getBytes());
                } catch (RemoteException ex) {
                    c.getChannelServer().reconnectWorld();
                }
                //AutobanManager.getInstance().broadcastMessage(c, c.getPlayer().getName() + " was auto banned for hacking.");
                //c.getPlayer().ban(c.getPlayer().getName() + " was auto banned for hacking (IP: " + c.getSession().getRemoteAddress().toString().split(":")[0] + ")");
                break;
        }
    }
}