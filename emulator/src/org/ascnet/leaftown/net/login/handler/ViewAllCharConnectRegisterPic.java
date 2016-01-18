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

package org.ascnet.leaftown.net.login.handler;

import org.ascnet.leaftown.client.MapleCharacter;
import org.ascnet.leaftown.client.MapleClient;
import org.ascnet.leaftown.net.AbstractMaplePacketHandler;
import org.ascnet.leaftown.net.login.LoginServer;
import org.ascnet.leaftown.tools.MaplePacketCreator;
import org.ascnet.leaftown.tools.Randomizer;
import org.ascnet.leaftown.tools.data.input.SeekableLittleEndianAccessor;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.sql.SQLException;

public class ViewAllCharConnectRegisterPic extends AbstractMaplePacketHandler {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ViewAllCharConnectRegisterPic.class);

    @Override
    public void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        slea.readByte();
        final int charId = slea.readInt();
        final int world = slea.readInt();
        final String macs = slea.readMapleAsciiString();
        final String lastMacAddress = slea.readMapleAsciiString();
        c.setWorld(world);
        c.setChannel(Randomizer.nextInt(LoginServer.getInstance().getChannels().size() + 1));
        c.updateMacs(macs, lastMacAddress);
        final String PIC = slea.readMapleAsciiString();
        c.setPIC(PIC);
        if (c.hasBannedMac()) {
            c.disconnect();
            return;
        }
        try {
            /*if (c.getIdleTask() != null) {
				c.getIdleTask().cancel(true);
				c.setIdleTask(null);
			}*/
            if (c.hasCharacter(charId))
                MapleCharacter.setLoggedInState(charId, 0);
            c.updateLoginState(MapleClient.LOGIN_SERVER_TRANSITION);
            final String channelServerIP = MapleClient.getChannelServerIPFromSubnet(c.getIP(), c.getChannel());
            if (channelServerIP.equals("0.0.0.0")) {
                final String[] socket = LoginServer.getInstance().getIP(c.getChannel()).split(":");
                c.sendPacket(MaplePacketCreator.getServerIP(InetAddress.getByName(socket[0]), Integer.parseInt(socket[1]), charId));
            } else {
                final String[] socket = LoginServer.getInstance().getIP(c.getChannel()).split(":");
                c.sendPacket(MaplePacketCreator.getServerIP(InetAddress.getByName(channelServerIP), Integer.parseInt(socket[1]), charId));
            }
        } catch (NullPointerException e) {
        } catch (UnknownHostException e) {
            log.error("Host not found", e);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}