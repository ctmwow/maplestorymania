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

package org.ascnet.leaftown.server.maps;

import org.ascnet.leaftown.client.MapleCharacter;
import org.ascnet.leaftown.net.MaplePacket;
import org.ascnet.leaftown.net.world.remote.WorldChannelInterface;
import org.ascnet.leaftown.server.TimerManager;
import org.ascnet.leaftown.tools.MaplePacketCreator;

import java.rmi.RemoteException;
import java.util.LinkedList;
import java.util.List;

/*
 * MapleTVEffect
 * Created by Lios
 * All credits to Cheetah and MrMysterious for creating
 * the MapleTV Method!  Good job guys~!
 */
public class MapleTVEffect {

    private List<String> message = new LinkedList<>();
    private final MapleCharacter user;
    private static boolean active;
    private final int type;
    private MapleCharacter partner = null;

    public MapleTVEffect(MapleCharacter user, MapleCharacter partner, List<String> msg, int type) {
        message = msg;
        this.user = user;
        this.type = type;
        this.partner = partner;
        broadcastTV(true);
    }

    public static boolean isActive() {
        return active;
    }

    private void setActive(boolean set) {
        MapleTVEffect.active = set;
    }

    private MaplePacket removeTV() {
        return MaplePacketCreator.removeTV();
    }

    public MaplePacket startTV() {
        return MaplePacketCreator.sendTV(user, message, type <= 2 ? type : type - 3, partner);
    }

    public void broadcastTV(boolean active) {
        WorldChannelInterface wci = user.getClient().getChannelServer().getWorldInterface();
        this.active = active;
        try {
            if (active) {
                wci.broadcastMessage(null, MaplePacketCreator.enableTV().getBytes());
                wci.broadcastMessage(null, startTV().getBytes());
                scheduleCancel();
            } else {
                wci.broadcastMessage(null, removeTV().getBytes());
            }
        } catch (RemoteException e) {
            user.getClient().getChannelServer().reconnectWorld();
        }
    }

    private void scheduleCancel() {
        int delay = 15000; // default. cbf adding it to switch
        switch (type) {
            case 0: //Do nothing
            case 1:
            case 2:
                break;
            case 3:
                delay = 15000;
                break;
            case 4:
                delay = 30000;
                break;
            case 5:
                delay = 60000;
                break;
        }
        TimerManager.getInstance().schedule(new Runnable() {

            @Override
            public void run() {
                broadcastTV(false);
            }
        }, delay);
    }
}