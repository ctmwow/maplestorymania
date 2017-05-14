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
    switch(cm.getPlayer().getMapId()) {
	case 930000000:
	    cm.sendOk("Bem vindo! Por favor, entre no portal.");
	    break;
	case 930000100:
	    cm.sendOk("Temos que eliminar todos esses monstros contaminados!");
	    break;
	case 930000200:
	    cm.sendOk("Temos que eliminar todos esses reatores contaminados!");
	    break;
	case 930000300:
	    cm.warpParty(930000400);
	    break;
	case 930000400:
	    if (cm.haveItem(4001169,20)) {
			cm.warpParty(930000500);
			cm.gainItem(4001169,-20);
	    } else if (!cm.haveItem(2270004)) {
			cm.gainItem(2270004,10);
			cm.sendOk("Boa sorte em purificar esses monstros!");
	    } else {
			cm.sendOk("Temos que purificar todos esses monstros contaminados! Me traga 20 Mármores de Monstro deles!");
	    }
	    break;
	case 930000600:
	    cm.sendNext("É isso! Coloque a Pedra Mágica no altar!");
	    break;
	case 930000700:
	    cm.removeAll(4001163);
	    cm.removeAll(4001169);
	    cm.removeAll(2270004);
	    cm.warp(930000800,0);
	    break;
    }
    cm.dispose();
}