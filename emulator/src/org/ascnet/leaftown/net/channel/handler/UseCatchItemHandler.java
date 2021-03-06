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

import org.ascnet.leaftown.client.MapleCharacter;
import org.ascnet.leaftown.client.MapleClient;
import org.ascnet.leaftown.client.MapleInventoryType;
import org.ascnet.leaftown.net.AbstractMaplePacketHandler;
import org.ascnet.leaftown.server.MapleItemInformationProvider;
import org.ascnet.leaftown.server.MapleInventoryManipulator;
import org.ascnet.leaftown.server.life.MapleMonster;
import org.ascnet.leaftown.tools.MaplePacketCreator;
import org.ascnet.leaftown.tools.data.input.SeekableLittleEndianAccessor;

/**
 * @author Pat
 */
public class UseCatchItemHandler extends AbstractMaplePacketHandler {
	private long messagedelay = System.currentTimeMillis();
    public void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
    	slea.readInt();
        slea.readShort();
        int itemId = slea.readInt();
        int monsterid = slea.readInt();
        MapleMonster mob = c.getPlayer().getMap().getMonsterByOid(monsterid);
    if (c.getPlayer().getInventory(MapleItemInformationProvider.getInstance().getInventoryType(itemId)).countById(itemId) <= 0) {
           return;
    }
    if (mob == null) {
           return;
    }
        switch (itemId) {
            case 2270000:
                    if (mob.getId() == 9300101) {
                            c.getPlayer().getMap().broadcastMessage(MaplePacketCreator.catchMonster(monsterid, itemId, (byte) 1));
                            mob.getMap().killMonster(mob, (MapleCharacter) mob.getMap().getCharacters().get(0), false, true, false, 0);
                            MapleInventoryManipulator.removeById(c, MapleInventoryType.USE, itemId, 1, true, true);
                            MapleInventoryManipulator.addById(c, 1902000, (short) 1, "");
                    }
                    c.sendPacket(MaplePacketCreator.enableActions());
                break;
            case 2270001:
                if (mob.getId() == 9500197) {
                if (System.currentTimeMillis() < c.getPlayer().getCatchDelay()) {
                    return;
                }
                    if (mob.getHp() < ((mob.getMaxHp() / 10) * 4)) {
                            c.getPlayer().getMap().broadcastMessage(MaplePacketCreator.catchMonster(monsterid, itemId, (byte) 1));
                            mob.getMap().killMonster(mob, (MapleCharacter) mob.getMap().getCharacters().get(0), false, true, false, 0);
                            MapleInventoryManipulator.removeById(c, MapleInventoryType.USE, itemId, 1, true, true);
                            MapleInventoryManipulator.addById(c, 4031830, (short) 1, "");
                    } else {
                        c.getPlayer().catchDelay(1000);
                        c.sendPacket(MaplePacketCreator.catchMessage(0));
                    }
                }
                    c.sendPacket(MaplePacketCreator.enableActions());
                break;
            case 2270002:
                if (mob.getId() == 9300157) {
                if (System.currentTimeMillis() < c.getPlayer().getCatchDelay()) {
                   if (!(System.currentTimeMillis() < this.messagedelay)) {
                    this.messagedelay = System.currentTimeMillis() + 1000;
                    c.sendPacket(MaplePacketCreator.catchMessage(1));
                    return;
                   }
                   return;
                }
                    if (mob.getHp() < ((mob.getMaxHp() / 10) * 4)) {
                        if (Math.random() < 0.5) { // 50% chance
                            c.getPlayer().catchDelay(800);
                            c.getPlayer().getMap().broadcastMessage(MaplePacketCreator.catchMonster(monsterid, itemId, (byte) 1));
                            mob.getMap().killMonster(mob, (MapleCharacter) mob.getMap().getCharacters().get(0), false, true, false, 0);
                            MapleInventoryManipulator.removeById(c, MapleInventoryType.USE, itemId, 1, true, true);
                            MapleInventoryManipulator.addById(c, 4031868, (short) 1, "");
                            c.getPlayer().updateAriantScore();
                        } else {
                            c.getPlayer().catchDelay(800);
                            c.getPlayer().getMap().broadcastMessage(MaplePacketCreator.catchMonster(monsterid, itemId, (byte) 0));
                        }
                    } else {
                        c.sendPacket(MaplePacketCreator.catchMessage(0));
                    }
                }
                c.sendPacket(MaplePacketCreator.enableActions());
                break;
            case 2270003:
                if (mob.getId() == 9500320) {
                    if (mob.getHp() < ((mob.getMaxHp() / 10) * 4)) {
                        c.getPlayer().getMap().broadcastMessage(MaplePacketCreator.catchMonster(monsterid, itemId, (byte) 1));
                        mob.getMap().killMonster(mob, (MapleCharacter) mob.getMap().getCharacters().get(0), false, true, false, 0);
                        MapleInventoryManipulator.removeById(c, MapleInventoryType.USE, itemId, 1, true, true);
                        MapleInventoryManipulator.addById(c, 4031887, (short) 1, "");
                    } else {
                        c.sendPacket(MaplePacketCreator.catchMessage(0));
                    }
                }
                c.sendPacket(MaplePacketCreator.enableActions());
                break;
            case 2270005:
                if (mob.getId() == 9300187) {
                    if (mob.getHp() < ((mob.getMaxHp() / 10) * 3)) {
                        c.getPlayer().getMap().broadcastMessage(MaplePacketCreator.catchMonster(monsterid, itemId, (byte) 1));
                        mob.getMap().killMonster(mob, (MapleCharacter) mob.getMap().getCharacters().get(0), false, true, false, 0);
                        MapleInventoryManipulator.removeById(c, MapleInventoryType.USE, itemId, 1, true, true);
                        MapleInventoryManipulator.addById(c, 2109001, (short) 1, "");
                    } else {
                        c.sendPacket(MaplePacketCreator.catchMessage(0));
                    }
                }
                c.sendPacket(MaplePacketCreator.enableActions());
                break;
            case 2270006:
                if (mob.getId() == 9300189) {
                    if (mob.getHp() < ((mob.getMaxHp() / 10) * 3)) {
                    c.getPlayer().getMap().broadcastMessage(MaplePacketCreator.catchMonster(monsterid, itemId, (byte) 1));
                    mob.getMap().killMonster(mob, (MapleCharacter) mob.getMap().getCharacters().get(0), false, true, false, 0);
                    MapleInventoryManipulator.removeById(c, MapleInventoryType.USE, itemId, 1, true, true);
                    MapleInventoryManipulator.addById(c, 2109002, (short) 1, "");
                    } else {
                    c.sendPacket(MaplePacketCreator.catchMessage(0));
                    }
                }
                c.sendPacket(MaplePacketCreator.enableActions());
                break;
            case 2270007:
                if (mob.getId() == 9300191) {
                    if (mob.getHp() < ((mob.getMaxHp() / 10) * 3)) {
                        c.getPlayer().getMap().broadcastMessage(MaplePacketCreator.catchMonster(monsterid, itemId, (byte) 1));
                        mob.getMap().killMonster(mob, (MapleCharacter) mob.getMap().getCharacters().get(0), false, true, false, 0);
                        MapleInventoryManipulator.removeById(c, MapleInventoryType.USE, itemId, 1, true, true);
                        MapleInventoryManipulator.addById(c, 2109003, (short) 1, "");
                    } else {
                        c.sendPacket(MaplePacketCreator.catchMessage(0));
                    }
                }
                c.sendPacket(MaplePacketCreator.enableActions());
                break;
            case 2270004:
                if (mob.getId() == 9300174) {
                    if (mob.getHp() < ((mob.getMaxHp() / 10) * 4)) {
                    c.getPlayer().getMap().broadcastMessage(MaplePacketCreator.catchMonster(monsterid, itemId, (byte) 1));
                    mob.getMap().killMonster(mob, (MapleCharacter) mob.getMap().getCharacters().get(0), false, true, false, 0);
                    MapleInventoryManipulator.removeById(c, MapleInventoryType.USE, itemId, 1, true, true);
                    MapleInventoryManipulator.addById(c, 2109000, (short) 1, "");
                    } else {
                    c.sendPacket(MaplePacketCreator.catchMessage(0));
                    }
                }
                c.sendPacket(MaplePacketCreator.enableActions());
                break;
            case 2270008:
                if (mob.getId() == 9500336) {
                    if (System.currentTimeMillis() < c.getPlayer().getCatchDelay()) {
                      if (!(System.currentTimeMillis() < this.messagedelay)) {
                        this.messagedelay = System.currentTimeMillis() + 1000;
                        c.getPlayer().dropMessage("Você não pode usar a Vara de Pesca ainda.");
                        return;
                      }
                      return;
                    }
                        c.getPlayer().catchDelay(3000);
                        c.getPlayer().getMap().broadcastMessage(MaplePacketCreator.catchMonster(monsterid, itemId, (byte) 1));
                        mob.getMap().killMonster(mob, (MapleCharacter) mob.getMap().getCharacters().get(0), false, true, false, 0);
                        MapleInventoryManipulator.removeById(c, MapleInventoryType.USE, itemId, 1, true, true);
                        MapleInventoryManipulator.addById(c, 2022323, (short) 1, "");
                    }
                c.sendPacket(MaplePacketCreator.enableActions());
                break;
            default:
                System.out.println("UseCatchItemHandler: \r\n" + slea.toString());
        }
    }
}