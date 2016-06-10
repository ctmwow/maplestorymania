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
import org.ascnet.leaftown.net.world.remote.WorldChannelInterface;
import org.ascnet.leaftown.net.world.remote.WorldLoginInterface;
import org.ascnet.leaftown.net.world.remote.WorldRegistry;

import javax.rmi.ssl.SslRMIClientSocketFactory;
import javax.rmi.ssl.SslRMIServerSocketFactory;
import java.rmi.ConnectException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Matze
 */
public class WorldRegistryImpl extends UnicastRemoteObject implements WorldRegistry 
{
    private static final long serialVersionUID = -5170574938159280746L;
    private static volatile WorldRegistryImpl instance = null;
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(WorldRegistryImpl.class);
    private final Map<Integer, ChannelWorldInterface> channelServer = new LinkedHashMap<>();
    private final List<LoginWorldInterface> loginServer = new LinkedList<>();
    private final Map<Integer, MapleParty> parties = new HashMap<>();
    private final AtomicInteger runningPartyId = new AtomicInteger();
    private final Map<Integer, MapleMessenger> messengers = new HashMap<>();
    private final AtomicInteger runningMessengerId = new AtomicInteger();
    private final Map<Integer, MapleFamily> familys = new LinkedHashMap<>();
    private final Map<Integer, MapleGuild> guilds = new LinkedHashMap<>();
    private final Map<Integer, MapleAlliance> alliances = new LinkedHashMap<>();
    private final PlayerBuffStorage buffStorage = new PlayerBuffStorage();

    private WorldRegistryImpl() throws RemoteException 
    {
        super(0x00, new SslRMIClientSocketFactory(), new SslRMIServerSocketFactory());
        
        DatabaseConnection.setProps(WorldServer.getInstance().getDbProp());

        final Connection con = DatabaseConnection.getConnection();
        
        try 
        {
        	final PreparedStatement ps = con.prepareStatement("SELECT MAX(party) + 1 FROM characters");
            final ResultSet rs = ps.executeQuery();
            rs.next();
            runningPartyId.set(rs.getInt(0x01));
            rs.close();
            ps.close();
        } 
        catch (SQLException e)
        {
            log.error("Cannot create instance of WorldRegistryImpl", e);
        }
        runningMessengerId.set(0x01);
    }

    public static WorldRegistryImpl getInstance() 
    {
        if (instance == null) 
        {
            try 
            {
                instance = new WorldRegistryImpl();
            }
            catch (RemoteException e) 
            {
                throw new RuntimeException(e);
            }
        }
        return instance;
    }

    private int getFreeChannelId() 
    {
        for (int i = 0x00; i < 0x1E; i++) 
        {
            if (!channelServer.containsKey(i))
                return i;
        }
        return -0x01;
    }

    public final WorldChannelInterface registerChannelServer(final String authKey, final ChannelWorldInterface cb) throws RemoteException 
    {
        try 
        { 
        	final Connection con = DatabaseConnection.getConnection();
        	
            try(final PreparedStatement ps = con.prepareStatement("SELECT * FROM channels WHERE `key` = SHA1(?) AND world = ?"))
            {
            	ps.setString(0x01, authKey);
                ps.setInt(0x02, WorldServer.getInstance().getWorldId());
                
                try(final ResultSet rs = ps.executeQuery())
                {
                	if (rs.next()) 
                    {
                        int channelId = rs.getInt("number");
                        
                        if (channelId < 0x01) 
                        {
                            channelId = getFreeChannelId();
                            
                            if (channelId == -0x01) 
                                throw new RuntimeException("Maximum channels reached");
                        }
                        else 
                        {
                            if (channelServer.containsKey(channelId)) 
                            {
                                final ChannelWorldInterface oldch = channelServer.get(channelId);
                                
                                try 
                                {
                                    oldch.shutdown(0x00);
                                }
                                catch (ConnectException ce)  { }
                            }
                        }
                        
                        channelServer.put(channelId, cb);
                        cb.setChannelId(channelId);

                        return new WorldChannelInterfaceImpl(cb, rs.getInt("channelid"));
                    }	
                }
            }   
        }
        catch (SQLException ex) 
        {
            log.error("Encountered database error while authenticating channelserver", ex);
        }
        throw new RuntimeException("Couldn't find a channel with the given key (" + authKey + ")");
    }

    public void deregisterChannelServer(int channel) throws RemoteException 
    {
        channelServer.remove(channel);
        
        for (LoginWorldInterface wli : loginServer) 
            wli.channelOffline(channel);

        log.info("Channel {} are now offline.", channel);
    }

    public final WorldLoginInterface registerLoginServer(final String authKey, final LoginWorldInterface cb) throws RemoteException 
    {
        log.info("Registering LOGIN SERVER...");
        
        try 
        {
            final Connection con = DatabaseConnection.getConnection();
            final PreparedStatement ps = con.prepareStatement("SELECT * FROM loginserver WHERE `key` = SHA1(?) AND world = ?");
            
            ps.setString(0x01, authKey);
            ps.setInt(0x02, WorldServer.getInstance().getWorldId());
            
            final ResultSet rs = ps.executeQuery();
            
            if (rs.next()) 
            {
                loginServer.add(cb);
                
                for (final ChannelWorldInterface cwi : channelServer.values()) 
                    cb.channelOnline(cwi.getChannelId(), cwi.getIP());
            }
            
            rs.close();
            ps.close();
            
            log.info("LOGIN SERVER registered successfully!");
        } 
        catch (Exception e) 
        {
            log.error("Encountered database error while authenticating loginserver", e);
        }
        
        return new WorldLoginInterfaceImpl();
    }

    public void deregisterLoginServer(final LoginWorldInterface cb) throws RemoteException 
    {
        loginServer.remove(cb);
    }

    public final List<LoginWorldInterface> getLoginServer() 
    {
        return new ArrayList<>(loginServer);
    }

    public final ChannelWorldInterface getChannel(final int channel) 
    {
        return channelServer.get(channel);
    }

    public final Set<Integer> getChannelServer() 
    {
        return new HashSet<>(channelServer.keySet());
    }

    public Collection<ChannelWorldInterface> getAllChannelServers() {
        return channelServer.values();
    }

    public MapleParty createParty(MaplePartyCharacter chrfor) {
        MapleParty party = new MapleParty(runningPartyId.getAndIncrement(), chrfor);
        parties.put(party.getId(), party);
        return party;
    }

    public MapleParty getParty(int partyid) {
        return parties.get(partyid);
    }

    public MapleParty disbandParty(int partyid) {
        return parties.remove(partyid);
    }

    public int createGuild(int leaderId, String name) 
    {
        return MapleGuild.createGuild(leaderId, name);
    }
    
    public void setFamily(MapleFamily family, int cid)
    {
    	try 
    	{
			getChannel(find(cid)).playerStorage().getCharacterById(cid).setMapleFamily(family);
		}
    	catch (RemoteException e) 
    	{
			e.printStackTrace();
		}
    }
    
    public final MapleFamily getFamily(final int id)
    {
    	synchronized (familys)
    	{
    		if(familys.get(id) != null)
    			return familys.get(id);
    		
    		final MapleFamily family = new MapleFamily(id);
    		
    		familys.put(id, family);
    		
    		return family;
    	}
    }
    
    public void removeFamily(final int id)
    {
    	synchronized (familys)
    	{
    		if(familys.get(id) == null)
    			return;
    		
    		familys.remove(id);
    	}
    }

    public final MapleGuild getGuild(int id) 
    {
        synchronized (guilds) 
        {
            if (guilds.get(id) != null)
                return guilds.get(id);

            final MapleGuild g = new MapleGuild(id);
            
            if (g.getId() == -0x01)
                return null;
            
            guilds.put(id, g);

            return g;
        }
    }

    public void clearGuilds() 
    {
        synchronized (guilds) 
        {
            guilds.clear();
        }
        
        try 
        {
            for (final ChannelWorldInterface cwi : getAllChannelServers()) 
                cwi.reloadGuildCharacters();
        }
        catch (RemoteException re) 
        {
            log.error("RemoteException occurred while attempting to reload guilds.", re);
        }
    }

    public void setGuildMemberOnline(final MapleGuildCharacter mgc, final boolean bOnline, final int channel) 
    {
        getGuild(mgc.getGuildId()).setOnline(mgc.getId(), bOnline, channel);
    }

    public int addGuildMember(MapleGuildCharacter mgc) 
    {
        try
        {
        	return guilds.get(mgc.getGuildId()).addGuildMember(mgc);
        }
        catch(NullPointerException npe)
        {
        	log.warn("Cannot find GUILD " + mgc.getGuildId(), npe);
        }
        return 0x00;
    }

    public void leaveGuild(MapleGuildCharacter mgc) 
    {
    	try
    	{
    		guilds.get(mgc.getGuildId()).leaveGuild(mgc);	
    	}
    	catch(NullPointerException npe)
        {
        	log.warn("Cannot find GUILD " + mgc.getGuildId(), npe);
        }
    }

    public boolean setGuildAllianceId(int gId, int aId) 
    {
    	try
    	{
    		guilds.get(gId).setAllianceId(aId);
    		return true;
    	}
    	catch(Exception e)
        {
        	log.warn("Cannot set Alliance G::" + gId + " A::" + aId, e);
        	return false;
        }
    }

    public void guildChat(int gid, String name, int cid, String msg) 
    {
        MapleGuild g = guilds.get(gid);
        
        if (g != null)
            g.guildChat(name, cid, msg);
    }

    public void changeRank(int gid, int cid, int newRank) 
    {
        MapleGuild g = guilds.get(gid);
        
        if (g != null)
            g.changeRank(cid, newRank);
    }

    public void expelMember(MapleGuildCharacter initiator, String name, int cid) 
    {
        MapleGuild g = guilds.get(initiator.getGuildId());
        
        if (g != null)
            g.expelMember(initiator, name, cid);
    }

    public void setGuildNotice(int gid, String notice) 
    {
        MapleGuild g = guilds.get(gid);
        
        if (g != null)
            g.setGuildNotice(notice);
    }

    public void memberLevelJobUpdate(MapleGuildCharacter mgc) 
    {
        MapleGuild g = guilds.get(mgc.getGuildId());
        
        if (g != null)
            g.memberLevelJobUpdate(mgc);
    }

    public void changeRankTitle(int gid, String[] ranks) 
    {
        MapleGuild g = guilds.get(gid);
        
        if (g != null)
            g.changeRankTitle(ranks);
    }

    public void setGuildEmblem(int gid, short bg, byte bgcolor, short logo, byte logocolor) 
    {
        MapleGuild g = guilds.get(gid);
        
        if (g != null)
            g.setGuildEmblem(bg, bgcolor, logo, logocolor);
    }

    public void disbandGuild(int gid)
    {
        synchronized (guilds) 
        {
            guilds.get(gid).disbandGuild();
            guilds.remove(gid);
        }
    }

    public boolean increaseGuildCapacity(int gid) 
    {
        MapleGuild g = guilds.get(gid);
        if (g != null)
            return g.increaseCapacity();
        return false;
    }

    public void gainGP(int gid, int amount) 
    {
        MapleGuild g = guilds.get(gid);
        
        if (g != null)
            g.gainGP(amount);
    }

    public MapleMessenger createMessenger(MapleMessengerCharacter chrfor) 
    {
        MapleMessenger messenger = new MapleMessenger(runningMessengerId.getAndIncrement(), chrfor);
        messengers.put(messenger.getId(), messenger);
        return messenger;
    }

    public MapleMessenger getMessenger(int messengerid) 
    {
        return messengers.get(messengerid);
    }

    public PlayerBuffStorage getPlayerBuffStorage() 
    {
        return buffStorage;
    }

    public void broadcastToGuild(int g, MaplePacket packet) 
    {
        MapleGuild guild = guilds.get(g);
        
        if (guild != null)
            guild.broadcast(packet);
    }

    public MapleAlliance getAlliance(int id) 
    {
        synchronized (alliances) 
        {
            if (alliances.containsKey(id)) 
                return alliances.get(id);

            return null;
        }
    }

    public void addAlliance(int id, MapleAlliance alliance) 
    {
        synchronized (alliances) 
        {
            if (!alliances.containsKey(id))
                alliances.put(id, alliance);
        }
    }

    public void disbandAlliance(int id)
    {
        synchronized (alliances) 
        {
            MapleAlliance alliance = alliances.get(id);
            
            if (alliance != null) 
            {
                for (Integer gid : alliance.getGuilds()) 
                    guilds.get(gid).setAllianceId(0x00);
                
                alliances.remove(id);
            }
        }
    }

    public void allianceMessage(int id, MaplePacket packet, int exception, int guildex) 
    {
        MapleAlliance alliance = alliances.get(id);
        if (alliance != null) 
        {
            for (Integer gid : alliance.getGuilds()) 
            {
                if (guildex == gid)
                    continue;

                MapleGuild guild = guilds.get(gid);
                if (guild != null)
                    guild.broadcast(packet, exception);
            }
        }
    }

    public boolean addGuildtoAlliance(int aId, int guildId) 
    {
        MapleAlliance alliance = alliances.get(aId);
        if (alliance != null) 
        {
            alliance.addGuild(guildId);
            return true;
        }
        return false;
    }

    public boolean removeGuildFromAlliance(int aId, int guildId)
    {
        MapleAlliance alliance = alliances.get(aId);
        if (alliance != null) 
        {
            alliance.removeGuild(guildId);
            return true;
        }
        return false;
    }

    public boolean setAllianceRanks(int aId, String[] ranks) 
    {
        MapleAlliance alliance = alliances.get(aId);
        if (alliance != null) 
        {
            alliance.setRankTitle(ranks);
            return true;
        }
        return false;
    }

    public boolean setAllianceNotice(int aId, String notice) 
    {
        MapleAlliance alliance = alliances.get(aId);
        if (alliance != null)
        {
            alliance.setNotice(notice);
            return true;
        }
        return false;
    }

    public boolean increaseAllianceCapacity(int aId, int inc) 
    {
        MapleAlliance alliance = alliances.get(aId);
        if (alliance != null) {
            alliance.increaseCapacity(inc);
            return true;
        }
        return false;
    }
    
    public int find(int characterId) throws RemoteException 
    {
        for (int i : getChannelServer()) 
        {
            ChannelWorldInterface cwi = getChannel(i);
            try 
            {
                if (cwi.isConnected(characterId))
                    return cwi.getChannelId();
            } 
            catch (RemoteException e) 
            {
                deregisterChannelServer(i);
            }
        }
        return -0x01;
    }
}