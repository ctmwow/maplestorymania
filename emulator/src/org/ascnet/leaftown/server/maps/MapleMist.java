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

import org.ascnet.leaftown.client.ISkill;
import org.ascnet.leaftown.client.MapleCharacter;
import org.ascnet.leaftown.client.MapleClient;
import org.ascnet.leaftown.client.SkillFactory;
import org.ascnet.leaftown.net.MaplePacket;
import org.ascnet.leaftown.server.MapleStatEffect;
import org.ascnet.leaftown.server.life.MapleMonster;
import org.ascnet.leaftown.server.life.MobSkill;
import org.ascnet.leaftown.tools.MaplePacketCreator;

import java.awt.Point;
import java.awt.Rectangle;

public class MapleMist extends AbstractMapleMapObject {

    private final Rectangle mistPosition;
    private MapleCharacter owner = null;
    private MapleStatEffect source = null;
    private MapleMonster mob;
    private MobSkill skill;
    private boolean isPoisonMist;
    private boolean isMobMist;
    private int skillDelay;

    public MapleMist(Rectangle mistPosition, MapleCharacter owner, MapleStatEffect source) {
        this.mistPosition = mistPosition;
        this.owner = owner;
        this.source = source;
        if (source == null) {
            isMobMist = false;
            isPoisonMist = true;
            skillDelay = 8;
        } else {
            switch (source.getSourceId()) {
                case 2111003:
                    isMobMist = false;
                    isPoisonMist = true;
                    skillDelay = 8;
                    break;
                case 4221006:
                    isMobMist = false;
                    isPoisonMist = false;
                    skillDelay = 8;
                    break;
                case 12111005:
                case 14111006:
                    isMobMist = false;
                    isPoisonMist = true;
                    skillDelay = 8;
                    break;
            }
        }
    }

    public MapleMist(Rectangle mistPosition, MapleMonster mob, MobSkill skill) {
        this.mistPosition = mistPosition;
        this.mob = mob;
        this.skill = skill;

        isMobMist = true;
        isPoisonMist = true;
        skillDelay = 0;
    }

    @Override
    public MapleMapObjectType getType() {
        return MapleMapObjectType.MIST;
    }

    @Override
    public Point getPosition() {
        return mistPosition.getLocation();
    }

    public MapleCharacter getOwner() {
        return owner;
    }

    public MapleMonster getMobOwner() {
        return mob;
    }

    public ISkill getSourceSkill() {
        return SkillFactory.getSkill(source.getSourceId());
    }

    public Rectangle getBox() {
        return mistPosition;
    }

    public boolean isPoisonMist() {
        return isPoisonMist;
    }

    public void setPoison(boolean poison) {
        isPoisonMist = poison;
    }

    public boolean isMobMist() {
        return isMobMist;
    }

    public void setMobMist(boolean mob) {
        isMobMist = mob;
    }

    public MobSkill getMobSkill() {
        return skill;
    }

    public int getSkillDelay() {
        return skillDelay;
    }

    @Override
    public void setPosition(Point position) {
        throw new UnsupportedOperationException();
    }

    public MaplePacket makeDestroyData() {
        if (owner != null) {
            owner.removeOwnedMapObject(this);
            owner = null;
        }
        return MaplePacketCreator.removeMist(getObjectId());
    }

    @Override
    public void sendDestroyData(MapleClient client) {
        client.sendPacket(makeDestroyData());
    }

    public MaplePacket makeSpawnData() {
        if (owner != null) {
            return MaplePacketCreator.spawnMist(getObjectId(), owner.getId(), getSourceSkill().getId(), owner.getSkillLevel(SkillFactory.getSkill(source.getSourceId())), this);
        }
        return MaplePacketCreator.spawnMist(getObjectId(), mob.getId(), skill.getSkillId(), skill.getSkillLevel(), this);
    }

    public MaplePacket makeFakeSpawnData(int level) {
        if (owner != null) {
            return MaplePacketCreator.spawnMist(getObjectId(), owner.getId(), 2111003, level, this);
        }
        return MaplePacketCreator.spawnMist(getObjectId(), mob.getId(), skill.getSkillId(), skill.getSkillLevel(), this);
    }

    @Override
    public void sendSpawnData(MapleClient client) {
        client.sendPacket(makeSpawnData());
    }

    public boolean makeChanceResult() {
        if (source == null)
            return false;
        return source.makeChanceResult();
    }
}