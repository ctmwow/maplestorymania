importPackage(Packages.org.ascnet.leaftown.server.maps);
importPackage(Packages.org.ascnet.leaftown.net.channel);
importPackage(Packages.org.ascnet.leaftown.tools);

function enter(pi) {
    var eim = pi.getPlayer().getEventInstance();
    var party = pi.getPlayer().getEventInstance().getPlayers();
    var realParty = pi.getParty();
    var playerStatus = pi.isLeader();
    if (playerStatus) {
        if (eim.getProperty("6stageclear") == null) {
            pi.warp(920010500, 0); 
            return true;
	} else {
            pi.getPlayer().getClient().getSession().write(MaplePacketCreator.serverNotice(5, "You may not go back in this room."));
            return false;
	}
    } else {
        if (party.get(0).getMapId() == 920010500) { 
            pi.warp(920010500, 0); 
            return true;
        } else {
            pi.getPlayer().getClient().getSession().write(MaplePacketCreator.serverNotice(5, "You may not go in this room if your leader is not in it."));
            return false;
        } 
    }
}