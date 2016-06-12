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

package org.ascnet.leaftown.scripting.maps;

import org.ascnet.leaftown.client.MapleClient;
import org.ascnet.leaftown.scripting.AbstractPlayerInteraction;
import org.ascnet.leaftown.tools.MaplePacketCreator;

public class MapScriptMethods extends AbstractPlayerInteraction 
{
    public MapScriptMethods(MapleClient c) 
    {
        super(c); 
    }
    
    public void displayAranIntro() 
    {
        switch (getPlayer().getMapId()) 
        {
            case 914090010:
                lockUI();
                getClient().sendPacket(MaplePacketCreator.showIntro("Effect/Direction1.img/aranTutorial/Scene0"));
                break;
            case 914090011:
            	getClient().sendPacket(MaplePacketCreator.showIntro("Effect/Direction1.img/aranTutorial/Scene1" + getPlayer().getGender()));
                break;
            case 914090012:
            	getClient().sendPacket(MaplePacketCreator.showIntro("Effect/Direction1.img/aranTutorial/Scene2" + getPlayer().getGender()));
                break;
            case 914090013:
            	getClient().sendPacket(MaplePacketCreator.showIntro("Effect/Direction1.img/aranTutorial/Scene3"));
                break;
            case 914090100:
                lockUI();
                getClient().sendPacket(MaplePacketCreator.showIntro("Effect/Direction1.img/aranTutorial/HandedPoleArm" + getPlayer().getGender()));
                break;
        }
    }
    
    public void startExplorerExperience() 
    {
    	switch(getPlayer().getMapId())
    	{
    		case 1020100:
    			getClient().sendPacket(MaplePacketCreator.showIntro("Effect/Direction3.img/swordman/Scene" + getPlayer().getGender()));
    			break;
    		case 1020200:
    			getClient().sendPacket(MaplePacketCreator.showIntro("Effect/Direction3.img/magician/Scene" + getPlayer().getGender()));
    			break;
    		case 1020300:
    			getClient().sendPacket(MaplePacketCreator.showIntro("Effect/Direction3.img/archer/Scene" + getPlayer().getGender()));
    			break;
    		case 1020400:
    			getClient().sendPacket(MaplePacketCreator.showIntro("Effect/Direction3.img/rogue/Scene" + getPlayer().getGender()));
    			break;
    		case 1020500:
    			getClient().sendPacket(MaplePacketCreator.showIntro("Effect/Direction3.img/pirate/Scene" + getPlayer().getGender()));
    			break;
    	}
    }
}