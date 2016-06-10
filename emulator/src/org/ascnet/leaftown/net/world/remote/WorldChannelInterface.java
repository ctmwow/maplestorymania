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

package org.ascnet.leaftown.net.world.remote;

import org.ascnet.leaftown.client.MapleFamily;
import org.ascnet.leaftown.net.MaplePacket;
import org.ascnet.leaftown.net.channel.remote.ChannelWorldInterface;
import org.ascnet.leaftown.net.world.CharacterIdChannelPair;
import org.ascnet.leaftown.net.world.MapleMessenger;
import org.ascnet.leaftown.net.world.MapleMessengerCharacter;
import org.ascnet.leaftown.net.world.MapleParty;
import org.ascnet.leaftown.net.world.MaplePartyCharacter;
import org.ascnet.leaftown.net.world.PartyOperation;
import org.ascnet.leaftown.net.world.PlayerBuffValueHolder;
import org.ascnet.leaftown.net.world.guild.MapleAlliance;
import org.ascnet.leaftown.net.world.guild.MapleGuild;
import org.ascnet.leaftown.net.world.guild.MapleGuildCharacter;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * @author Matze
 */
public interface WorldChannelInterface extends Remote, WorldChannelCommonOperations 
{
    public Properties getDatabaseProperties() throws RemoteException;

    public Properties getGameProperties() throws RemoteException;

    public void serverReady() throws RemoteException;

    public String getIP(int channel) throws RemoteException;

    public int find(String charName) throws RemoteException;

    public int find(int characterId) throws RemoteException;

    public Map<Integer, Integer> getConnected() throws RemoteException;

    MapleParty createParty(MaplePartyCharacter chrfor) throws RemoteException;

    MapleParty getParty(int partyid) throws RemoteException;

    public void updateParty(int partyid, PartyOperation operation, MaplePartyCharacter target) throws RemoteException;

    public void partyChat(int partyid, String chattext, String namefrom) throws RemoteException;

    public boolean isAvailable() throws RemoteException;

    public ChannelWorldInterface getChannelInterface(int channel) throws RemoteException;

    public WorldLocation getLocation(String name) throws RemoteException;

    public CharacterIdChannelPair[] multiBuddyFind(int charIdFrom, int[] characterIds) throws RemoteException;
    
    public void setFamily(MapleFamily family, int cid) throws RemoteException;

    public MapleFamily getFamily(int id) throws RemoteException;
    
    public MapleGuild getGuild(int id) throws RemoteException;

    public void clearGuilds() throws RemoteException;

    public void setGuildMemberOnline(MapleGuildCharacter mgc, boolean bOnline, int channel) throws RemoteException;

    public int addGuildMember(MapleGuildCharacter mgc) throws RemoteException;

    public void leaveGuild(MapleGuildCharacter mgc) throws RemoteException;

    public void guildChat(int gid, String name, int cid, String msg) throws RemoteException;

    public void changeRank(int gid, int cid, int newRank) throws RemoteException;

    public void expelMember(MapleGuildCharacter initiator, String name, int cid) throws RemoteException;

    public void setGuildNotice(int gid, String notice) throws RemoteException;

    public void memberLevelJobUpdate(MapleGuildCharacter mgc) throws RemoteException;

    public void changeRankTitle(int gid, String[] ranks) throws RemoteException;

    public int createGuild(int leaderId, String name) throws RemoteException;

    public void setGuildEmblem(int gid, short bg, byte bgcolor, short logo, byte logocolor) throws RemoteException;

    public void disbandGuild(int gid) throws RemoteException;

    public boolean increaseGuildCapacity(int gid) throws RemoteException;

    public void gainGP(int gid, int amount) throws RemoteException;

    public MapleAlliance getAlliance(int id) throws RemoteException;

    public void addAlliance(int id, MapleAlliance addAlliance) throws RemoteException;

    public void disbandAlliance(int id) throws RemoteException;

    public boolean setAllianceNotice(int aId, String notice) throws RemoteException;

    public boolean setAllianceRanks(int aId, String[] ranks) throws RemoteException;

    public boolean removeGuildFromAlliance(int aId, int guildId) throws RemoteException;

    public boolean addGuildtoAlliance(int aId, int guildId) throws RemoteException;

    public boolean setGuildAllianceId(int gId, int aId) throws RemoteException;

    public boolean increaseAllianceCapacity(int aId, int inc) throws RemoteException;

    public void broadcastToGuild(int guildid, MaplePacket packet) throws RemoteException;

    public void allianceMessage(int id, MaplePacket packet, int exception, int guildex) throws RemoteException;

    public void broadcastToAlliance(int id, MaplePacket packet) throws RemoteException;

    public String listGMs() throws RemoteException;

    MapleMessenger createMessenger(MapleMessengerCharacter chrfor) throws RemoteException;

    MapleMessenger getMessenger(int messengerid) throws RemoteException;

    public void leaveMessenger(int messengerid, MapleMessengerCharacter target) throws RemoteException;

    public void joinMessenger(int messengerid, MapleMessengerCharacter target, String from, int fromchannel) throws RemoteException;

    public void silentJoinMessenger(int messengerid, MapleMessengerCharacter target, int position) throws RemoteException;

    public void silentLeaveMessenger(int messengerid, MapleMessengerCharacter target) throws RemoteException;

    public void messengerChat(int messengerid, String chattext, String namefrom) throws RemoteException;

    public void declineChat(String target, String namefrom) throws RemoteException;

    public void updateMessenger(int messengerid, String namefrom, int fromchannel) throws RemoteException;

    public void addBuffsToStorage(int chrid, List<PlayerBuffValueHolder> toStore) throws RemoteException;

    public List<PlayerBuffValueHolder> getBuffsFromStorage(int chrid) throws RemoteException;
}