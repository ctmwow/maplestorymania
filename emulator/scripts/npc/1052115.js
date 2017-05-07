var status = 0;
var section = 0;
importPackage(java.lang);
//questid 29931, infoquest 7662
function action(mode, type, selection) {
    if (mode == 1) {
	status++;
    } else {
	status--;
    }
    if (status == 1) {
	if (cm.getMapId() == 910320001) {
		cm.warp(910320000, 0);
		cm.dispose();
	} else if (cm.getMapId() == 910330001) {
		var itemid = 4001321;
		if (!cm.canHold(itemid)) {
			cm.sendOk("É necessário 1 slot vago no seu inventário 'ETC'.");
		} else {
			cm.gainItem(itemid,1);
			cm.warp(910320000, 0);
		}
		cm.dispose();
	} else if (cm.getMapId() >= 910320100 && cm.getMapId() <= 910320304) {
		cm.sendYesNo("Gostaria de sair deste lugar?");
		status = 99;
	} else {
		cm.sendSimple("Meu nome é Mr.Lim.\r\n#b#e#L1#Entrar na Plataforma Empoeirada.#l#n\r\n#L2#Vá em direção ao Trem 999.#l\r\n#L3#Receba uma <Medalha do Empregado Honorário>.l#k");
	}
    } else if (status == 2) {
		section = selection;
		if (selection == 1) {
			if (cm.getPlayer().getLevel() < 25 || cm.getPlayer().getLevel() > 30 || !cm.isPartyLeader()) {
				cm.sendOk("Você deve estar entre os leveis 25-30 e ser líder.");
			} else {
				if (!cm.start_PyramidSubway(-1)) {
					cm.sendOk("A Plataforma Empoeirada está cheia no momento.");
				}
			}
			//todo
		} else if (selection == 2) {
			if (cm.haveItem(4001321)) {
				if (cm.bonus_PyramidSubway(-1)) {
					cm.gainItem(4001321, -1);
				} else {
					cm.sendOk("O Trem 999 está cheia no momento");
				}
			} else {
				cm.sendOk("Você não tem o cartão de embarque.");
			}
		} else if (selection == 3) {
			var record = cm.getQuestRecord(7662);
			var data = record.getCustomData();
			if (data == null) {
				record.setCustomData("0");
				data = record.getCustomData();
			}
			var mons = parseInt(data);
			if (mons < 10000) {
				cm.sendOk("Por favor derrotar pelo menos 10.000 monstros na Estação e procure por mim novamente. Mortes: " + mons);
			} else if (cm.canHold(1142141) && !cm.haveItem(1142141)){
				cm.gainItem(1142141,1);
				cm.forceStartQuest(29931);
				cm.forceCompleteQuest(29931);
			} else {
				cm.sendOk("Por favor, limpe um espaço do seu inventário 'EQUIP' para receber o item.");
			}
		}
		cm.dispose();
	} else if (status == 100) {
		cm.warp(910320000,0);
		cm.dispose();
	}
}