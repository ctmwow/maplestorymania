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
import org.ascnet.leaftown.client.messages.MessageCallback;
import org.ascnet.leaftown.tools.performance.CPUSampler;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class ProfilingCommands implements Command {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ProfilingCommands.class);

    @Override
    public void execute(MapleClient c, MessageCallback mc, String[] splitted) {
        if (splitted[0].equalsIgnoreCase("!startProfiling")) {
            CPUSampler sampler = CPUSampler.getInstance();
            sampler.addIncluded("org.ascnet.leaftown");
            sampler.start();
        } else if (splitted[0].equalsIgnoreCase("!stopProfiling")) {
            CPUSampler sampler = CPUSampler.getInstance();
            try {
                String filename = "odinprofile.txt";
                if (splitted.length > 1) {
                    filename = splitted[1];
                }
                File file = new File(filename);
                int index = 0;
                while (file.exists()) {
                    index++;
                    file = new File("odinprofile_" + index + ".txt");
                }
                sampler.stop();
                FileWriter fw = new FileWriter(file);
                sampler.save(fw, 1, 10);
                fw.close();
            } catch (IOException e) {
                log.error("THROW", e);
            }
            sampler.reset();
        }
    }

    @Override
    public CommandDefinition[] getDefinition() {
        return new CommandDefinition[] {
                new CommandDefinition("startProfiling", "", "Starts the CPU Sampling based profiler", 5),
                new CommandDefinition("stopProfiling", "<File Name>", "Stops the Profiler and saves the results to the given fileName", 5)
        };
    }
}