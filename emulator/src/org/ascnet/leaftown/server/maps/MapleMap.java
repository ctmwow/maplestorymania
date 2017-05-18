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

import java.awt.Point;
import java.awt.Rectangle;
import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.ascnet.leaftown.client.Equip;
import org.ascnet.leaftown.client.IItem;
import org.ascnet.leaftown.client.Item;
import org.ascnet.leaftown.client.MapleBuffStat;
import org.ascnet.leaftown.client.MapleCharacter;
import org.ascnet.leaftown.client.MapleClient;
import org.ascnet.leaftown.client.MapleDisease;
import org.ascnet.leaftown.client.MapleInventoryType;
import org.ascnet.leaftown.client.MaplePet;
import org.ascnet.leaftown.client.SkillFactory;
import org.ascnet.leaftown.client.anticheat.CheatingOffense;
import org.ascnet.leaftown.client.messages.MessageCallback;
import org.ascnet.leaftown.client.status.MonsterStatus;
import org.ascnet.leaftown.client.status.MonsterStatusEffect;
import org.ascnet.leaftown.database.DatabaseConnection;
import org.ascnet.leaftown.net.MaplePacket;
import org.ascnet.leaftown.net.channel.ChannelServer;
import org.ascnet.leaftown.net.world.MaplePartyCharacter;
import org.ascnet.leaftown.scripting.maps.MapScriptManager;
import org.ascnet.leaftown.server.MapleInventoryManipulator;
import org.ascnet.leaftown.server.MapleItemInformationProvider;
import org.ascnet.leaftown.server.MapleOxQuiz;
import org.ascnet.leaftown.server.MaplePlayerNPC;
import org.ascnet.leaftown.server.MaplePortal;
import org.ascnet.leaftown.server.MapleStatEffect;
import org.ascnet.leaftown.server.TimerManager;
import org.ascnet.leaftown.server.life.MapleLifeFactory;
import org.ascnet.leaftown.server.life.MapleMonster;
import org.ascnet.leaftown.server.life.MapleNPC;
import org.ascnet.leaftown.server.life.MobSkill;
import org.ascnet.leaftown.server.life.MobSkillFactory;
import org.ascnet.leaftown.server.life.SpawnPoint;
import org.ascnet.leaftown.server.playerinteractions.IMaplePlayerShop;
import org.ascnet.leaftown.tools.MaplePacketCreator;
import org.ascnet.leaftown.tools.Pair;
import org.ascnet.leaftown.tools.Randomizer;
import org.ascnet.leaftown.tools.StringUtil;

public class MapleMap 
{
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(MapleMap.class);
    private static final int MAX_OID = 20000;
    private static final List<MapleMapObjectType> rangedMapobjectTypes = Arrays.asList(MapleMapObjectType.NPC, MapleMapObjectType.ITEM, MapleMapObjectType.MONSTER, MapleMapObjectType.DOOR, MapleMapObjectType.SUMMON, MapleMapObjectType.REACTOR);
    /**
     * Holds a mapping of all oid -> MapleMapObject on this map. mapobjects is NOT a synchronized collection since it
     * has to be synchronized together with runningOid that's why all access to mapobjects have to be done trough an
     * explicit synchronized block
     */
    private final Map<Integer, MapleMapObject> mapobjects = new LinkedHashMap<>();
    private final Collection<SpawnPoint> monsterSpawn = new LinkedList<>();
    private final AtomicInteger spawnedMonstersOnMap = new AtomicInteger(0);
    private final Collection<MapleCharacter> characters = new LinkedHashSet<>();
    private final Map<Integer, MaplePortal> portals = new HashMap<>();
    private final Map<Integer, Point> seats = new HashMap<>();
    private final List<MaplePortal> spawnPoints = new ArrayList<>();
    private final List<Rectangle> areas = new ArrayList<>();
    private final List<MaplePlayerNPC> playerNPCs = new ArrayList<>();
    private MapleFootholdTree footholds = null;
    private MapleMapEffect mapEffect = null;
    private ScheduledFuture<?> mapEffectSch = null;
    private MapleMapTimer mapTimer = null;
    private final ArrayList<MapleMapTimer> hiddenMapTimer = new ArrayList<>();
    private MapleOxQuiz ox = null;
    private ScheduledFuture<?> spawnWorker = null;
    private short decHP = 0, createMobInterval = 9000;
    private final int mapId, returnMapId, channel, dropLife = 180000;
    private int runningOid = 100, forcedReturnMap = 999999999, timeLimit, protectItem = 0, fieldLimit = 0/*, maxRegularSpawn = 0*/;
    private int levelLimit, lvForceMove;
    private final float origMobRate;
    private float monsterRate;
    private boolean everlast = false, allowShops, partyOnly;
    private boolean dropsDisabled = false, spawnsEnabled = true, clock, boat, docked, town, hasEvent, muted, lootable = true;
    private String mapName, streetName, onUserEnter, onFirstUserEnter;
    private boolean canEnter = true, canExit = true, cannotInvincible = false, canVipRock = true, allowSkills = true, canMovement = true;
    private ScheduledFuture<?> dojoSpawn = null;
    private MapleBuffZone buffZone;
    private final Lock objectLock = new ReentrantLock();
    //begin HenesysPQ variables
    private int hpq_Bunny_Hits = 0;  
    private int riceCakeNum = 0;
    //end HenesysPQ variables

    public MapleMap(int mapId, int channel, int returnMapId, float monsterRate, boolean isInstance) 
    {
        this.mapId = mapId;
        this.channel = channel;
        this.returnMapId = returnMapId;
        this.monsterRate = monsterRate;
        origMobRate = monsterRate;
        
        if (monsterRate > 0 && isInstance)
            spawnWorker = TimerManager.getInstance().register(new RespawnWorker(), createMobInterval);
        if (getPlayerNPCMap() != -1)
            loadPlayerNPCs();
    }
    
    public final void resetFully() 
    {
        resetFully(true);
    }

    public final void resetFully(final boolean respawn) 
    {
    	killAllMonsters(false);
        resetReactors();
        removeDrops();
        resetSpawn();
        resetPortals();
        
        if (respawn) 
            respawn();
    }
    
    public final void removeDrops() 
    {
        List<MapleMapObject> items = getAllItems();
        for (MapleMapObject i : items) 
        {
        	for(MapleCharacter character : characters)
        		i.sendDestroyData(character.getClient());
        }
    }

    public boolean canEnter() {
        return canEnter;
    }

    public boolean canExit() {
        return canExit;
    }

    public void setCanEnter(boolean b) {
        canEnter = b;
    }

    public void setCanExit(boolean b) {
        canExit = b;
    }

    public void toggleDrops() {
        dropsDisabled = !dropsDisabled;
    }

    public int getId() {
        return mapId;
    }

    public MapleMap getReturnMap() {
        return ChannelServer.getInstance(channel).getMapFactory().getMap(returnMapId);
    }

    public int getReturnMapId() {
        return returnMapId;
    }

    public int getForcedReturnId() {
        return forcedReturnMap;
    }

    public MapleMap getForcedReturnMap() {
        return ChannelServer.getInstance(channel).getMapFactory().getMap(forcedReturnMap);
    }

    public void setForcedReturnMap(int map) {
        forcedReturnMap = map;
    }

    public int getTimeLimit() {
        return timeLimit;
    }

    public void setTimeLimit(int timeLimit) {
        this.timeLimit = timeLimit;
    }

    public boolean getMuted() {
        return muted;
    }

    public void setMuted(boolean isMuted) {
        muted = isMuted;
    }

    public boolean isLootable() {
        return lootable;
    }

    public void setLootable(boolean loot) {
        lootable = loot;
    }

    public boolean canUseSkills() {
        return allowSkills;
    }
    
    public void setSpawns(boolean value)
    {
    	spawnsEnabled = value;
    }

    public void setAllowSkills(boolean allow) {
        allowSkills = allow;
        final List<MapleCharacter> allChars = getCharacters();
        if (!allow) {
            MobSkill ms = MobSkillFactory.getMobSkill(120, 1);
            for (MapleCharacter mc : allChars) {
                if (!mc.isGM()) {
                    mc.giveDebuff(MapleDisease.GM_DISABLE_SKILL, ms, true);
                }
            }
        } else {
            for (MapleCharacter mc : allChars) {
                mc.dispelDebuff(MapleDisease.GM_DISABLE_SKILL);
            }
        }
    }

    public boolean canMove() {
        return canMovement;
    }

    public void setCanMove(boolean move) {
        canMovement = move;
        final List<MapleCharacter> allChars = getCharacters();
        if (!move) {
            MobSkill ms = MobSkillFactory.getMobSkill(123, 1);
            for (MapleCharacter mc : allChars) {
                if (!mc.hasGMLevel(2)) {
                    mc.giveDebuff(MapleDisease.GM_DISABLE_MOVEMENT, ms, true);
                }
            }
        } else {
            for (MapleCharacter mc : allChars) {
                mc.dispelDebuff(MapleDisease.GM_DISABLE_MOVEMENT);
            }
        }
    }

    public void addMapObject(MapleMapObject mapobject) {
        objectLock.lock();
        try {
            mapobject.setObjectId(runningOid);
            mapobjects.put(runningOid, mapobject);
            incrementRunningOid();
        } finally {
            objectLock.unlock();
        }
    }

    private void spawnAndAddRangedMapObject(MapleMapObject mapobject, DelayedPacketCreation packetbakery, SpawnCondition condition) 
    {
        objectLock.lock();
        try 
        {
            mapobject.setObjectId(runningOid);
            for (MapleCharacter chr : characters) 
            {
                if (condition == null || condition.canSpawn(chr)) 
                {
                    if (chr.getPosition().distanceSq(mapobject.getPosition()) <= MapleCharacter.MAX_VIEW_RANGE_SQ) 
                    {
                        if (chr.canSeeItem(mapobject)) 
                        {
                            chr.addVisibleMapObject(mapobject);
                            packetbakery.sendPackets(chr.getClient());
                        }
                    }
                }
            }
            mapobjects.put(runningOid, mapobject);
            incrementRunningOid();
        } 
        finally 
        {
            objectLock.unlock();
        }
    }

    public void spawnMesoDrop(final int meso, Point position, final MapleMapObject dropper, final MapleCharacter owner, final boolean ffaLoot, final byte type) {
        spawnMesoDrop(meso, position, dropper, owner, ffaLoot, type, false);
    }

    public void spawnMesoDrop(final int meso, Point position, final MapleMapObject dropper, final MapleCharacter owner, final boolean ffaLoot, final byte type, final boolean isPlayerDrop) {
        TimerManager tMan = TimerManager.getInstance();
        final Point droppos = calcDropPos(position, position);
        final MapleMapItem mdrop = new MapleMapItem(meso, droppos, dropper, owner, type);
        mdrop.setPlayerDrop(isPlayerDrop);
        spawnAndAddRangedMapObject(mdrop, new DelayedPacketCreation() {

            public void sendPackets(MapleClient c) {
                c.sendPacket(MaplePacketCreator.dropMesoFromMapObject(meso, mdrop.getObjectId(), type == 4 ? 0 : dropper.getObjectId(), type == 4 ? 0 : type == 1 ? owner.getPartyId() : owner.getId(), dropper.getPosition(), droppos, (byte) 1, type, isPlayerDrop));
            }
        }, null);
        tMan.schedule(new ExpireMapItemJob(mdrop), dropLife);
    }

    private void incrementRunningOid() {
        runningOid++;
        for (int numIncrements = 1; numIncrements < MAX_OID; numIncrements++) {
            if (runningOid > MAX_OID) {
                runningOid = 100;
            }
            if (mapobjects.containsKey(runningOid)) {
                runningOid++;
            } else {
                return;
            }
        }
        throw new RuntimeException("Out of OIDs on map " + mapId + " (channel: " + channel + ")");
    }

    public void mapMessage(int type, String message) 
    {
        broadcastMessage(MaplePacketCreator.serverNotice(type, message));
    }

    public void removeMapObject(int num) {
        objectLock.lock();
        try {
            final MapleMapObject obj = mapobjects.remove(num);
            if (obj != null) {
                for (MapleCharacter character : characters) {
                    character.removeVisibleMapObject(obj);
                }
            }
        } finally {
            objectLock.unlock();
        }
    }

    public void removeMapObject(MapleMapObject obj) {
        objectLock.lock();
        try {
            mapobjects.remove(obj.getObjectId());
            for (MapleCharacter character : characters) {
                character.removeVisibleMapObject(obj);
            }
        } finally {
            objectLock.unlock();
        }
    }
    
    public Point getGroundBelow(Point pos) 
    {
        Point spos = new Point(pos.x, pos.y - 3); // Using -3 fixes issues with spawning pets causing a lot of issues.
        spos = calcPointBelow(spos);
        spos.y--;//shouldn't be null!
        return spos;
    }

    private Point calcPointBelow(Point initial) {
        MapleFoothold fh = footholds.findBelow(initial);
        if (fh == null) {
            return null;
        }
        int dropY = fh.getY1();
        if (!fh.isWall() && fh.getY1() != fh.getY2()) {
            double s1 = Math.abs(fh.getY2() - fh.getY1());
            double s2 = Math.abs(fh.getX2() - fh.getX1());
            double s4 = Math.abs(initial.x - fh.getX1());
            double alpha = Math.atan(s2 / s1);
            double beta = Math.atan(s1 / s2);
            double s5 = Math.cos(alpha) * s4 / Math.cos(beta);
            if (fh.getY2() < fh.getY1()) {
                dropY = fh.getY1() - (int) s5;
            } else {
                dropY = fh.getY1() + (int) s5;
            }
        }
        return new Point(initial.x, dropY);
    }

    private Point calcDropPos(Point initial, Point fallback) {
        Point ret = calcPointBelow(new Point(initial.x, initial.y - 99));
        if (ret == null) {
            return fallback;
        }
        return ret;
    }

    private void dropFromMonster(MapleCharacter dropOwner, MapleMonster monster) 
    {
        if (dropsDisabled || monster.dropsDisabled())
            return;

        MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();

        final int maxDrops = monster.getMaxDrops(dropOwner);
        final boolean explosive = monster.isExplosive();

        List<Integer> toDrop = new ArrayList<>();

        for (int i = 0; i < maxDrops; i++) 
            toDrop.add(monster.getDrop(dropOwner));

        if (dropOwner.getEventInstance() == null && !monster.isDojoMinion()) 
        {
            int chance = Randomizer.nextInt(100);
            if (chance < 30 && !monster.isHalloweenBoss()) { // 20% chance of getting a maple leaf
                toDrop.add(4001126);
            }
            chance = Randomizer.nextInt(100);
            if (monster.getId() == 8800002 && chance < 50) { // Zakum
                toDrop.add(2388023);
            } else if (chance == 7 || chance == 8 || chance == 99 || chance == 23) { // 1% Chance of getting a monster card (Actual Value = 0.5 - 2%)
                int cardid = ii.getMobCardId(monster.getId());
                if (cardid != -1) 
                    toDrop.add(cardid);
            } else if (monster.isBoss() && chance >= 80) 
            {
                int cardid = ii.getMobCardId(monster.getId());
                if (cardid != -1)
                    toDrop.add(cardid);
            }
            chance = Randomizer.nextInt(100);
            if (mapId / 100000 == 2400 && chance < 10)
                toDrop.add(4001393);
        }
        if (monster.isDojoMinion()) 
        {
            toDrop.add(2022432); //dojo power elixir
            toDrop.add(2022433); //dojo all cure
        }
        if (monster.getId() == 8810018) 
        {
            toDrop.add(2290096);
        } 
        else if (monster.getId() == 9001011) 
        {
            int proofOfExam = 0;
            switch (dropOwner.getJob().getId()) 
            {
                case 1100:
                    proofOfExam = 4032096;
                    break;
                case 1200:
                    proofOfExam = 4032097;
                    break;
                case 1300:
                    proofOfExam = 4032098;
                    break;
                case 1400:
                    proofOfExam = 4032099;
                    break;
                case 1500:
                    proofOfExam = 4032100;
                    break;
            }
            if (proofOfExam != 0 && !dropOwner.haveItem(proofOfExam, 30, false, false))
                toDrop.add(proofOfExam);
        }
        
        Set<Integer> alreadyDropped = new HashSet<>();
        int htpendants = 0;
        int htstones = 0;
        for (int i = 0; i < toDrop.size(); i++) 
        {
            if (toDrop.get(i) == 1122000) 
            {
                if (htpendants > 3)
                    toDrop.set(i, -1);
                else
                    htpendants++;
            } 
            else if (toDrop.get(i) == 4001094) 
            {
                if (htstones > 2)
                    toDrop.set(i, -1);
                else
                    htstones++;
            } 
            else if (alreadyDropped.contains(toDrop.get(i)) && !explosive) 
            {
                toDrop.remove(i);
                i--;
            } 
            else
                alreadyDropped.add(toDrop.get(i));
        }
        if (monster.getId() == 9400608) 
        {
            for (int i = 0; i < 5; i++)
                toDrop.add(4001168);
        }
        if (monster.getId() == 9400633) 
        {
            for (int i = 0; i < 10; i++) 
                toDrop.add(2022428);
        } 
        else if (monster.isHalloweenBoss()) 
        {
            final int GREEN = 2022105;
            final int RED = 2022106;
            final int BLUE = 2022107;
            final int[][] candyDrops = {
                    {9500325, RED, -1, -1},
                    {9500327, RED, GREEN, -1},
                    {9500329, RED, GREEN, -1},
                    {9400571, RED, GREEN, -1},
                    {9500328, RED, GREEN, -1},
                    {9500330, RED, GREEN, -1},
                    {9400572, RED, GREEN, -1},
                    {9400575, RED, GREEN, -1},
                    {9400576, RED, GREEN, -1},
                    {9500173, RED, GREEN, BLUE},
                    {9500174, RED, GREEN, BLUE},
                    {9500331, BLUE, -1, -1},
                    {9500332, BLUE, -1, -1}
            };
            toDrop.clear();
            for (int[] candyDrop : candyDrops) 
            {
                if (candyDrop[0] == monster.getId()) 
                {
                    while (toDrop.size() < 6) 
                    {
                        for (int k = 1; k < 4; k++) 
                        {
                            if (candyDrop[k] != -1)
                                toDrop.add(candyDrop[k]);
                        }
                    }
                }
            }
            if (toDrop.size() > maxDrops)
                toDrop = toDrop.subList(0, maxDrops);
        }

        if (toDrop.isEmpty())
            return; // Nothing to drop. Don't need to place items.

        final Point[] toPoint = new Point[toDrop.size()];
        int shiftDirection = 0;
        int shiftCount = 0;

        int curX = Math.min(Math.max(monster.getPosition().x - 25 * (toDrop.size() / 2), footholds.getMinDropX() + 25), footholds.getMaxDropX() - toDrop.size() * 25);
        int curY = Math.max(monster.getPosition().y, footholds.getY1());
        while (shiftDirection < 3 && shiftCount < 1000) 
        {
            if (shiftDirection == 1) 
                curX += 25;
            else if (shiftDirection == 2)
                curX -= 25;
            
            for (int i = 0; i < toDrop.size(); i++) 
            {
                MapleFoothold wall = footholds.findWall(new Point(curX, curY), new Point(curX + toDrop.size() * 25, curY));
                if (wall != null) 
                {
                    if (wall.getX1() < curX) 
                    {
                        shiftDirection = 1;
                        shiftCount++;
                        break;
                    } 
                    else if (wall.getX1() == curX) 
                    {
                        if (shiftDirection == 0)
                            shiftDirection = 1;
                        shiftCount++;
                        break;
                    } 
                    else 
                    {
                        shiftDirection = 2;
                        shiftCount++;
                        break;
                    }
                } 
                else if (i == toDrop.size() - 1)
                    shiftDirection = 3;
                
                final Point dropPos = calcDropPos(new Point(curX + i * 25, curY), new Point(monster.getPosition()));
                toPoint[i] = new Point(curX + i * 25, curY);
                final int drop = toDrop.get(i);
                byte dropType = 0;
              
                if (explosive)
                    dropType = 3;
                else if (monster.isFfaLoot())
                    dropType = 2;
                else if (dropOwner.getParty() != null)
                    dropType = 1;
                
                if (drop == -1) 
                {
                	if(monster.isDojoMinion() || monster.isCPQMonster())
                		return;
                	
                    final int mesoRate = ChannelServer.getInstance(channel).getMesoRate();
                    double mesoDecrease = Math.pow(0.93, monster.getExp() / 300.0);
                    if (mesoDecrease > 1.0)
                        mesoDecrease = 1.0;
                    if (mesoDecrease <= 0.0 && (monster.getId() == 8810018 || monster.getId() == 8800002))
                        mesoDecrease = Math.random();
                    int tempmeso = Math.min(30000, (int) (mesoDecrease * monster.getExp() * (1.0 + Math.random() * 20) / 10.0));
                    if (dropOwner.getBuffedValue(MapleBuffStat.MESOUP) != null)
                        tempmeso = (int) (tempmeso * dropOwner.getBuffedValue(MapleBuffStat.MESOUP).doubleValue() / 100.0);
                    if (tempmeso < 1 && (monster.getId() == 8810018 || monster.getId() == 8800002))
                        tempmeso = Randomizer.nextInt(30000);
                        
                    final int meso = tempmeso;

                    if (meso > 0) 
                    {
                        final MapleMonster dropMonster = monster;
                        final MapleCharacter dropChar = dropOwner;
                        final byte fDropType = dropType;
                        TimerManager.getInstance().schedule(new Runnable() 
                        {
                            public void run() 
                            {
                                spawnMesoDrop(meso * mesoRate, dropPos, dropMonster, dropChar, explosive, fDropType);
                            }
                        }, monster.getAnimationTime("die1") + i);
                    }
                } 
                else 
                {
                    IItem idrop;
                    MapleInventoryType type = ii.getInventoryType(drop);
                    if (type.equals(MapleInventoryType.EQUIP)) 
                    {
                        Equip nEquip = ii.randomizeStats((Equip) ii.getEquipById(drop));
                        idrop = nEquip;
                    } 
                    else 
                    {
                        idrop = new Item(drop, (byte) 0, (short) 1);
                        if (ii.isArrowForBow(drop) || ii.isArrowForCrossBow(drop))
                            idrop.setQuantity((short) (1 + 100 * Math.random()));
                        else if (idrop.getItemId() == 4001106 && monster.getId() == 9400218)
                            idrop.setQuantity((short) 50);
                        else if (ii.isThrowingStar(drop) || ii.isShootingBullet(drop))
                            idrop.setQuantity((short) 1);
                    }

                    idrop.log("Created as a drop from monster " + monster.getObjectId() + " (" + monster.getId() + ") at " + dropPos.toString() + " on map " + mapId, false);

                    final MapleMapItem mdrop = new MapleMapItem(idrop, dropPos, monster, dropOwner, dropType);
                    final MapleMapObject dropMonster = monster;
                    final MapleCharacter dropChar = dropOwner;
                    final TimerManager tMan = TimerManager.getInstance();

                    tMan.schedule(new Runnable() 
                    {
                        @Override
                        public void run() 
                        {
                            spawnAndAddRangedMapObject(mdrop, new DelayedPacketCreation() 
                            {
                                public void sendPackets(MapleClient c) 
                                {
                                    c.sendPacket(MaplePacketCreator.dropItemFromMapObject(drop, mdrop.getObjectId(), dropMonster.getObjectId(), mdrop.getDropType() == 1 ? dropChar.getPartyId() : dropChar.getId(), dropMonster.getPosition(), dropPos, (byte) 1, mdrop.getItem().getExpiration(), mdrop.getDropType(), false));
                                    activateItemReactors(mdrop);
                                }
                            }, null);
                            tMan.schedule(new ExpireMapItemJob(mdrop), dropLife);
                            
                        }
                    }, monster.getAnimationTime("die1") + i);
                }
            }
        }
    }
    
    //begin HenesysPQ functions    
    public void resetRiceCakes() {
        this.riceCakeNum = 0;
    }
    
    public void addBunnyHit() {
        this.hpq_Bunny_Hits ++;
    }
    
    private void monsterItemDrop(final MapleMonster m, final IItem item, final long delay) 
    {
    	final ScheduledFuture<?> monsterItemDrop =  TimerManager.getInstance().register(new Runnable() 
        {
            @Override
            public void run() 
            {
                if (MapleMap.this.hpq_Bunny_Hits >= 5) 
                {
	                MapleMap.this.broadcastMessage(MaplePacketCreator.serverNotice(0, "O Coelhinho da lua está se sentindo doente. Proteja-o para que ele possa fazer deliciosos bolinhos de arroz.")); // Your choice, I think it should be in MobHitMobFriendly
	                MapleMap.this.hpq_Bunny_Hits = 0;
	                return;
                }
                
                if (m.getMap().getId() == 910010000)
                {
                    MapleMap.this.riceCakeNum++;
                    if (MapleMap.this.countMobOnMap(m.getId()) > 0)
                    {
                        if (item.getItemId() == 4001101) 
                            MapleMap.this.broadcastMessage(MaplePacketCreator.serverNotice(0, "O Coelhinho da lua fez o bolinho de número " + riceCakeNum + "."));
                        
                        final Point dropPoint = new Point();
                        dropPoint.setLocation(m.getPosition().getX() - 60, m.getPosition().getY());
                        
                        spawnItemDrop(m, null, item, dropPoint, true, true);
                    }
                }
            }
        }, delay, delay);
    	
    	final Thread bunnyWatcher = new Thread(new Runnable() 
    	{
			@Override
			public void run() 
			{
				while(true)
				{
					if(MapleMap.this.countMobOnMap(m.getId()) == 0)
					{
						monsterItemDrop.cancel(true);
						break;
					}
					
					try 
					{
						Thread.sleep(delay);
					} catch (InterruptedException e) {}
				}
			}
		});
    	bunnyWatcher.setName("Bunny Dead Watch");
    	bunnyWatcher.setPriority(Thread.MIN_PRIORITY);
    	bunnyWatcher.start();
    }
    //end HenesysPQ functions

    public boolean damageMonster(MapleCharacter chr, MapleMonster monster, int damage) {
        if (!isDojoMap()) { // it'd be easy with bamboo rain
            if (damage > 10000) {
                chr.finishAchievement(20);
            }
            if (damage >= 99999) {
                chr.finishAchievement(21);
            }
            if (damage >= 199999) {
                chr.finishAchievement(43);
            }
        }

        if (monster.getId() == 8800000) {
            for (MapleMapObject object : getAllMonsters()) {
                MapleMonster mons = getMonsterByOid(object.getObjectId());
                if (mons != null && mons.getId() >= 8800003 && mons.getId() <= 8800010) {
                    return true;
                }
            }
        }

        if (monster.isAlive()) {
            if (damage > 0) {
                monster.damage(chr, damage, true);
                if (monster.getSponge() != null) {
                    damageMonster(chr, monster.getSponge(), damage);
                }
                if (!monster.isAlive()) {
                    killMonster(monster, chr, true, false, 1);
                }
            }
            return true;
        }
        return false;
    }

    public void killMonster(final MapleMonster monster, final MapleCharacter chr, final boolean withDrops) {
        killMonster(monster, chr, withDrops, false, 1);
    }

    public void killMonster(final MapleMonster monster, final MapleCharacter chr, final boolean withDrops, final boolean secondTime) {
        killMonster(monster, chr, withDrops, secondTime, 1);
    }

    public void killMonster(int monsId) {
        for (MapleMapObject mmo : getAllMonsters()) {
            if (((MapleMonster) mmo).getId() == monsId) {
                killMonster((MapleMonster) mmo, characters.iterator().next(), false);
            }
        }
    }

    public void killMonster(int monsId, MapleCharacter trigger) {
        for (MapleMapObject mmo : getAllMonsters()) {
            if (((MapleMonster) mmo).getId() == monsId) {
                killMonster((MapleMonster) mmo, trigger, false);
            }
        }
    }

    public void killMonster(final MapleMonster monster, final MapleCharacter chr, final boolean withDrops, final boolean secondTime, int animation) {
        if (chr == null || monster == null)
            return;
        if (chr.getCheatTracker().checkHPLoss()) {
            chr.getCheatTracker().registerOffense(CheatingOffense.ATTACK_WITHOUT_GETTING_HIT);
        }
        if (monster.getId() == 8810018 && !secondTime) {
            TimerManager.getInstance().schedule(new Runnable() {

                @Override
                public void run() {
                    killMonster(monster, chr, withDrops, true, 1);
                    killAllMonsters(false);
                }
            }, 3000);
            return;
        }
        final List<MapleCharacter> chars = getCharacters();
        if (monster.getBuffToGive() > -1) {
            broadcastMessage(MaplePacketCreator.showOwnBuffEffect(monster.getBuffToGive(), 11, (byte) chr.getLevel()));
            MapleItemInformationProvider mii = MapleItemInformationProvider.getInstance();
            MapleStatEffect statEffect = mii.getItemEffect(monster.getBuffToGive());
            for (MapleCharacter character : chars) {
                if (character.isAlive()) {
                	statEffect.applyTo(character);
                    broadcastMessage(MaplePacketCreator.showBuffeffect(character.getId(), monster.getBuffToGive(), 11, (byte) character.getLevel(), (byte) 3));
                }
            }
        }
        if (isDojoMap() && monster.getId() > 9300183 && monster.getId() < 9300216) { // that'll do =|
            disableDojoSpawn();
            monster.disableDrops();
            int newpoints = (getDojoStage() - getDojoStage() / 5) / 5 + 2;
            for (MapleCharacter character : chars) {
                if (character.isAlive()) {
                    character.getDojo().setPoints(character.getDojo().getPoints() + newpoints);
                    character.getClient().sendPacket(MaplePacketCreator.playSound("Dojang/clear"));
                    character.getClient().sendPacket(MaplePacketCreator.showEffect("dojang/end/clear"));
                    character.getClient().sendPacket(MaplePacketCreator.playerMessage("You received " + newpoints + " training points. Your total training score is now " + character.getDojo().getPoints() + "."));
                    character.getClient().sendPacket(MaplePacketCreator.dojoWarpUp());
                }
            }
        }
        
       if (monster.getId() == 9300166) { //ariant pq bomb animation
            animation = 2; //
        }

        spawnedMonstersOnMap.decrementAndGet();
        monster.setHp(0);

        MapleCharacter dropOwner = monster.killBy(chr, channel);

        broadcastMessage(MaplePacketCreator.killMonster(monster.getObjectId(), animation));
        removeMapObject(monster);

        if (monster.getId() >= 8800003 && monster.getId() <= 8800010) {
            boolean makeZakReal = true;
            List<MapleMapObject> objects = getAllMonsters();
            for (MapleMapObject object : objects) {
                MapleMonster mons = getMonsterByOid(object.getObjectId());
                if (mons != null && mons.getId() >= 8800003 && mons.getId() <= 8800010) {
                    makeZakReal = false;
                }
            }
            if (makeZakReal) {
                for (MapleMapObject object : objects) {
                    MapleMonster mons = getMonsterByOid(object.getObjectId());
                    if (mons != null && mons.getId() == 8800000) {
                        final Point pos = mons.getPosition();
                        killAllMonsters(true);
                        spawnMonsterOnGroundBelow(MapleLifeFactory.getMonster(8800000), pos);
                    }
                }
            }
        }

        if (withDrops && !monster.dropsDisabled()) {
            if (dropOwner == null) {
                dropOwner = chr;
            }
            dropFromMonster(dropOwner, monster);
        }
        monster.dispose();
    }
    
    public void killMonster(final MapleMonster monster, final MapleCharacter chr, final boolean withDrops, final boolean catched, final boolean secondTime, int animation) {
        if (monster.getId() == 8810018 && !secondTime) {
            TimerManager.getInstance().schedule(new Runnable() {
                @Override
                public void run() {
                    killMonster(monster, chr, withDrops, false, true, 1);
                    killAllMonsters(true);
                }
            }, 3000);
            return;
        }
        spawnedMonstersOnMap.decrementAndGet();
        monster.setHp(0);
        broadcastMessage(MaplePacketCreator.killMonster(monster.getObjectId(), true), monster.getPosition());
        removeMapObject(monster);
        MapleCharacter dropOwner = monster.killBy(chr,chr.getClient().getChannel());
        if (withDrops && !monster.dropsDisabled()) {
            if (dropOwner == null) {
                    dropOwner = chr;
            }
            dropFromMonster(dropOwner, monster);
        }
    }

    public void killMonster(final MapleMonster monster) {
        killMonster(monster, 3, false);
    }

    public void killMonster(final MapleMonster monster, int anim, boolean revive) {
        spawnedMonstersOnMap.decrementAndGet();
        monster.setHp(0);
        if (revive)
            monster.spawnRevives(this);
        broadcastMessage(MaplePacketCreator.killMonster(monster.getObjectId(), anim));
        removeMapObject(monster);
        monster.dispose();
    }

    public void killAllMonsters(boolean drop) {
        killAllMonsters(drop, false);
    }

    public void killAllMonsters(boolean drop, boolean revive) 
    {
        for (MapleMapObject monstermo : getAllMonsters()) 
        {
            MapleMonster monster = (MapleMonster) monstermo;
            spawnedMonstersOnMap.decrementAndGet();
            monster.setHp(0);
            
            if (revive)
            	monster.spawnRevives(this);
            
            broadcastMessage(MaplePacketCreator.killMonster(monster.getObjectId(), true), monster.getPosition());
            removeMapObject(monster);
            
            if (drop)
                dropFromMonster(getCharacters().get(Randomizer.nextInt(characters.size())), monster);
            
            monster.dispose();
        }
    }

    public void destroyReactor(int oid) {
        final MapleReactor reactor = getReactorByOid(oid);
        broadcastMessage(MaplePacketCreator.destroyReactor(reactor));
        reactor.setAlive(false);
        removeMapObject(reactor);
        reactor.setTimerActive(false);
        if (reactor.getDelay() > 0) {
            TimerManager.getInstance().schedule(new Runnable() {

                @Override
                public void run() {
                    respawnReactor(reactor);
                }
            }, reactor.getDelay());
        }
    }

    public void destroyReactors() {
        List<MapleMapObject> reactors = getAllReactors();
        for (MapleMapObject reactor : reactors) {
            destroyReactor(reactor.getObjectId());
        }
    }

    /*
     * command to reset all item-reactors in a map to state 0 for GM/NPC use - not tested (broken reactors get removed
     * from mapobjects when destroyed) Should create instances for multiple copies of non-respawning reactors...
     */
    public void resetReactors() {
        for (MapleMapObject o : getAllReactors()) {
            MapleReactor reactor = (MapleReactor) o;
            reactor.setState((byte) 0);
            reactor.setTimerActive(false);
            broadcastMessage(MaplePacketCreator.triggerReactor(reactor, 0));
        }
    }

    public void resetPortals() {
        for (MaplePortal portal : portals.values()) {
            portal.setSpawned(false);
            portal.setPortalStatus(MaplePortal.OPEN);
        }
    }

    /*
     * command to shuffle the positions of all reactors in a map for PQ purposes (such as ZPQ/LMPQ)
     */
    public void shuffleReactors() {
        List<Point> points = new ArrayList<>();
        final List<MapleMapObject> reactors = getAllReactors();
        for (MapleMapObject o : reactors) {
            points.add(o.getPosition());
        }
        Collections.shuffle(points);
        for (MapleMapObject o : reactors) {
            o.setPosition(points.remove(points.size() - 1));
        }
    }

    /**
     * Automatically finds a new controller for the given monster from the chars on the map...
     *
     * @param monster
     */
    public void updateMonsterController(MapleMonster monster) 
    {
        if (monster.getController() != null) 
        {
            // monster has a controller already, check if he's still on this map
            if (monster.getController().getMap() != this) 
            {
                log.warn("Monstercontroller wasn't on same map");
                monster.getController().stopControllingMonster(monster);
            }
            else 
                return; // controller is on the map, monster has an controller, everything is fine
        }
        
        int mincontrolled = -0x01;
        MapleCharacter newController = null;
        objectLock.lock();
        
        try 
        {
            for (MapleCharacter chr : characters) 
            {
                if (!chr.isHidden() && (chr.getControlledMonsters().size() < mincontrolled || mincontrolled == -1)) 
                {
                    mincontrolled = chr.getControlledMonsters().size();
                    newController = chr;
                }
            }
        }
        finally 
        {
            objectLock.unlock();
        }
        
        if (newController != null) 
        {
            if (monster.isFirstAttack()) 
            {
                newController.controlMonster(monster, true);
                monster.setControllerHasAggro(true);
                monster.setControllerKnowsAboutAggro(true);
            } 
            else// was a new controller found? (if not no one is on the map) 
                newController.controlMonster(monster, false);
        }
    }

    public boolean containsNPC(int npcid) {
        for (MapleMapObject obj : getAllNPCs()) {
            if (((MapleNPC) obj).getId() == npcid) {
                return true;
            }
        }
        return false;
    }

    public int getNPCbyID(int npcid) {
        for (MapleMapObject obj : getAllNPCs()) {
            if (((MapleNPC) obj).getId() == npcid) {
                return obj.getObjectId();
            }
        }
        return 0;
    }

    public MapleMapObject getMapObject(int oid) {
        return mapobjects.get(oid);
    }

    /**
     * returns a monster with the given oid, if no such monster exists returns null
     *
     * @param oid
     * @return
     */
    public MapleMonster getMonsterByOid(int oid) {
        MapleMapObject mmo = getMapObject(oid);
        if (mmo instanceof MapleMonster) {
            return (MapleMonster) mmo;
        }
        return null;
    }

    public MapleReactor getReactorByOid(int oid) {
        MapleMapObject mmo = getMapObject(oid);
        if (mmo instanceof MapleReactor) {
            return (MapleReactor) mmo;
        }
        return null;
    }

    public MapleReactor getReactorByName(String name) {
        for (MapleMapObject obj : getAllReactors()) {
            MapleReactor reactor = (MapleReactor) obj;
            if (reactor.getName().equals(name)) {
                return reactor;
            }
        }
        return null;
    }

    public void spawnMonsterOnGroundBelow(MapleMonster monster, Point pos) {
        Point spos = new Point(pos.x, pos.y - 1);
        spos = calcPointBelow(spos);
        spos.y -= 1;
        monster.setPosition(spos);
        spawnMonster(monster);
    }
    
    public void spawnMonsterOnGroundBelow(int mobid, int x, int y, String msg) {
        MapleMonster mob = MapleLifeFactory.getMonster(mobid);
        if (mob != null) {
            Point point = new Point(x, y);
            spawnMonsterOnGroundBelow(mob, point);
            this.broadcastMessage(MaplePacketCreator.serverNotice(6, msg));
        }
    }

    public void spawnMonsterOnGroundBelowForce(final MapleMonster monster, Point pos) {
        Point spos = new Point(pos.x, pos.y - 1);
        spos = calcPointBelow(spos);
        spos.y -= 1;
        monster.setPosition(spos);
        monster.setMap(this);
        doRemoveAfter(monster);
        spawnAndAddRangedMapObject(monster, new DelayedPacketCreation() {

            public void sendPackets(MapleClient c) {
                c.sendPacket(MaplePacketCreator.spawnMonster(monster, true, 0, 0));
            }
        }, null);
        updateMonsterController(monster);
        spawnedMonstersOnMap.incrementAndGet();
    }

    private void doRemoveAfter(final MapleMonster monster) {
        int removeAfter = monster.getRemoveAfter();
        if (removeAfter > 0) {
            TimerManager.getInstance().schedule(new Runnable() {

                @Override
                public void run() {
                    if (monster == null || !monster.isAlive())
                        return;
                    killMonster(monster, 1, true);
                    if (monster.isBoss() && !characters.isEmpty()) {
                        MaplePacket noticeAutoKill = MaplePacketCreator.topMessage(monster.getName() + " has been automatically killed");
                        broadcastMessage(noticeAutoKill);
                    }
                }
            }, removeAfter * 1000);
        }
    }

    public void spawnFakeMonsterOnGroundBelow(MapleMonster mob, Point pos) {
        Point spos = new Point(pos.x, pos.y - 1);
        spos = calcPointBelow(spos);
        spos.y -= 1;
        mob.setPosition(spos);
        spawnFakeMonster(mob);
    }

    public void spawnRevives(final MapleMonster monster, final int link) {
        monster.setMap(this);

        if (monster.getId() / 100 == 88100) { // ht
            MapleMonster mob;
            for (MapleMapObject obj : getAllMonsters()) {
                mob = getMonsterByOid(obj.getObjectId());
                if (mob != null && mob.getId() == 8810018) {
                    monster.setSponge(mob);
                    break;
                }
            }
        }
        spawnAndAddRangedMapObject(monster, new DelayedPacketCreation() {

            public void sendPackets(MapleClient c) {
                c.sendPacket(MaplePacketCreator.spawnMonster(monster, true, 0, link)); // TODO effect
            }
        }, null);
        updateMonsterController(monster);
        spawnedMonstersOnMap.incrementAndGet();
    }

    public void spawnMonster(final MapleMonster monster) 
    {
        if (characters.isEmpty() && !isPQMap()) // Without this monsters on PQ maps never spawn
            return;
        
        monster.setMap(this);
        doRemoveAfter(monster);
        spawnAndAddRangedMapObject(monster, new DelayedPacketCreation() 
        {
            public void sendPackets(MapleClient c) 
            {
                c.sendPacket(MaplePacketCreator.spawnMonster(monster, true, 0, 0));
                
                if (monster.getDropPeriodTime() > 0) //9300102 - Watchhog, 9300061 - Moon Bunny (HPQ)
                { 
                    if (monster.getId() == 9300102) 
                        monsterItemDrop(monster, new Item(4031507, (byte) 0, (short) 1), monster.getDropPeriodTime());
                    else if (monster.getId() == 9300061) 
                        monsterItemDrop(monster, new Item(4001101, (byte) 0, (short) 1), monster.getDropPeriodTime() / 6);
                }
            }
        }, null);
        
        if (monster.getId() == 9300166) {
            //Bomb
            final MapleMap map = this;
            TimerManager.getInstance().schedule(new Runnable() {
                public void run() {
                    killMonster(monster, (MapleCharacter) getCharacters().get(0), false);
                    for (MapleMapObject ob : map.getMapObjectsInRange(monster.getPosition(), 40000, Arrays.asList(MapleMapObjectType.PLAYER))) {
                        MapleCharacter chr = (MapleCharacter) ob;
                        if (chr != null) {
                            if (chr.hasShield()) {
                                chr.cancelShield();
                                continue;
                            }
                            int hasJewels = chr.countItem(4031868);
                            if (hasJewels <= 0) {
                                chr.giveDebuff(MapleDisease.STUN, MobSkillFactory.getMobSkill(123, 11));
                                continue;
                            }
                            int drop = (int) (Math.random() * hasJewels);
                            if (drop > 5) {
                                drop = (int) (Math.random() * 5);
                            }
                            if (drop < 1) {
                                drop = 1;
                            }
                            MapleInventoryManipulator.removeById(chr.getClient(), MapleInventoryType.ETC, 4031868, (short) drop, false, false);
                            for (int i = 0; i < drop; i++) {
                                Point pos = chr.getPosition();
                                int x = pos.x;
                                int y = pos.y;
                                if (Math.random() < 0.5) {
                                    x -= (int) (Math.random() * 100);
                                } else {
                                    x += (int) (Math.random() * 100);
                                }
                                map.spawnItemDrop(ob, chr, new Item(4031868, (byte) -1, (short) 1), new Point(x, y), true, true);
                            }
                            broadcastMessage(MaplePacketCreator.updateAriantPQRanking(chr.getName(), chr.getAPQScore(), false));
                        }
                    }
                }
            }, 3000 + (int) (Math.random() * 2000));
        }
        
        updateMonsterController(monster);
        spawnedMonstersOnMap.incrementAndGet();
    }

    public void spawnMonsterWithEffect(final MapleMonster monster, final int effect, Point pos) {
        monster.setMap(this);
        doRemoveAfter(monster);
        Point spos = new Point(pos.x, pos.y - 1);
        spos = calcPointBelow(spos);
        spos.y -= 1;
        monster.setPosition(spos);
        monster.disableDrops();
        spawnAndAddRangedMapObject(monster, new DelayedPacketCreation() {

            public void sendPackets(MapleClient c) {
                c.sendPacket(MaplePacketCreator.spawnMonster(monster, true, effect, 0));
            }
        }, null);
        updateMonsterController(monster);
        spawnedMonstersOnMap.incrementAndGet();
    }

    public void spawnFakeMonster(final MapleMonster monster) {
        monster.setMap(this);
        monster.setFake(true);
        spawnAndAddRangedMapObject(monster, new DelayedPacketCreation() {

            public void sendPackets(MapleClient c) {
                c.sendPacket(MaplePacketCreator.spawnMonster(monster, true, -4, 0));
            }
        }, null);
        spawnedMonstersOnMap.incrementAndGet();
    }

    public void spawnReactor(final MapleReactor reactor) {
        reactor.setMap(this);
        spawnAndAddRangedMapObject(reactor, new DelayedPacketCreation() {

            public void sendPackets(MapleClient c) {
                c.sendPacket(reactor.makeSpawnData());
            }
        }, null);
    }

    private void respawnReactor(final MapleReactor reactor) {
        reactor.setState((byte) 0);
        reactor.setAlive(true);
        spawnReactor(reactor);
    }

    public void spawnDoor(final MapleDoor door) {
        spawnAndAddRangedMapObject(door, new DelayedPacketCreation() {

                    public void sendPackets(MapleClient c) {
                        c.sendPacket(MaplePacketCreator.spawnDoor(door.getOwner().getId(), door.getTargetPosition(), false));
                        if (door.getOwner().getParty() != null && (door.getOwner() == c.getPlayer() || c.getPlayer() != null && door.getOwner().getParty().containsMember(new MaplePartyCharacter(c.getPlayer())))) {
                            c.sendPacket(MaplePacketCreator.partyPortal(door.getTown().getId(), door.getTarget().getId(), door.getTargetPosition()));
                        }
                        c.sendPacket(MaplePacketCreator.spawnPortal(door.getTown().getId(), door.getTarget().getId(), door.getTargetPosition()));
                        c.sendPacket(MaplePacketCreator.enableActions());
                    }
                }, new SpawnCondition() {

                    public boolean canSpawn(MapleCharacter chr) {
                        return chr.getMapId() == door.getTarget().getId() || chr == door.getOwner() && chr.getParty() == null;
                    }
                }
        );
    }

    public void spawnSummon(final MapleSummon summon) {
        spawnAndAddRangedMapObject(summon, new DelayedPacketCreation() {

            public void sendPackets(MapleClient c) {
            	if (summon != null)
            	{
	                int skillLevel = summon.getOwner().getSkillLevel(SkillFactory.getSkill(summon.getSkill()));
	                c.sendPacket(MaplePacketCreator.spawnSpecialMapObject(summon, skillLevel, true));
            	}
            }
        }, null);
    }

    public void spawnMist(final MapleMist mist, final int duration, boolean fake) {    	
        if (hasEvent) {
            return;
        }
        addMapObject(mist);
        if (mist.getOwner() != null)
            mist.getOwner().addOwnedObject(this, mist);
        broadcastMessage(fake ? mist.makeFakeSpawnData(30) : mist.makeSpawnData());
        TimerManager tMan = TimerManager.getInstance();
        final ScheduledFuture<?> poisonSchedule;
        if (!fake && mist.isPoisonMist()) {
            Runnable poisonTask = new Runnable() {

                @Override
                public void run() {
                    List<MapleMapObject> affectedMonsters = getMapObjectsInRect(mist.getBox(), Collections.singletonList(MapleMapObjectType.MONSTER));
                    for (MapleMapObject mo : affectedMonsters) {
                        if (mist.makeChanceResult() && mist.getOwner() != null) {
                            MonsterStatusEffect poisonEffect = new MonsterStatusEffect(Collections.singletonMap(MonsterStatus.POISON, 1), mist.getSourceSkill(), false);
                            ((MapleMonster) mo).applyStatus(mist.getOwner(), poisonEffect, true, duration);
                        }
                    }
                }
            };
            poisonSchedule = tMan.register(poisonTask, 2000, 2500);
        } else {
            poisonSchedule = null;
        }
        tMan.schedule(new Runnable() {

            @Override
            public void run() {
                removeMapObject(mist);
                if (poisonSchedule != null) {
                    poisonSchedule.cancel(false);
                }
                broadcastMessage(mist.makeDestroyData());
            }
        }, duration);
    }

    public void disappearingItemDrop(final MapleMapObject dropper, final MapleCharacter owner, final IItem item, Point pos) {
        final Point droppos = calcDropPos(pos, pos);
        final MapleMapItem drop = new MapleMapItem(item, droppos, dropper, owner, (byte) 0);
        broadcastMessage(MaplePacketCreator.dropItemFromMapObject(item.getItemId(), drop.getObjectId(), 0, 0, dropper.getPosition(), droppos, (byte) 3, item.getExpiration(), (byte) 0, true), drop.getPosition());
    }

    public void spawnItemDrop(final MapleMapObject dropper, final MapleCharacter owner, final IItem item, Point pos, final boolean ffaDrop, final boolean expire) {
        spawnItemDrop(dropper, owner, item, pos, ffaDrop, expire, false);
    }

    public void spawnItemDrop(final MapleMapObject dropper, final MapleCharacter owner, final IItem item, Point pos, final boolean ffaDrop, final boolean expire, final boolean isPlayerDrop) 
    {
        TimerManager tMan = TimerManager.getInstance();
        final Point droppos = calcDropPos(pos, pos);
        byte dropType = 0;
        
        if (ffaDrop)
            dropType = 2;
        else if (owner.getParty() != null)
            dropType = 1;
        
        final MapleMapItem drop = new MapleMapItem(item, droppos, dropper, owner, dropType);
        drop.setPlayerDrop(isPlayerDrop);
        spawnAndAddRangedMapObject(drop, new DelayedPacketCreation() 
        {
            public void sendPackets(MapleClient c) 
            {
                c.sendPacket(MaplePacketCreator.dropItemFromMapObject(item.getItemId(), drop.getObjectId(), 0, drop.getDropType() == 1 ? owner.getPartyId() : isPlayerDrop ? 0 : owner == null ? 0 : owner.getId(), dropper.getPosition(), droppos, (byte) 1, item.getExpiration(), drop.getDropType(), isPlayerDrop));
            }
        }, null);
        
        broadcastMessage(MaplePacketCreator.dropItemFromMapObject(item.getItemId(), drop.getObjectId(), 0, drop.getDropType() == 1 ? owner.getPartyId() : isPlayerDrop ? 0 : owner == null ? 0 : owner.getId(), dropper.getPosition(), droppos, (byte) 0, item.getExpiration(), dropType, isPlayerDrop), drop.getPosition(), drop);

        if (expire)
            tMan.schedule(new ExpireMapItemJob(drop), dropLife);

        activateItemReactors(drop); 
    }
    
    public void addMapTimer(int durationmin, int durationmax) {
        addMapTimer(durationmin, durationmax, new String[0], false, true, null);
    }

    public void addMapTimer(int durationmin, int durationmax, String[] commands, boolean repeat, boolean shown, MapleCharacter faek) {
        if (shown && mapTimer != null)
            return;
        if (shown) {
            mapTimer = new MapleMapTimer(durationmin, durationmax, commands, repeat, true, faek, this, channel);
        } else {
            hiddenMapTimer.add(new MapleMapTimer(durationmin, durationmax, commands, repeat, false, faek, this, channel));
        }
    }

    public void clearShownMapTimer() {
        if (mapTimer != null) {
            mapTimer.getSF0F().cancel(false);
        }
        mapTimer = null;
        broadcastMessage(MaplePacketCreator.removeMapTimer());
    }

    public void clearHiddenMapTimer(int id) {
        hiddenMapTimer.get(id).getSF0F().cancel(false);
        hiddenMapTimer.remove(id);
    }

    public void clearHiddenMapTimer(MapleMapTimer mmt) {
        mmt.getSF0F().cancel(false);
        hiddenMapTimer.remove(mmt);
    }

    public void clearHiddenMapTimers() {
        for (MapleMapTimer mmt : hiddenMapTimer) {
            mmt.getSF0F().cancel(false);
        }
        hiddenMapTimer.clear();

    }

    public MapleMapTimer getShownMapTimer() {
        return mapTimer;
    }

    public String[] mapTimerDebug() {
        List<String> ls = new ArrayList<>();
        final SimpleDateFormat f = new SimpleDateFormat("HH:mm:ss, d MMMMM yyyy");
        if (mapTimer != null) {
            ls.add("SHOWN repeat:" + mapTimer.getRepeat() + " startTime:" + mapTimer.getStartTime().toString() + " timeLeft:" + mapTimer.getTimeLeft() + " commands:" + StringUtil.joinStringFrom(mapTimer.getCommands(), 0, ";"));
        }
        int id = 0;
        for (MapleMapTimer mt : hiddenMapTimer) {
            ls.add("HIDDEN" + id + " repeat:" + mt.getRepeat() + " startTime:" + f.format(mt.getStartTime().getTime()) + " timeLeft:" + mt.getTimeLeft() + " commands:" + StringUtil.joinStringFrom(mt.getCommands(), 0, ";"));
            id++;
        }
        return ls.toArray(new String[ls.size()]);
    }

    private void activateItemReactors(MapleMapItem drop) {
        IItem item = drop.getItem();
        final TimerManager tMan = TimerManager.getInstance(); // check for reactors on map that might use this item
        for (MapleMapObject o : getAllReactors()) {
            MapleReactor reactor = (MapleReactor) o;
            if (reactor.getReactorType() == 100) {
                if (reactor.getReactItem().getLeft() == item.getItemId() && reactor.getReactItem().getRight() <= item.getQuantity()) {
                    Rectangle area = reactor.getArea();
                    if (area.contains(drop.getPosition())) {
                        MapleClient ownerClient = null;
                        if (drop.getOwner() != null) {
                            ownerClient = drop.getOwner().getClient();
                        }
                        if (!reactor.isTimerActive() || getMapObject(reactor.getReactingWith().getObjectId()) == null) {
                            tMan.schedule(new ActivateItemReactor(drop, reactor, ownerClient), 5000);
                            reactor.setTimerActive(true);
                            reactor.setReactingWith(drop);
                        }
                    }
                }
            }
        }
    }

    public void ariantPQStart() {
        int i = 1;
        for (MapleCharacter chars2 : getCharacters()) {
            broadcastMessage(MaplePacketCreator.updateAriantPQRanking(chars2.getName(), 0, false));
            broadcastMessage(MaplePacketCreator.serverNotice(0, MaplePacketCreator.updateAriantPQRanking(chars2.getName(), 0, false).toString()));
            if (getCharacters().size() > i) {
                broadcastMessage(MaplePacketCreator.updateAriantPQRanking(null, 0, true));
                broadcastMessage(MaplePacketCreator.serverNotice(0, MaplePacketCreator.updateAriantPQRanking(chars2.getName(), 0, true).toString()));
            }
            i++;
        }
    }

    public void startMapEffect(String msg, int itemId) {
        if (mapEffect != null) {
            return;
        }
        mapEffect = new MapleMapEffect(msg, itemId);
        broadcastMessage(mapEffect.makeStartData());
        mapEffectSch = TimerManager.getInstance().schedule(new Runnable() {

            @Override
            public void run() {
                broadcastMessage(mapEffect.makeDestroyData());
                mapEffect = null;
                mapEffectSch = null;
            }
        }, 30000);
    }

    public void stopMapEffect() {
        if (mapEffect == null) {
            return;
        }
        if (mapEffectSch != null) {
            mapEffectSch.cancel(false);
            mapEffectSch = null;
        }
        broadcastMessage(mapEffect.makeDestroyData());

        mapEffect = null;
    }

    /**
     * Adds a player to this map and sends necessary data
     *
     * @param chr
     */
    public void addPlayer(final MapleCharacter chr) 
    {
        objectLock.lock();
        
        try 
        {
            characters.add(chr);
            mapobjects.put(chr.getObjectId(), chr);
        }
        finally 
        {
            objectLock.unlock();
        }
        
        for (final MaplePlayerNPC pnpc : playerNPCs) 
            chr.getClient().sendPacket(MaplePacketCreator.getPlayerNPC(pnpc));

        if (!chr.isHidden()) 
        {
            broadcastMessage(chr, MaplePacketCreator.spawnPlayerMapobject(chr, false), true);
            broadcastMessage(chr, MaplePacketCreator.playerGuildName(chr), false);
            broadcastMessage(chr, MaplePacketCreator.playerGuildInfo(chr), false);
            
            for (final MapleCharacter c : getCharacters()) 
            {
                if (c.hasGMLevel(0x00000005) && !c.isHidden()) 
                { 
                    chr.finishAchievement(0x0000000B);
                    break;
                }
            }
        } 
        else 
        {
        	chr.getClient().sendPacket(MaplePacketCreator.giveGMHide(true));
        	final List<Pair<MapleBuffStat, Integer>> stat = Collections.singletonList(new Pair<>(MapleBuffStat.DARKSIGHT, 1));
        	chr.getClient().sendPacket(MaplePacketCreator.giveBuff(chr, 9101004, 99999, stat)); // rever buff HIDE GM
        }
        
        sendObjectPlacement(chr);
        
        if (mapId >= 914000200 && mapId <= 914000220) 
            chr.getClient().sendPacket(MaplePacketCreator.tempStatsUpdate());
        else 
            chr.getClient().sendPacket(MaplePacketCreator.resetStats());
        
        if (chr.isUILocked() && onUserEnter.length() == 0x00000000) 
        {
            chr.getClient().sendPacket(MaplePacketCreator.hideUI(false));
            chr.getClient().sendPacket(MaplePacketCreator.lockWindows(false));
        }
        
        if (mapId == 0x00000001 || mapId == 0x00000002 || mapId == 809000101 || mapId == 809000201)
            chr.getClient().sendPacket(MaplePacketCreator.showEquipEffect());

        if (decHP > 0x00000000)
            chr.startDecHPSchedule();

        if (onUserEnter.length() != 0x00000000) 
            MapScriptManager.getInstance().getMapScript(chr.getClient(), onUserEnter, false);

        if (onFirstUserEnter.length() != 0x00000000)
            if (getCharacters().size() == 0x00000001)
                MapScriptManager.getInstance().getMapScript(chr.getClient(), onFirstUserEnter, true);
        //begin HenesysPQ and Kenta checks
        if (mapId == 923010000 && getMonsterByOid(9300102) == null) { // Kenta's Mount Quest
            spawnMonsterOnGroundBelow(MapleLifeFactory.getMonster(9300102), new Point(77, 426));
        } else if (mapId == 910010000) { // Henesys Party Quest
            List<MapleMapObject> monsters = chr.getClient().getPlayer().getMap().getMapObjectsInRange(chr.getClient().getPlayer().getPosition(), Double.POSITIVE_INFINITY, Arrays.asList(MapleMapObjectType.MONSTER));
            for (MapleMapObject monstermo : monsters) {
                MapleMonster monster = (MapleMonster) monstermo;
                chr.getClient().getPlayer().getMap().killMonster(monster, chr.getClient().getPlayer(), true);
            }           
            //setSpawns(false);        
            //end HenesysPQ and Kenta checks
        }
        
        final List<MaplePet> pets = chr.getPets();
        
        for (final MaplePet pet : pets) 
        {
        	pet.setPos(getGroundBelow(chr.getPosition()));
            chr.getClient().sendPacket(MaplePacketCreator.showPet(chr, pet, false, false, true));
        } 
        
        chr.updatePetPositions(null);

        final MapleStatEffect summonStat = chr.getStatForBuff(MapleBuffStat.SUMMON);
        
        if (summonStat != null) 
        {
            final MapleSummon summon = chr.getSummons().get(summonStat.getSourceId());
            summon.setPosition(chr.getPosition());
            chr.getMap().spawnSummon(summon);
            updateMapObjectVisibility(chr, summon);
        }
        
        if (mapEffect != null)
            mapEffect.sendStartData(chr.getClient());

        if (timeLimit > 0x00000000 && getForcedReturnMap() != null) 
        {
            chr.getClient().sendPacket(MaplePacketCreator.getClock(timeLimit));
            chr.startMapTimeLimitTask(this, getForcedReturnMap());
        }
        
        if (chr.getBuffedValue(MapleBuffStat.MONSTER_RIDING) != null)
            if (FieldLimit.CANNOTUSEMOUNTS.check(fieldLimit))
                chr.cancelBuffStats(MapleBuffStat.MONSTER_RIDING);
      
        if (mapTimer != null) 
            mapTimer.sendSpawnData(chr.getClient());
        
        if (chr.getEventInstance() != null && chr.getEventInstance().isTimerStarted()) 
            chr.getClient().sendPacket(MaplePacketCreator.getClock((int) (chr.getEventInstance().getTimeLeft() / 0x000003E8)));
        
        if (hasClock()) 
        {
            final Calendar cal = Calendar.getInstance();
            chr.getClient().sendPacket(MaplePacketCreator.getClockTime(cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), cal.get(Calendar.SECOND)));
        }

        if (hasBoat() == 0x00000002) 
            chr.getClient().sendPacket(MaplePacketCreator.onContiState(true));
        else if (hasBoat() == 0x00000001 && (chr.getMapId() != 200090000 || chr.getMapId() != 200090010)) 
            chr.getClient().sendPacket(MaplePacketCreator.onContiState(false));

        chr.receivePartyMemberHP();
        
        if (!canMovement && !chr.isGM()) 
            chr.giveDebuff(MapleDisease.GM_DISABLE_MOVEMENT, MobSkillFactory.getMobSkill(123, 1), true);
        if (!allowSkills && !chr.isGM()) 
            chr.giveDebuff(MapleDisease.GM_DISABLE_SKILL, MobSkillFactory.getMobSkill(120, 1), true);
        
        if (mapId == 677000005 && countMobOnMap(9400609) == 0) 
        {
            broadcastMessage(MaplePacketCreator.serverNotice(6, "Andras has appeared."));
            spawnMonsterOnGroundBelow(MapleLifeFactory.getMonster(9400609), new Point(389, 96));
        }
    }

    public void removeNPC(int npcid) {
        MapleNPC npc = getNPC(npcid);
        broadcastMessage(MaplePacketCreator.removeNPC(npc.getObjectId()));
        removeMapObject(npc);
    }

    public MapleNPC getNPC(int npcid) {
        for (MapleMapObject obj : getAllNPCs()) {
            MapleNPC npc = (MapleNPC) obj;
            if (npc.getId() == npcid)
                return npc;
        }
        return null;
    }

    public void addNPC(int npcid, int x, int y) {
        addNPC(npcid, new Point(x, y));
    }

    public void addNPC(int npcid, Point pos) {
        MapleNPC npc = MapleLifeFactory.getNPC(npcid);
        if (npc == null) {
            log.error("Trying to spawn an NPC that doesn't exist on map " + mapId);
            return;
        }
        npc.setPosition(pos);
        npc.setCy(pos.y);
        npc.setRx0(pos.x - 50);
        npc.setRx1(pos.x + 50);
        npc.setFh(footholds.findBelow(pos).getId());
        npc.setCustom(true);
        addMapObject(npc);
        broadcastMessage(MaplePacketCreator.spawnNPC(npc));
    }

    public void removePlayer(MapleCharacter chr) {
        objectLock.lock();
        try {
            characters.remove(chr);
        } finally {
            objectLock.unlock();
        }
        removeMapObject(chr.getObjectId());
        broadcastMessage(MaplePacketCreator.removePlayerFromMap(chr.getId()));
        if (chr.getNumControlledMonsters() > 0) {
            final List<MapleMonster> monsters = new ArrayList<>(chr.getControlledMonsters());
            for (MapleMonster monster : monsters) {
                monster.setController(null);
                monster.setControllerHasAggro(false);
                monster.setControllerKnowsAboutAggro(false);
                updateMonsterController(monster);
            }
        }
        chr.leaveMap();
        chr.cancelMapTimeLimitTask();

        for (MapleSummon summon : chr.getSummons().values()) {
            if (summon.isPuppet()) {
                chr.cancelBuffStats(MapleBuffStat.PUPPET);
            } else {
                removeMapObject(summon);
            }
        }
        chr.dispelDebuff(MapleDisease.GM_DISABLE_SKILL);
        chr.dispelDebuff(MapleDisease.GM_DISABLE_MOVEMENT);
        chr.offBeacon(true);
    }

    /**
     * Broadcast a message to everyone in the map
     *
     * @param packet
     */
    public void broadcastMessage(MaplePacket packet) 
    {
        broadcastMessage(null, packet, Double.POSITIVE_INFINITY, null, null);
    }

    /**
     * Nonranged. Repeat to source according to parameter.
     *
     * @param source
     * @param packet
     * @param repeatToSource
     */
    public void broadcastMessage(MapleCharacter source, MaplePacket packet, boolean repeatToSource) 
    {
        broadcastMessage(repeatToSource ? null : source, packet, Double.POSITIVE_INFINITY, source.getPosition(), null);
    }
    
    public void broadcastGMMessage(MapleCharacter source, MaplePacket packet, boolean repeatToSource) 
    {
        broadcastGMMessage(repeatToSource ? null : source, packet, Double.POSITIVE_INFINITY, source.getPosition(), null);
    }

    /**
     * Ranged and repeat according to parameters.
     *
     * @param source
     * @param packet
     * @param repeatToSource
     * @param ranged
     */
    public void broadcastMessage(MapleCharacter source, MaplePacket packet, boolean repeatToSource, boolean ranged) {
        broadcastMessage(repeatToSource ? null : source, packet, ranged ? MapleCharacter.MAX_VIEW_RANGE_SQ : Double.POSITIVE_INFINITY, source.getPosition(), null);
    }

    /**
     * Always ranged from Point.
     *
     * @param packet
     * @param rangedFrom
     */
    public void broadcastMessage(MaplePacket packet, Point rangedFrom) {
        broadcastMessage(null, packet, MapleCharacter.MAX_VIEW_RANGE_SQ, rangedFrom, null);
    }

    public void broadcastMessage(MaplePacket packet, Point rangedFrom, MapleMapObject mo) {
        broadcastMessage(null, packet, MapleCharacter.MAX_VIEW_RANGE_SQ, rangedFrom, mo);
    }

    /**
     * Always ranged from point. Does not repeat to source.
     *
     * @param source
     * @param packet
     * @param rangedFrom
     */
    public void broadcastMessage(MapleCharacter source, MaplePacket packet, Point rangedFrom) {
        broadcastMessage(source, packet, MapleCharacter.MAX_VIEW_RANGE_SQ, rangedFrom, null);
    }

    private void broadcastMessage(MapleCharacter source, MaplePacket packet, double rangeSq, Point rangedFrom, MapleMapObject mo) 
    {
        objectLock.lock();
        try 
        {
            for (MapleCharacter chr : characters) 
            {
                if (chr != source) 
                {
                    if (rangeSq < Double.POSITIVE_INFINITY) 
                    {
                        if (rangedFrom.distanceSq(chr.getPosition()) <= rangeSq && chr.canSeeItem(mo)) 
                            chr.getClient().sendPacket(packet);
                    }
                    else 
                        chr.getClient().sendPacket(packet);
                }
            }
        } 
        finally 
        {
            objectLock.unlock();
        }
    }
    
    private void broadcastGMMessage(MapleCharacter source, MaplePacket packet, double rangeSq, Point rangedFrom, MapleMapObject mo) 
    {
        objectLock.lock();
        try  
        {
            for (MapleCharacter chr : characters) 
            {
                if (chr != source && (chr.getGMLevel() > source.getGMLevel())) 
                {
                    if (rangeSq < Double.POSITIVE_INFINITY) 
                    {
                        if (rangedFrom.distanceSq(chr.getPosition()) <= rangeSq && chr.canSeeItem(mo)) 
                            chr.getClient().sendPacket(packet);
                    }
                    else 
                        chr.getClient().sendPacket(packet);
                }
            }
        } 
        finally 
        {
            objectLock.unlock();
        }
    }

    private boolean isNonRangedType(MapleMapObjectType type) 
    {
        switch (type) 
        {
            case PLAYER:
            case MIST:
            case HIRED_MERCHANT:
                return true;
			default:
				break;
        }
        return false;
    }

    private void sendObjectPlacement(MapleCharacter chr) {
        final List<MapleMapObject> objects = getMapObjectsInRange(new Point(0, 0), Double.POSITIVE_INFINITY, Arrays.asList(MapleMapObjectType.PLAYER, MapleMapObjectType.MIST, MapleMapObjectType.HIRED_MERCHANT, MapleMapObjectType.MONSTER));
        for (MapleMapObject o : objects) {
            if (isNonRangedType(o.getType())) {
                o.sendSpawnData(chr.getClient());
            } else if (o instanceof MapleMonster) {
                updateMonsterController((MapleMonster) o);
            }
        }

        if (chr != null) {
            for (MapleMapObject o : getMapObjectsInRange(chr.getPosition(), MapleCharacter.MAX_VIEW_RANGE_SQ, rangedMapobjectTypes)) {
                if (o instanceof MapleReactor) {
                    if (((MapleReactor) o).isAlive()) {
                        o.sendSpawnData(chr.getClient());
                        chr.addVisibleMapObject(o);
                    }
                } else {
                    if (chr.canSeeItem(o)) {
                        o.sendSpawnData(chr.getClient());
                        chr.addVisibleMapObject(o);
                    }
                }
            }
        } else {
            log.info("sendObjectPlacement invoked with null char");
        }
    }

    public final List<MapleMapObject> getAllItems() {
        return getMapObjectsInRange(new Point(0, 0), Double.POSITIVE_INFINITY, Collections.singletonList(MapleMapObjectType.ITEM));
    }

    public final List<MapleMapObject> getAllNPCs() {
        return getMapObjectsInRange(new Point(0, 0), Double.POSITIVE_INFINITY, Collections.singletonList(MapleMapObjectType.NPC));
    }

    public final List<MapleMapObject> getAllReactors() {
        return getMapObjectsInRange(new Point(0, 0), Double.POSITIVE_INFINITY, Collections.singletonList(MapleMapObjectType.REACTOR));
    }

    public final List<MapleMapObject> getAllMonsters() {
        return getMapObjectsInRange(new Point(0, 0), Double.POSITIVE_INFINITY, Collections.singletonList(MapleMapObjectType.MONSTER));
    }

    public final List<MapleMapObject> getAllDoors() {
        return getMapObjectsInRange(new Point(0, 0), Double.POSITIVE_INFINITY, Collections.singletonList(MapleMapObjectType.DOOR));
    }

    public List<MapleMapObject> getMapObjectsInRange(Point from, double rangeSq, List<MapleMapObjectType> types) {
        List<MapleMapObject> ret = new LinkedList<>();
        objectLock.lock();
        try {
            for (MapleMapObject l : mapobjects.values()) {
                if (types.contains(l.getType()) && from.distanceSq(l.getPosition()) <= rangeSq) {
                    ret.add(l);
                }
            }
        } finally {
            objectLock.unlock();
        }
        return ret;
    }

    public List<IMaplePlayerShop> getPlayerShops() {
        List<IMaplePlayerShop> ret = new LinkedList<>();
        for (MapleMapObject l : getMapObjectsInRange(new Point(0, 0), Double.POSITIVE_INFINITY, Arrays.asList(MapleMapObjectType.HIRED_MERCHANT, MapleMapObjectType.SHOP))) {
            ret.add((IMaplePlayerShop) l);
        }
        return ret;
    }

    public List<MapleMapObject> getMapObjectsInRect(Rectangle box, List<MapleMapObjectType> types) {
        List<MapleMapObject> ret = new LinkedList<>();
        objectLock.lock();
        try {
            for (MapleMapObject l : mapobjects.values()) {
                if (types.contains(l.getType())) {
                    if (box.contains(l.getPosition())) {
                        ret.add(l);
                    }
                }
            }
        } finally {
            objectLock.unlock();
        }
        return ret;
    }

    public List<MapleCharacter> getPlayersInRect(Rectangle box, List<MapleCharacter> chr) {
        List<MapleCharacter> character = new LinkedList<>();
        objectLock.lock();
        try {
            for (MapleCharacter a : characters) {
                if (chr.contains(a.getClient().getPlayer())) {
                    if (box.contains(a.getPosition())) {
                        character.add(a);
                    }
                }
            }
        } finally {
            objectLock.unlock();
        }
        return character;
    }

    public List<MapleCharacter> getPlayersInRect(Rectangle box) {
        List<MapleCharacter> character = new LinkedList<>();
        objectLock.lock();
        try {
            for (MapleCharacter a : characters) {
                if (box.contains(a.getPosition())) {
                    character.add(a);
                }
            }
        } finally {
            objectLock.unlock();
        }
        return character;
    }

    public void addSeat(int seat, Point p) {
        seats.put(seat, p);
    }

    public Point getSeat(int seat) {
        return seats.get(seat);
    }

    public void addPortal(MaplePortal myPortal) {
        portals.put(myPortal.getId(), myPortal);
        if (myPortal.getType() == MaplePortal.SPAWN_POINT)
            spawnPoints.add(myPortal);
    }

    public MaplePortal getPortal(String portalname) 
    {
        for (MaplePortal port : portals.values()) 
        {
            if (port.getName().equals(portalname)) 
                return port;
        }
        return null;
    }

    public MaplePortal getPortal(int portalid) {
        return portals.get(portalid);
    }

    public List<MaplePortal> getSpawnPoints() {
        return spawnPoints;
    }

    public MaplePortal getRandomSpawnPoint() {
        return spawnPoints.get(Randomizer.nextInt(spawnPoints.size()));
    }

    public void addMapleArea(Rectangle rec) {
        areas.add(rec);
    }

    public List<Rectangle> getAreas() {
        return new ArrayList<>(areas);
    }

    public Rectangle getArea(int index) {
        return areas.get(index);
    }

    public void setFootholds(MapleFootholdTree footholds) {
        this.footholds = footholds;
    }

    public MapleFootholdTree getFootholds() {
        return footholds;
    }

    public void addMonsterSpawn(MapleMonster monster, final byte carnivalTeam, int mobTime) {
        Point newpos = calcPointBelow(monster.getPosition());
        newpos.y -= 1;
        SpawnPoint sp = new SpawnPoint(monster, newpos, carnivalTeam, mobTime);

        monsterSpawn.add(sp);
    }

    public void addRawMonsterSpawn(SpawnPoint sp) {
        monsterSpawn.add(sp);
    }

    public float getMonsterRate() {
        return monsterRate;
    }

    public List<MapleCharacter> getCharacters() {
        final List<MapleCharacter> chars = new LinkedList<>();
        objectLock.lock();
        try {
            for (MapleCharacter chr : characters) {
                chars.add(chr);
            }
        } finally {
            objectLock.unlock();
        }
        return chars;
    }

    public MapleCharacter getCharacterById(int id) {
        objectLock.lock();
        try {
            for (MapleCharacter c : characters) {
                if (c.getId() == id) {
                    return c;
                }
            }
        } finally {
            objectLock.unlock();
        }
        return null;
    }

    private void updateMapObjectVisibility(MapleCharacter chr, MapleMapObject mo) {
        if (!chr.isMapObjectVisible(mo)) { // monster entered view range
            if (mo instanceof MapleSummon || mo.getPosition().distanceSq(chr.getPosition()) <= MapleCharacter.MAX_VIEW_RANGE_SQ) {
                if (chr.canSeeItem(mo)) {
                    chr.addVisibleMapObject(mo);
                    mo.sendSpawnData(chr.getClient());
                }
            }
        } else { // monster left view range
            if (!(mo instanceof MapleSummon) && mo.getPosition().distanceSq(chr.getPosition()) > MapleCharacter.MAX_VIEW_RANGE_SQ) {
                chr.removeVisibleMapObject(mo);
                mo.sendDestroyData(chr.getClient());
            }
        }
    }

    public void moveMonster(MapleMonster monster, Point reportedPos) {
        monster.setPosition(reportedPos);
        MapleFoothold fh = footholds.findBelow(reportedPos);
        if (fh != null)
            monster.setFh(fh.getId());
        objectLock.lock();
        try {
            for (MapleCharacter chr : characters) {
                updateMapObjectVisibility(chr, monster);
            }
        } finally {
            objectLock.unlock();
        }
    }

    public void movePlayer(MapleCharacter player, Point newPosition) {
        player.setPosition(newPosition);
        final MapleFoothold fh = footholds.findBelow(newPosition);
        player.setFoothold(fh != null ? fh.getId() : 0);
        Collection<MapleMapObject> visibleObjects = player.getVisibleMapObjects();
        MapleMapObject[] visibleObjectsNow = visibleObjects.toArray(new MapleMapObject[visibleObjects.size()]);
        for (MapleMapObject mo : visibleObjectsNow) {
            if (mo != null) {
                if (mapobjects.get(mo.getObjectId()) == mo) {
                    updateMapObjectVisibility(player, mo);
                } else {
                    player.removeVisibleMapObject(mo);
                }
            }
        }
        for (MapleMapObject mo : getMapObjectsInRange(player.getPosition(), MapleCharacter.MAX_VIEW_RANGE_SQ, rangedMapobjectTypes)) {
            if (mo != null) {
                if (!player.isMapObjectVisible(mo) && player.canSeeItem(mo)) {
                    mo.sendSpawnData(player.getClient());
                    player.addVisibleMapObject(mo);
                }
            }
        }
        if (mapId == 240040611) {
            if (!getMapObjectsInRange(player.getPosition(), 25000, Collections.singletonList(MapleMapObjectType.REACTOR)).isEmpty()) {
                MapleReactor reactor = getReactorById(2408004);
                if (reactor.getState() == 0) {
                    reactor.hitReactor(player.getClient());
                }
            }
        }
        if (!canMovement && !player.isGM()) {
            // No more movement!
            if (!player.getDiseases().contains(MapleDisease.GM_DISABLE_MOVEMENT)) {
                player.giveDebuff(MapleDisease.GM_DISABLE_MOVEMENT, MobSkillFactory.getMobSkill(123, 1), true);
            }
        }
    }

    public MaplePortal findClosestSpawnPoint(Point from) {
        MaplePortal closest = null;
        double shortestDistance = Double.POSITIVE_INFINITY;
        for (MaplePortal portal : spawnPoints) {
            double distance = portal.getPosition().distanceSq(from);
            if (distance < shortestDistance) {
                closest = portal;
                shortestDistance = distance;
            }
        }
        return closest;
    }
    
    public MaplePortal findClosestPortal(Point from) 
    {
        MaplePortal closest = null;
        double shortestDistance = Double.POSITIVE_INFINITY;
        for (MaplePortal portal : portals.values()) 
        {
            double distance = portal.getPosition().distanceSq(from);
            if (distance < shortestDistance) 
            {
                closest = portal;
                shortestDistance = distance;
            }
        }
        return closest;
    }

    public void setSpawnRateMulti(int sr) {
        if (sr == 0) {
            return;
        }
        boolean decSpawn = sr < 0;
        if (decSpawn) {
            if (origMobRate < 1)
                monsterRate *= -sr;
            else if (origMobRate >= 1)
                monsterRate /= -sr;
        } else {
            if (origMobRate < 1)
                monsterRate /= sr;
            else if (origMobRate >= 1)
                monsterRate *= sr;
        }

    }

    public float getSpawnRate() {
        return monsterRate;
    }

    public float getOrigSpawnRate() {
        return origMobRate;
    }

    public void setSpawnRate(float sr) {
        monsterRate = sr;
    }

    public void resetSpawnRate() {
        monsterRate = origMobRate;
    }

    public boolean isSpawnRateModified() {
        return monsterRate != origMobRate;
    }

    public void spawnDebug(MessageCallback mc) {
        mc.dropMessage("Spawndebug...");
        mc.dropMessage("Mapobjects in map: " + mapobjects.size() + " \"spawnedMonstersOnMap\": " + spawnedMonstersOnMap + " spawnpoints: " + monsterSpawn.size() + " maxRegularSpawn: " + getMaxCurrentSpawn() + " spawnRate: " + monsterRate + " original spawnRate: " + origMobRate);
        mc.dropMessage("actual monsters: " + getAllMonsters().size());
    }

    private int getMaxCurrentSpawn() {
        if (origMobRate < 1) {
            return (int) (monsterSpawn.size() / monsterRate) - spawnedMonstersOnMap.get();
        } else {
            return (int) (monsterSpawn.size() * monsterRate) - spawnedMonstersOnMap.get();
        }
    }

    public Collection<MaplePortal> getPortals() {
        return portals.values();
    }

    public String getMapName() {
        return mapName;
    }

    public void setMapName(String mapName) {
        this.mapName = mapName;
    }

    public String getStreetName() {
        return streetName;
    }

    public void setClock(boolean hasClock) {
        clock = hasClock;
    }

    public boolean hasClock() {
        return clock;
    }

    public void setTown(boolean isTown) {
        town = isTown;
    }

    public boolean isTown() {
        return town;
    }

    public void setAllowShops(boolean allowShops) {
        this.allowShops = allowShops;
    }

    public boolean allowShops() {
        return allowShops;
    }

    public void setStreetName(String streetName) {
        this.streetName = streetName;
    }

    public void setEverlast(boolean everlast) {
        this.everlast = everlast;
    }

    public boolean getEverlast() {
        return everlast;
    }

    public int getSpawnedMonstersOnMap() {
        return spawnedMonstersOnMap.get();
    }

    public void resetSpawn() {
        if (spawnWorker != null) {
            spawnWorker.cancel(true);
        }
        if (monsterRate > 0) {
            spawnWorker = TimerManager.getInstance().register(new RespawnWorker(), createMobInterval);
        }
    }

    private class ExpireMapItemJob implements Runnable {

        private final MapleMapItem mapitem;

        public ExpireMapItemJob(MapleMapItem mapitem) {
            this.mapitem = mapitem;
        }

        @Override
        public void run() {
            if (mapitem != null && mapitem == getMapObject(mapitem.getObjectId())) {
                synchronized (mapitem) {
                    if (mapitem.isPickedUp()) {
                        return;
                    }
                    broadcastMessage(MaplePacketCreator.removeItemFromMap(mapitem.getObjectId(), 0, 0), mapitem.getPosition());
                    removeMapObject(mapitem);
                    mapitem.setPickedUp(true);
                }
            }
        }
    }

    private class ActivateItemReactor implements Runnable {

        private final MapleMapItem mapitem;
        private final MapleReactor reactor;
        private final MapleClient c;

        public ActivateItemReactor(MapleMapItem mapitem, MapleReactor reactor, MapleClient c) {
            this.mapitem = mapitem;
            this.reactor = reactor;
            this.c = c;
        }

        @Override
        public void run() {
            reactor.setTimerActive(false);
            if (mapitem != null && mapitem == getMapObject(mapitem.getObjectId())) {
                synchronized (mapitem) {
                    TimerManager tMan = TimerManager.getInstance();
                    if (mapitem.isPickedUp()) {
                        return;
                    }
                    broadcastMessage(MaplePacketCreator.removeItemFromMap(mapitem.getObjectId(), 0, 0), mapitem.getPosition());
                    removeMapObject(mapitem);
                    reactor.hitReactor(c);
                    if (reactor.getDelay() > 0) { //This shit is negative.. Fix?
                        tMan.schedule(new Runnable() {

                            @Override
                            public void run() {
                                reactor.setState((byte) 0);
                                broadcastMessage(MaplePacketCreator.triggerReactor(reactor, 0));
                            }
                        }, reactor.getDelay());
                    }
                }
            }
        }
    }

    private class RespawnWorker implements Runnable {

        @Override
        public void run() {
            respawn();
        }
    }

    private class DojoSpawn implements Runnable {

        @Override
        public void run() {
            if (countMobOnMap(getDojoBoss()) == 1) {
                for (MapleMapObject mmo : getAllMonsters()) {
                    MapleMonster mob = (MapleMonster) mmo;
                    if (mob.getId() == getDojoBoss()) {
                        for (int mid : mob.getDojoBossSpawns()) {
                            if (mid != 0) {
                                spawnMonsterOnGroundBelow(MapleLifeFactory.getMonster(mid), mob.getPosition());
                            }
                        }
                    }
                }
            }
        }
    }

    public final void setCreateMobInterval(final short createMobInterval) {
        this.createMobInterval = createMobInterval;
    }

    public final void loadMonsterRate() {
		/*final int spawnSize = monsterSpawn.size();
		maxRegularSpawn = Math.round(spawnSize * monsterRate);
		if (maxRegularSpawn < 2) {
			maxRegularSpawn = 2;
		} else if (maxRegularSpawn > spawnSize) {
			maxRegularSpawn = spawnSize - (spawnSize / 15);
		}
		Collection<SpawnPoint> newSpawn = new LinkedList<SpawnPoint>();
		Collection<SpawnPoint> newBossSpawn = new LinkedList<SpawnPoint>();
		for (final SpawnPoint s : monsterSpawn) {
			if (s.isBoss()) {
				newBossSpawn.add(s);
			} else {
				newSpawn.add(s);
			}
		}
		monsterSpawn.clear();
		monsterSpawn.addAll(newBossSpawn);
		monsterSpawn.addAll(newSpawn);*/
        if (!monsterSpawn.isEmpty()) {
            if (spawnWorker != null)
                spawnWorker.cancel(true);
            spawnWorker = TimerManager.getInstance().register(new Runnable() {

                @Override
                public void run() {
                    respawn();
                }
            }, createMobInterval);
        }
    }

    public void respawn() 
    {
        if (characters.isEmpty())
            return;
	    if (!spawnsEnabled)
	    	return;
        
		/*final int numShouldSpawn = maxRegularSpawn - spawnedMonstersOnMap.get();
		if (numShouldSpawn > 0) {
			int spawned = 0;

			final List<SpawnPoint> randomSpawn = new ArrayList<SpawnPoint>(monsterSpawn);
			Collections.shuffle(randomSpawn);

			for (SpawnPoint spawnPoint : randomSpawn) {
				if (spawnPoint.shouldSpawn()) {
					spawnPoint.spawnMonster(this);
					spawned++;
				}
				if (spawned >= numShouldSpawn) {
					break;
				} 
			}
		}*/

        if (mapId == 230040400 || mapId == 240010500) {
            int ispawnedMonstersOnMap = spawnedMonstersOnMap.get();
            int numShouldSpawn = (int) Math.round(Math.random() * (2 + characters.size() / 1.5 + ((int) (monsterSpawn.size() / monsterRate) - ispawnedMonstersOnMap) / 4.0));
            if (numShouldSpawn + ispawnedMonstersOnMap > (int) (monsterSpawn.size() / monsterRate)) {
                numShouldSpawn = (int) (monsterSpawn.size() / monsterRate) - ispawnedMonstersOnMap;
            }
            if (numShouldSpawn > 0) {
                List<SpawnPoint> randomSpawn = new ArrayList<>(monsterSpawn);
                Collections.shuffle(randomSpawn);
                int spawned = 0;
                for (SpawnPoint spawnPoint : randomSpawn) {
                    if (spawnPoint.shouldSpawn()) {
                        spawnPoint.spawnMonster(this);
                        spawned++;
                    }
                    if (spawned >= numShouldSpawn) {
                        break;
                    }
                }
            }
        } else {
            int numShouldSpawn = getMaxCurrentSpawn();
            if (numShouldSpawn > 0) {
                int spawned = 0;
                for (SpawnPoint spawnPoint : monsterSpawn) {
                    if (!spawnPoint.isBoss())
                        continue;
                    if (spawnPoint.shouldSpawn()) {
                        spawnPoint.spawnMonster(this);
                        spawned++;
                    }
                    if (spawned >= numShouldSpawn) {
                        break;
                    }
                }
                List<SpawnPoint> randomSpawn = new ArrayList<>(monsterSpawn);
                Collections.shuffle(randomSpawn);
                if (spawned < numShouldSpawn) {
                    for (SpawnPoint spawnPoint : randomSpawn) {
                        if (spawnPoint.isBoss())
                            continue;
                        if (spawnPoint.shouldSpawn()) {
                            spawnPoint.spawnMonster(this);
                            spawned++;
                        }
                        if (spawned >= numShouldSpawn) {
                            break;
                        }
                    }
                }
            }
        }
    }

    private static interface DelayedPacketCreation {

        void sendPackets(MapleClient c);
    }

    private static interface SpawnCondition {

        boolean canSpawn(MapleCharacter chr);
    }

    public short getHPDec() {
        return decHP;
    }

    public void setHPDec(short delta) {
        decHP = delta;
    }

    public int getHPDecProtect() {
        return protectItem;
    }

    public void setHPDecProtect(int delta) {
        protectItem = delta;
    }

    public int hasBoat() {
        if (boat && docked) {
            return 2;
        } else if (boat) {
            return 1;
        } else {
            return 0;
        }
    }

    public void setBoat(boolean hasBoat) {
        boat = hasBoat;
    }

    public void setDocked(boolean isDocked) {
        docked = isDocked;
    }

    public void setEvent(boolean hasEvent) {
        this.hasEvent = hasEvent;
    }

    public boolean hasEvent() {
        return hasEvent;
    }

    public int countCharsOnMap() {
        return characters.size();
    }

    public int countMobOnMap(int monsterid) {
        int count = 0;
        for (MapleMapObject mmo : getAllMonsters()) {
            if (((MapleMonster) mmo).getId() == monsterid) {
                count++;
            }
        }
        return count;
    }

    public void setOx(MapleOxQuiz set) {
        ox = set;
    }

    public MapleOxQuiz getOx() {
        return ox;
    }

    public MapleReactor getReactorById(int id) {
        for (MapleMapObject obj : getAllReactors()) {
            MapleReactor reactor = (MapleReactor) obj;
            if (reactor.getId() == id) {
                return reactor;
            }
        }
        return null;
    }

    public boolean isPQMap() { //Does NOT include CPQ maps
        return mapId > 922010000 && mapId < 922011100 || mapId >= 103000800 && mapId < 103000890;
    }

    public boolean isCPQMap() {
        switch (mapId) {
            case 980000101:
            case 980000201:
            case 980000301:
            case 980000401:
            case 980000501:
            case 980000601:
                return true;
            default:
                return false;
        }
    }

    public boolean isBlueCPQMap() {
        switch (mapId) {
            case 980000501:
            case 980000601:
                return true;
            default:
                return false;
        }
    }

    public boolean isPurpleCPQMap() {
        switch (mapId) {
            case 980000301:
            case 980000401:
                return true;
            default:
                return false;
        }
    }

    public void addClock(int seconds) {
        broadcastMessage(MaplePacketCreator.getClock(seconds));
    }

    public boolean cannotInvincible() {
        return cannotInvincible;
    }

    public void setCannotInvincible(boolean b) {
        cannotInvincible = b;
    }

    public void setFieldLimit(int fl) {
        fieldLimit = fl;
        canVipRock = !FieldLimit.CANNOTVIPROCK.check(fl);
    }

    public int getFieldLimit() {
        return fieldLimit;
    }

    public boolean canVipRock() {
        return canVipRock;
    }

    public IMaplePlayerShop getMaplePlayerShopByOwnerName(String name) {
        for (MapleMapObject l : getMapObjectsInRange(new Point(0, 0), Double.POSITIVE_INFINITY, Arrays.asList(MapleMapObjectType.HIRED_MERCHANT, MapleMapObjectType.SHOP))) {
            IMaplePlayerShop aps = (IMaplePlayerShop) l;
            if (aps.getOwnerName().equalsIgnoreCase(name)) {
                return aps;
            }
        }
        return null;
    }

    public void setFirstUserEnter(String onFirstUserEnter) {
        this.onFirstUserEnter = onFirstUserEnter;
    }

    public void setUserEnter(String onUserEnter) {
        this.onUserEnter = onUserEnter;
    }

    public void setPartyOnly(boolean party) {
        partyOnly = party;
    }

    public boolean isPartyOnly() {
        return partyOnly;
    }

    public void setLevelLimit(int limit) {
        levelLimit = limit;
    }

    public int getLevelLimit() {
        return levelLimit;
    }

    public void setLevelForceMove(int limit) {
        lvForceMove = limit;
    }

    public int getLevelForceMove() {
        return lvForceMove;
    }

    public boolean isDojoMap() {
        return mapId / 1000000 == 925 && getDojoStage() != 0 && getDojoStage() % 6 != 0;
    }

    public int getDojoStage() {
        return Integer.parseInt(String.valueOf(mapId).substring(5, 7));
    }

    public int getDojoBoss() {
        return 9300183 + getDojoStage() - getDojoStage() / 6;
    }

    public boolean isDojoRestMap() {
        return mapId / 1000000 == 925 && getDojoStage() != 0 && getDojoStage() % 6 == 0;
    }

    public int getNextDojoMap() {
        String f = "%02d";
        String z = String.format(f, getDojoStage() + 1);
        return Integer.parseInt("92502" + z + "00");
    }

    public void enableDojoSpawn() {
        dojoSpawn = TimerManager.getInstance().register(new DojoSpawn(), 7000, 7000);
    }

    public void disableDojoSpawn() {
        if (dojoSpawn != null) {
            dojoSpawn.cancel(false);
            dojoSpawn = null;
        }
    }

    private int getPlayerNPCMap() {
        switch (mapId) {
            case 100000204:
            case 100000205:
                return 300;
            case 101000004:
            case 101000005:
                return 100;
            case 102000004:
            case 102000005:
                return 200;
            case 103000008:
            case 103000009:
                return 400;
        }
        return -1;
    }

    private void loadPlayerNPCs() {
        Connection con = DatabaseConnection.getConnection();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = con.prepareStatement("SELECT * FROM characters WHERE level = 200 AND (job >= " + getPlayerNPCMap() + " AND job < " + (getPlayerNPCMap() - 100) + ") AND gm < 2 ORDER BY lastLevelUpTime LIMIT 10");
            rs = ps.executeQuery();
            while (rs.next()) {
                playerNPCs.add(new MaplePlayerNPC(rs.getInt("id"), rs.getString("name"), rs.getInt("hair"), rs.getInt("face"), rs.getInt("skin"), rs.getInt("gender"), rs.getInt("job")));
            }
            rs.close();
            ps.close();
        } catch (SQLException e) {
            System.out.println("Error loading PlayerNPCs for Map : " + mapId);
        }
    }

    public void setBuffZone(MapleBuffZone zone) {
        buffZone = zone;
    }

    public MapleBuffZone getBuffZone() {
        return buffZone;
    }
    
    public void broadcastShip(final boolean state) {
        broadcastMessage(MaplePacketCreator.onContiState(state));
        this.setDocked(state);
    }
    
    public void broadcastEnemyShip(final boolean state) {
        broadcastMessage(MaplePacketCreator.onContiMove(state));
        this.setDocked(state);
    }
    
    public void warpEveryone(int to) {
        List<MapleCharacter> players = new ArrayList<>(getCharacters());
        
        for (MapleCharacter chr : players) {
            chr.changeMap(to, 0);
        }
    }
}