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

package org.ascnet.leaftown.net.world;

import org.ascnet.leaftown.client.MapleFamily;
import org.ascnet.leaftown.database.DatabaseConnection;
import org.ascnet.leaftown.net.MaplePacket;
import org.ascnet.leaftown.net.channel.remote.ChannelWorldInterface;
import org.ascnet.leaftown.net.login.remote.LoginWorldInterface;
import org.ascnet.leaftown.net.world.guild.MapleAlliance;
import org.ascnet.leaftown.net.world.guild.MapleGuild;
import org.ascnet.leaftown.net.world.guild.MapleGuildCharacter;
import org.ascnet.leaftown.net.world.remote.CheaterData;
import org.ascnet.leaftown.net.world.remote.WorldChannelInterface;
import org.ascnet.leaftown.net.world.remote.WorldLocation;
import org.ascnet.leaftown.tools.CollectionUtil;

import javax.rmi.ssl.SslRMIClientSocketFactory;
import javax.rmi.ssl.SslRMIServerSocketFactory;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * @author Matze
 */
public class WorldChannelInterfaceImpl extends UnicastRemoteObject implements WorldChannelInterface 
{
    private static final long serialVersionUID = -5568606556235590482L;
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(WorldChannelInterfaceImpl.class);
    private ChannelWorldInterface cb;
    private int dbId;
    private boolean ready = false;

    public WorldChannelInterfaceImpl() throws RemoteException {
        super(0, new SslRMIClientSocketFactory(), new SslRMIServerSocketFactory());
    }

    public WorldChannelInterfaceImpl(ChannelWorldInterface cb, int dbId) throws RemoteException {
        super(0, new SslRMIClientSocketFactory(), new SslRMIServerSocketFactory());
        this.cb = cb;
        this.dbId = dbId;
    }

    public Properties getDatabaseProperties() throws RemoteException {
        return WorldServer.getInstance().getDbProp();
    }

    public Properties getGameProperties() throws RemoteException {
        Properties ret = new Properties(WorldServer.getInstance().getWorldProp());
        try {
            Connection con = DatabaseConnection.getConnection();
            PreparedStatement ps = con.prepareStatement("SELECT * FROM channelconfig WHERE channelid = ?");
            ps.setInt(1, dbId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                ret.setProperty(rs.getString("name"), rs.getString("value"));
            }
            rs.close();
            ps.close();
        } catch (SQLException ex) {
            log.error("Could not retrieve channel configuration", ex);
        }
        return ret;
    }

    public void serverReady() throws RemoteException {
        ready = true;
        for (LoginWorldInterface wli : WorldRegistryImpl.getInstance().getLoginServer()) {
            try {
                wli.channelOnline(cb.getChannelId(), cb.getIP());
            } catch (RemoteException e) {
                WorldRegistryImpl.getInstance().deregisterLoginServer(wli);
            }
        }
        log.info("Channel {} is online.", cb.getChannelId());
    }

    public boolean isReady() {
        return ready;
    }

    public String getIP(int channel) throws RemoteException {
        ChannelWorldInterface cwi = WorldRegistryImpl.getInstance().getChannel(channel);
        if (cwi == null) {
            return "0.0.0.0:0";
        } else {
            try {
                return cwi.getIP();
            } catch (RemoteException e) {
                WorldRegistryImpl.getInstance().deregisterChannelServer(channel);
                return "0.0.0.0:0";
            }
        }
    }

    public void whisper(String sender, String target, int channel, String message) throws RemoteException {
        for (int i : WorldRegistryImpl.getInstance().getChannelServer()) {
            ChannelWorldInterface cwi = WorldRegistryImpl.getInstance().getChannel(i);
            try {
                cwi.whisper(sender, target, channel, message);
            } catch (RemoteException e) {
                WorldRegistryImpl.getInstance().deregisterChannelServer(i);
            }
        }
    }

    public boolean isConnected(String charName, boolean removePlayer) throws RemoteException {
        for (int i : WorldRegistryImpl.getInstance().getChannelServer()) {
            ChannelWorldInterface cwi = WorldRegistryImpl.getInstance().getChannel(i);
            try {
                if (cwi.isConnected(charName, removePlayer)) {
                    return true;
                }
            } catch (RemoteException e) {
                WorldRegistryImpl.getInstance().deregisterChannelServer(i);
            }
        }
        return false;
    }

    public void broadcastMessage(String sender, final byte[] message) throws RemoteException {
        broadcastMessage(sender, message, false);
    }

    public void broadcastMessage(String sender, final byte[] message, boolean smega) throws RemoteException {
        for (int i : WorldRegistryImpl.getInstance().getChannelServer()) {
            ChannelWorldInterface cwi = WorldRegistryImpl.getInstance().getChannel(i);
            try {
                cwi.broadcastMessage(sender, message, smega);
            } catch (RemoteException e) {
                WorldRegistryImpl.getInstance().deregisterChannelServer(i);
            }
        }
    }

    public int find(String charName) throws RemoteException {
        for (int i : WorldRegistryImpl.getInstance().getChannelServer()) {
            ChannelWorldInterface cwi = WorldRegistryImpl.getInstance().getChannel(i);
            try {
                if (cwi.isConnected(charName, false)) {
                    return cwi.getChannelId();
                }
            } catch (RemoteException e) {
                WorldRegistryImpl.getInstance().deregisterChannelServer(i);
            }
        }
        return -1;
    }

    @Override
    public int find(int characterId) throws RemoteException {
        for (int i : WorldRegistryImpl.getInstance().getChannelServer()) {
            ChannelWorldInterface cwi = WorldRegistryImpl.getInstance().getChannel(i);
            try {
                if (cwi.isConnected(characterId)) {
                    return cwi.getChannelId();
                }
            } catch (RemoteException e) {
                WorldRegistryImpl.getInstance().deregisterChannelServer(i);
            }
        }
        return -1;
    }

    public void shutdown(int time) throws RemoteException {
        for (LoginWorldInterface lwi : WorldRegistryImpl.getInstance().getLoginServer()) {
            try {
                lwi.shutdown();
            } catch (RemoteException e) {
                WorldRegistryImpl.getInstance().deregisterLoginServer(lwi);
            }
        }
        for (int i : WorldRegistryImpl.getInstance().getChannelServer()) {
            ChannelWorldInterface cwi = WorldRegistryImpl.getInstance().getChannel(i);
            try {
                cwi.shutdown(time);
            } catch (RemoteException e) {
                WorldRegistryImpl.getInstance().deregisterChannelServer(i);
            }
        }
    }

    public Map<Integer, Integer> getConnected() throws RemoteException {
        Map<Integer, Integer> ret = new HashMap<>();
        int total = 0;
        for (int i : WorldRegistryImpl.getInstance().getChannelServer()) {
            ChannelWorldInterface cwi = WorldRegistryImpl.getInstance().getChannel(i);
            try {
                int curConnected = cwi.getConnected();
                ret.put(i, curConnected);
                total += curConnected;
            } catch (RemoteException e) {
                WorldRegistryImpl.getInstance().deregisterChannelServer(i);
            }
        }
        ret.put(0, total);
        return ret;
    }

    public void loggedOn(String name, int characterId, int channel, int[] buddies) throws RemoteException {
        for (int i : WorldRegistryImpl.getInstance().getChannelServer()) {
            ChannelWorldInterface cwi = WorldRegistryImpl.getInstance().getChannel(i);
            try {
                cwi.loggedOn(name, characterId, channel, buddies);
            } catch (RemoteException e) {
                WorldRegistryImpl.getInstance().deregisterChannelServer(i);
            }
        }
    }

    @Override
    public void loggedOff(String name, int characterId, int channel, int[] buddies) throws RemoteException {
        for (int i : WorldRegistryImpl.getInstance().getChannelServer()) {
            ChannelWorldInterface cwi = WorldRegistryImpl.getInstance().getChannel(i);
            try {
                cwi.loggedOff(name, characterId, channel, buddies);
            } catch (RemoteException e) {
                WorldRegistryImpl.getInstance().deregisterChannelServer(i);
            }
        }
    }

    //TODO only notify channels where partymembers are?
    public void updateParty(int partyid, PartyOperation operation, MaplePartyCharacter target) throws RemoteException {
        MapleParty party = WorldRegistryImpl.getInstance().getParty(partyid);
        if (party == null) {
            throw new IllegalArgumentException("no party with the specified partyid exists");
        }
        switch (operation) {
            case JOIN:
                party.addMember(target);
                break;
            case EXPEL:
            case LEAVE:
                party.removeMember(target);
                break;
            case DISBAND:
                WorldRegistryImpl.getInstance().disbandParty(partyid);
                break;
            case SILENT_UPDATE:
            case LOG_ONOFF:
                party.updateMember(target);
                break;
            case CHANGE_LEADER:
                party.setLeader(target);
                break;
            default:
                throw new RuntimeException("Unhandeled updateParty operation " + operation.name());
        }
        for (int i : WorldRegistryImpl.getInstance().getChannelServer()) {
            ChannelWorldInterface cwi = WorldRegistryImpl.getInstance().getChannel(i);
            try {
                cwi.updateParty(party, operation, target);
            } catch (RemoteException e) {
                WorldRegistryImpl.getInstance().deregisterChannelServer(i);
            }
        }
    }

    public MapleParty createParty(MaplePartyCharacter chrfor) throws RemoteException {
        return WorldRegistryImpl.getInstance().createParty(chrfor);
    }

    public MapleParty getParty(int partyid) throws RemoteException {
        return WorldRegistryImpl.getInstance().getParty(partyid);
    }

    @Override
    public void partyChat(int partyid, String chattext, String namefrom) throws RemoteException {
        MapleParty party = WorldRegistryImpl.getInstance().getParty(partyid);
        if (party == null) {
            throw new IllegalArgumentException("no party with the specified partyid exists");
        }
        for (int i : WorldRegistryImpl.getInstance().getChannelServer()) {
            ChannelWorldInterface cwi = WorldRegistryImpl.getInstance().getChannel(i);
            try {
                cwi.partyChat(party, chattext, namefrom);
            } catch (RemoteException e) {
                WorldRegistryImpl.getInstance().deregisterChannelServer(i);
            }
        }
    }

    public boolean isAvailable() throws RemoteException {
        return true;
    }

    public WorldLocation getLocation(String charName) throws RemoteException {
        for (int i : WorldRegistryImpl.getInstance().getChannelServer()) {
            ChannelWorldInterface cwi = WorldRegistryImpl.getInstance().getChannel(i);
            try {
                if (cwi.isConnected(charName, false)) {
                    return new WorldLocation(cwi.getLocation(charName), cwi.getChannelId());
                }
            } catch (RemoteException e) {
                WorldRegistryImpl.getInstance().deregisterChannelServer(i);
            }
        }
        return null;
    }

    public List<CheaterData> getCheaters() throws RemoteException {
        List<CheaterData> allCheaters = new ArrayList<>();
        for (int i : WorldRegistryImpl.getInstance().getChannelServer()) {
            ChannelWorldInterface cwi = WorldRegistryImpl.getInstance().getChannel(i);
            try {
                allCheaters.addAll(cwi.getCheaters());
            } catch (RemoteException e) {
                WorldRegistryImpl.getInstance().deregisterChannelServer(i);
            }
        }
        Collections.sort(allCheaters);
        return CollectionUtil.copyFirst(allCheaters, 10);
    }

    @Override
    public ChannelWorldInterface getChannelInterface(int channel) {
        ChannelWorldInterface cwi = WorldRegistryImpl.getInstance().getChannel(channel);
        return cwi;
    }

    @Override
    public void buddyChat(int[] recipientCharacterIds, int cidFrom, String nameFrom, String chattext) throws RemoteException {
        for (ChannelWorldInterface cwi : WorldRegistryImpl.getInstance().getAllChannelServers()) {
            cwi.buddyChat(recipientCharacterIds, cidFrom, nameFrom, chattext);
        }
    }

    @Override
    public CharacterIdChannelPair[] multiBuddyFind(int charIdFrom, int[] characterIds) throws RemoteException {
        List<CharacterIdChannelPair> foundsChars = new ArrayList<>(characterIds.length);
        for (int i : WorldRegistryImpl.getInstance().getChannelServer()) {
            ChannelWorldInterface cwi = WorldRegistryImpl.getInstance().getChannel(i);
            for (int charid : cwi.multiBuddyFind(charIdFrom, characterIds)) {
                foundsChars.add(new CharacterIdChannelPair(charid, i));
            }
        }
        return foundsChars.toArray(new CharacterIdChannelPair[foundsChars.size()]);
    }
    
    @Override
    public MapleFamily getFamily(int id) throws RemoteException 
    {
        return WorldRegistryImpl.getInstance().getFamily(id);
    }

    @Override
    public MapleGuild getGuild(int id) throws RemoteException {
        return WorldRegistryImpl.getInstance().getGuild(id);
    }

    @Override
    public void clearGuilds() throws RemoteException {
        WorldRegistryImpl.getInstance().clearGuilds();
    }

    @Override
    public void setGuildMemberOnline(MapleGuildCharacter mgc, boolean bOnline, int channel) throws RemoteException {
        WorldRegistryImpl.getInstance().setGuildMemberOnline(mgc, bOnline, channel);
    }

    @Override
    public int addGuildMember(MapleGuildCharacter mgc) throws RemoteException {
        return WorldRegistryImpl.getInstance().addGuildMember(mgc);
    }

    @Override
    public void guildChat(int gid, String name, int cid, String msg) throws RemoteException {
        WorldRegistryImpl.getInstance().guildChat(gid, name, cid, msg);
    }

    @Override
    public void leaveGuild(MapleGuildCharacter mgc) throws RemoteException {
        WorldRegistryImpl.getInstance().leaveGuild(mgc);
    }

    @Override
    public void changeRank(int gid, int cid, int newRank) throws RemoteException {
        WorldRegistryImpl.getInstance().changeRank(gid, cid, newRank);
    }

    @Override
    public void expelMember(MapleGuildCharacter initiator, String name, int cid) throws RemoteException {
        WorldRegistryImpl.getInstance().expelMember(initiator, name, cid);
    }

    @Override
    public void setGuildNotice(int gid, String notice) throws RemoteException {
        WorldRegistryImpl.getInstance().setGuildNotice(gid, notice);
    }

    @Override
    public void memberLevelJobUpdate(MapleGuildCharacter mgc) throws RemoteException {
        WorldRegistryImpl.getInstance().memberLevelJobUpdate(mgc);
    }

    @Override
    public void changeRankTitle(int gid, String[] ranks) throws RemoteException {
        WorldRegistryImpl.getInstance().changeRankTitle(gid, ranks);
    }

    @Override
    public int createGuild(int leaderId, String name) throws RemoteException {
        return WorldRegistryImpl.getInstance().createGuild(leaderId, name);
    }

    @Override
    public void setGuildEmblem(int gid, short bg, byte bgcolor, short logo, byte logocolor) throws RemoteException {
        WorldRegistryImpl.getInstance().setGuildEmblem(gid, bg, bgcolor, logo, logocolor);
    }

    @Override
    public void disbandGuild(int gid) throws RemoteException {
        WorldRegistryImpl.getInstance().disbandGuild(gid);
    }

    @Override
    public boolean increaseGuildCapacity(int gid) throws RemoteException {
        return WorldRegistryImpl.getInstance().increaseGuildCapacity(gid);
    }

    @Override
    public void gainGP(int gid, int amount) throws RemoteException {
        WorldRegistryImpl.getInstance().gainGP(gid, amount);
    }

    public String listGMs() throws RemoteException {
        java.util.Collection<ChannelWorldInterface> cwis = WorldRegistryImpl.getInstance().getAllChannelServers();

        StringBuilder builder = new StringBuilder();
        for (ChannelWorldInterface cwi : cwis) {
            builder.append(cwi.listGMs());
        }

        return builder.toString();
    }

    public MapleMessenger createMessenger(MapleMessengerCharacter chrfor) throws RemoteException {
        return WorldRegistryImpl.getInstance().createMessenger(chrfor);
    }

    public MapleMessenger getMessenger(int messengerid) throws RemoteException {
        return WorldRegistryImpl.getInstance().getMessenger(messengerid);
    }

    public void messengerInvite(String sender, int messengerid, String target, int fromchannel) throws RemoteException {
        for (int i : WorldRegistryImpl.getInstance().getChannelServer()) {
            ChannelWorldInterface cwi = WorldRegistryImpl.getInstance().getChannel(i);
            try {
                cwi.messengerInvite(sender, messengerid, target, fromchannel);
            } catch (RemoteException e) {
                WorldRegistryImpl.getInstance().deregisterChannelServer(i);
            }
        }
    }

    public void leaveMessenger(int messengerid, MapleMessengerCharacter target) throws RemoteException {
        MapleMessenger messenger = WorldRegistryImpl.getInstance().getMessenger(messengerid);
        if (messenger == null) {
            throw new IllegalArgumentException("No messenger with the specified messengerid exists");
        }
        int position = messenger.getPositionByName(target.getName());
        messenger.removeMember(target);

        for (int i : WorldRegistryImpl.getInstance().getChannelServer()) {
            ChannelWorldInterface cwi = WorldRegistryImpl.getInstance().getChannel(i);
            try {
                cwi.removeMessengerPlayer(messenger, position);
            } catch (RemoteException e) {
                WorldRegistryImpl.getInstance().deregisterChannelServer(i);
            }
        }
    }

    public void joinMessenger(int messengerid, MapleMessengerCharacter target, String from, int fromchannel) throws RemoteException {
        MapleMessenger messenger = WorldRegistryImpl.getInstance().getMessenger(messengerid);
        if (messenger == null) {
            throw new IllegalArgumentException("No messenger with the specified messengerid exists");
        }
        messenger.addMember(target);

        for (int i : WorldRegistryImpl.getInstance().getChannelServer()) {
            ChannelWorldInterface cwi = WorldRegistryImpl.getInstance().getChannel(i);
            try {
                cwi.addMessengerPlayer(messenger, from, fromchannel, target.getPosition());
            } catch (RemoteException e) {
                WorldRegistryImpl.getInstance().deregisterChannelServer(i);
            }
        }
    }

    public void messengerChat(int messengerid, String chattext, String namefrom) throws RemoteException {
        MapleMessenger messenger = WorldRegistryImpl.getInstance().getMessenger(messengerid);
        if (messenger == null) {
            throw new IllegalArgumentException("No messenger with the specified messengerid exists");
        }
        for (int i : WorldRegistryImpl.getInstance().getChannelServer()) {
            ChannelWorldInterface cwi = WorldRegistryImpl.getInstance().getChannel(i);
            try {
                cwi.messengerChat(messenger, chattext, namefrom);
            } catch (RemoteException e) {
                WorldRegistryImpl.getInstance().deregisterChannelServer(i);
            }
        }
    }

    public void declineChat(String target, String namefrom) throws RemoteException {
        for (int i : WorldRegistryImpl.getInstance().getChannelServer()) {
            ChannelWorldInterface cwi = WorldRegistryImpl.getInstance().getChannel(i);
            try {
                cwi.declineChat(target, namefrom);
            } catch (RemoteException e) {
                WorldRegistryImpl.getInstance().deregisterChannelServer(i);
            }
        }
    }

    public void updateMessenger(int messengerid, String namefrom, int fromchannel) throws RemoteException {
        MapleMessenger messenger = WorldRegistryImpl.getInstance().getMessenger(messengerid);
        int position = messenger.getPositionByName(namefrom);

        for (int i : WorldRegistryImpl.getInstance().getChannelServer()) {
            ChannelWorldInterface cwi = WorldRegistryImpl.getInstance().getChannel(i);
            try {
                cwi.updateMessenger(messenger, namefrom, position, fromchannel);
            } catch (RemoteException e) {
                WorldRegistryImpl.getInstance().deregisterChannelServer(i);
            }
        }
    }

    public void silentLeaveMessenger(int messengerid, MapleMessengerCharacter target) throws RemoteException {
        MapleMessenger messenger = WorldRegistryImpl.getInstance().getMessenger(messengerid);
        if (messenger == null) {
            throw new IllegalArgumentException("No messenger with the specified messengerid exists");
        }
        messenger.silentRemoveMember(target);
    }

    public void silentJoinMessenger(int messengerid, MapleMessengerCharacter target, int position) throws RemoteException {
        MapleMessenger messenger = WorldRegistryImpl.getInstance().getMessenger(messengerid);
        if (messenger == null) {
            throw new IllegalArgumentException("No messenger with the specified messengerid exists");
        }
        messenger.silentAddMember(target, position);
    }

    public void addBuffsToStorage(int chrid, List<PlayerBuffValueHolder> toStore) throws RemoteException {
        PlayerBuffStorage buffStorage = WorldRegistryImpl.getInstance().getPlayerBuffStorage();
        buffStorage.addBuffsToStorage(chrid, toStore);
    }

    public List<PlayerBuffValueHolder> getBuffsFromStorage(int chrid) throws RemoteException {
        PlayerBuffStorage buffStorage = WorldRegistryImpl.getInstance().getPlayerBuffStorage();
        return buffStorage.getBuffsFromStorage(chrid);
    }

    public void spouseChat(String sender, String target, String message) throws RemoteException {
        for (int i : WorldRegistryImpl.getInstance().getChannelServer()) {
            ChannelWorldInterface cwi = WorldRegistryImpl.getInstance().getChannel(i);
            try {
                cwi.spouseChat(sender, target, message);
            } catch (RemoteException e) {
                WorldRegistryImpl.getInstance().deregisterChannelServer(i);
            }
        }
    }

    public void broadcastGMMessage(String sender, final byte[] message) throws RemoteException {
        for (int i : WorldRegistryImpl.getInstance().getChannelServer()) {
            ChannelWorldInterface cwi = WorldRegistryImpl.getInstance().getChannel(i);
            try {
                cwi.broadcastGMMessage(sender, message);
            } catch (RemoteException e) {
                WorldRegistryImpl.getInstance().deregisterChannelServer(i);
            }
        }
    }

    @Override
    public MapleAlliance getAlliance(int id) throws RemoteException {
        return WorldRegistryImpl.getInstance().getAlliance(id);
    }

    @Override
    public void addAlliance(int id, MapleAlliance alliance) throws RemoteException {
        WorldRegistryImpl.getInstance().addAlliance(id, alliance);
    }

    @Override
    public void disbandAlliance(int id) throws RemoteException {
        WorldRegistryImpl.getInstance().disbandAlliance(id);
    }

    @Override
    public boolean setAllianceNotice(int aId, String notice) throws RemoteException {
        return WorldRegistryImpl.getInstance().setAllianceNotice(aId, notice);
    }

    @Override
    public boolean setAllianceRanks(int aId, String[] ranks) throws RemoteException {
        return WorldRegistryImpl.getInstance().setAllianceRanks(aId, ranks);
    }

    @Override
    public boolean removeGuildFromAlliance(int aId, int guildId) throws RemoteException {
        return WorldRegistryImpl.getInstance().removeGuildFromAlliance(aId, guildId);
    }

    @Override
    public boolean addGuildtoAlliance(int aId, int guildId) throws RemoteException {
        return WorldRegistryImpl.getInstance().addGuildtoAlliance(aId, guildId);
    }

    @Override
    public boolean setGuildAllianceId(int gId, int aId) throws RemoteException {
        return WorldRegistryImpl.getInstance().setGuildAllianceId(gId, aId);
    }

    @Override
    public boolean increaseAllianceCapacity(int aId, int inc) throws RemoteException {
        return WorldRegistryImpl.getInstance().increaseAllianceCapacity(aId, inc);
    }

    @Override
    public void broadcastToGuild(int guildid, MaplePacket packet) throws RemoteException {
        WorldRegistryImpl.getInstance().broadcastToGuild(guildid, packet);
    }

    @Override
    public void broadcastToAlliance(int id, MaplePacket packet) throws RemoteException {
        WorldRegistryImpl.getInstance().allianceMessage(id, packet, -1, -1);
    }

    @Override
    public void allianceMessage(int id, MaplePacket packet, int exception, int guildex) throws RemoteException {
        WorldRegistryImpl.getInstance().allianceMessage(id, packet, exception, guildex);
    }

    public void broadcastWorldMessage(String message) throws RemoteException {
        for (int i : WorldRegistryImpl.getInstance().getChannelServer()) {
            ChannelWorldInterface cwi = WorldRegistryImpl.getInstance().getChannel(i);
            try {
                cwi.broadcastWorldMessage(message);
            } catch (RemoteException e) {
                WorldRegistryImpl.getInstance().deregisterChannelServer(i);
            }
        }
    }

	@Override
	public void setFamily(MapleFamily family, int cid) throws RemoteException 
	{
		getChannelInterface(WorldRegistryImpl.getInstance().find(cid)).playerStorage().getCharacterById(cid).setMapleFamily(family);
	}
}