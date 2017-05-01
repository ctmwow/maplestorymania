importPackage(Packages.org.ascnet.leaftown.net.world);
importPackage(Packages.org.ascnet.leaftown.client);
importPackage(Packages.org.ascnet.leaftown.server.life);
importPackage(Packages.org.ascnet.leaftown.server.maps);
importPackage(java.lang);

var exitMap = 0;
var waitingMap = 1;
var reviveMap = 2;
var fieldMap = 3;
var winnerMap = 4;
var loserMap = 5;

function init() { }

function monsterValue(eim, mobId) 
{
    return 1;
}

function start(eim) 
{
    eim.setProperty("started", "true");
}

function setup(mapid) 
{
    var map = parseInt(mapid);
    var eim = em.newInstance("CarnivalPQ" + mapid);
    eim.setInstanceMap(980000000); // <exit>
    eim.setInstanceMap(map);
    eim.setInstanceMap(map+2);
    eim.setInstanceMap(map+1).resetFully();
    eim.setInstanceMap(map+3);
    eim.setInstanceMap(map+4);
    eim.setProperty("forfeit", "false");
    eim.setProperty("blue", "-1");
    eim.setProperty("red", "-1");
	eim.setProperty("started", "false");
    var portal = eim.getMapInstance(reviveMap).getPortal("pt00");
	var scriptStart = map % 1000; // last three digits of the map
	var scriptEnd = scriptStart / 100; // the first digit of the three last digits of the map
    portal.setScriptName("MCrevive" + scriptEnd); // portals one through six calculated and used
    return eim;
}

function playerEntry(eim, player) 
{
    player.changeMap(eim.getMapInstance(waitingMap), eim.getMapInstance(waitingMap).getPortal(0));
}

function leftParty(eim, player) 
{
    disbandParty(eim);
}

function disbandParty(eim) 
{
	disposeAll(eim);
}

function playerExit(eim, player) 
{
	eim.unregisterPlayer(player);
    player.getMap().removePlayer(player);
    player.setMap(eim.getMapInstance(exitMap));
}

function monsterKilled(eim, chr, cp) 
{
	chr.gainCP(cp);
}

function warpOut(eim) 
{
    if (!eim.getProperty("started").equals("true")) 
    {
		if (eim.getProperty("blue").equals("-1"))
            disposeAll(eim);
    } 
    else 
    {
		var blueParty = getParty(eim, "blue");
		var redParty = getParty(eim, "red");
		
    	if (blueParty.isWinner()) 
    	{
    	    blueParty.warp(eim.getMapInstance(winnerMap), 0);
    	    redParty.warp(eim.getMapInstance(loserMap), 0);
    	} 
    	else 
    	{
    	    redParty.warp(eim.getMapInstance(winnerMap), 0);
    	    blueParty.warp(eim.getMapInstance(loserMap), 0);
    	}
		eim.disposeIfPlayerBelow(100, 0);
    }
}

function playerRevive(eim, player) 
{
	if (player.getCP() >= 10) 
		player.gainCP(-10); // otherwise we go -10 and when we respawn we can crash.

	player.addHP(50);
	player.changeMap(eim.getMapInstance(reviveMap), eim.getMapInstance(reviveMap).getPortal(0));
	
	return false;
}

function playerDisconnected(eim, player) 
{
    player.setMap(eim.getMapInstance(exitMap));
    
    if(player.getMonsterCarnival() != null)
    	player.getMonsterCarnival().playerDisconnected(player.getId());
	
	eim.unregisterPlayer(player);
}

function cancelSchedule() {}
function clearPQ(eim) {}
function allMonstersDead(eim) {}
function changedMap(eim, chr, mapid) {}
function playerDead(eim, player) {} // we handle this in playerRevive now