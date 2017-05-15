var minPlayers = 1;

function init() {
	em.setProperty("state", "0");
	em.setProperty("leader", "true");
}

function setup(eim, leaderid) {
	em.setProperty("state", "1");
	em.setProperty("leader", "true");
	mapaSaida = em.getChannelServer().getMapFactory().getMap(674030100); 
    var eim = em.newInstance("MV" + leaderid);
	eim.setInstanceMap(674030000).resetFully();
	eim.setInstanceMap(674030200).resetFully();
	eim.setInstanceMap(674030300).resetFully();
    var eventTime = 1800000;
    eim.schedule("timeOut", eventTime); // invokes "timeOut" in how ever many seconds.
    eim.startEventTimer(eventTime);
    return eim;
}

function playerEntry(eim, player) {
    var map = eim.getMapInstance(0);
    player.changeMap(map, map.getPortal(0));
}

function playerRevive(eim, player) {
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
    if (mapid != 674030000 && mapid != 674030200 && mapid != 674030300) {
		eim.unregisterPlayer(player);

		if (eim.getPlayerCount() == 0) {
			em.setProperty("state", "0");
			em.setProperty("leader", "true");
		}
    }
}

function playerDisconnected(eim, player) {
    playerExit(eim, player);
}

function monsterValue(eim, mobId) {
    if (mobId == 9400589) { //MV
		eim.broadcastPlayerMsg(6, "MV foi eliminado!");
    	eim.restartEventTimer(60000); //1 mins
		eim.schedule("warpWinnersOut", 55000);
    }
    return 1;
}

function warpWinnersOut(eim) {
	eim.restartEventTimer(300000); //5 mins
	var party = eim.getPlayers();
	var map = eim.getMapInstance(2);
	for (var i = 0; i < party.size(); i++) {
		party.get(i).changeMap(map, map.getPortal(0));
	}
}

function playerExit(eim, player) {
	eim.unregisterPlayer(player);
	player.changeMap(mapaSaida, mapaSaida.getPortal(0));
	if (eim.getPlayerCount() == 0) {
		em.setProperty("state", "0");
		em.setProperty("leader", "true");
	}
}

function end(eim) {
    var iter = eim.getPlayers().iterator();
	while (iter.hasNext()) {
		var player = iter.next();
		eim.unregisterPlayer(player);
		player.changeMap(mapaSaida, mapaSaida.getPortal(0));
	}
	eim.dispose();
	em.setProperty("state", "0");
	em.setProperty("leader", "true");
}

function clearPQ(eim) {
    end(eim);
}

function allMonstersDead(eim) {
}

function leftParty (eim, player) {
    var party = eim.getPlayers();
    if (party.size() < minPlayers) {
        for (var i = 0; i < party.size(); i++)
            playerExit(eim,party.get(i));
        eim.dispose();
    } else
        playerExit(eim, player);
}
function disbandParty (eim) {
	end(eim);
}
function playerDead(eim, player) {
	playerExit(eim, player);
}
function cancelSchedule() {}