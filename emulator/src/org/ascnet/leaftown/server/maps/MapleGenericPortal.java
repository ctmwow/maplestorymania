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
import org.ascnet.leaftown.client.MapleClient;
import org.ascnet.leaftown.client.anticheat.CheatingOffense;
import org.ascnet.leaftown.scripting.portal.PortalScriptManager;
import org.ascnet.leaftown.server.MaplePortal;
import org.ascnet.leaftown.server.fourthjobquests.FourthJobQuestsPortalHandler;
import org.ascnet.leaftown.tools.MaplePacketCreator;

import java.awt.Point;

public class MapleGenericPortal implements MaplePortal {

    private String name;
    private String target;
    private Point position;
    private int targetmap;
    private final int type;
    private int id;
    private String scriptName;
    private boolean status = true;
    private boolean spawned = false;

    public MapleGenericPortal(int type) {
        this.type = type;
    }

    @Override
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Point getPosition() {
        return position;
    }

    @Override
    public String getTarget() {
        return target;
    }

    @Override
    public int getTargetMapId() {
        return targetmap;
    }

    @Override
    public int getType() {
        return type;
    }

    @Override
    public String getScriptName() {
        return scriptName;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setPosition(Point position) {
        this.position = position;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    public void setTargetMapId(int targetmapid) {
        targetmap = targetmapid;
    }

    @Override
    public void setScriptName(String scriptName) {
        this.scriptName = scriptName;
    }

    @Override
    public void setPortalStatus(boolean newStatus) {
        status = newStatus;
    }

    @Override
    public boolean getPortalStatus() {
        return status;
    }

    @Override
    public void setSpawned(boolean newSpawned) {
        spawned = newSpawned;
    }

    @Override
    public boolean hasSpawned() {
        return spawned;
    }

    @Override
    public void enterPortal(MapleClient c) {
        MapleCharacter player = c.getPlayer();
        double distanceSq = position.distanceSq(player.getPosition());
        if (distanceSq > 22500) {
            player.getCheatTracker().registerOffense(CheatingOffense.USING_FARAWAY_PORTAL, "D" + Math.sqrt(distanceSq));
        }

        boolean changed = false;
        if (scriptName != null) {
            if (!FourthJobQuestsPortalHandler.handlePortal(scriptName, c.getPlayer()))
                changed = PortalScriptManager.getInstance().executePortalScript(this, c);
        } else if (targetmap != 999999999) {
            MapleMap to;
            if (player.getEventInstance() == null) {
                to = c.getChannelServer().getMapFactory().getMap(targetmap);
            } else {
                to = player.getEventInstance().getMapInstance(targetmap);
            }
            if (!player.getMap().canExit() && !player.isGM()) {
                c.sendPacket(MaplePacketCreator.serverNotice(5, "You are not allowed to exit this map."));
                c.sendPacket(MaplePacketCreator.enableActions());
                return;
            }
            if (!to.canEnter() && !player.isGM()) {
                c.sendPacket(MaplePacketCreator.serverNotice(5, "You are not allowed to enter " + to.getStreetName() + " : " + to.getMapName()));
                c.sendPacket(MaplePacketCreator.enableActions());
                return;
            }
            if ((player.getLevel() >= to.getLevelForceMove() || player.getLevel() < to.getLevelLimit()) && !player.isGM()) {
                c.sendPacket(MaplePacketCreator.portalBlock((byte) 3));
                return;
            }
            MaplePortal pto = to.getPortal(target);
            if (pto == null) { // fallback for missing portals - no real life case anymore - intresting for not implemented areas
                pto = to.getRandomSpawnPoint();
            }
            c.getPlayer().changeMap(to, pto); //late resolving makes this harder but prevents us from loading the whole world at once
            changed = true;
        }
        if (!changed) {
            c.sendPacket(MaplePacketCreator.enableActions());
        }
    }
}