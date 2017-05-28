function enter(pi) {
    if (pi.getPlayer().getMap().getAllMonsters().size() == 0) {
		pi.warp(925100100,0); //next
    } else {
		pi.playerMessage(5, "The portal is not opened yet.");
    }
}