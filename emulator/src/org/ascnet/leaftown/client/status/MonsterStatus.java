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

package org.ascnet.leaftown.client.status;

import org.ascnet.leaftown.client.MapleDisease;
import org.ascnet.leaftown.tools.ValueHolder;

import java.io.Serializable;

public enum MonsterStatus implements ValueHolder<Integer>, Serializable {

    WATK(0x1),
    WDEF(0x2),
    MATK(0x4),
    MDEF(0x8),
    ACC(0x10),
    AVOID(0x20),
    SPEED(0x40),
    STUN(0x80), //this is possibly only the bowman stun
    FREEZE(0x100),
    POISON(0x200),
    SEAL(0x400),
    TAUNT(0x800),
    WEAPON_ATTACK_UP(0x1000),
    WEAPON_DEFENSE_UP(0x2000),
    MAGIC_ATTACK_UP(0x4000),
    MAGIC_DEFENSE_UP(0x8000),
    DOOM(0x10000),
    SHADOW_WEB(0x20000),
    WEAPON_IMMUNITY(0x40000),
    MAGIC_IMMUNITY(0x80000),
    DAMAGE_IMMUNITY(0x200000),
    NINJA_AMBUSH(0x400000),
    BURN(0x1000000),
    DARKNESS(0x2000000),
    HYPNOTIZED(0x10000000),
	NEUTRALISE(0x2), // First int on v.87 or else it won't work.
    IMPRINT(0x4),
    MONSTER_BOMB(0x8),
    MAGIC_CRASH(0x10),
    // Speshul comes after
    EMPTY(0x8000000),
    SUMMON(0x80000000); // All summon bag mobs have.

    static final long serialVersionUID = 0L;
    private final int i;

    private MonsterStatus(int i) {
        this.i = i;
    }
    
    public static final MapleDisease getLinkedDisease(final MonsterStatus skill) {
        switch (skill) {
            case STUN:
            case SHADOW_WEB:
                return MapleDisease.STUN;
            case POISON:
            case BURN:
                return MapleDisease.POISON;
            case SEAL:
            case MAGIC_CRASH:
                return MapleDisease.SEAL;
            case FREEZE:
                return MapleDisease.FREEZE;
            case DARKNESS:
                return MapleDisease.DARKNESS;
            case SPEED:
                return MapleDisease.SLOW;
        }
        return null;
    }

    @Override
    public Integer getValue() {
        return i;
    }
}