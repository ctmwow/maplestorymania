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
/* 	Author: 		Blue
	Name:	 		Garnox
	Map(s): 		New Leaf City : Town Center
	Description: 		Quest - Pet Re-Evolution
*/
importPackage(Packages.org.ascnet.leaftown.server);

var status = -1;

function end(mode, type, selection) {
	if (mode == -1) {
		qm.dispose();
	} else {
		if (mode == 1)
			status++;
		else
			status--;
		if (status == 0) {
			qm.sendYesNo("Tudo bem, vamos fazer isso de novo, vamos?!! Como de costume, será aleatório, e eu vou tirar uma das suas Rochas de Evolução.\r\n\r #r#ePronto?#n#k");
		} else if (status == 1) {
			qm.sendNextPrev("Então, aqui vamo nós!!! #rHYAHH!#k");
		} else if (status == 2) {
			var pet = 0;
			if (qm.getPlayer().getPet(0).getItemId() >= 5000029 && qm.getPlayer().getPet(0).getItemId() <= 5000033) {
				var pet = 0;
			} else if (qm.getPlayer().getPet(1).getItemId() >= 5000029 && qm.getPlayer().getPet(0).getItemId() <= 5000033) {
				var pet = 1;
			} else if (qm.getPlayer().getPet(2).getItemId() >= 5000029 && qm.getPlayer().getPet(0).getItemId() <= 5000033) {
				var pet = 2;
			} else {
				qm.sendOk("Algo deu errado.");
				qm.dispose();
			}
			var id = qm.getPlayer().getPet(pet).getItemId();
			
			var rand = 1 + Math.floor(Math.random() * 10);
			
			var after = 0;
			
			if (rand >= 1 && rand <= 3) {
				after = 5000030;
			} else if (rand >= 4 && rand <= 6) {
				after = 5000031;
			} else if (rand >= 7 && rand <= 9) {
				after = 5000032;
			} else if (rand == 10) {
				after = 5000033;
			} else {
				qm.sendOk("Algo deu errado.");
				qm.dispose();
			}
			qm.evolvePet(pet, after);
			qm.sendOk("Wow! Funcionou novamente! #rVocê pode encontrar seu novo bicho de estimação no inventário 'CASH'.\r Volte aqui com 10,000 mesos e outra Rocha da Evolução se você não gostou!\r\n\r\n#fUI/UIWindow.img/QuestIcon/4/0#\r\n#v"+after+"# #t"+after+"#");
			qm.dispose();
		}
	}
}