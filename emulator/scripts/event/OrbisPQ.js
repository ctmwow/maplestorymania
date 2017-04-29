importPackage(java.lang);
importPackage(Packages.world);
importPackage(Packages.client);
importPackage(Packages.server.maps);
importPackage(Packages.server.life);
importPackage(Packages.scripting.npc);

var exitMap;
var instanceId;
var minPlayers = 1;
var stg2_combo0 = Array("3", "2", "1");
var stg2_combo1 = Array("0", "0", "1"); //unique combos only
var stg2_combo2 = Array("0", "1", "1");
var stg6_combo = Array("00", "01", "02", "03", "04", "05", "06", "07", "08", "09", "10", "11", "12", "13", "14", "15", "16");
var cx = Array(200, -300, -300, -300, 200, 200, 200, -300, -300, 200, 200, -300, -300, 200); //even = 200 odd = -300
var cy = Array(-2321, -2114, -2910, -2510, -1526, -2716, -717, -1310, -3357, -1912, -1122, -1736, -915, -3116);

function init() {
	instanceId = 1;
}

function monsterValue(eim, mobId) {
	return 1;
}

function setup() {
	instanceId = em.getChannelServer().getInstanceId();
	exitMap = em.getChannelServer().getMapFactory().getMap(920011200); //Teh exit map :) <---------t
	var instanceName = "OrbisPQ" + instanceId;

	var eim = em.newInstance(instanceName);
	
	var mf = eim.getMapFactory();
	
	em.getChannelServer().addInstanceId();
	
	var map = mf.getMap(920010000);//wutt
	var centerMap = eim.getMapInstance(920010100);
	centerMap.getPortal(13).setScriptName("orbisPQSealedRoom");
	centerMap.getPortal(4).setScriptName("orbisPQWalkway");
	centerMap.getPortal(12).setScriptName("orbisPQStorage");
	centerMap.getPortal(5).setScriptName("orbisPQLobby");
	centerMap.getPortal(14).setScriptName("orbisPQOnTheWayUp");
	centerMap.getPortal(15).setScriptName("orbisPQLounge");
	centerMap.getPortal(16).setScriptName("orbisPQRoomOfDarkness");
	var walkwayMap = eim.getMapInstance(920010200);
	var storageMap = eim.getMapInstance(920010300);
	var lobbyMap = eim.getMapInstance(920010400);
	var sealedRoomMap = eim.getMapInstance(920010500);
	var loungeMap = eim.getMapInstance(920010600);
	var onTheWayUpMap = eim.getMapInstance(920010700);
	var bossMap = eim.getMapInstance(920010800);
	var jailMap = eim.getMapInstance(920010900);
	var roomOfDarknessMap = eim.getMapInstance(920011000);
	var bonusMap = eim.getMapInstance(920011100);
	var endMap = eim.getMapInstance(920011300);
	walkwayMap.getPortal(13).setScriptName("orbisPQWalkwayExit");
	storageMap.getPortal(1).setScriptName("orbisPQStorageExit");
	lobbyMap.getPortal(8).setScriptName("orbisPQLobbyExit");
	sealedRoomMap.getPortal(3).setScriptName("orbisPQSRExit");
	loungeMap.getPortal(17).setScriptName("orbisPQLoungeExit");
	onTheWayUpMap.getPortal(23).setScriptName("orbisPQOnTheWayUpExit");
	bossMap.getPortal(1).setScriptName("orbisPQGardenExit");
	roomOfDarknessMap.getPortal(1).setScriptName("orbisPQRoomOfDarknessExit");
	//-->Fuck we are done with portals -.-
	eim.setProperty("killedCellions", "0");
	eim.setProperty("papaSpawned", "no");
	em.schedule("timeOut", 60 * 60000);
	em.schedule("broadcastClock", 1500);
	eim.setProperty("entryTimestamp",System.currentTimeMillis() + (60 * 60000));
	em.getProperties().clear();
        em.setProperty("state", "1");
	em.setProperty("leader", "true");
	em.getChannelServer().addInstanceId();
	em.setProperty("stage", "0"); //center stage
	em.setProperty("pre", "0"); //first stage
	em.setProperty("finished", "0"); //first stage
	em.setProperty("stage2", "0"); //num.spawned in storage, 15 = done
	em.setProperty("stage3", "0"); //lobby
	em.setProperty("stage4", "0"); //sealed
	em.setProperty("killedCellions", "0");
	var rand_combo = java.lang.Math.floor(java.lang.Math.random() * stg2_combo0.length);
	var rand_num = java.lang.Math.random();
	var combo0 = rand_num < 0.33 ? true : false;
	var combo1 = rand_num < 0.66 ? true : false;
	em.setProperty("stage4_0", combo0 ? stg2_combo0[rand_combo] : (combo1 ? stg2_combo1[rand_combo] : stg2_combo2[rand_combo]));
	em.setProperty("stage4_1", combo0 ? stg2_combo1[rand_combo] : (combo1 ? stg2_combo2[rand_combo] : stg2_combo0[rand_combo]));
	em.setProperty("stage4_2", combo0 ? stg2_combo2[rand_combo] : (combo1 ? stg2_combo0[rand_combo] : stg2_combo1[rand_combo]));
	em.setProperty("stage6", "0"); //on way up ... hard

	for (var b = 0; b < stg6_combo.length; b++) { //stage6_001
		for (var y = 0; y < 4; y++) { //stage number
			em.setProperty("stage6_" + stg6_combo[b] + "" + (y+1) + "", "0");
		}
	}
	for (var b = 0; b < stg6_combo.length; b++) { //stage6_001	
		var found = false;
		while (!found) {
			for (var x = 0; x < 4; x++) {
				if (!found) {
					var founded = false;
					for (var z = 0; z < 4; z++) { //check if any other stages have this value set already.
						if (em.getProperty("stage6_" + stg6_combo[b] + "" + (z+1) + "") == null) {
							em.setProperty("stage6_" + stg6_combo[b] + "" + (z+1) + "", "0");
						} else if (em.getProperty("stage6_" + stg6_combo[b] + "" + (z+1) + "").equals("1")) {
							founded = true;
							break;
						}
					}
					if (!founded && java.lang.Math.random() < 0.25) {
						em.setProperty("stage6_" + stg6_combo[b] + "" + (x+1) + "", "1");
						found = true;
						break;
					}
				}
			}
		}
	}
	//STILL not done yet! levers = 2 of them
	for (var i = 0; i < 3; i++) {
		em.setProperty("stage62_" + i + "", "0");
	}
	var found_1 = false;
	while(!found_1) {
		for (var i = 0; i < 3; i++) {
			if (em.getProperty("stage62_" + i + "") == null) {
				em.setProperty("stage62_" + i + "", "0");
			} else if (!found_1 && java.lang.Math.random() < 0.2) {
				em.setProperty("stage62_" + i + "", "1");
				found_1 = true;
			}
		}
	}
	var found_2 = false;
	while(!found_2) {
		for (var i = 0; i < 3; i++) {
			if (em.getProperty("stage62_" + i + "") == null) {
				em.setProperty("stage62_" + i + "", "0");
			} else if (!em.getProperty("stage62_" + i + "").equals("1") && !found_2 && java.lang.Math.random() < 0.2) {
				em.setProperty("stage62_" + i + "", "1");
				found_2 = true;
			}
		}
	}
	em.setProperty("stage7", "0"); //papa spawned
	em.setProperty("done", "0");
	return eim;
}

function playerEntry(eim, player) {4001063
	var map = eim.getMapInstance(920010000);
	player.changeMap(map, map.getPortal(0));
	player.getClient().getSession().write(net.sf.odinms.tools.MaplePacketCreator.getClock((Long.parseLong(eim.getProperty("entryTimestamp")) - System.currentTimeMillis()) / 1000));
	var texttt = "Oi! Meu nome e Eak e eu sou o camareiro da deusa Minerva. Nao se preocupe: voce nao podera me ver agora. Quando a deusa foi presa em uma estatua, eu automaticamente perdi os meus poderes. Se voce conseguir recuperar o poder da Nuvem Magica de Orbis, entao, eu poderei recuperar o meu corpo e minha forma original. Por favor colete #b20#k Pedacos de Nuvem e me entregue. Agora, como uma pequena e quase brilhante luz."
	player.getClient().getSession().write(net.sf.odinms.tools.MaplePacketCreator.getNPCTalk(2013001, /*(byte)*/ 0, texttt, "00 00"));
}

function playerDead(eim, player) {
}

function playerRevive(eim, player) {
	if (eim.isLeader(player)) { //check for party leader
		//boot whole party and end
		var party = eim.getPlayers();
		for (var i = 0; i < party.size(); i++) {
			playerExit(eim, party.get(i));
		}
		eim.dispose();
	}
	else { //boot dead player
		// If only 5 players are left, uncompletable:
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
	if (eim.isLeader(player)) { //check for party leader
		//PWN THE PARTY (KICK OUT)
		var party = eim.getPlayers();
		for (var i = 0; i < party.size(); i++) {
			if (party.get(i).equals(player)) {
				removePlayer(eim, player);
			}			
			else {
				playerExit(eim, party.get(i));
			}
		}
		eim.dispose();
	}
	else { //KICK THE D/CED CUNT
		// If only 5 players are left, uncompletable:
		var party = eim.getPlayers();
		if (party.size() < minPlayers) {
			for (var i = 0; i < party.size(); i++) {
				playerExit(eim,party.get(i));
			}
			eim.dispose();
		}
		else
			playerExit(eim, player);
	}
}

function leftParty(eim, player) {			
	// If only 5 players are left, uncompletable:
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
	//boot whole party and end
	var party = eim.getPlayers();
	for (var i = 0; i < party.size(); i++) {
		playerExit(eim, party.get(i));
	}
	eim.dispose();
}

function playerExit(eim, player) {
	eim.unregisterPlayer(player);
	player.cancelAllBuffs(); //We don't want people going out with wonky blessing >=(
	player.changeMap(exitMap, exitMap.getPortal(0));
}

//Those offline cuntts
function removePlayer(eim, player) {
	eim.unregisterPlayer(player);
	player.getMap().removePlayer(player);
	player.setMap(exitMap);
}

function clearPQ(eim) {
	// W00t! Bonus!!
	var iter = eim.getPlayers().iterator();
        var bonusMap = eim.getMapInstance(920011100);
        while (iter.hasNext()) {
                var player = iter.next();
		player.changeMap(bonusMap, bonusMap.getPortal(0));
		eim.setProperty("entryTimestamp",System.currentTimeMillis() + (1 * 60000));
        player.getClient().getSession().write(net.sf.odinms.tools.MaplePacketCreator.getClock(60));
		}
        eim.schedule("finish", 60000)
}

function finish(eim) {
		var dMap = eim.getMapInstance(920011300);
        var iter = eim.getPlayers().iterator();
        while (iter.hasNext()) {
			var player = iter.next();
			eim.unregisterPlayer(player);
	        player.changeMap(dMap, dMap.getPortal(0));
		}
	eim.dispose();
}

function allMonstersDead(eim) {
        //Open Portal? o.O
}

function cancelSchedule() {
}

function timeOut() {
	var iter = em.getInstances().iterator();
	while (iter.hasNext()) {
		var eim = iter.next();
		if (eim.getPlayerCount() > 0) {
			var pIter = eim.getPlayers().iterator();
			while (pIter.hasNext()) {
				playerExit(eim, pIter.next());
			}
		}
		eim.dispose();
	}
}

function playerClocks(eim, player) {
  if (player.getMap().hasTimer() == false){
	player.getClient().getSession().write(net.sf.odinms.tools.MaplePacketCreator.getClock((Long.parseLong(eim.getProperty("entryTimestamp")) - System.currentTimeMillis()) / 1000));
	//player.getMap().setTimer(true);
	}
}

function playerTimer(eim, player) {
	if (player.getMap().hasTimer() == false) {
		player.getMap().setTimer(true);
	}
}

function changedMap(eim, player, mapid) {
        if (mapid < 920010000 || mapid > 920011100) {
            eim.unregisterPlayer(player);

            if (eim.disposeIfPlayerBelow(0, 0)) {
                    em.setProperty("state", "0");
                    em.setProperty("leader", "true");
            }
        } else if (mapid == 920011100 && em.getProperty("done").equals("0")) { //bonus
            em.setProperty("done", "1");
            eim.restartEventTimer(60000); //minute
        }
    }

function broadcastClock(eim, player) {
	//var party = eim.getPlayers();
	var iter = em.getInstances().iterator();
	while (iter.hasNext()) {
		var eim = iter.next();
		if (eim.getPlayerCount() > 0) {
			var pIter = eim.getPlayers().iterator();
			while (pIter.hasNext()) {
				playerClocks(eim, pIter.next());
			}
		}
		//em.schedule("broadcastClock", 1600);
	}
	// for (var kkl = 0; kkl < party.size(); kkl++) {
		// party.get(kkl).getMap().setTimer(true);
	// }
	var iterr = em.getInstances().iterator();
	while (iterr.hasNext()) {
		var eim = iterr.next();
		if (eim.getPlayerCount() > 0) {
			var pIterr = eim.getPlayers().iterator();
			while (pIterr.hasNext()) {
				//playerClocks(eim, pIter.next());
				playerTimer(eim, pIterr.next());
			}
		}
		//em.schedule("broadcastClock", 1600);
	}
	em.schedule("broadcastClock", 1600);
}

function dispose() {

}