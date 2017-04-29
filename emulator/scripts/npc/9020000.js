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

/**
-- Odin JavaScript --------------------------------------------------------------------------------
	Lakelis - Victoria Road: Kerning City (103000000)
-- By ---------------------------------------------------------------------------------------------
	Stereo
-- Version Info -----------------------------------------------------------------------------------
	1.0 - First Version by Stereo
---------------------------------------------------------------------------------------------------
**/

var status;
var minLevel = 21;
var maxLevel = 255;
var minPlayers = 1;
var maxPlayers = 6;

function start() {
    status = -1;
    action(1, 0, 0);
}

function action(mode, type, selection) {
    if (mode == 1)
        status++;
    else {
        cm.dispose();
        return;
    }
    if (status == 0) {
        if (cm.getParty() == null) { // No Party
            cm.sendOk("Que tal você e seu grupo terminarem uma missão juntos? Aqui você vai encontrar obstáculos e problemas que só poderão ser resolvidos em equipe. Se quiser tentar, peça ao #blíder do seu grupo#k para falar comigo.");
            cm.dispose();
        } else if (!cm.isPartyLeader()) { // Not Party Leader
            cm.sendOk("Se você quiser tentar a missão, por favor diga ao #bleader do seu Ggrupo#k para falar comigo.");
            cm.dispose();
        } else {
            var party = cm.getParty().getMembers();
            var inMap = cm.partyMembersInMap();
            var levelValid = 0;
            for (var i = 0; i < party.size(); i++) {
                if (party.get(i).getLevel() >= minLevel && party.get(i).getLevel() <= maxLevel)
                    levelValid++;
            }
            if (inMap < minPlayers || inMap > maxPlayers) {
                cm.sendOk("Seu grupo não possui "+minPlayers+" membros. Certifique-se de que todos os seus membros estejam presentes e qualificados para participar nesta missão.");
                cm.dispose();
            } else if (levelValid != inMap) {
                cm.sendOk("Certifique-se de que todos os seus membros estejam presentes e qualificados para participar nesta missão. Esta PQ requer jogadores do level "+minLevel+" ao level "+maxLevel+". Eu vejo #b" + levelValid + "#k membros que estão no level correto. Se isso parece um engano, #breconecte-se#k, ou recrie o grupo.");
                cm.dispose();
            } else {
                var em = cm.getEventManager("KerningPQ");
                if (em == null) {
                    cm.sendOk("Este evento está indisponível.");
                } else if (em.getProperty("KPQOpen").equals("true")) {
                    // Begin the PQ.
                    em.startInstance(cm.getParty(), cm.getPlayer().getMap());
                    //cm.removeFromParty(4001008, party);
                    //cm.removeFromParty(4001007, party);
                    em.setProperty("KPQOpen" , "false");
                } else {
                    cm.sendNext("Um outro grupo já entrou para completar a missão. Por favor, tente mais tarde.");
                }
                cm.dispose();
            }
        }
    }
}