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

package org.ascnet.leaftown.server.life;

import org.ascnet.leaftown.client.MapleCharacter;
import org.ascnet.leaftown.server.maps.MapleMap;

import java.awt.Point;
import java.util.concurrent.atomic.AtomicInteger;

public class SpawnPoint {

    private final MapleMonster monster;
    private final Point pos;
    private long nextPossibleSpawn;
    private final int mobTime;
    private final AtomicInteger spawnedMonsters = new AtomicInteger(0);
    private final byte carnivalTeam;
    
    /**
     * Whether the spawned monster is immobile
     */
    private final boolean immobile;
    private final boolean boss;

    public SpawnPoint(MapleMonster monster, Point pos, final byte carnivalTeam, int mobTime) {
        super();
        this.monster = monster;
        this.pos = new Point(pos);
        this.mobTime = mobTime;
        immobile = !monster.isMobile();
        boss = monster.isBoss();
        nextPossibleSpawn = System.currentTimeMillis();
        
        this.carnivalTeam = carnivalTeam;
    }

    public boolean shouldSpawn() {
        return shouldSpawn(System.currentTimeMillis());
    }

    protected boolean shouldSpawn(long now) {
        if (mobTime < 0) {
            return false;
        }
        if ((mobTime != 0 || immobile) && spawnedMonsters.get() > 0 || spawnedMonsters.get() > 2) {
            return false;
        }
        return nextPossibleSpawn <= now;
    }

    /**
     * Spawns the monster for this spawnpoint. Creates a new MapleMonster instance for that and returns it.
     *
     * @param mapleMap
     * @return
     */
    public boolean spawnMonster(MapleMap mapleMap) {
        MapleMonster mob = new MapleMonster(monster);
        if (mob.getId() == 9400568)
            return false;
        mob.setPosition(new Point(pos));
        mob.setCarnivalTeam(carnivalTeam);
        
        spawnedMonsters.incrementAndGet();
        mob.addListener(new MonsterListener() {

            @Override
            public void monsterKilled(MapleMonster monster, MapleCharacter highestDamageChar) {
                nextPossibleSpawn = System.currentTimeMillis();

                if (mobTime > 0) {
                    if (boss) {
                        nextPossibleSpawn += mobTime / (double) 10 * (2.5 + 10 * Math.random()) * 1000;
                    } else {
                        nextPossibleSpawn += mobTime * 1000;
                    }
                } else {
                    nextPossibleSpawn += monster.getAnimationTime("die1");
                }
                spawnedMonsters.decrementAndGet();
            }
        });
        mapleMap.spawnMonster(mob);
        return true;
    }

    public boolean isBoss() {
        return boss;
    }
}