function enter(pi) {
    if (!pi.getPortal().hasSpawned()) {
        var rightHead = Packages.org.ascnet.leaftown.server.life.MapleLifeFactory.getMonster(8810025);
        pi.getPlayer().getMap().spawnMonsterOnGroundBelow(rightHead, new java.awt.Point(-370, 259));
        pi.playerMessage(5, "The cave shakes as Horntail's right head emerges from the darkness!");
	pi.getPortal().setSpawned(true);
	pi.schedulePortalSpawn(pi.getPlayer().getMap(), "next00", true, 10000);
   //   pi.createMapMonitor(pi.getPlayer().getMap().getId(), "mob00");
//	pi.createMapMonitor(pi.getPlayer().getMap().getId(), true, pi.getPlayer().getMap().getId(), "mob00", 0, -1);
	return false;
    }
    return false;
}