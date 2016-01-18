/*
 * This file is part of AscNet Leaftown.
 * Copyright (C) 2014 Ascension Network
 *
 * AscNet Leaftown is a fork of the OdinMS MapleStory Server.
 * The following is the original copyright notice:
 *
 *     This file is part of the OdinMS Maple Story Server
 *     Copyright (C) 2008 Patrick Huy <patrick.huy@frz.cc>
 *                        Matthias Butz <matze@odinms.de>
 *                        Jan Christian Meyer <vimes@odinms.de>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License version 3
 * as published by the Free Software Foundation. You may not use, modify
 * or distribute this program under any other version of the
 * GNU Affero General Public License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.ascnet.leaftown.client.messages.commands;

import org.ascnet.leaftown.client.MapleCharacter;
import org.ascnet.leaftown.client.MapleCharacterUtil;
import org.ascnet.leaftown.client.MapleClient;
import org.ascnet.leaftown.client.messages.Command;
import org.ascnet.leaftown.client.messages.CommandDefinition;
import org.ascnet.leaftown.client.messages.MessageCallback;
import org.ascnet.leaftown.client.messages.ServernoticeMapleClientMessageCallback;
import org.ascnet.leaftown.net.world.remote.CheaterData;
import org.ascnet.leaftown.server.maps.MapleMap;
import org.ascnet.leaftown.tools.MaplePacketCreator;

import java.rmi.RemoteException;
import java.util.List;

public class CheaterHuntingCommands implements Command {

    @Override
    public void execute(MapleClient c, MessageCallback mc, String[] splitted) throws Exception {
        if (splitted[0].equals("!whosthere")) {
            MessageCallback callback = new ServernoticeMapleClientMessageCallback(c);
            StringBuilder builder = new StringBuilder("Players on Map: ");
            MapleMap map = c.getPlayer().getMap();
            if (splitted.length > 1) {
                map = c.getChannelServer().getMapFactory().getMap(Integer.parseInt(splitted[1]));
            }
            if (map == null) {
                mc.dropMessage("MapID doesn't exist.");
                return;
            }
            for (MapleCharacter chr : map.getCharacters()) {
                if (builder.length() > 150) { // wild guess :o
                    builder.setLength(builder.length() - 2);
                    callback.dropMessage(builder.toString());
                    builder = new StringBuilder();
                }
                builder.append(MapleCharacterUtil.makeMapleReadable(chr.getName()));
                builder.append(", ");
            }
            builder.setLength(builder.length() - 2);
            c.sendPacket(MaplePacketCreator.serverNotice(6, builder.toString()));
        } else if (splitted[0].equals("!cheaters")) {
            try {
                List<CheaterData> cheaters = c.getChannelServer().getWorldInterface().getCheaters();
                for (int x = cheaters.size() - 1; x >= 0; x--) {
                    CheaterData cheater = cheaters.get(x);
                    mc.dropMessage(cheater.getInfo());
                }
            } catch (RemoteException e) {
                c.getChannelServer().reconnectWorld();
            }
        }
    }

    @Override
    public CommandDefinition[] getDefinition() {
        return new CommandDefinition[] {
                new CommandDefinition("whosthere", "", "Gives you a list of players on your map", 1),
                new CommandDefinition("cheaters", "", "Gives you a list of cheaters", 4)
        };
    }
}