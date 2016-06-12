/*
	This file is part of the OdinMS Maple Story Server
    Copyright (C) 2008 Patrick Huy <patrick.huy@frz.cc>
		       Matthias Butz <matze@odinms.de>
		       Jan Christian Meyer <vimes@odinms.de>

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as
    published by the Free Software Foundation version 3 as published by
    the Free Software Foundation. You may not use, modify or distribute
    this program under any other version of the GNU Affero General Public
    License.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/
/* 
	Map(s): 			Empress' Road : Training Forest III
	Description: 		Takes you to Entrance to Drill Hall
*/

var mapp = -1;
var map = 0;

function enter(pi) 
{
    if (pi.isQuestStarted(20701)) 
    	map = 913000000;
    if (pi.isQuestStarted(20702)) 
    	map = 913000100;
    if (pi.isQuestStarted(20703)) 
    	map = 913000200;
    
    if (map > 0) 
    {
    	if (pi.getPlayerCount(map) == 0) 
    	{
    		var mapp = pi.getMap(map);
    		mapp.respawn();
    		
    		pi.warp(map, 0);
    	} 
    	else 
    		pi.playerMessage(5, "Alguém já está nesse mapa.");
    } 
    else 
    	pi.playerMessage(5, "Hall #1 só pode ser acessado se você está envolvido no treinamento Aclimatação de Kiku");
    	
    return true;
}