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

import org.ascnet.leaftown.client.Equip;
import org.ascnet.leaftown.client.IItem;
import org.ascnet.leaftown.client.MapleClient;
import org.ascnet.leaftown.client.MapleInventoryType;
import org.ascnet.leaftown.client.SkillFactory;
import org.ascnet.leaftown.net.AbstractMaplePacketHandler;
import org.ascnet.leaftown.server.MakerItemFactory;
import org.ascnet.leaftown.server.MakerItemFactory.MakerItemCreateEntry;
import org.ascnet.leaftown.server.MapleInventoryManipulator;
import org.ascnet.leaftown.server.MapleItemInformationProvider;
import org.ascnet.leaftown.tools.MaplePacketCreator;
import org.ascnet.leaftown.tools.Pair;
import org.ascnet.leaftown.tools.Randomizer;
import org.ascnet.leaftown.tools.data.input.SeekableLittleEndianAccessor;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Jay Estrella and PurpleMadness
 */
public class MakerHandler extends AbstractMaplePacketHandler {

    @Override
    public void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
        final int type = slea.readInt();
        switch (type) {
            case 1:
                final int toCreate = slea.readInt();
                int itemToGet = toCreate;
                final boolean stimulator = slea.readByte() == 1;
                List<Integer> gems = new ArrayList<>();
                final int numGems = slea.readInt();
                for (int i = 0; i < numGems; i++) {
                    int gem = slea.readInt();
                    if (!gems.contains(gem) && gem / 10000 == 425) {
                        gems.add(gem);
                    }
                }
                if (toCreate / 10000 == 425) { // are we making a gem :o
                    if (toCreate % 10 == 0) { //ii.getName(toCreate).startsWith("Basic")) {
                        boolean promoted = false;
                        if (Math.ceil(Math.random() * 100.0) > 85) { //15%
                            itemToGet++; // make it intermediate
                            promoted = true;
                        }
                        if (promoted && Math.ceil(Math.random() * 100.0) > 95) { //5%
                            c.getPlayer().finishAchievement(50);
                            itemToGet++; // make it advanced
                        }
                    } else if (toCreate % 10 == 1) { //ii.getName(toCreate).startsWith("Intermediate")) {
                        if (Math.ceil(Math.random() * 100.0) > 90) { //10%
                            itemToGet++; // make it advanced
                        }
                    }
                }
                MakerItemCreateEntry recipe = MakerItemFactory.getItemCreateEntry(toCreate);
                if (canCreate(c, recipe) && !c.getPlayer().getInventory(ii.getInventoryType(toCreate)).isFull()) {
                    final List<Pair<Short, IItem>> allChanges = new ArrayList<>();
                    final List<Pair<Integer, Integer>> itemAmount = new ArrayList<>();
                    for (Pair<Integer, Integer> p : recipe.getReqItems()) {
                        final int toRemove = p.getLeft();
                        final int count = p.getRight();
                        allChanges.addAll(MapleInventoryManipulator.removeItem(c, ii.getInventoryType(toRemove), toRemove, count, false));
                        itemAmount.add(new Pair<>(toRemove, -count));
                    }
                    if (stimulator && c.getPlayer().haveItem(recipe.getStimulator(), 1, false, false)) {
                        IItem create = ii.randomizeStats(applyGems((Equip) ii.getEquipById(toCreate), gems), true);
                        allChanges.addAll(MapleInventoryManipulator.removeItem(c, MapleInventoryType.ETC, recipe.getStimulator(), 1, false));
                        itemAmount.add(new Pair<>(recipe.getStimulator(), -1));
                        for (Integer gem : gems) {
                            allChanges.addAll(MapleInventoryManipulator.removeItem(c, MapleInventoryType.ETC, gem, 1, false));
                            itemAmount.add(new Pair<>(gem, -1));
                        }
                        if (Math.ceil(Math.random() * 100.0) > 10) {
                            allChanges.addAll(MapleInventoryManipulator.addByItem(c, create, "Creating item using Maker Skill", false));
                            itemAmount.add(new Pair<>(create.getItemId(), 1));
                        } else {
                            c.sendPacket(MaplePacketCreator.serverNotice(1, "The item was destroyed in the process."));
                        }
                    } else {
                        if (ii.getInventoryType(toCreate) != MapleInventoryType.EQUIP) {
                            allChanges.addAll(MapleInventoryManipulator.addByItemId(c, itemToGet, recipe.getRewardAmount(), "Creating item using Maker Skill", false));
                            itemAmount.add(new Pair<>(itemToGet, (int) recipe.getRewardAmount()));
                        } else {
                            IItem create = ii.randomizeStats(applyGems((Equip) ii.getEquipById(toCreate), gems));
                            allChanges.addAll(MapleInventoryManipulator.addByItem(c, create, "Creating item using Maker Skill", false));
                            itemAmount.add(new Pair<>(toCreate, (int) recipe.getRewardAmount()));
                            for (Integer gem : gems) {
                                allChanges.addAll(MapleInventoryManipulator.removeItem(c, MapleInventoryType.ETC, gem, 1, false));
                                itemAmount.add(new Pair<>(gem, -1));
                            }
                        }
                    }
                    c.sendPacket(MaplePacketCreator.modifyInventory(true, allChanges));
                    c.sendPacket(MaplePacketCreator.getShowItemGain(itemAmount));
                } else {
                    c.disconnect(); // Should only occur if there's a packet editor!
                }
                break;
            case 3:
                final int itemId = slea.readInt();
                if (c.getPlayer().haveItem(itemId, 100, false, false)) {
                    if (getMonsterCrystal(itemId) != 0) {
                        int monsterCrystal = getMonsterCrystal(itemId);
                        MapleInventoryManipulator.removeById(c, MapleInventoryType.ETC, itemId, 100, false, false);
                        c.sendPacket(MaplePacketCreator.getShowItemGain(itemId, (short) -1, true));
                        MapleInventoryManipulator.addById(c, monsterCrystal, (short) 1, "Creating item using Maker Skill");
                        c.sendPacket(MaplePacketCreator.getShowItemGain(monsterCrystal, (short) 1, true));
                    }
                }
                break;
            case 4:
                final int disassembleId = slea.readInt();
                final int quantity = slea.readInt();
                final short position = slea.readShort();
                int reqLv = ii.getReqLevel(disassembleId);
                double maxCrystals = 5.0;
                if (ii.isWeapon(disassembleId)) {
                    maxCrystals = maxCrystals + 2.0; //make weapons slightly more rewarding to disassemble
                }
                int toMake = (int) Math.ceil(Math.random() * maxCrystals);
                int monsterCrystal = getMonsterCrystalByLevel(reqLv);
                final IItem toUse = c.getPlayer().getInventory(ii.getInventoryType(disassembleId)).getItem(position);
                if (toUse == null || toUse.getItemId() != disassembleId || toUse.getQuantity() < 1) {
                    return;
                }
                if (monsterCrystal != 0) {
                    MapleInventoryManipulator.removeFromSlot(c, MapleInventoryType.EQUIP, position, quantity, false, false);
                    c.sendPacket(MaplePacketCreator.getShowItemGain(disassembleId, (short) -1, true));
                    MapleInventoryManipulator.addById(c, monsterCrystal, (short) toMake, "Creating item using Maker Skill");
                    c.sendPacket(MaplePacketCreator.getShowItemGain(monsterCrystal, (short) toMake, true));
                }
                break;
        }
    }

    private boolean canCreate(MapleClient c, MakerItemCreateEntry recipe) {
        return hasItems(c, recipe) && hasMesos(c, recipe) && hasLevel(c, recipe) && hasSkill(c, recipe);
    }

    private boolean hasItems(MapleClient c, MakerItemCreateEntry recipe) {
        MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
        for (Pair<Integer, Integer> p : recipe.getReqItems()) {
            int count = p.getRight();
            int itemId = p.getLeft();

            if (c.getPlayer().getInventory(ii.getInventoryType(itemId)).countById(itemId) < count) {
                return false;
            }
        }

        return true;
    }

    private boolean hasMesos(MapleClient c, MakerItemCreateEntry recipe) {
        return c.getPlayer().getMeso() >= recipe.getCost();
    }

    private boolean hasLevel(MapleClient c, MakerItemCreateEntry recipe) {
        return c.getPlayer().getLevel() >= recipe.getReqLevel();
    }

    private boolean hasSkill(MapleClient c, MakerItemCreateEntry recipe) {
        if (c.getPlayer().getJob().getId() >= 1000) // KoC Maker skill.
            return c.getPlayer().getSkillLevel(SkillFactory.getSkill(10001007)) >= recipe.getReqSkillLevel();
        else
            return c.getPlayer().getSkillLevel(SkillFactory.getSkill(1007)) >= recipe.getReqSkillLevel();
    }

    private int getMonsterCrystal(int etcId) {
        int monsterCrystal = 0;
        switch (etcId - 4000000) {
            case 123:
            case 24:
            case 86:
            case 32:
            case 167:
            case 107:
            case 95:
            case 87:
            case 73:
            case 370:
            case 371:
            case 96:
            case 109:
            case 113:
            case 99:
            case 13:
            case 67:
            case 88:
            case 35:
            case 59:
            case 103:
            case 108:
            case 153:
            case 115:
            case 43:
            case 26:
            case 29:
            case 104:
            case 154:
            case 100:
            case 105:
            case 110:
            case 116:
            case 117:
            case 276:
            case 23:
            case 222:
            case 159:
            case 158:
            case 114:
            case 31:
            case 76:
            case 120:
            case 278:
            case 290:
            case 277:
            case 111:
            case 101:
            case 157:
            case 155:
            case 58:
            case 118:
            case 78:
            case 156:
            case 112:
            case 204:
            case 14:
            case 89:
            case 178:
            case 102:
            case 60:
            case 169:
            case 45:
            case 62:
            case 90:
            case 205:
            case 44:
            case 36:
            case 125:
            case 91:
            case 170:
            case 48:
            case 286:
                monsterCrystal = 4260000; //basic 1
                break;
            case 81:
            case 33:
            case 61:
            case 70:
            case 72:
            case 171:
            case 71:
            case 126:
            case 51:
            case 41:
            case 22:
            case 298:
            case 55:
            case 283:
            case 284:
            case 172:
            case 69:
            case 206:
            case 25:
            case 288:
            case 52:
            case 177:
            case 75:
            case 285:
            case 50:
            case 382:
            case 223:
                monsterCrystal = 4260001; //basic 2
                break;
            case 128:
            case 92:
            case 282:
            case 207:
            case 143:
            case 57:
            case 93:
            case 295:
            case 49:
            case 176:
            case 129:
            case 289:
            case 144:
            case 56:
            case 296:
            case 145:
            case 226:
            case 227:
            case 28:
                monsterCrystal = 4260002; //basic 3
                break;
            case 236:
            case 79:
            case 260:
            case 208:
            case 74:
            case 130:
            case 229:
            case 230:
            case 46:
            case 53:
            case 146:
            case 237:
            case 261:
            case 131:
            case 231:
            case 238:
            case 54:
                monsterCrystal = 4260003; //intermediate 1
                break;
            case 239:
            case 240:
            case 132:
            case 241:
            case 147:
            case 179:
            case 133:
            case 242:
            case 148:
            case 80:
            case 232:
            case 233:
            case 234:
                monsterCrystal = 4260004; //intermediate 2
                break;
            case 134:
            case 149:
            case 264:
            case 265:
            case 268:
            case 135:
            case 150:
            case 225:
            case 266:
            case 180:
                monsterCrystal = 4260005; //intermediate 3
                break;
            case 269:
            case 270:
            case 448:
            case 181:
            case 267:
            case 272:
            case 449:
            case 450:
            case 271:
            case 274:
                monsterCrystal = 4260006; //advanced 1
                break;
            case 273:
            case 452:
            case 453:
                monsterCrystal = 4260007; //advanced 2
                break;
            case 454:
            case 455:
            case 457:
            case 458:
                monsterCrystal = 4260008; //advanced 3
                break;
        }
        if (monsterCrystal != 4260008 && monsterCrystal != 0) {
            if (Math.ceil(Math.random() * 100.0) <= 10) { //10% chance on higher crystal
                monsterCrystal++;
            }
        }
        return monsterCrystal;
    }

    private int getMonsterCrystalByLevel(int level) {
        int monsterCrystal = 0;
        if (level > 30 && level <= 50) {
            monsterCrystal = 4260000;
        } else if (level > 50 && level <= 60) {
            monsterCrystal = 4260001;
        } else if (level > 60 && level <= 70) {
            monsterCrystal = 4260002;
        } else if (level > 70 && level <= 80) {
            monsterCrystal = 4260003;
        } else if (level > 70 && level <= 80) {
            monsterCrystal = 4260003;
        } else if (level > 80 && level <= 90) {
            monsterCrystal = 4260004;
        } else if (level > 90 && level <= 100) {
            monsterCrystal = 4260005;
        } else if (level > 100 && level <= 110) {
            monsterCrystal = 4260006;
        } else if (level > 110 && level <= 120) {
            monsterCrystal = 4260007;
        } else if (level > 120) {
            monsterCrystal = 4260008;
        }

        if (monsterCrystal != 4260008 && monsterCrystal != 0) {
            if (Math.ceil(Math.random() * 100.0) <= 10) { //10% chance on higher crystal
                monsterCrystal++;
            }
        }
        return monsterCrystal;
    }

    private Equip applyGems(Equip eq, List<Integer> gems) {
        MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
        final String[] allProps = {"PAD", "MAD", "ACC", "EVA", "Speed", "Jump", "MaxHP", "MaxMP", "STR", "INT", "LUK", "DEX", "ReqLevel", "randOption", "randStat"};
        for (int gem : gems) {
            for (int i = 0; i < allProps.length; i++) {
                int stat = ii.getGemStatbyName(gem, allProps[i]);
                if (stat == 0)
                    continue;
                switch (i) {
                    case 0:
                        eq.setWatk((short) (eq.getWatk() + stat));
                        break;
                    case 1:
                        eq.setMatk((short) (eq.getMatk() + stat));
                        break;
                    case 2:
                        eq.setAcc((short) (eq.getAcc() + stat));
                        break;
                    case 3:
                        eq.setAvoid((short) (eq.getAvoid() + stat));
                        break;
                    case 4:
                        eq.setSpeed((short) (eq.getSpeed() + stat));
                        break;
                    case 5:
                        eq.setJump((short) (eq.getJump() + stat));
                        break;
                    case 6:
                        eq.setHp((short) (eq.getHp() + stat));
                        break;
                    case 7:
                        eq.setMp((short) (eq.getMp() + stat));
                        break;
                    case 8:
                        eq.setStr((short) (eq.getStr() + stat));
                        break;
                    case 9:
                        eq.setInt((short) (eq.getInt() + stat));
                        break;
                    case 10:
                        eq.setLuk((short) (eq.getLuk() + stat));
                        break;
                    case 11:
                        eq.setDex((short) (eq.getDex() + stat));
                        break;
                    case 12:
                        //eq.setReqLevel((short) (eq.getReqLevel() + stat));
                        break;
                    case 13: {
                        final List<Integer> statPool = new ArrayList<>(4);
                        if (eq.getWatk() > 0) {
                            statPool.add(0);
                        }
                        if (eq.getMatk() > 0) {
                            statPool.add(1);
                        }
                        if (eq.getSpeed() > 0) {
                            statPool.add(2);
                        }
                        if (eq.getJump() > 0) {
                            statPool.add(3);
                        }
                        if (statPool.isEmpty())
                            break;
                        int increase = Math.ceil(Math.random() * 100.0) <= 50 ? -1 : 1;
                        switch (statPool.get(Randomizer.nextInt(statPool.size()))) {
                            case 0:
                                eq.setWatk((short) (eq.getWatk() + stat * increase));
                                break;
                            case 1:
                                eq.setMatk((short) (eq.getMatk() + stat * increase));
                                break;
                            case 2:
                                eq.setSpeed((short) (eq.getSpeed() + stat * increase));
                                break;
                            case 3:
                                eq.setJump((short) (eq.getJump() + stat * increase));
                                break;
                        }
                        break;
                    }
                    case 14: {
                        final List<Integer> statPool = new ArrayList<>(6);
                        if (eq.getStr() > 0) {
                            statPool.add(0);
                        }
                        if (eq.getDex() > 0) {
                            statPool.add(1);
                        }
                        if (eq.getInt() > 0) {
                            statPool.add(2);
                        }
                        if (eq.getLuk() > 0) {
                            statPool.add(3);
                        }
                        if (eq.getAcc() > 0) {
                            statPool.add(4);
                        }
                        if (eq.getAvoid() > 0) {
                            statPool.add(5);
                        }
                        if (statPool.isEmpty())
                            break;
                        int increase = Math.ceil(Math.random() * 100.0) <= 50 ? -1 : 1;
                        switch (statPool.get(Randomizer.nextInt(statPool.size()))) {
                            case 0:
                                eq.setStr((short) (eq.getStr() + stat * increase));
                                break;
                            case 1:
                                eq.setDex((short) (eq.getDex() + stat * increase));
                                break;
                            case 2:
                                eq.setInt((short) (eq.getInt() + stat * increase));
                                break;
                            case 3:
                                eq.setLuk((short) (eq.getLuk() + stat * increase));
                                break;
                            case 4:
                                eq.setAcc((short) (eq.getAcc() + stat * increase));
                                break;
                            case 5:
                                eq.setAvoid((short) (eq.getAvoid() + stat * increase));
                                break;
                        }
                        break;
                    }
                }
                break;
            }
        }
        return eq;
    }
}