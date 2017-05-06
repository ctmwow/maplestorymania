importPackage(Packages.org.ascnet.leaftown.server.maps);
importPackage(Packages.org.ascnet.leaftown.net.channel);
importPackage(Packages.org.ascnet.leaftown.odinms.tools);

function enter(pi) {
    var eim = pi.getPlayer().getEventInstance();
    var party = pi.getPlayer().getEventInstance().getPlayers();
    var realParty = pi.getParty();
    var playerStatus = pi.isPartyLeader();
    var mf = eim.getMapFactory();
    var map = mf.getMap(920010100);
    if (playerStatus) {
	for (var i = 0; i < party.size(); i++) {
            party.get(i).changeMap(map, map.getPortal(0));
	}
	return true;	
    } else {
	pi.getPlayer().getClient().getSession().write(MaplePacketCreator.serverNotice(5, "Only the party leader has the desision whether to leave this room or not."));
	return false;
    }
}