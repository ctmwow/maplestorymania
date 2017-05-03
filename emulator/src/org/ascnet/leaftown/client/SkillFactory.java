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

package org.ascnet.leaftown.client;

import org.ascnet.leaftown.provider.DataUtil;
import org.ascnet.leaftown.provider.MapleData;
import org.ascnet.leaftown.provider.MapleDataProvider;
import org.ascnet.leaftown.provider.MapleDataProviderFactory;
import org.ascnet.leaftown.tools.StringUtil;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

public class SkillFactory {

    private final static Map<Integer, ISkill> skills = new HashMap<>(713);
    private final static MapleDataProvider datasource = MapleDataProviderFactory.getDataProvider("Skill");
    private final static MapleData stringData = MapleDataProviderFactory.getDataProvider("String").getData("Skill.img");

    public static ISkill getSkill(int id) {
        /*ISkill ret = skills.get(id);
		if (ret == null) {
			MapleData skillRoot = datasource.getData(StringUtil.getLeftPaddedStr(String.valueOf(id / 10000), '0', 3) + ".img");
			if (skillRoot == null)
				return null;
			MapleData skillData = skillRoot.getChildByPath("skill/" + StringUtil.getLeftPaddedStr(String.valueOf(id), '0', 7));
			if (skillData == null)
				return null;
			ISkill skill = Skill.loadFromData(id, skillData);
			if (skill != null)
				ret = skills.put(id, skill);
		}*/
        return skills.get(id);
    }

    public static String getSkillName(int id) {
        String strId = Integer.toString(id);
        strId = StringUtil.getLeftPaddedStr(strId, '0', 7);
        MapleData skillroot = stringData.resolve(strId);
        if (skillroot != null) {
            return DataUtil.toString(skillroot.getChild("name"), "");
        }
        return null;
    }

    public static void loadSkills() {
        for (MapleData iFile : datasource.getRoot()) {
            if (iFile.getName().length() <= 8) {
                MapleData skillRoots = datasource.getData(iFile.getName()).resolve("skill/");
                for (MapleData skill : skillRoots) {
                    int skillId = Integer.parseInt(skill.getName());
                    skills.put(skillId, Skill.loadFromData(skillId, skill));
                }
            }
        }
    }

    public static Collection<ISkill> getAllSkills() {
        return skills.values();
    }
}