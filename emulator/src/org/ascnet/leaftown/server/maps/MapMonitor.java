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

import org.ascnet.leaftown.net.channel.ChannelServer;
import org.ascnet.leaftown.server.MaplePortal;
import org.ascnet.leaftown.server.MapleSquad;
import org.ascnet.leaftown.server.MapleSquadType;
import org.ascnet.leaftown.server.TimerManager;
import org.ascnet.leaftown.server.life.MapleNPC;
import org.ascnet.leaftown.tools.MaplePacketCreator;

import java.util.concurrent.ScheduledFuture;

/**
 * @author iamSTEVE
 */
public class MapMonitor {

    private ScheduledFuture<?> monitorSchedule;
    private final MapleMap map;
    private final MaplePortal portal;
    private final int ch;
    private final MapleReactor reactor;

    public MapMonitor(final MapleMap map, MaplePortal portal, int ch, MapleReactor reactor) {
        this.map = map;
        this.portal = portal;
        this.ch = ch;
        this.reactor = reactor;
        monitorSchedule = TimerManager.getInstance().register(new Runnable() {

            @Override
            public void run() {
                if (map.getCharacters().isEmpty()) {
                    reset();
                }
            }
        }, 5000);
    }

    public MapMonitor(final MapleMap map, MaplePortal portal, int ch, MapleReactor reactor, long initialDelay) {
        this.map = map;
        this.portal = portal;
        this.ch = ch;
        this.reactor = reactor;
        monitorSchedule = TimerManager.getInstance().register(new Runnable() {

            @Override
            public void run() {
                if (map.getCharacters().isEmpty()) {
                    reset();
                }
            }
        }, 5000, initialDelay);
    }

    public void reset() {
        monitorSchedule.cancel(false);
        monitorSchedule = null;
        map.killAllMonsters(false);
        for (MapleMapObject npcmo : map.getAllNPCs()) {
            MapleNPC npc = (MapleNPC) npcmo;
            if (npc.isCustom()) {
                map.removeMapObject(npc.getObjectId());
            }
        }
        if (portal != null) {
            portal.setPortalStatus(MaplePortal.OPEN);
            portal.setSpawned(false);
        }
        if (reactor != null) {
            reactor.setState((byte) 0);
            reactor.getMap().broadcastMessage(MaplePacketCreator.triggerReactor(reactor, 0));
        }
        map.resetReactors();
        map.resetPortals();
        MapleSquad squad;
        ChannelServer channel = ChannelServer.getInstance(ch);
        if (map.getId() == 280030000) {
            squad = channel.getMapleSquad(MapleSquadType.ZAKUM);
            if (squad != null) {
                channel.removeMapleSquad(channel.getMapleSquad(MapleSquadType.ZAKUM), MapleSquadType.ZAKUM);
            }
        } else if (map.getId() == 240060000) {
            squad = channel.getMapleSquad(MapleSquadType.HORNTAIL);
            if (squad != null) {
                if (channel.getMapFactory().getMap(240060100).getCharacters().isEmpty() && channel.getMapFactory().getMap(240060200).getCharacters().isEmpty()) {
                    channel.removeMapleSquad(channel.getMapleSquad(MapleSquadType.HORNTAIL), MapleSquadType.HORNTAIL);
                }
            }
        } else if (map.getId() == 240060100) {
            squad = channel.getMapleSquad(MapleSquadType.HORNTAIL);
            if (squad != null) {
                if (channel.getMapFactory().getMap(240060200).getCharacters().isEmpty()) {
                    channel.removeMapleSquad(channel.getMapleSquad(MapleSquadType.HORNTAIL), MapleSquadType.HORNTAIL);
                }
            }
        } else if (map.getId() == 240060200) {
            squad = channel.getMapleSquad(MapleSquadType.HORNTAIL);
            if (squad != null) {
                channel.removeMapleSquad(channel.getMapleSquad(MapleSquadType.HORNTAIL), MapleSquadType.HORNTAIL);
            }
        }
    }
}