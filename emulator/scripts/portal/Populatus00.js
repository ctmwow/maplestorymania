function enter(pi) {
    var papuMap = pi.getClient().getChannelServer().getMapFactory().getMap(220080001);
    if (papuMap.getCharacters().size() == 0) {
        papuMap.resetReactors();
	pi.playPortalSE();
    } else { // someone is inside
        for (var i = 0; i < 3; i++) {
            if (papuMap.getMonsterById(8500000 + i) != null) {
                pi.playerMessage(5, "The battle against Papulatus has already begun, so you may not enter this place.");
                return false;
            }
        }
    }
    pi.warp(220080001, "st00");
    return true;
}