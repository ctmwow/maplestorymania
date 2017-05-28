/*
	This file is part of the OdinMS Maple Story Server
    Copyright (C) 2008 Patrick Huy <patrick.huy@frz.cc> 
                       Matthias Butz <matze@odinms.de>
                       Jan Christian Meyer <vimes@odinms.de>

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License version 3
    as published by the Free Software Foundation. You may not use, modify
    or distribute this program under any other version of the
    GNU Affero General Public License.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

/**
-- Odin JavaScript --------------------------------------------------------------------------------
	Pietri - Ludibirum Maze PQ
-- By ---------------------------------------------------------------------------------------------
	Raz
-- Version Info -----------------------------------------------------------------------------------
	1.0 - First Version by Raz
---------------------------------------------------------------------------------------------------
* */

var status;
var quant;

importPackage(Packages.client);

function start() {
	quant = cm.itemQuantity(4001106);
	status = -1;
	action(1, 0, 0);
}

function action(mode, type, selection) {
	if (status == 1 && mode == 0) {
		cm.sendNext("Eu acho que você quer coletar mais cupons. Me avise quando você desejar sair do Labirinto.");
		cm.dispose();
		return;
	}
	if (mode == 1)
		status++;
	else {
		cm.dispose();
		return;
	}
	if (status == 0) {
		cm.sendNext("Bom trabalho, você escapou do labirinto! Você coletou os cupons dos monstros escondidos no seu caminho no labirinto?");
	} else if (status == 1) {
		if (cm.getParty() != null && cm.isPartyLeader()) {
			if (quant >= 30) {
				cm.sendYesNo("Até agora você coletou #b" + quant + " cupons#k com seu esforço coletivo. São estes todos os cupons que seu grupo coletou?");
			} else {
				cm.sendYesNo("Bom trabalho! Se você coletar mais que 30 cupons, você irá receber um prêmio bem legal! Gostaria de ir para a saída?");
			}
		} else {
			cm.sendNext("Dependendo do número de cupons que seu grupo coletou, poderá haver uma surpresa bônus!");
		}
	} else if (status == 2) {
		if (cm.getParty() != null && cm.isPartyLeader()) {
			if (quant >= 30) {
				var party = cm.getPlayer().getEventInstance().getPlayers();
				var myParty = cm.getParty().getMembers();
				eim = cm.getPlayer().getEventInstance();
				cm.givePartyExp(50 * quant, party);
				cm.givePartyNX(3 * quant, party);
				cm.removeAll(4001106);
				//Finish PQ
				eim.clearPQ();
				cm.dispose();
			} else {
				eim.leftParty();
				cm.dispose();
			}
		} else {
			cm.sendPrev("Por favor fale com #bseu líder do grupo#k para falar comigo após recolher todos os cupons dos membros do grupo.");
			cm.dispose();
        }
	}
}