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
	AirPlane between KC and CBD
-- By ---------------------------------------------------------------------------------------------
	Information
-- Version Info -----------------------------------------------------------------------------------
	1.0 - First Version by Information
---------------------------------------------------------------------------------------------------
**/

importPackage(Packages.org.ascnet.leaftown.tools);

//Time Setting is in millisecond
var closeTime = 24 * 1000; //The time to close the gate
var beginTime = 30 * 1000; //The time to begin the ride
var rideTime = 15 * 1000; //The time that require move to destination
var KC_bfd;
var Plane_to_CBD;
var CBD_docked;
var CBD_bfd;
var Plane_to_KC;
var KC_docked;

function init() {
    KC_bfd = em.getChannelServer().getMapFactory().getMap(540010100);
    CBD_bfd = em.getChannelServer().getMapFactory().getMap(540010001);
    Plane_to_CBD = em.getChannelServer().getMapFactory().getMap(540010101);
    Plane_to_KC = em.getChannelServer().getMapFactory().getMap(540010002);
    CBD_docked = em.getChannelServer().getMapFactory().getMap(540010000);
    KC_docked = em.getChannelServer().getMapFactory().getMap(103000000);
    scheduleNew();
}

function scheduleNew() {
    em.setProperty("docked", "true");
    em.setProperty("entry", "true");
    em.schedule("stopEntry", closeTime);
    em.schedule("takeoff", beginTime);
}

function stopEntry() {
    em.setProperty("entry","false");
}

function takeoff() {
    em.setProperty("docked","false");
    KC_bfd.warpEveryone(Plane_to_CBD.getId());
    CBD_bfd.warpEveryone(Plane_to_KC.getId());
    em.schedule("arrived", rideTime); //The time that require move to destination
}

function arrived() {
    Plane_to_CBD.warpEveryone(CBD_docked.getId());
    Plane_to_KC.warpEveryone(KC_docked.getId());
        
    scheduleNew();
}

function cancelSchedule() {
}