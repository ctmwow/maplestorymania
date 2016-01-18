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
import org.ascnet.leaftown.net.MaplePacket;
import org.ascnet.leaftown.net.channel.ChannelServer;
import org.ascnet.leaftown.tools.MaplePacketCreator;
import org.ascnet.leaftown.tools.StringUtil;

import java.rmi.RemoteException;
import java.util.Collection;

public class NoticeCommand implements Command {

    public static int getNoticeType(String typestring) {
        switch (typestring) {
            case "n":
                return 0;
            case "p":
                return 1;
            case "l":
                return 2;
            case "nv":
                return 5;
            case "v":
                return 5;
            case "b":
                return 6;
            case "sm":
                return -2;
            case "tm":
                return -3;
            case "ln":
                return 22;
            case "smn":
                return 18;
        }
        return -1;
    }

    public static int getNoticeRange(String rangestr) {
        if (rangestr.equals("m")) {
            return 0;
        } else if (rangestr.equals("c")) {
            return 1;
        }
        return 2;
    }

    @Override
    public void execute(MapleClient c, MessageCallback mc, String[] splitted) throws Exception {
        StringBuilder notice = new StringBuilder();
        switch (splitted[0]) {
            case "!notice":
                int joinmod = 1;
                int range = -1;
                switch (splitted[1]) {
                    case "m":
                        range = 0;
                        break;
                    case "c":
                        range = 1;
                        break;
                    case "w":
                        range = 2;
                        break;
                }

                int tfrom = 2;
                if (range == -1) {
                    range = 2;
                    tfrom = 1;
                }
                int type = getNoticeType(splitted[tfrom]);
                if (type == -1) {
                    type = 0;
                    joinmod = 0;
                }

                if (splitted[tfrom].equals("nv")) {
                    notice.append("[Notice] ");
                }
                joinmod += tfrom;
                if (type == 18 || type == 22) {
                    notice = new StringBuilder(c.getPlayer().getName() + " : ");
                    type -= 20;
                }
                notice.append(StringUtil.joinStringFrom(splitted, joinmod));
                MaplePacket packet = null;
                switch (type) {
                    case -2:
                        packet = MaplePacketCreator.serverNotice(3, 99, notice.toString(), false);
                        break;
                    case -3:
                        packet = MaplePacketCreator.topMessage(notice.toString());
                        break;
                    default:
                        packet = MaplePacketCreator.serverNotice(type, notice.toString());
                        break;
                }
                if (packet == null) {
                    mc.dropMessage("An unknown error occured. Report please.");
                    return;
                }
                if (range == 0) {
                    c.getPlayer().getMap().broadcastMessage(packet);
                } else if (range == 1) {
                    c.getChannelServer().broadcastPacket(packet);
                } else {
                    try {
                        ChannelServer.getInstance(1).getWorldInterface().broadcastMessage(c == null ? "IRCBot" : c.getPlayer().getName(), packet.getBytes());
                    } catch (RemoteException e) {
                        c.getChannelServer().reconnectWorld();
                    }
                }
                break;
            case "!me":
                MaplePacket msgpacket = MaplePacketCreator.serverNotice(6, "[" + c.getPlayer().getName() + "] " + StringUtil.joinStringFrom(splitted, 1));
                c.getChannelServer().getWorldInterface().broadcastMessage(c.getPlayer().getName(), msgpacket.getBytes());
                break;
            case "!gmtalk":
                String name = c.getPlayer().getName();
                ChannelServer.getInstance(1).getWorldInterface().broadcastGMMessage(null, MaplePacketCreator.serverNotice(6, name + " : " + StringUtil.joinStringFrom(splitted, 1)).getBytes());
                break;
            case "!servermessage":
                Collection<ChannelServer> cservs = ChannelServer.getAllInstances();
                String outputMessage = StringUtil.joinStringFrom(splitted, 1);
                for (ChannelServer cserv : cservs) {
                    cserv.setServerMessage(outputMessage);
                }
                break;
        }
    }

    @Override
    public CommandDefinition[] getDefinition() {
        return new CommandDefinition[] {
                new CommandDefinition("notice", "[mcw] [n/p/l/nv/v/b] message", "", 4),
                new CommandDefinition("me", "message", "send a message with your name as the prefix", 4),
                new CommandDefinition("gmtalk", "message", "send a message to all GMs in the server", 1),
                new CommandDefinition("servermessage", "<new message>", "Changes the servermessage to the new message", 4)
        };
    }
}