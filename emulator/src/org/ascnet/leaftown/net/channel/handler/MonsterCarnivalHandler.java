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

import java.awt.Point;

import org.ascnet.leaftown.client.MapleCharacter;
import org.ascnet.leaftown.client.MapleClient;
import org.ascnet.leaftown.client.MapleDisease;
import org.ascnet.leaftown.client.SkillFactory;
import org.ascnet.leaftown.net.AbstractMaplePacketHandler;
import org.ascnet.leaftown.net.world.MaplePartyCharacter;
import org.ascnet.leaftown.server.life.MapleLifeFactory;
import org.ascnet.leaftown.server.life.MapleMonster;
import org.ascnet.leaftown.server.life.MobSkillFactory;
import org.ascnet.leaftown.tools.MaplePacketCreator;
import org.ascnet.leaftown.tools.data.input.SeekableLittleEndianAccessor;

public class MonsterCarnivalHandler extends AbstractMaplePacketHandler 
{
    @Override
    public void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) 
    {
    	if(c.getPlayer().getMonsterCarnival() == null)
    	{
    		c.sendPacket(MaplePacketCreator.enableActions());
    		return;
    	}
    	
        int tab = slea.readByte();
        int num = slea.readByte();
        
        if (tab == 0x00000000) // MONSTER SPAWN TAB
        {
        	final int cpCost = getMonsterCost(num);
        	
        	if(c.getPlayer().getCP() >= cpCost)
        		c.getPlayer().gainCP(-cpCost);
        	else
        	{
        		c.sendPacket(MaplePacketCreator.serverNotice(0x00000005, "CP insuficiente!"));
        		c.sendPacket(MaplePacketCreator.enableActions());
        		return;
        	}
        	
        	c.getPlayer().getMap().broadcastMessage(MaplePacketCreator.playerSummoned(c.getPlayer().getName(), tab, num));
        	
        	MapleMonster monster = MapleLifeFactory.getMonster(getMonsterIdByNum(num));
        	monster.setCarnivalTeam((byte) (c.getPlayer().getTeam()));
        	
            c.getPlayer().getMap().spawnMonsterOnGroundBelow(monster, randomizePosition(c.getPlayer().getMapId(), 0x00000001));
            c.sendPacket(MaplePacketCreator.enableActions());
        }
        else if (tab == 0x00000001) // DEBUFF TAB
        {
        	
        }
        else if (tab == 0x00000002) // SKILL TAB
        {
        	
        }
    }

    private Point randomizePosition(int mapid, int team) 
    {
        int posx = 0x00000000;
        int posy = 0x00000000;
        if (mapid == 980000301) //room 3 iirc 
        {
            posy = 0x000000A2;
            if (team == 0x00000000)
                posx = rand(-0x00000612, -0x00000097);
            else
                posx = rand(0x00000094, 0x00000623);
        }
        return new Point(posx, posy);
    }

    /* MOB SUMMOM LIST
		1 - Brown Teddy - 9300127
		2 - Bloctopus  - 9300128
		3 - Ratz - 9300129
		4 - Chronos  - 9300130
		5 - Toy Trojan - 9300131
		6 - Tick-Tock - 9300132
		7 - Robo - 9300133
		8 - King Bloctopus - 9300134
		9 - Master Chronos - 9300135
		10 - Rombot - 9300136
	*/
    private final int getMonsterIdByNum(final int num) 
    {
        if(num >= 0x00000000 && num <= 0x00000009)
        	return 9300127 + num;
        else
        	return 210100; // wrong mob number. spawn a slime
    }
    
    private final int getMonsterCost(final int num)
    {
    	switch(num)
    	{
	    	case 0x00000000:
	    	case 0x00000001:
	    		return 0x00000007;
	    	case 0x00000002:
	    	case 0x00000003:
	    		return 0x00000008;
	    	case 0x00000004:
	    	case 0x00000005:
	    		return 0x00000009;
	    	case 0x00000006:
	    		return 0x0000000A;
	    	case 0x00000007:
	    		return 0x0000000B;
	    	case 0x00000008:
	    		return 0x0000000C;
	    	case 0x00000009:
	    		return 0x0000001E;
    		default:
    			return 0x00000000;
    	}
    }
    
    private static int rand(int lbound, int ubound) 
    {
        return (int) (Math.random() * (ubound - lbound + 0x00000001) + lbound);
    }
}