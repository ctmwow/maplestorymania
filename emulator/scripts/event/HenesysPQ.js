importPackage(Packages.org.ascnet.leaftown.net.world);
importPackage(Packages.org.ascnet.leaftown.client);
importPackage(Packages.org.ascnet.leaftown.server.life);
importPackage(Packages.org.ascnet.leaftown.server.maps);
importPackage(java.lang);


var PQMap3;
var mapaSaida;
var mapaInicial;
var instanceId;
var minPlayers = 1;

function init() 
{
    mapaSaida = em.getChannelServer().getMapFactory().getMap(910010300); 
    mapaInicial = em.getChannelServer().getMapFactory().getMap(910010000); // <main>
    em.setProperty("state", "0");
    instanceId = 1;
}

function monsterValue(eim, mobId) 
{
	return 1;
}

function setup(eim) 
{
    em.setProperty("semente", "0");
    em.setProperty("state", "1");
    
    var eim = em.newInstance("HenesysPQ");
    var mf = eim.getMapFactory();
    var map = mf.getMap(910010000);
    
    map.setSpawns(false);
    
    em.setProperty("cakeNum", "1");
    em.setProperty("shouldDrop", "false");
   
   	var eventTime = 10 * 60000;
    eim.schedule("timeOut", eventTime); // invokes "timeOut" in how ever many seconds.
    eim.startEventTimer(eventTime);
    return eim;	
}

function playerEntry(eim, player) {
    var map = eim.getMapInstance(mapaInicial.getId());
    player.changeMap(map, map.getPortal(0));
}
  
function playerDead(eim, player) {
    if (player.isAlive()) {
        if (eim.isPartyLeader(player)) {
            var party = eim.getPlayers();
            for (var i = 0; i < party.size(); i++)
                playerExit(eim, party.get(i));
            eim.dispose();
        } else {
            var partyz = eim.getPlayers();
            if (partyz.size() < minPlayers) {
                for (var j = 0; j < partyz.size(); j++)
                    playerExit(eim,partyz.get(j));
                eim.dispose();
            } else
                playerExit(eim, player);
        }
    }
}

function playerRevive(eim, player) { 
    if (eim.isPartyLeader(player) || party.size() <= minPlayers) { 
        var party = eim.getPlayers();
        for (var i = 0; i < party.size(); i++)
            playerExit(eim, party.get(i));
        eim.dispose();
    } else
        playerExit(eim, player);
}

function playerDisconnected(eim, player) {
    var party = eim.getPlayers();
    if (eim.isPartyLeader(player) || party.size() < minPlayers) {
        var party = eim.getPlayers();
        for (var i = 0; i < party.size(); i++)
            if (party.get(i).equals(player))
                removePlayer(eim, player);
            else
                playerExit(eim, party.get(i));
        eim.dispose();
    } else
        removePlayer(eim, player);
}

function leftParty(eim, player) {
    var party = eim.getPlayers();
    if (party.size() < minPlayers) {
        for (var i = 0; i < party.size(); i++)
            playerExit(eim,party.get(i));
        eim.dispose();
    } else
        playerExit(eim, player);
}

function disbandParty(eim) {
    var party = eim.getPlayers();
    for (var i = 0; i < party.size(); i++) {
        playerExit(eim, party.get(i));
    }
    eim.dispose();
    em.setProperty("state", "0");
}

function playerExit(eim, player) {
	eim.unregisterPlayer(player);
	player.changeMap(mapaSaida, mapaSaida.getPortal(0));
        if (eim.getPlayerCount() == 0) {
	 em.setProperty("state", "0");
     }
}

function removePlayer(eim, player) {
	eim.unregisterPlayer(player);
	player.getMap().removePlayer(player);
	player.setMap(mapaSaida);
        if (eim.getPlayerCount() == 0) {
	em.setProperty("state", "0");
     }
}

function clearPQ(eim) {
	var iter = eim.getPlayers().iterator();
        var bonusMap = eim.getMapInstance(910010200);
        while (iter.hasNext()) {
		var player = iter.next();
			player.changeMap(bonusMap, bonusMap.getPortal(0));
			eim.setProperty("entryTimestamp",System.currentTimeMillis() + (5 * 60000));
			player.getClient().getSession().write(MaplePacketCreator.getClock(300));
		}
        eim.schedule("finish", 5 * 60000)
        em.setProperty("state", "0");
}

function finish(eim) {
	var dMap = eim.getMapInstance(910010400);
	var iter = eim.getPlayers().iterator();
	while (iter.hasNext()) {
		var player = iter.next();
		eim.unregisterPlayer(player);
		player.changeMap(dMap, dMap.getPortal(0));
	}
	eim.dispose();
	em.setProperty("state", "0");
}

function allMonstersDead(eim) {
}

function cancelSchedule() {
}

function dispose() {
    em.cancelSchedule();
    em.setProperty("state", "0");
}

function timeOut(eim) {
    if (eim != null) {
        if (eim.getPlayerCount() > 0) {
            var pIter = eim.getPlayers().iterator();
            while (pIter.hasNext())
                playerExit(eim, pIter.next());
        }
        eim.dispose();
    }
}

function changedMap(eim, player, mapid) {
}

function schedule(eim, player) {
}
