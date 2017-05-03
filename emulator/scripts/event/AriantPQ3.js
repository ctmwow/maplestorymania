/*
 * @Author Jvlaple
 * Ariant Coliseum (3)
 * 2014-21-09
 */


/* global Packages, java */

importPackage(java.lang);

importPackage(Packages.org.ascnet.leaftown.world);
importPackage(Packages.org.ascnet.leaftown.client);
importPackage(Packages.org.ascnet.leaftown.server);
importPackage(Packages.org.ascnet.leaftown.server.maps);
importPackage(Packages.org.ascnet.leaftown.tools);

var exitMap;
var instanceId;
var minPlayers = 2;

function init() {
	exitMap = em.getChannelServer().getMapFactory().getMap(980010020); //Teh exit map :) <---------t
	doneMap = em.getChannelServer().getMapFactory().getMap(980010010);
}

function monsterValue(eim, mobId) {
	return 1;
}

function setup() {
	instanceId = em.getChannelServer().getInstanceId();
	var instanceName = "AriantPQ3_" + instanceId;
	var eim = em.newInstance(instanceName);
	var mf = eim.getMapFactory();
	em.getChannelServer().addInstanceId();
	var eventTime = 10 * 60000 + 10000;
	eim.schedule("timeOut", eventTime); // invokes "timeOut" in how ever many seconds.
	eim.schedule("scoreBoard", 10 * 60000); 
	eim.schedule("broadcastClock", 1500);
	eim.setProperty("entryTimestamp",System.currentTimeMillis() + (10 * 60000));
	var tehwat = Math.random() * 3;
	if (tehwat > 1) {
		eim.setProperty("theWay", "darkness");
	} else {
		eim.setProperty("theWay", "light");
	}
	respawn(eim);
	return eim;
}

function playerEntry(eim, player) {
	var map = eim.getMapInstance(980010301);
	player.changeMap(map, map.getPortal(0));
	player.getClient().sendPacket(MaplePacketCreator.getClock((Long.parseLong(eim.getProperty("entryTimestamp")) - System.currentTimeMillis()) / 1000));
}

function respawn(eim) {	
        var map = eim.getMapInstance(980010301);
	map.respawn();
	eim.schedule("respawn", 8000);
}

function playerDead(eim, player) {
}

function playerRevive(eim, player) {
	if (eim.isSquadLeader(player, MapleSquadType.ARIANT3)) { 
		var squad = player.getClient().getChannelServer().getMapleSquad(MapleSquadType.ARIANT3);
		player.getClient().getChannelServer().removeMapleSquad(squad, MapleSquadType.ARIANT3);
		var party = eim.getPlayers();
		for (var i = 0; i < party.size(); i++) {
			if (party.get(i).equals(player)) {
				removePlayer(eim, player);
			}			
			else {
				playerExit(eim, party.get(i));
			}
		}
	}
	else { 
		var party = eim.getPlayers();
		if (party.size() < minPlayers) {
			for (var i = 0; i < party.size(); i++) {
				playerExit(eim,party.get(i));
			}
		}
		else
			playerExit(eim, player);
	}
}

function playerDisconnected(eim, player) {
	if (eim.isSquadLeader(player, MapleSquadType.ARIANT1)) { 
		var party = eim.getPlayers();
		var squad = player.getClient().getChannelServer().getMapleSquad(MapleSquadType.ARIANT3);
		player.getClient().getChannelServer().removeMapleSquad(squad, MapleSquadType.ARIANT3);
		for (var i = 0; i < party.size(); i++) {
			if (party.get(i).equals(player)) {
				removePlayer(eim, player);
			}			
			else {
				playerExit(eim, party.get(i));
			}
		}
	}
	else { 
		var party = eim.getPlayers();
		if (party.size() < minPlayers) {
			for (var i = 0; i < party.size(); i++) {
				playerExit(eim,party.get(i));
			}
		}
		else
			playerExit(eim, player);
	}
}

function dispose() {
    em.cancelSchedule();
}

function leftParty(eim, player) {			
}

function disbandParty(eim) {
}

function playerExit(eim, player) {
	eim.unregisterPlayer(player);
	player.changeMap(exitMap, exitMap.getPortal(0));
}

function playerDone(eim, player) {
	eim.unregisterPlayer(player);
	player.changeMap(doneMap, doneMap.getPortal(0));
	var squad = player.getClient().getChannelServer().getMapleSquad(MapleSquadType.ARIANT3);
	if (eim.getProperty("disbanded") == null) {
	player.getClient().getChannelServer().removeMapleSquad(squad, MapleSquadType.ARIANT3);
	eim.setProperty("disbanded", "done");	
	}
}

function removePlayer(eim, player) {
	eim.unregisterPlayer(player);
	player.getMap().removePlayer(player);
	player.setMap(exitMap);
}

function clearPQ(eim) {
	var party = eim.getPlayers();
	for (var i = 0; i < party.size(); i++) {
		playerExit(eim, party.get(i));
	}
}

function allMonstersDead(eim) {
}

function cancelSchedule() {
}

function timeOut(eim) {
    if (eim != null) {
        if (eim.getPlayerCount() > 0) {
            var pIter = eim.getPlayers().iterator();
            while (pIter.hasNext())
                playerDone(eim, pIter.next());
        }
    }
}

function scoreBoard(eim, player) {
	var iter = em.getInstances().iterator();
	var shouldScheduleThis = true;
	while (iter.hasNext()) {
		var eim = iter.next();
		if (eim.getPlayerCount() > 0) {
			var map = eim.getMapInstance(980010101);
			map.broadcastMessage(MaplePacketCreator.showAriantScoreBoard());
			shouldScheduleThis = false;
		}
	 }
	 if (shouldScheduleThis)
	 em.schedule("scoreBoard", 100000);
 }


function playerClocks(eim, player) {
	if (player.getMap().hasClock() == false){
		player.getClient().sendPacket(MaplePacketCreator.getClock((Long.parseLong(eim.getProperty("entryTimestamp")) - System.currentTimeMillis()) / 1000));
	}
}

function playerTimer(eim, player) {
	if (player.getMap().hasClock() == false) {
		player.getMap().setClock(true);
	}
}

function broadcastClock(eim, player) {
	var iter = em.getInstances().iterator();
	while (iter.hasNext()) {
		var eim = iter.next();
		if (eim.getPlayerCount() > 0) {
			var pIter = eim.getPlayers().iterator();
			while (pIter.hasNext()) {
				playerClocks(eim, pIter.next());
			}
		}
	}
	var iterr = em.getInstances().iterator();
	while (iterr.hasNext()) {
		var eim = iterr.next();
		if (eim.getPlayerCount() > 0) {
			var pIterr = eim.getPlayers().iterator();
			while (pIterr.hasNext()) {
				playerTimer(eim, pIterr.next());
			}
		}
	}
	em.schedule("broadcastClock", 1600);
}
