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

/**
 * Guild Alliance NPC
 */

var status;
var choice;
var guildName;

function start() {
	//cm.sendOk("The Guild Alliance is currently under development.");
	//cm.dispose();
	status = -1;
	action(1,0,0);
}

function action(mode, type, selection) {
	if (mode == 1) {
		status++;
	} else {
		cm.dispose();
		return;
	}
	if (status == 0) {
		cm.sendSimple("Olá! Eu sou #bLenario#k, responsável pela Alianças de Clã.\r\n#b#L0#Você pode me dizer o que é uma Aliança de Clã??#l\r\n#L1#Como faço uma Aliança de Clã?#l\r\n#L2#Quero fazer uma Aliança de Clã.#l\r\n#L3#Quero aumentar a capacidade da Aliança do Clã.#l\r\n#L4#Quero desfazer com a Aliança do Clã.#l");
	} else if (status == 1) {
		choice = selection;
	    if (selection == 0) {
		    cm.sendOk("Aliança de Clã é exatamente como se diz, uma união de um número de clãs para formar um super grupo. Estou encarregado de gerir estas Alianças de Clãs.");
			cm.dispose();
		} else if (selection == 1) {
			cm.sendOk("Para fazer uma Aliança de Clã, 2 Mestres de Clã precisam estar em um grupo. O líder deste grupo será designado como o Mestre da Aliança do Clã.");
			cm.dispose();
		} else if(selection == 2) {
			if (cm.getPlayer().getParty() == null || cm.getPartyMembers() == null || cm.getPartyMembers().size() != 2 || !cm.isLeader()) {
				cm.sendOk("Você não pode criar uma aliança até que você entre em um grupo de 2 pessoas que sejam Mestres de Clã."); //Not real text
				cm.dispose();
			} else if (cm.getPartyMembers().get(0).getGuildId() <= 0 || cm.getPartyMembers().get(0).getGuildRank() > 1) {
				cm.sendOk("Você não pode formar uma Aliança de Clãs até que você possua um clã");
				cm.dispose();
			} else if (cm.getPartyMembers().get(1).getGuildId() <= 0 || cm.getPartyMembers().get(1).getGuildRank() > 1) {
				cm.sendOk("Seu membro do grupo não parece possuir um clã.");
				cm.dispose();
			} else {
				var gs = cm.getGuild(cm.getPlayer().getGuildId());
				var gs2 = cm.getGuild(cm.getPartyMembers().get(1).getGuildId());
				if (gs.getAllianceId() > 0) {
					cm.sendOk("Você não pode formar uma Aliança de Clã se você já está filiado a uma aliança diferente.");
					cm.dispose();
				} else if (gs2.getAllianceId() > 0) {
					cm.sendOk("Seu membro do grupo já está filiado a uma Aliança de Clã.");
					cm.dispose();
				} else if (cm.partyMembersInMap() < 2) {
					cm.sendOk("É necessário que o outro membro do grupo se encontre no mapa.");
					cm.dispose();
				} else
					cm.sendYesNo("Oh, você está interessado em formar uma Aliança de Clã?");
			}
		} else if (selection == 3) {
			if (cm.getPlayer().getGuildRank() == 1 && cm.getPlayer().getAllianceRank() == 1) {
				cm.sendYesNo("Para aumentar a capacidade, você precisará pagar 10,000,000 mesos. Tem certeza de que deseja prosseguir?"); //ExpandGuild Text
			} else {
			    cm.sendOk("Somente o Mestre da Aliança do Clã pode expandir a capacidade da Aliança.");
				cm.dispose();
			}
		} else if(selection == 4) {
			if (cm.getPlayer().getGuildRank() == 1 && cm.getPlayer().getAllianceRank() == 1) {
				cm.sendYesNo("Tem certeza de que deseja desfazer com a Aliança do Clã?");
			} else {
				cm.sendOk("Somente o Mestre da Aliança do Clã pode desfazer com a Aliança do Clã.");
				cm.dispose();
			}
		}
	} else if(status == 2) {
	    if (choice == 2) {
		    cm.sendGetText("Digite o nome da sua nova Aliança do Clã. (máx. 12 letras)", "", 0, 0, 1);
		} else if (choice == 3) {
			if (cm.getPlayer().getGuildId() <= 0) {
				cm.sendOk("Você não pode aumentar uma Aliança de Clã inexistente.");
				cm.dispose();
			} else {
				if (cm.addCapacityToAlliance()) {
					cm.sendOk("Você adicionou capacidade à sua aliança.");
				} else {
					cm.sendOk("Sua Aliança de Clã já tem muita capacidade. O máximo são 5 clãs.");
				}
				cm.dispose();
			}
		} else if (choice == 4) {
			if (cm.getPlayer().getGuildId() <= 0) {
				cm.sendOk("Você não pode desfazer com uma Aliança de Clã inexistente.");
				cm.dispose();
			} else {
				if (cm.disbandAlliance(cm.getClient(), cm.getPlayer().getGuild().getAllianceId())) {
					cm.sendOk("Sua aliança foi desfeita.");
				} else {
					cm.sendOk("Ocorreu um erro ao desfazer a Aliança do Clã");
				}
				cm.dispose();
			}
		}
	} else if (status == 3) {
		guildName = cm.getText();
	    cm.sendYesNo("#b"+ guildName + "#k será o nome da sua Aliança de Clã?");
	} else if (status == 4) {
			if (!cm.createAlliance(cm.getPlayer(), cm.getPartyMembers().get(1), guildName)) {
				cm.sendNext("Este nome não está disponível, escolha outro."); //Not real text
				status = 1;
				choice = 2;
			} else
				cm.sendOk("Você formou uma Aliança de Clã com sucesso.");
			cm.dispose();
	}
}