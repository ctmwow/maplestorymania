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

import org.ascnet.leaftown.client.MapleClient;
import org.ascnet.leaftown.client.messages.Command;
import org.ascnet.leaftown.client.messages.CommandDefinition;
import org.ascnet.leaftown.client.messages.CommandProcessor;
import org.ascnet.leaftown.client.messages.MessageCallback;

public class HelpCommand implements Command {

    @Override
    public void execute(MapleClient c, MessageCallback mc, String[] splittedLine) throws Exception {
        int page = CommandProcessor.getOptionalIntArg(splittedLine, 1, 1);
        CommandProcessor.getInstance().dropHelp(c.getPlayer(), mc, page);
    }

    @Override
    public CommandDefinition[] getDefinition() {
        return new CommandDefinition[] {
                new CommandDefinition("mycommands", "[page - defaults to 1]", "Shows the help", 4)
        };
    }
}