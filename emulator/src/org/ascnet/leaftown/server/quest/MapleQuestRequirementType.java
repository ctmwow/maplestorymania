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

package org.ascnet.leaftown.server.quest;

/**
 * @author Matze
 */
public enum MapleQuestRequirementType {

    UNDEFINED(-1),
    JOB(0),
    ITEM(1),
    QUEST(2),
    MIN_LEVEL(3),
    MAX_LEVEL(4),
    END_DATE(5),
    MOB(6),
    NPC(7),
    FIELD_ENTER(8),
    INTERVAL(9),
    SCRIPT(10),
    PET(11),
    MIN_PET_TAMENESS(12),
    DAY_BY_DAY(13),
    INFO(14),
    MONSTER_BOOK(15);

    public MapleQuestRequirementType getItem() {
        return ITEM;
    }

    final byte type;

    private MapleQuestRequirementType(int type) {
        this.type = (byte) type;
    }

    public byte getType() {
        return type;
    }

    public static MapleQuestRequirementType getByType(byte type) {
        for (MapleQuestRequirementType l : MapleQuestRequirementType.values()) {
            if (l.type == type) {
                return l;
            }
        }
        return null;
    }

    public static MapleQuestRequirementType getByWZName(String name) {
        switch (name) {
            case "job":
                return JOB;
            case "info":
                return INFO;
            case "quest":
                return QUEST;
            case "item":
                return ITEM;
            case "lvmin":
                return MIN_LEVEL;
            case "lvmax":
                return MAX_LEVEL;
            case "end":
                return END_DATE;
            case "mob":
                return MOB;
            case "npc":
                return NPC;
            case "fieldEnter":
                return FIELD_ENTER;
            case "interval":
                return INTERVAL;
            case "startscript":
                return SCRIPT;
            case "endscript":
                return SCRIPT;
            case "pet":
                return PET;
            case "pettamenessmin":
                return MIN_PET_TAMENESS;
            case "dayByDay":
                return DAY_BY_DAY;
            case "mbmin":
                return MONSTER_BOOK;
            default:
                return UNDEFINED;
        }
    }
}
