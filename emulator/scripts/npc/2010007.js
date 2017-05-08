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
 * Guild Creation NPC
 */
var status = 0;
var sel;

function start() {
    cm.sendSimple("O que você gostaria de fazer?\r\n#b#L0#Criar um Clã#l\r\n#L1#Desfazer seu Clã#l\r\n#L2#Aumentar a Capacidade do Clã#l#k");
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
        if (status == 1) {
            sel = selection;
            if (selection == 0) {
                if (cm.getPlayer().getGuildId() > 0) {
                    cm.sendOk("Você não pode criar umo novo clã enquanto estiver em um.");
                    cm.dispose();
                } else
                    cm.sendYesNo("Criar um Clã custa #b" + cm.getPlayer().guildCost() + " mesos#k, você tem certeza de que quer continuar?");
            } else if (selection == 1) {
                if (cm.getPlayer().getGuildId() < 1 || cm.getPlayer().getGuildRank() != 1) {
                    cm.sendOk("Você só pode desfazer um clã se você for o mestre daquele clã.");
                    cm.dispose();
                } else
                    cm.sendYesNo("Tem certeza de que deseja desfazer a seu clã? Você não será capaz de recuperá-la depois e todos os GPs terão desaparecido.");
            } else if (selection == 2) {
                if (cm.getPlayer().getGuildId() < 1 || cm.getPlayer().getGuildRank() != 1) {
                    cm.sendOk("Você só pode aumentar a capacidade de seu clã se você for o mestre.");
                    cm.dispose();
                } else
                    cm.sendYesNo("Aumentar a capacidade de seu clã em mais #b5 jogadores#k custa #b" + cm.getPlayer().capacityCost() +" mesos#k, você tem certeza de que quer continuar?");
            }
        } else if (status == 2) {
            if (sel == 0 && cm.getPlayer().getGuildId() <= 0) {
                cm.getPlayer().genericGuildMessage(1);
                cm.dispose();
            } else if (cm.getPlayer().getGuildId() > 0 && cm.getPlayer().getGuildRank() == 1) {
                if (sel == 1) {
                    cm.getPlayer().disbandGuild();
                    cm.dispose();
                } else if (sel == 2) {
                    cm.getPlayer().increaseGuildCapacity();
                    cm.dispose();
                }
            }
        }
    }
}
