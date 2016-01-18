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
import org.ascnet.leaftown.client.MapleClient;
import org.ascnet.leaftown.client.messages.Command;
import org.ascnet.leaftown.client.messages.CommandDefinition;
import org.ascnet.leaftown.client.messages.MessageCallback;
import org.ascnet.leaftown.net.channel.ChannelServer;

import java.util.Collection;

public class SaveAllCommand implements Command {

    @Override
    public void execute(MapleClient c, MessageCallback mc, String[] splitted) throws Exception {
        if (splitted[0].equals("!saveall")) {
            Collection<ChannelServer> ccs = ChannelServer.getAllInstances();
            for (ChannelServer chan : ccs) {
                if (chan != null) {
                    mc.dropMessage("Saving characters on channel " + chan.getChannel());
                    Collection<MapleCharacter> allchars = chan.getPlayerStorage().getAllCharacters();
                    MapleCharacter chrs[] = allchars.toArray(new MapleCharacter[allchars.size()]);
                    for (MapleCharacter chr : chrs) {
                        try {
                            if (chr != null) {
                                chr.saveToDB(true);
                            }
                        } catch (Exception e) {
                        }
                    }
                }
            }
            mc.dropMessage("All characters have been saved.");
        }
    }

    @Override
    public CommandDefinition[] getDefinition() {
        return new CommandDefinition[] {
                new CommandDefinition("saveall", "", "Saves all characters", 4)
        };
    }
}