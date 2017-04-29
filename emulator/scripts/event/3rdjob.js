/*3rd Job Event
  Author: Shedder
*/

importPackage(Packages.client);
importPackage(Packages.server.life);
importPackage(Packages.tools);

var minPlayers = 1;
var instanceId;

function init() {
	em.setProperty("state", "0");
}


function monsterValue(eim, mobId) {
    return 1;
}

function setup(eim) {
    em.setProperty("state", "1");
	instanceId = em.getChannelServer().getInstanceId();
	var instanceName = "3rdjob_" + instanceId;
	var eim = em.newInstance(instanceName);
	return eim;
	
}

function setClassVars(player) {
    var returnMapId;
    var monsterId;
    var mapId;    
    if (player.getJob().equals(MapleJob.FP_WIZARD) ||
        player.getJob().equals(MapleJob.IL_WIZARD) ||
        player.getJob().equals(MapleJob.CLERIC)) {
        mapId = 108010201;
        returnMapId = 100040106;
        monsterId = 9001001;
    } else if (player.getJob().equals(MapleJob.FIGHTER) ||
        player.getJob().equals(MapleJob.PAGE) ||
        player.getJob().equals(MapleJob.SPEARMAN)) {
        mapId = 108010301;
        returnMapId = 105070001;
        monsterId = 9001000;
    } else if (player.getJob().equals(MapleJob.ASSASSIN) ||
        player.getJob().equals(MapleJob.BANDIT)) {
        mapId = 108010401;
        returnMapId = 107000402;
        monsterId = 9001003;
    } else if (player.getJob().equals(MapleJob.HUNTER) ||
        player.getJob().equals(MapleJob.CROSSBOWMAN)) {
        mapId = 108010101;
        returnMapId = 105040305;
        monsterId = 9001002;
    } else if (player.getJob().equals(MapleJob.BRAWLER) ||
        player.getJob().equals(MapleJob.GUNSLINGER)) {
        mapId = 108010501;
        returnMapId = 105040305;
        monsterId = 9001004;
    }
    return new Array(mapId, returnMapId, monsterId);
}

function playerEntry(eim, player) {
    var info = setClassVars(player);
    var mapId = info[0];
    var returnMapId = info[1];
    var monsterId = info[2];
	var startMap = mapId - 1;
    var map = eim.getMapFactory().getMap(startMap);
    map.toggleDrops();
    player.changeMap(map, map.getPortal(0));
	var eventTime = 20 * 60000;
    em.schedule("timeOut", eim, eventTime); // invokes "timeOut" in how ever many seconds.
	eim.startEventTimer(eventTime);
    var mob = MapleLifeFactory.getMonster(monsterId);
    eim.registerMonster(mob);
    //map.spawnMonsterOnGroudBelow(mob, new java.awt.Point(200, 20));
}

function playerDead(eim, player) {
    //playerExit(eim, player);
	//eim.dispose();
}

function playerRevive(eim, player) { 
    playerExit(eim, player);
    eim.dispose();	
}

function playerDisconnected(eim, player) {
    removePlayer(eim, player);
    playerExit(eim, player);
	eim.dispose();
}

function leftParty(eim, player) {
}

function disbandParty(eim) {
}

function playerExit(eim, player) {
	var info = setClassVars(player);
	var mapId = info[0];
    var returnMapId = info[1];
    var monsterId = info[2];
	var map = eim.getMapFactory().getMap(returnMapId);
	eim.unregisterPlayer(player);
	player.changeMap(map, map.getPortal(0));
        if (eim.getPlayerCount() == 0) {
	 em.setProperty("state", "0");
     }
}

function removePlayer(eim, player) {
	var info = setClassVars(player);
	var mapId = info[0];
    var returnMapId = info[1];
    var monsterId = info[2];
	var map = eim.getMapFactory().getMap(returnMapId);
	eim.unregisterPlayer(player);
	player.changeMap(map, map.getPortal(0));
    if (eim.getPlayerCount() == 0) {
		em.setProperty("state", "0");
	}
}

function allMonstersDead(eim) {
}


function cancelSchedule() {
}

function warpOut(eim) {
    var iter = eim.getPlayers().iterator();
    while (iter.hasNext()) {
        var player = iter.next();
        var info = setClassVars(player);
        var mapId = info[0];
        var returnMapId = info[1];
        var monsterId = info[2];
        var map = em.getChannelServer().getMapFactory().getMap(returnMapId);
        player.changeMap(map, map.getPortal(0));
        eim.unregisterPlayer(player);
    }
    eim.dispose();
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