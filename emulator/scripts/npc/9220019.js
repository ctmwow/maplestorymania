function start() {
    cm.sendYesNo("Você gostaria de voltar?");
}

function action(mode, type, selection) {
    if (mode == 1) {
	var eim = cm.getPlayer().getEventInstance();
	if (eim != null)
		eim.unregisterPlayer(cm.getPlayer());
	if (cm.getMapId() == 674030200) { //boss map
		cm.warp(674030100,0);
	} else {
		var map = cm.getPlayer().getSavedLocation("EVENT");
		if (map > -1 && map != cm.getMapId()) {
			cm.warp(map, 0);
		} else {
    			cm.warp(100000000, 0);
		}
	}
    }
    cm.dispose();
}