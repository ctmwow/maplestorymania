/* 
 * This file is part of the OdinMS Maple Story Server
    Copyright (C) 2008 Patrick Huy <patrick.huy@frz.cc> 
                       Matthias Butz <matze@odinms.de>
                       Jan Christian Meyer <vimes@odinms.de>

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License version 3
    as published by the Free Software Foundation. You may not use, modify
    or distribute this program under any other version of the
    GNU Affero General Public License.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

/**
-- Odin JavaScript --------------------------------------------------------------------------------
	Boats Between Ellinia and Orbis
-- By ---------------------------------------------------------------------------------------------
	Information
-- Version Info -----------------------------------------------------------------------------------
	1.6 - Fix for infinity looping [Information]
	1.5 - Ship/boat is now showed 
	    - Removed temp message[Information]
	    - Credit to Snow/superraz777 for old source
	    - Credit to Titan/Kool for the ship/boat packet
	1.4 - Fix typo [Information]
	1.3 - Removing some function since is not needed [Information]
	    - Remove register player menthod [Information]
	    - Remove map instance and use reset reactor function [Information]
	1.2 - Should be 2 ship not 1 [Information]
	1.1 - Add timer variable for easy edit [Information]
	1.0 - First Version by Information
---------------------------------------------------------------------------------------------------
**/

importPackage(Packages.org.ascnet.leaftown.client);
importPackage(Packages.org.ascnet.leaftown.tools);
importPackage(Packages.org.ascnet.leaftown.server.life);

//Time Setting is in millisecond
var closeTime = 50 * 1000; //The time to close the gate
var beginTime = 60 * 1000; //The time to begin the ride
var rideTime = 120 * 1000; //The time that require move to destination
var invasionTime = 30 * 1000; //The time to balrog ship approach
var invasionDelay = 5 * 1000; //The time that spawn balrog
var Orbis_btf;
var Boat_to_Orbis;
var Orbis_Boat_Cabin;
var Orbis_docked;
var Ellinia_btf;
var Ellinia_Boat_Cabin;
var Ellinia_docked;

function init() {
    Orbis_btf = em.getChannelServer().getMapFactory().getMap(200000112);
    Ellinia_btf = em.getChannelServer().getMapFactory().getMap(101000301);
    Boat_to_Orbis = em.getChannelServer().getMapFactory().getMap(200090010);
    Boat_to_Ellinia = em.getChannelServer().getMapFactory().getMap(200090000);
    Orbis_Boat_Cabin = em.getChannelServer().getMapFactory().getMap(200090011);
    Ellinia_Boat_Cabin = em.getChannelServer().getMapFactory().getMap(200090001);
    Ellinia_docked = em.getChannelServer().getMapFactory().getMap(101000300);
    Orbis_Station = em.getChannelServer().getMapFactory().getMap(200000100);
    Orbis_docked = em.getChannelServer().getMapFactory().getMap(200000111);
    
    Ellinia_docked.setDocked(true);
    Orbis_docked.setDocked(true);
    
    scheduleNew();
}

function scheduleNew() {
    em.setProperty("docked", "true");
    
    em.setProperty("entry", "true");
    em.setProperty("haveBalrog", "false");
    em.schedule("stopentry", closeTime);
    em.schedule("takeoff", beginTime);
}

function stopentry() {
    em.setProperty("entry","false");
    Orbis_Boat_Cabin.resetReactors();   //boxes
    Ellinia_Boat_Cabin.resetReactors();
}

function takeoff() {
    Orbis_btf.warpEveryone(Boat_to_Ellinia.getId());
    Ellinia_btf.warpEveryone(Boat_to_Orbis.getId());
    Ellinia_docked.broadcastShip(false);
    Orbis_docked.broadcastShip(false);
    
    em.setProperty("docked","false");
    
    em.schedule("approach", invasionTime);
    em.schedule("arrived", rideTime);
}

function arrived() {
    Boat_to_Orbis.warpEveryone(Orbis_Station.getId());
    Orbis_Boat_Cabin.warpEveryone(Orbis_Station.getId());
    Boat_to_Ellinia.warpEveryone(Ellinia_docked.getId());
    Ellinia_Boat_Cabin.warpEveryone(Ellinia_docked.getId());
    Orbis_docked.broadcastShip(true);
    Ellinia_docked.broadcastShip(true);
    Boat_to_Orbis.broadcastEnemyShip(false);
    Boat_to_Ellinia.broadcastEnemyShip(false);
    Boat_to_Orbis.killAllMonsters(false);
    Boat_to_Ellinia.killAllMonsters(false);
    em.setProperty("haveBalrog", "false");
    scheduleNew();
}

function approach() {
    if (Math.floor(Math.random() * 10) < 10) {
        em.setProperty("haveBalrog","true");
        Boat_to_Orbis.broadcastEnemyShip(true);
        Boat_to_Ellinia.broadcastEnemyShip(true);
        Boat_to_Orbis.broadcastMessage(MaplePacketCreator.musicChange("Bgm04/ArabPirate"));
        Boat_to_Ellinia.broadcastMessage(MaplePacketCreator.musicChange("Bgm04/ArabPirate"));
        
        em.schedule("invasion", invasionDelay);
    }
}

function invasion() {
    var map1 = Boat_to_Ellinia;
    var pos1 = new java.awt.Point(-538, 143);
    map1.spawnMonsterOnGroundBelow(MapleLifeFactory.getMonster(8150000), pos1);
    map1.spawnMonsterOnGroundBelow(MapleLifeFactory.getMonster(8150000), pos1);

    var map2 = Boat_to_Orbis;
    var pos2 = new java.awt.Point(339, 148);
    map2.spawnMonsterOnGroundBelow(MapleLifeFactory.getMonster(8150000), pos2);
    map2.spawnMonsterOnGroundBelow(MapleLifeFactory.getMonster(8150000), pos2);
}

function cancelSchedule() {
}

function allMonstersDead(eim) {
    //do nothing; LMPQ has nothing to do with monster killing
}