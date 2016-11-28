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
var status = -1;

function start() 
{
    cm.sendNext("Guerreiros possuem um enorme poder com resistência para guardá-lo, e eles brilham em situação de combate corpo a corpo. ataques regulares são poderosos para começar, e armado com habilidades complexas, o trabalho é perfeito para ataques explosivos.");
}

function action(mode, type, selection) 
{
    status++;
    if (mode != 1)
    {
        if(mode == 0)
           cm.sendNext("Se você deseja experimentar o que é ser um guerreiro, venha me ver novamente.");
           
        cm.dispose();
        return;
    }
    
    if (status == 0) 
    {
        cm.sendYesNo("Gostaria de experimentar o que é ser um guerreiro?");
    }
    else if (status == 1)
    {
		cm.lockUI();
        cm.warp(1020100, 0);
		cm.dispose();
    }
}