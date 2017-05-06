
importPackage(Packages.org.ascnet.leaftown.world);
importPackage(Packages.org.ascnet.leaftown.client);
importPackage(Packages.org.ascnet.leaftown.server.maps);
importPackage(Packages.org.ascnet.leaftown.tools);
importPackage(java.lang);

var exitMap;
var instanceId;
var minPlayers = 1;

function init() {
	instanceId = 1;
        em.setProperty("state", "0");
}

function monsterValue(eim, mobId) {
	return 1;
}

function setup() {
    em.setProperty("state", "1")
	instanceId = em.getChannelServer().getInstanceId();
	exitMap = em.getChannelServer().getMapFactory().getMap(922010000); //Teh exit map :) <---------t
	var instanceName = "LudiPQ" + instanceId;
	var eim = em.newInstance(instanceName);
	var mf = eim.getMapFactory();
	var eventTime = 60 * (1000 * 60); // 60 mins.
	em.getChannelServer().addInstanceId();
	var map = mf.getMap(922010100);//wutt
	var map = eim.getMapInstance(922010100);
	mf.getMap(922010200).resetReactors();
	mf.getMap(922010201).resetReactors();
	mf.getMap(922010300).resetReactors();
	mf.getMap(922010700).resetReactors();
	map.getPortal(2).setScriptName("lpq2");
	var map1 = eim.getMapInstance(922010200);
	map1.getPortal(2).setScriptName("lpq3");
	var map2 = eim.getMapInstance(922010300);
	map2.getPortal(2).setScriptName("lpq4");
	var map3 = eim.getMapInstance(922010400);
	map3.getPortal(7).setScriptName("lpq5");
	var map4 = eim.getMapInstance(922010500);
	map4.getPortal(8).setScriptName("lpq6");
	var map5 = eim.getMapInstance(922010700);
	map5.getPortal(2).setScriptName("lpq8");
	var map6 = eim.getMapInstance(922010800);
	map6.getPortal(2).setScriptName("lpqboss");
	var mapBoss = eim.getMapInstance(922010900);
	mapBoss.getPortal(0).setScriptName("blank");
    eim.schedule("timeOut", eventTime); // invokes "timeOut" in how ever many seconds.
    eim.startEventTimer(eventTime); // Sends a clock packet and tags a timer to the players.

	return eim;
}

function playerEntry(eim, player) {
	var map = eim.getMapInstance(922010100);
	player.changeMap(map, map.getPortal(0));
}

function playerDead(eim, player) {
}

function playerRevive(eim, player) {
	if (eim.isPartyLeader(player)) { 
		var party = eim.getPlayers();
		for (var i = 0; i < party.size(); i++) {
			playerExit(eim, party.get(i));
		}
		eim.dispose();
	}
	else { 
		var party = eim.getPlayers();
		if (party.size() <= minPlayers) {
			for (var i = 0; i < party.size(); i++) {
				playerExit(eim,party.get(i));
			}
			eim.dispose();
		}
		else
			playerExit(eim, player);
	}
}

function playerDisconnected(eim, player) {
	if (eim.isPartyLeader(player)) { 
		var party = eim.getPlayers();
		for (var i = 0; i < party.size(); i++) {
			if (party.get(i).equals(player)) {
				removePlayer(eim, player);
			} else {
				playerExit(eim, party.get(i));
			}
		}
		eim.dispose();
	} else { 
		var party = eim.getPlayers();
		if (party.size() < minPlayers) {
			for (var i = 0; i < party.size(); i++) {
				playerExit(eim,party.get(i));
			}
			eim.dispose();
		} else
			playerExit(eim, player);
	}
}

function leftParty(eim, player) {			
	var party = eim.getPlayers();
	if (party.size() <= minPlayers) {
		for (var i = 0; i < party.size(); i++) {
			playerExit(eim,party.get(i));
		}
		eim.dispose();
	}
	else
	        playerExit(eim, player);
         
}

function disbandParty(eim) {
	var party = eim.getPlayers();
	for (var i = 0; i < party.size(); i++) {
		playerExit(eim, party.get(i));
	}
	eim.dispose();
}

function playerExit(eim, player) {
	eim.unregisterPlayer(player);
	player.changeMap(exitMap, exitMap.getPortal(0));
        if (eim.getPlayerCount() == 0) {
	 em.setProperty("state", "0");
      }
}

function removePlayer(eim, player) {
	eim.unregisterPlayer(player);
	player.getMap().removePlayer(player);
	player.setMap(exitMap);
        if (eim.getPlayerCount() == 0) {
	 em.setProperty("state", "0");
      }
}

function clearPQ(eim) {
	var iter = eim.getPlayers().iterator();
        var bonusMap = eim.getMapInstance(922011000);
        while (iter.hasNext()) {
                var player = iter.next();
		player.changeMap(bonusMap, bonusMap.getPortal(0));
		eim.setProperty("entryTimestamp",System.currentTimeMillis() + (1 * 60000));
        player.getClient().getSession().write(MaplePacketCreator.getClock(60));
		}
        eim.schedule("finish", 60000)
        em.setProperty("state", "0");
}

function finish(eim) {
		var dMap = eim.getMapInstance(922011100);
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
