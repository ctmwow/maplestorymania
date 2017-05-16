/*
	This file is part of the OdinMS Maple Story Server
    Copyright (C) 2008 Patrick Huy <patrick.huy@frz.cc> 
					   Matthias Butz <matze@odinms.de>
					   Jan Christian Meyer <vimes@odinms.de>

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as
    published by the Free Software Foundation version 3 as published by
    the Free Software Foundation. You may not use, modify or distribute
    this program under any other version of the GNU Affero General Public
    License.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

/**
-- Odin JavaScript --------------------------------------------------------------------------------
	Trains between Orbis and Ludibrium
-- By ---------------------------------------------------------------------------------------------
	Information
-- Version Info -----------------------------------------------------------------------------------
        1.6 - Modified for ShootSource (Moogra)
	1.5 - Fix for infinity looping [Information]
	1.4 - Ship/boat is now showed
	    - Removed temp message[Information]
	    - Credit to Snow/superraz777 for old source
	    - Credit to Titan/Kool for the ship/boat packet
	1.3 - Removing some function since is not needed [Information]
	    - Remove register player menthod [Information]
	1.2 - It should be 2 ships not 1 [Information]
	1.1 - Add timer variable for easy edit [Information]
	1.0 - First Version by Information
---------------------------------------------------------------------------------------------------
**/

importPackage(Packages.org.ascnet.leaftown.tools);

var Orbis_btf;
var Train_to_Orbis;
var Orbis_docked;
var Ludibrium_btf;
var Train_to_Ludibrium;
var Ludibrium_docked;
var Orbis_Station;
var Ludibrium_Station;

//Time Setting is in millisecond
var closeTime = 50 * 1000; //The time to close the gate
var beginTime = 60 * 1000; //The time to begin the ride
var rideTime = 60 * 1000; //The time that require move to destination

function init() {
    Orbis_btf = em.getChannelServer().getMapFactory().getMap(200000122);
    Ludibrium_btf = em.getChannelServer().getMapFactory().getMap(220000111);
    Train_to_Orbis = em.getChannelServer().getMapFactory().getMap(200090110);
    Train_to_Ludibrium = em.getChannelServer().getMapFactory().getMap(200090100);
    Orbis_docked = em.getChannelServer().getMapFactory().getMap(200000121);
    Ludibrium_docked = em.getChannelServer().getMapFactory().getMap(220000110);
    Orbis_Station = em.getChannelServer().getMapFactory().getMap(200000100);
    Ludibrium_Station = em.getChannelServer().getMapFactory().getMap(220000100);
    
    scheduleNew();
}

function scheduleNew() {
    em.setProperty("docked", "true");
    Orbis_docked.setDocked(true);
    Ludibrium_docked.setDocked(true);
    
    em.setProperty("entry", "true");
    em.schedule("stopEntry", closeTime); //The time to close the gate
    em.schedule("takeoff", beginTime); //The time to begin the ride
}

function stopEntry() {
    em.setProperty("entry","false");
}

function takeoff() {
    Orbis_btf.warpEveryone(Train_to_Ludibrium.getId());
    Ludibrium_btf.warpEveryone(Train_to_Orbis.getId());
    Orbis_docked.broadcastShip(false);
    Ludibrium_docked.broadcastShip(false);
    
    em.setProperty("docked","false");
    Orbis_docked.setDocked(false);
    Ludibrium_docked.setDocked(false);
    
    em.schedule("arrived", rideTime); //The time that require move to destination
}

function arrived() {
    Train_to_Orbis.warpEveryone(Orbis_Station.getId());
    Train_to_Ludibrium.warpEveryone(Ludibrium_Station.getId());
    Orbis_docked.broadcastShip(true);
    Ludibrium_docked.broadcastShip(true);
    scheduleNew();
}

function cancelSchedule() {
}