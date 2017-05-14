var minPlayers = 1;

function init() {
	em.setProperty("state", "0");
	em.setProperty("leader", "true");
}

function monsterValue(eim, mobId) {
    return 1;
}

function setup(eim, leaderid) {
	em.setProperty("state", "1");
	em.setProperty("leader", "true");
	mapaSaida = em.getChannelServer().getMapFactory().getMap(930000800); 
    var eim = em.newInstance("Ellin" + leaderid);
	eim.setInstanceMap(930000000).resetFully();
	eim.setInstanceMap(930000100).resetFully();
	eim.setInstanceMap(930000200).resetFully();
	eim.setInstanceMap(930000300).resetFully();
	eim.setInstanceMap(930000400).resetFully();
	var map = eim.setInstanceMap(930000500);
	map.resetFully();
	map.shuffleReactors();
	eim.setInstanceMap(930000600).resetFully();
	eim.setInstanceMap(930000700).resetFully();
	var eventTime = 1800000;
    eim.schedule("timeOut", eventTime); // invokes "timeOut" in how ever many seconds.
    eim.startEventTimer(eventTime);
    return eim;
}

function playerEntry(eim, player) {
    var map = eim.getMapInstance(0);
    player.changeMap(map, map.getPortal(0));
    //player.tryPartyQuest(1206);
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
    if (mapid < 930000000 || mapid > 930000700) {
		eim.unregisterPlayer(player);

		if (eim.getPlayerCount() == 0) {
			em.setProperty("state", "0");
			em.setProperty("leader", "true");
		}
    }
}

function playerDisconnected(eim, player) {
    return 0;
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

function playerExit(eim, player) {
	eim.unregisterPlayer(player);
	player.changeMap(mapaSaida, mapaSaida.getPortal(0));
		if (eim.getPlayerCount() == 0) {
			em.setProperty("state", "0");
			em.setProperty("leader", "true");
		}
}

function clearPQ(eim) {
    end(eim);
}

function allMonstersDead(eim) {
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

function disbandParty (eim) {
	end(eim);
}

function dispose() {
    em.cancelSchedule();
    em.setProperty("state", "0");
	em.setProperty("leader", "true");
}

function playerDead(eim, player) {}
function cancelSchedule() {}