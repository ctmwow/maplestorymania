/*
	This file is part of the OdinMS Maple Story Server
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
importPackage(Packages.org.ascnet.leaftown.server.maps);

var status = 0;
var minLevel = 55;
var maxLevel = 100;
var minPlayers = 0;
var maxPlayers = 6;

function start() {
    status = -1;
    action(1, 0, 0);
}

function action(mode, type, selection) {
    if (mode == -1) {
        cm.dispose();
    } else {
        if (mode == 0 && status == 0) {
            cm.dispose();
            return;
        }
        if (mode == 1)
            status++;
        else
            status--;
        if (status == 0) {
            if (cm.getParty() == null) {
                cm.sendOk("Por favor, volte quando formar um grupo.");
                cm.dispose();
                return;
            }
            if (!cm.isPartyLeader()) {
                cm.sendSimple("Você não é o líder do grupo.");
                cm.dispose();
            } else {
                var party = cm.getParty().getMembers();
                var mapId = cm.getPlayer().getMapId();
                var next = true;
                var levelValid = 0;
                var inMap = 0;
                if (party.size() < minPlayers || party.size() > maxPlayers)
                    next = false;
                else {
                    for (var i = 0; i < party.size() && next; i++) {
                        if ((party.get(i).getLevel() >= minLevel) && (party.get(i).getLevel() <= maxLevel))
                            levelValid += 1;
                        if (party.get(i).getMapId() == mapId)
                            inMap += 1;
                    }
                    if (levelValid < minPlayers || inMap < minPlayers)
                        next = false;
                }
                if (next) {
                    var em = cm.getEventManager("PiratePQ");
                    if (em == null) {
                        cm.sendOk("A missão de grupo do Lorde Pirata não está disponível.");
                        cm.dispose();
                    }
                    else {
                        em.startInstance(cm.getParty(),cm.getPlayer().getMap());
						party = cm.getPlayer().getEventInstance().getPlayers();
						cm.dispose();
                    }
                    cm.dispose();
                }
                else {
                    cm.sendOk("Seu grupo não é possui seis pessoas. Verifique se todos seus membros estão presentes para participar na missão. Vejo que #b" + levelValid.toString() + " #kmembros estão no level correto, e #b" + inMap.toString() + "#k estão em meu mapa. Se estiver errado, #brelogue#k ou crie o grupo novamente.");
                    cm.dispose();
                }
            }
        }
        else {
            cm.sendOk("A missão de grupo do Lorde Pirata não existe.");
            cm.dispose();
        }
    }
}