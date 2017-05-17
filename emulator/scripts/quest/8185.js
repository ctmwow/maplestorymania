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
importPackage(Packages.org.ascnet.leaftown.client);
importPackage(Packages.org.ascnet.leaftown.server);
importPackage(Packages.org.ascnet.leaftown.tools);

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
			qm.sendNextPrev("#e#bHey, você conseguiu!#n#k \r\n#rUau!#k Agora eu posso terminar meus estudos em seu bicho de estimação!");
		} else if (status == 1) {
			if (mode == 0) {
				qm.sendOk("Eu vejo... Volte quando quiser. Estou muito animado para fazer isso.");
				qm.dispose();
			} else {
				qm.sendNextPrev("Lembrando: a cor do seu novo dragão será #e#raleatória#k#n! As cores são #gverde, #bazul, #rvermelho, #dou muito raramente#k, preto. \r\n\r\n#fUI/UIWindow.img/QuestIcon/5/0# \r\n\r Se você não gostar da cor nova do seu animal de estimação, ou se você desejar mudar a cor do bicho de estimação outra vez, #evocê pode mudá-la!#n Simplesmente #dcompre outra Rocha da Evolução, junte 10,000 mesos, #ke #dequipe seu novo bicho de estimação#k antes de falar comigo novamente, mas é claro, eu não posso devolver o seu bicho de estimação como um dragão bebê, apenas para outro dragão adulto.");
			}
		} else if (status == 2) {
			qm.sendYesNo("Agora deixe-me tentar evoluir seu bicho de estimação. Está pronto? Quer ver seu dragão bebê tornar=se em um dragão adulto preto, azul, verde calmo ou vermelho impetuoso? Ele ainda terá a mesma proximidade, level, nome, plenitude, fome e equipamentos, caso você esteja preocupado. \r\n\r #b#eVocê deseja continuar ou tem algumas coisas de última hora para fazer primeiro?#k#n");
                } else if (status == 3) {
			qm.sendNextPrev("Tudo bem, aqui vamos nós! #rHYAHH!#k");
		} else if (status == 4) {
			
			var pet = 0;
			if (qm.getPlayer().getPet(0).getItemId() >= 5000029 && qm.getPlayer().getPet(0).getItemId() <= 5000033) {
				var pet = 0;
			} else if (qm.getPlayer().getPet(1).getItemId() >= 5000029 && qm.getPlayer().getPet(1).getItemId() <= 5000033) {
				var pet = 1;
			} else if (qm.getPlayer().getPet(2).getItemId() >= 5000029 && qm.getPlayer().getPet(2).getItemId() <= 5000033) {
				var pet = 2;
			} else {
				qm.sendOk("Algo deu errado.");
				qm.dispose();
			}
			
			if (pet == null || !qm.haveItem(5380000,1)) {
				cm.sendOk("Você não atende aos requisitos. Você precisa de #i5380000##t5380000#, Bem como qualquer um dos #d#i5000029##t5000029##k, #g#i5000030##t5000030##k, #r#i5000031##t5000031##k, #b#i5000032##t5000032##k, ou #e#i5000033##t5000033##n equipados com level 15 acima. Por favor, volte quando estiver pronto.");
				cm.dispose();
			}else {
				
				var after = 0;
				
				var id = qm.getPlayer().getPet(pet).getItemId();
				
				if (id < 5000029 || id > 5000033) {
					qm.sendOk("Algo deu errado.");
					qm.dispose();
				}
				
				var rand = 1 + Math.floor(Math.random() * 10);
				
				var petInfo = qm.getPlayer().getPet(pet);				
				
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
				qm.gainItem(5380000, -1);
				qm.gainMeso(-10000);
				qm.evolvePet(pet, after);
				//qm.sendOk("#bESPLÊNDIDO! FUNCIONOU!#k Seu dragão cresceu maravilhosamente! #rVocê pode encontrar seu novo bicho de estimação no inventário 'CASH'.\r Ele costumava a ser um #i" + id + "##t" + id + "#, e agora é \r um #i" + after + "##t" + after + "#!#k \r\n\r\n#fUI/UIWindow.img/QuestIcon/4/0#\r\n#v"+after+"# #t"+after+"#\r\n\r\n#fUI/UIWindow.img/QuestIcon/8/0# 1000 EXP\r\n#fUI/UIWindow.img/QuestIcon/9/0# 2 Closeness\r\n#fUI/UIWindow.img/QuestIcon/6/0# 1 Fame");
				qm.sendOk("#bESPLÊNDIDO! FUNCIONOU!#k Seu dragão cresceu maravilhosamente! #rVocê pode encontrar seu novo bicho de estimação no inventário 'CASH'.#k\r\n\r\n#fUI/UIWindow.img/QuestIcon/4/0#\r\n#v"+after+"# #t"+after+"#");
				qm.dispose();
			}
		}
	}
}