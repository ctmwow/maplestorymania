function enter(pi) {
    if (!pi.getPortal().hasSpawned()) {
        var leftHead = Packages.org.ascnet.leaftown.server.life.MapleLifeFactory.getMonster(8810024);
        pi.getPlayer().getMap().spawnMonsterOnGroundBelow(leftHead, new java.awt.Point(895, 229));
        pi.playerMessage(5, "The cave shakes as Horntail's left head emerges from the darkness!");
        pi.getPortal().setSpawned(true);
        pi.schedulePortalSpawn(pi.getPlayer().getMap(), "next00", true, 10000);
    //  pi.createMapMonitor(pi.getPlayer().getMap().getId(), true, pi.getPlayer().getMap().getId(), "mob00", 0, -1);
        return false;
    }
    return false;
}

