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

import org.ascnet.leaftown.client.MapleClient;
import org.ascnet.leaftown.net.AbstractMaplePacketHandler;
import org.ascnet.leaftown.server.life.MapleMonster;
import org.ascnet.leaftown.server.maps.MapleMap;
import org.ascnet.leaftown.tools.MaplePacketCreator;
import org.ascnet.leaftown.tools.Randomizer;
import org.ascnet.leaftown.tools.data.input.SeekableLittleEndianAccessor;

/**
 * @author iamSTEVE
 */
public class FriendlyMobDamagedHandler extends AbstractMaplePacketHandler 
{

    @Override
    public void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) 
    {
    	MapleMap map = c.getPlayer().getMap();
        int attacker = slea.readInt();
        
        slea.readInt(); //charId
        
        int damaged = slea.readInt();
        
        MapleMonster mob = c.getPlayer().getMap().getMonsterByOid(damaged);
        
        if (c.getPlayer().getMap().getMonsterByOid(damaged) == null || c.getPlayer().getMap().getMonsterByOid(attacker) == null)
        {
        	c.sendPacket(MaplePacketCreator.enableActions());
            return;
        }
        
        int damage = Randomizer.nextInt(((c.getPlayer().getMap().getMonsterByOid(damaged).getMaxHp() / 13 + c.getPlayer().getMap().getMonsterByOid(attacker).getPADamage() * 10)));
        c.getPlayer().getMap().broadcastMessage(MaplePacketCreator.MobDamageMobFriendly(c.getPlayer(), mob, damage), mob.getPosition());
        
        
        if(mob.getId() == 9300061 || mob.getId() == 9300102)
        {
            map.addBunnyHit();
            
            if(mob.getHp() < 1)
            {
            	c.getPlayer().getMap().broadcastMessage(MaplePacketCreator.serverNotice(5, "[Notícia] Você falhou na missão."));
            	c.getPlayer().getMap().killAllMonsters(false);
            	
            	c.getPlayer().getEventInstance().disbandParty();
            }
        }
        c.sendPacket(MaplePacketCreator.enableActions());
    }
}