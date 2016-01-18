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

import org.ascnet.leaftown.provider.DataUtil;
import org.ascnet.leaftown.provider.MapleData;
import org.ascnet.leaftown.provider.MapleDataProvider;
import org.ascnet.leaftown.provider.MapleDataProviderFactory;
import org.ascnet.leaftown.tools.Pair;
import org.ascnet.leaftown.tools.StringUtil;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Danny (Leifde)
 */
public class MobAttackInfoFactory {

    private final static Map<Pair<Integer, Integer>, MobAttackInfo> mobAttacks = new HashMap<>();
    private final static MapleDataProvider dataSource = MapleDataProviderFactory.getDataProvider("Mob");

    public static MobAttackInfo getMobAttackInfo(MapleMonster mob, int attack) {
        MobAttackInfo ret = mobAttacks.get(new Pair<>(mob.getId(), attack));
        if (ret != null) {
            return ret;
        }
        synchronized (mobAttacks) {
            // see if someone else that's also synchronized has loaded the skill by now
            ret = mobAttacks.get(new Pair<>(mob.getId(), attack));
            if (ret == null) {
                MapleData mobData = dataSource.getData(StringUtil.getLeftPaddedStr(Integer.toString(mob.getId()) + ".img", '0', 11));
                if (mobData != null) {
                    // MapleData infoData = mobData.getChildByName("info");
                    String linkedmob = DataUtil.toString(mobData.resolve("link"), "");
                    if (linkedmob.length() != 0) {
                        mobData = dataSource.getData(StringUtil.getLeftPaddedStr(linkedmob + ".img", '0', 11));
                    }
                    MapleData attackData = mobData.resolve("attack" + (attack + 1) + "/info");
                    if (attackData != null) {
                        MapleData deadlyAttack = attackData.getChild("deadlyAttack");
                        int mpBurn = DataUtil.toInt(attackData.resolve("mpBurn"), 0);
                        int disease = DataUtil.toInt(attackData.resolve("disease"), 0);
                        int level = DataUtil.toInt(attackData.resolve("level"), 0);
                        int mpCon = DataUtil.toInt(attackData.resolve("conMP"), 0);
                        ret = new MobAttackInfo(mob.getId(), attack);
                        ret.setDeadlyAttack(deadlyAttack != null);
                        ret.setMpBurn(mpBurn);
                        ret.setDiseaseSkill(disease);
                        ret.setDiseaseLevel(level);
                        ret.setMpCon(mpCon);
                    }
                }
                mobAttacks.put(new Pair<>(mob.getId(), attack), ret);
            }
            return ret;
        }
    }
}