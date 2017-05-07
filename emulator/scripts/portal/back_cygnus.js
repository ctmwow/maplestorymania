function enter(pi) {
    if (pi.getMap().getAllMonsters().size() == 0) {
	pi.warp(271040200,0);
    } else {
	pi.playerMessage("Empress blocks you from the portal.");
    }
}