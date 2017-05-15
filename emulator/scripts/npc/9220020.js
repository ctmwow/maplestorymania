function start() {
    status = -1;
    action(1, 0, 0);
}

function action(mode, type, selection) {
    if (mode == 1) {
		status++;
    } else {
		status--;
    }
    if (!cm.isPartyLeader()) {
		cm.sendOk("Desejo que o seu líder fale comigo.");
		cm.dispose();
		return;
    }
    if (cm.haveItem(4032248,17)) {
		cm.warpParty(674030200);
		cm.gainItem(4032248,-17);
    } else {
		cm.sendOk("Hey! Encontre 17 Maps to MV's Lair que estão nas rochas por aqui!");
    }
    cm.dispose();
}