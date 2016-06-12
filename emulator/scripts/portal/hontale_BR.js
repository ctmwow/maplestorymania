function enter(pi) {
    if (pi.getPlayer().getMap().getId() == 240060000) {
	if (pi.getPortal().hasSpawned()) {
            pi.warp(240060100, "st00");
            return true;
	} else {
            pi.playerMessage(5, "A deathening roar is heard, you must slay Horntail's left head before you can proceed!");
        }
    } else if (pi.getPlayer().getMap().getId() == 240060100) {
	if (pi.getPortal().hasSpawned()) {
            pi.warp(240060200, "sp");
            return true;
	} else {
            pi.playerMessage(5, "A deathening roar is heard, you must slay Horntail's right head before you can proceed!");
	}
    }
    return false;
}

