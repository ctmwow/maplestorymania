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

package org.ascnet.leaftown.client.messages.commands;

import org.ascnet.leaftown.client.MapleCharacter;
import org.ascnet.leaftown.client.MapleClient;
import org.ascnet.leaftown.client.anticheat.CheatingOffense;
import org.ascnet.leaftown.client.messages.Command;
import org.ascnet.leaftown.client.messages.CommandDefinition;
import org.ascnet.leaftown.client.messages.IllegalCommandSyntaxException;
import org.ascnet.leaftown.client.messages.MessageCallback;
import org.ascnet.leaftown.net.channel.ChannelServer;
import org.ascnet.leaftown.server.MaplePortal;
import org.ascnet.leaftown.server.TimerManager;
import org.ascnet.leaftown.server.life.MapleNPC;
import org.ascnet.leaftown.server.life.MobSkill;
import org.ascnet.leaftown.server.life.MobSkillFactory;
import org.ascnet.leaftown.server.maps.MapleMap;
import org.ascnet.leaftown.server.maps.MapleMapObject;
import org.ascnet.leaftown.server.maps.MapleReactor;
import org.ascnet.leaftown.server.maps.MapleReactorFactory;
import org.ascnet.leaftown.server.maps.MapleReactorStats;
import org.ascnet.leaftown.server.quest.MapleQuest;
import org.ascnet.leaftown.tools.MaplePacketCreator;

import java.util.List;

public class DebugCommands implements Command {

    @Override
    public void execute(MapleClient c, MessageCallback mc, String[] splitted) throws Exception {
        MapleCharacter player = c.getPlayer();
        switch (splitted[0]) {
            case "!resetquest":
                MapleQuest.getInstance(Integer.parseInt(splitted[1])).forfeit(c.getPlayer());
                break;
            case "!nearestportal":
                final MaplePortal portal = player.getMap().findClosestPortal(player.getPosition());
                mc.dropMessage(portal.getName() + " id: " + portal.getId() + " script: " + portal.getScriptName());
                break;
            case "!spawndebug":
                c.getPlayer().getMap().spawnDebug(mc);
                break;
            case "!timerdebug":
                TimerManager.getInstance().dropDebugInfo(mc);
                break;
            case "!threads": {
                Thread[] threads = new Thread[Thread.activeCount()];
                Thread.enumerate(threads);
                String filter = "";
                if (splitted.length > 1) {
                    filter = splitted[1];
                }
                for (int i = 0; i < threads.length; i++) {
                    String tstring = threads[i].toString();
                    if (tstring.toLowerCase().contains(filter.toLowerCase())) {
                        mc.dropMessage(i + ": " + tstring);
                    }
                }
                break;
            }
            case "!showtrace": {
                if (splitted.length < 2) {
                    throw new IllegalCommandSyntaxException(2);
                }
                Thread[] threads = new Thread[Thread.activeCount()];
                Thread.enumerate(threads);
                Thread t = threads[Integer.parseInt(splitted[1])];
                mc.dropMessage(t.toString() + ":");
                for (StackTraceElement elem : t.getStackTrace()) {
                    mc.dropMessage(elem.toString());
                }
                break;
            }
            case "!fakerelog":
                c.sendPacket(MaplePacketCreator.getCharInfo(player));
                player.getMap().removePlayer(player);
                player.getMap().addPlayer(player);
                break;
            case "!toggleoffense":
                try {
                    CheatingOffense co = CheatingOffense.valueOf(splitted[1]);
                    co.setEnabled(!co.isEnabled());
                } catch (IllegalArgumentException iae) {
                    mc.dropMessage("Offense " + splitted[1] + " not found");
                }
                break;
            case "!tdrops":
                player.getMap().toggleDrops();
                break;
            case "!dropd":
                if (splitted.length > 1) {
                    player.getMap().toggleDrops();
                    final ChannelServer cservv = player.getClient().getChannelServer();
                    final int mapid = player.getMapId();
                    c.sendPacket(MaplePacketCreator.getClock(Integer.parseInt(splitted[1])));
                    TimerManager.getInstance().schedule(new Runnable() {

                        @Override
                        public void run() {
                            cservv.getMapFactory().getMap(mapid).toggleDrops();
                        }
                    }, Integer.parseInt(splitted[1]) * 1000);
                }
                break;
            case "!givebuff": {
                long mask = 0;
                mask |= Long.decode(splitted[1]);
                c.sendPacket(MaplePacketCreator.giveBuffTest(1000, 60, mask));
                break;
            }
            case "!givemonsbuff": {
                int mask = 0;
                mask |= Integer.decode(splitted[1]);
                MobSkill skill = MobSkillFactory.getMobSkill(128, 1);
                c.sendPacket(MaplePacketCreator.applyMonsterStatusTest(Integer.valueOf(splitted[2]), mask, 0, skill, Integer.valueOf(splitted[3])));
                break;
            }
            case "!givemonstatus": {
                int mask = 0;
                mask |= Integer.decode(splitted[1]);
                c.sendPacket(MaplePacketCreator.applyMonsterStatusTest2(Integer.valueOf(splitted[2]), mask, 1000, Integer.valueOf(splitted[3])));
                break;
            }
            case "!sreactor": {
                MapleReactorStats reactorSt = MapleReactorFactory.getReactor(Integer.parseInt(splitted[1]));
                MapleReactor reactor = new MapleReactor(reactorSt, Integer.parseInt(splitted[1]));
                reactor.setDelay(-1);
                reactor.setPosition(c.getPlayer().getPosition());
                c.getPlayer().getMap().spawnReactor(reactor);
                break;
            }
            case "!hreactor": {
                boolean breakn = splitted.length > 2 && splitted[2].equalsIgnoreCase("break");
                if (splitted[1].equalsIgnoreCase("all")) {
                    List<MapleMapObject> reactors = c.getPlayer().getMap().getAllReactors();
                    for (MapleMapObject reactorL : reactors) {
                        MapleReactor reactor2l = (MapleReactor) reactorL;
                        do {
                            reactor2l.hitReactor(c);
                        } while (breakn && !reactor2l.isBroken());
                    }
                } else {
                    MapleReactor reactor = c.getPlayer().getMap().getReactorByOid(Integer.parseInt(splitted[1]));
                    do {
                        reactor.hitReactor(c);
                    } while (breakn && !reactor.isBroken());
                }
                break;
            }
            case "!lreactor":
                List<MapleMapObject> reactors = c.getPlayer().getMap().getAllReactors();
                for (MapleMapObject reactorL : reactors) {
                    MapleReactor reactor2l = (MapleReactor) reactorL;
                    mc.dropMessage("Reactor oID: " + reactor2l.getObjectId() + " reactorID: " + reactor2l.getId() + " Position: " + reactor2l.getPosition().toString() + " State: " + reactor2l.getState());
                }
                break;
            case "!dreactor":
                if (splitted[1].equals("all")) {
                    c.getPlayer().getMap().destroyReactors();
                } else {
                    c.getPlayer().getMap().destroyReactor(Integer.parseInt(splitted[1]));
                }
                break;
            case "!rreactor":
                if (splitted.length > 1 && splitted[1].equalsIgnoreCase("-f")) {
                    c.getChannelServer().getMapFactory().respawnReactors(c.getPlayer().getMap());
                } else {
                    c.getPlayer().getMap().resetReactors();
                }
                break;
            case "!cleardrops": {
                MapleMap map = c.getPlayer().getMap();
                List<MapleMapObject> items = map.getAllItems();
                for (MapleMapObject itemmo : items) {
                    map.removeMapObject(itemmo);
                    map.broadcastMessage(MaplePacketCreator.removeItemFromMap(itemmo.getObjectId(), 0, c.getPlayer().getId()));
                }
                mc.dropMessage("You have cleared " + items.size() + " item(s).");
                break;
            }
            case "!toggleloot": {
                MapleMap map = c.getPlayer().getMap();
                map.setLootable(!map.isLootable());
                map.broadcastMessage(MaplePacketCreator.serverNotice(5, map.getMapName() + " is now " + (map.isLootable() ? "lootable" : "unlootable" + ".")));
                break;
            }
            case "!questdebug":
                c.getPlayer().toggleQuestDebug();
                break;
            case "!energycharge":
                if (splitted[1].equals("forever")) {
                    c.getPlayer().toggleEnergyChargeForever();
                    mc.dropMessage("Your Energy Charge will " + (c.getPlayer().isEnergyChargeForever() ? "" : "not ") + "last forever.");
                    return;
                }
                int amt = 10000;
                if (splitted.length >= 2)
                    amt = Integer.parseInt(splitted[1]);
                c.getPlayer().handleEnergyChargeGain(amt, true);
                break;
            case "!packetlog":
                String pl = splitted[1];
                MapleCharacter mCh = c.getChannelServer().getPlayerStorage().getCharacterByName(pl);
                if (mCh == null) {
                    mc.dropMessage("Invalid character name");
                    return;
                }
                boolean enable = splitted[2].equalsIgnoreCase("true");
                mCh.setPacketLogging(enable);
                mCh.getClient().setPacketLog(enable);
                mc.dropMessage(mCh.getName() + " now has a packet log? " + mCh.getClient().hasPacketLog());
                break;
            case "!combo":
                c.getPlayer().setComboCounter(Short.parseShort(splitted[1]));
                c.sendPacket(MaplePacketCreator.showAranComboCounter(player.getComboCounter()));
                break;
        }
    }

    @Override
    public CommandDefinition[] getDefinition() {
        return new CommandDefinition[] {
                new CommandDefinition("resetquest", "<questid>", "Resets the specified quest", 5),
                new CommandDefinition("nearestportal", "", "Gives you the nearest portal", 5),
                new CommandDefinition("spawndebug", "", "", 5),
                new CommandDefinition("timerdebug", "", "", 5),
                new CommandDefinition("threads", "", "", 5),
                new CommandDefinition("showtrace", "", "", 5),
                new CommandDefinition("toggleoffense", "", "", 5),
                new CommandDefinition("fakerelog", "", "", 4),
                new CommandDefinition("tdrops", "", "", 5),
                new CommandDefinition("givebuff", "", "", 5),
                new CommandDefinition("givemonsbuff", "", "", 5),
                new CommandDefinition("givemonstatus", "", "", 5),
                new CommandDefinition("sreactor", "[id]", "Spawn a Reactor", 5),
                new CommandDefinition("hreactor", "[object ID]", "Hit reactor", 5),
                new CommandDefinition("rreactor", "", "Resets all reactors", 4),
                new CommandDefinition("lreactor", "", "List reactors", 5),
                new CommandDefinition("dreactor", "", "Remove a Reactor", 5),
                new CommandDefinition("dropd", "", "toggle drops on a map for a set time", 4),
                new CommandDefinition("cleardrops", "", "Clear all drops on the current map", 4),
                new CommandDefinition("toggleloot", "", "Makes item pickup un/available to the map", 4),
                new CommandDefinition("questdebug", "", "", 4),
                new CommandDefinition("energycharge", "", "", 4),
                new CommandDefinition("packetlog", "<charname> <true/false>", "", 4),
                new CommandDefinition("combo", "", "", 4)
        };
    }
}