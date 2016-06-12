/*
 * This file is part of Maple Story Mania Server
 * Copyright (C) 2016
 *
 * Maple Story Mania is a fork of the OdinMS MapleStory Server.
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

package org.ascnet.leaftown.scripting.reactor;

import org.ascnet.leaftown.client.Equip;
import org.ascnet.leaftown.client.IItem;
import org.ascnet.leaftown.client.Item;
import org.ascnet.leaftown.client.MapleCharacter;
import org.ascnet.leaftown.client.MapleClient;
import org.ascnet.leaftown.client.MapleInventoryType;
import org.ascnet.leaftown.scripting.AbstractPlayerInteraction;
import org.ascnet.leaftown.server.MapleItemInformationProvider;
import org.ascnet.leaftown.server.life.MapleLifeFactory;
import org.ascnet.leaftown.server.life.MapleMonster;
import org.ascnet.leaftown.server.life.MapleMonsterInformationProvider.DropEntry;
import org.ascnet.leaftown.server.life.MapleNPC;
import org.ascnet.leaftown.server.maps.MapleMap;
import org.ascnet.leaftown.server.maps.MapleReactor;
import org.ascnet.leaftown.tools.MaplePacketCreator;
import org.ascnet.leaftown.tools.Randomizer;

import java.awt.Point;
import java.util.LinkedList;
import java.util.List;

/**
 * @author Lerk, STK
 * @version 1.1
 */

public class ReactorActionManager extends AbstractPlayerInteraction 
{
    private final MapleReactor reactor;
    private final MapleClient c;

    public ReactorActionManager(final MapleClient c, final MapleReactor reactor) 
    {
        super(c);
        
        this.c = c;
        this.reactor = reactor;
    }

    public void dropItems() 
    {
        dropItems(false, 0x00, 0x00, 0x00, 0x00);
    }

    public void dropItems(final boolean meso, final int mesoChance, final int minMeso, final int maxMeso) 
    {
        dropItems(meso, mesoChance, minMeso, maxMeso, 0x00);
    }

    public void dropItems(final boolean meso, final int mesoChance, final int minMeso, final int maxMeso, final int minItems) 
    {
        final List<DropEntry> chances = getDropChances();
        final List<DropEntry> items = new LinkedList<>();
        
        int numItems = 0;

        if (meso && Math.random() < 0x01 / (double) mesoChance) 
            items.add(new DropEntry(0, mesoChance, 0));

        for (final DropEntry d : chances) 
        {
            if (c.getPlayer().canSeeItem(d.itemid) && Math.random() < 1 / (double) d.chance) 
            {
                numItems++;
                items.add(d);
            }
        }

        while (items.size() < minItems) 
        {
            items.add(new DropEntry(0x00, mesoChance));
            numItems++;
        }

        java.util.Collections.shuffle(items);

        final Point dropPos = reactor.getPosition();

        dropPos.x -= 12 * numItems;

        for (final DropEntry d : items) 
        {
            if (d.itemid == 0x00) 
            {
                final int range = maxMeso - minMeso;
                final int mesoDrop = (Randomizer.nextInt(range) + minMeso) * getClient().getChannelServer().getMesoRate();
                
                reactor.getMap().spawnMesoDrop(mesoDrop, dropPos, reactor, getPlayer(), meso, (byte) 0x00);
            }
            else 
            {
                IItem drop;
                
                final MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
                
                if (ii.getInventoryType(d.itemid) != MapleInventoryType.EQUIP) 
                    drop = new Item(d.itemid, (byte) 0x00, (short) 0x01);
                else 
                    drop = ii.randomizeStats((Equip) ii.getEquipById(d.itemid));
                
                reactor.getMap().spawnItemDrop(reactor, getPlayer(), drop, dropPos, false, true);
            }
            
            dropPos.x += 25;
        }
    }

    private List<DropEntry> getDropChances() 
    {
        return ReactorScriptManager.getInstance().getDrops(reactor.getId());
    }

    public void spawnMonster(final int id) 
    {
        spawnMonster(id, 0x01, getPosition());
    }

    public void spawnMonster(final int id, final int x, final int y) 
    {
        spawnMonster(id, 0x01, new Point(x, y));
    }

    public void spawnMonster(final int id, final int qty) 
    {
        spawnMonster(id, qty, getPosition());
    }

    public void spawnMonster(final int id, final int qty, final int x, final int y) 
    {
        spawnMonster(id, qty, new Point(x, y));
    }

    private void spawnMonster(final int id, final int qty, final Point pos) 
    {
        for (int i = 0x00; i < qty; i++) 
        {
            final MapleMonster mob = MapleLifeFactory.getMonster(id);
            reactor.getMap().spawnMonsterOnGroundBelow(mob, pos);
            
            if (getPlayer().getEventInstance() != null)
                getPlayer().getEventInstance().registerMonster(mob);
        }
    }
    
    public final Point getPosition() 
    {
        final Point pos = reactor.getPosition();
        pos.y -= 0x0A;
        return pos;
    }

    /**
     * Spawns an NPC at the reactor's location
     *
     * @param [Int] npcId
     */
    public void spawnNpc(final int npcId) 
    {
        spawnNpc(npcId, getPosition());
    }

    public void spawnNpc(final int npcId, final MapleCharacter owner) 
    {
        spawnNpc(npcId, getPosition(), owner);
    }

    /**
     * Spawns an NPC at a custom position
     *
     * @param [Int] npcId
     * @param [Int] X
     * @param [Int] Y
     */
    public void spawnNpc(final int npcId, final int x, final int y) 
    {
        spawnNpc(npcId, new Point(x, y));
    }

    /**
     * Spawns an NPC at a custom position
     *
     * @param [Int]   npcId
     * @param [Point] pos
     */
    public void spawnNpc(final int npcId, final Point pos) 
    {
        final MapleNPC npc = MapleLifeFactory.getNPC(npcId);
        
        if (npc != null && !npc.getName().equals("MISSINGNO")) 
        {
            npc.setPosition(pos);
            npc.setCy(pos.y);
            npc.setRx0(pos.x + 50);
            npc.setRx1(pos.x - 50);
            npc.setFh(reactor.getMap().getFootholds().findBelow(pos).getId());
            npc.setCustom(true);
            
            reactor.getMap().addMapObject(npc);
            reactor.getMap().broadcastMessage(MaplePacketCreator.spawnNPC(npc));
        }
    }

    /**
     * Spawns an NPC at a Custom Position to a Custom Character
     * @param [Int] npcId
     * @param [Point] pos
     * @param [MapleCharacter] owner
     */
    public void spawnNpc(final int npcId, final Point pos, final MapleCharacter owner) 
    {
        final MapleNPC npc = MapleLifeFactory.getNPC(npcId);
        
        if (npc != null && !npc.getName().equals("MISSINGNO")) 
        {
            npc.setPosition(pos);
            npc.setCy(pos.y);
            npc.setRx0(pos.x + 50);
            npc.setRx1(pos.x - 50);
            npc.setFh(reactor.getMap().getFootholds().findBelow(pos).getId());
            npc.setCustom(true);
            npc.setOwner(owner);
            
            reactor.getMap().addMapObject(npc);
            reactor.getMap().broadcastMessage(MaplePacketCreator.spawnNPC(npc));
        }
    }

    public final MapleReactor getReactor() 
    {
        return reactor;
    }

    public void spawnFakeMonster(final int id) 
    {
        spawnFakeMonster(id, 0x01, getPosition());
    }

    public void spawnFakeMonster(final int id, final int x, final int y) 
    {
        spawnFakeMonster(id, 0x01, new Point(x, y));
    }

    public void spawnFakeMonster(final int id, final int qty) 
    {
        spawnFakeMonster(id, qty, getPosition());
    }

    public void spawnFakeMonster(final int id, final int qty, final int x, final int y) 
    {
        spawnFakeMonster(id, qty, new Point(x, y));
    }

    private void spawnFakeMonster(final int id, final int qty, final Point pos) 
    {
        for (int i = 0x00; i < qty; i++) 
            reactor.getMap().spawnFakeMonsterOnGroundBelow(MapleLifeFactory.getMonster(id), pos);
    }

    public void killAll() 
    {
        reactor.getMap().killAllMonsters(false);
    }

    public void killMonster(final int monsId) 
    {
        reactor.getMap().killMonster(monsId);
    }

    public void warpMap(final int mapId, final int portal) 
    {
        for (final MapleCharacter mch : getClient().getPlayer().getMap().getCharacters()) 
        {
            final MapleMap target = getClient().getChannelServer().getMapFactory().getMap(mapId);
            mch.changeMap(target, target.getPortal(portal));
        }
    }
}