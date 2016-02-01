function enter(pi) {
    var Mapz= pi.getClient().getChannelServer().getMapFactory().getMap(541010100);
    if (Mapz.getCharacters().size() == 0) {
        Mapz.resetReactors();
    } else { // someone is inside
        for (var i = 0; i < 3; i++) {
            if (Mapz.getMonsterById(9420513 + i) != null) {
                pi.getPlayer().dropMessage("The battle agains has already begun, so you may not enter this place.");
                return false;
            }
        }
    }
    pi.warp(541010100, "sp");
    return true;
}