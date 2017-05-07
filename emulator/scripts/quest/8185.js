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
	Description: 		Quest - Pet Evolution2
*/

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
			qm.sendNextPrev("#e#bHey, você conseguiu!#n#k \r\n#rUau!#k Agora eu posso terminar meus estudos em seu mascote!");
		} else if (status == 1) {
			if (mode == 0) {
				qm.sendOk("Eu vejo... Volte quando quiser. Estou muito animado para fazer isso.");
				qm.dispose();
			} else {
				qm.sendNextPrev("Lembrando: a cor do seu novo dragão será #e#raleatória#k#n! As cores são #gverde, #bazul, #rvermelho, #dou muito raramente#k, preto. \r\n\r\n#fUI/UIWindow.img/QuestIcon/5/0# \r\n\r Se você não gostar da cor nova do seu animal de estimação, ou se você desejar mudar a cor do mascote outra vez, #evocê pode mudá-la!#n Simplesmente #dcompre outra Rocha da Evolução, junte 10,000 mesos, #ke #dequipe seu novo mascote#k antes de falar comigo novamente, mas é claro, eu não posso devolver o seu mascote como um dragão bebê, apenas para outro dragão adulto.");
			}
		} else if (status == 2) {
			qm.sendYesNo("Agora deixe-me tentar evoluir seu mascote. Está pronto? Quer ver seu dragão bebê tornar=se em um dragão adulto preto, azul, verde calmo ou vermelho impetuoso? Ele ainda terá a mesma proximidade, level, nome, plenitude, fome e equipamentos, caso você esteja preocupado. \r\n\r #b#eVocê deseja continuar ou tem algumas coisas de última hora para fazer primeiro?#k#n");
                } else if (status == 3) {
			qm.sendNextPrev("Tudo bem, aqui vamos nós! #rHYAHH!#k");
		} else if (status == 4) {
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
				qm.sendOk("Algo errado. Tente novamente.");
				qm.dispose();
			}
			qm.getPlayer().unequipAllPets(); //IMPORTANT, you can bug/crash yourself if you don't unequip the pet to be deleted
			SpawnPetHandler.evolve(qm.getPlayer().getClient(), 5000029, after);
			qm.sendOk("#bESPLÊNDIDO! FUNCIONOU!#k Seu dragão cresceu maravilhosamente! #rVocê pode encontrar seu novo mascote no inventário 'CASH'.\r Ele costumava a ser um #i" + id + "##t" + id + "#, e agora é \r um #i" + after + "##t" + after + "#!#k \r\n\r\n#fUI/UIWindow.img/QuestIcon/4/0#\r\n#v"+after+"# #t"+after+"#\r\n\r\n#fUI/UIWindow.img/QuestIcon/8/0# 1000 EXP\r\n#fUI/UIWindow.img/QuestIcon/9/0# 2 Closeness\r\n#fUI/UIWindow.img/QuestIcon/6/0# 1 Fame\r\n#fUI/UIWindow.img/QuestIcon/7/0# 100 Mesos");
			qm.dispose();
		}
	}
}