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

function start(ms) 
{
	switch (getPlayer().getMapId()) { 
		case 103000800: 
			getPlayer().getMap().startMapEffect("Solve the question and gather the amount of passes!", 5120017); 
			break; 
		case 103000801: 
			getPlayer().getMap().startMapEffect("Get on the ropes and unveil the correct combination!", 5120017); 
			break; 
		case 103000802: 
			getPlayer().getMap().startMapEffect("Get on the platforms and unveil the correct combination!", 5120017); 
			break; 
		case 103000803: 
			getPlayer().getMap().startMapEffect("Get on the barrels and unveil the correct combination!", 5120017); 
			break; 
		case 103000804: 
			getPlayer().getMap().startMapEffect("Defeat King Slime and his minions!", 5120017); 
			break; 
	} 
	break;
}