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

var returnTo = new Array(200000141, 250000100);
var rideTo = new Array(250000100, 200000141);
var birdRide = new Array(200090300, 200090310);
var myRide;
var returnMap;
var exitMap;
var map;
var timeOnRide = 60; //Seconds
var onRide;

function init() {
        
}

function setup() {
	var eim = em.newInstance("Hak_" + + em.getProperty("player"));
	return eim;
}

function playerEntry(eim, player) {
	if (player.getMapId() == returnTo[0]) {
		myRide = 0;
	} else {
		myRide = 1;
	}
	exitMap = eim.getEm().getChannelServer().getMapFactory().getMap(rideTo[myRide]);
        returnMap = eim.getMapFactory().getMap(returnTo[myRide]);
        onRide = eim.getMapFactory().getMap(birdRide[myRide]);
        player.changeMap(onRide, onRide.getPortal(0));
        player.getClient().getSession().write(MaplePacketCreator.getClock(timeOnRide));
        eim.schedule("timeOut", timeOnRide * 1000);
}

function timeOut(eim) {
        end(eim);
}

function playerExit(eim, player, success) {
        eim.unregisterPlayer(player);
        player.changeMap(success ? exitMap.getId() : returnMap.getId(), 0);
}

function end(eim) {
        var party = eim.getPlayers();
        for (var i = 0; i < party.size(); i++) {
            playerExit(eim, party.get(i), true);
        }
        eim.dispose();
}

function playerDisconnected(eim, player) {
        playerExit(eim, player, false);
}

function cancelSchedule() {}

function dispose(eim) {}