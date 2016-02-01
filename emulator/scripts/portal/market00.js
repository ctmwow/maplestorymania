importPackage(Packages.org.ascnet.leaftown.server.maps);
importPackage(Packages.org.ascnet.leaftown.net.channel);

function enter(pi) {
    var returnMap = pi.getPlayer().getSavedLocation(SavedLocationType.FREE_MARKET);
    if (returnMap < 0) {
	returnMap = 102000000; // to fix people who entered the fm trough an unconventional way
    }
    var target = pi.getPlayer().getClient().getChannelServer().getMapFactory().getMap(returnMap);
    var targetPortal;
    
    if (returnMap == 230000000) { // aquaroad has a different fm portal - maybe we should store the used portal too?
        targetPortal = target.getPortal("market01");
    } else {
	targetPortal = target.getPortal("market00");
    }
    if (targetPortal == null) {
	targetPortal = target.getPortal(0);
    }
    pi.getPlayer().clearSavedLocation(SavedLocationType.FREE_MARKET);
    pi.getPlayer().changeMap(target, targetPortal);
    return true;
}