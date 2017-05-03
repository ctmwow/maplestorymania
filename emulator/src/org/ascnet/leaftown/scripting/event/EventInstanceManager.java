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

package org.ascnet.leaftown.scripting.event;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ScheduledFuture;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.script.ScriptException;

import org.ascnet.leaftown.client.MapleCharacter;
import org.ascnet.leaftown.database.DatabaseConnection;
import org.ascnet.leaftown.net.world.MapleParty;
import org.ascnet.leaftown.net.world.MaplePartyCharacter;
import org.ascnet.leaftown.server.MapleSquad;
import org.ascnet.leaftown.server.MapleSquadType;
import org.ascnet.leaftown.server.TimerManager;
import org.ascnet.leaftown.server.life.MapleMonster;
import org.ascnet.leaftown.server.maps.MapleMap;
import org.ascnet.leaftown.server.maps.MapleMapFactory;

/**
 * @author Matze
 */
public class EventInstanceManager 
{
    private final List<Integer> mapIds = new LinkedList<>();
    private final List<Boolean> isInstanced = new LinkedList<>();
    private final List<MapleCharacter> chars = new LinkedList<>();
    private final List<MapleMonster> mobs = new LinkedList<>();
    private final Map<MapleCharacter, Integer> killCount = new HashMap<>();
    private EventManager em;
    private MapleMapFactory mapFactory;
    private final String name;
    private final Properties props = new Properties();
    private long timeStarted = 0;
    private long eventTime = 0;

    public EventInstanceManager(EventManager em, String name) 
    {
        this.em = em;
        this.name = name;
        
        mapFactory = em.getChannelServer().getMapFactory();
    }
    
    public final MapleMap setInstanceMap(final int mapid) //gets instance map from the channelserv 
    {
        mapIds.add(mapid);
        isInstanced.add(false);
        
        return mapFactory.getMap(mapid);
    }

    public void registerPlayer(MapleCharacter chr) 
    {
        if (chr != null && chr.getEventInstance() == null) 
        {
            try 
            {
                chars.add(chr);
                chr.setEventInstance(this);
                em.getIv().invokeFunction("playerEntry", this, chr);
            }
            catch (ScriptException | NoSuchMethodException ex) 
            {
                Logger.getLogger(EventInstanceManager.class.getName()).log(Level.SEVERE, "Error found in event script " + em.getName(), ex);
            }
        }
    }

    public void unregisterPlayer(MapleCharacter chr) 
    {
        chars.remove(chr);
        chr.setEventInstance(null);
        
        if(chr.getMonsterCarnival() != null)
        	chr.resetCP();
        
        if(getPlayerCount() == 0)
        	dispose();
    }
    
    public void startEventTimer(long time) 
    {
        timeStarted = System.currentTimeMillis();
        eventTime = time;
    }

    public boolean isTimerStarted() 
    {
        return eventTime > 0 && timeStarted > 0;
    }

    public boolean isEventStarted()
    {
    	return props.getProperty("started", "false").equals("true");
    }
    
    public long getTimeLeft() 
    {
        return eventTime - (System.currentTimeMillis() - timeStarted);
    }
    
    public final void broadcastPlayerMsg(final int type, final String msg) 
    {
        for (MapleCharacter chr : getPlayers())
            chr.dropMessage(type, msg);
    }

    public void registerParty(MapleParty party, MapleMap map) 
    {
        for (MaplePartyCharacter pc : new ArrayList<>(party.getMembers())) 
        {
            if (pc.isOnline())
                registerPlayer(map.getCharacterById(pc.getId()));
        }
    }

    public void registerSquad(MapleSquad squad, MapleMap map) 
    {
        for (MapleCharacter player : new ArrayList<>(squad.getMembers())) 
        {
            if (map.getCharacterById(player.getId()) != null) 
                registerPlayer(player);
        }
    }

    public int getPlayerCount() 
    {
        return chars.size();
    }

    public List<MapleCharacter> getPlayers() 
    {
        return new ArrayList<>(chars);
    }

    public boolean allPlayersInMap(int mapid) 
    {
        int inMap = 0;
        for (MapleCharacter c : getPlayers()) 
        {
            if (c.getMapId() == mapid)
                inMap++;
        }
        return inMap >= getPlayerCount(); // Even though it should never be more than... lol
    }

    public void registerMonster(MapleMonster mob) 
    {
        mobs.add(mob);
        mob.setEventInstance(this);
    }

    public void unregisterMonster(MapleMonster mob) 
    {
        if (mob != null) 
        {
            mobs.remove(mob);
            mob.setEventInstance(null);
        }
        if (mobs.isEmpty()) 
        {
            try 
            {
                em.getIv().invokeFunction("allMonstersDead", this);
            } 
            catch (ScriptException | NoSuchMethodException ex) 
            {
                Logger.getLogger(EventInstanceManager.class.getName()).log(Level.SEVERE, "Error found in event script " + em.getName(), ex);
            }
        }
    }

    public void playerKilled(MapleCharacter chr) 
    {
        try 
        {
            em.getIv().invokeFunction("playerDead", this, chr);
        } 
        catch (ScriptException | NoSuchMethodException ex) 
        {
            Logger.getLogger(EventInstanceManager.class.getName()).log(Level.SEVERE, "Error found in event script " + em.getName(), ex);
        }
    }

    public boolean revivePlayer(MapleCharacter chr) 
    {
        try 
        {
            Object b = em.getIv().invokeFunction("playerRevive", this, chr);
            if (b instanceof Boolean) 
                return (Boolean) b;
        } 
        catch (ScriptException | NoSuchMethodException ex) 
        {
            Logger.getLogger(EventInstanceManager.class.getName()).log(Level.SEVERE, "Error found in event script " + em.getName(), ex);
        }
        return true;
    }

    public void playerDisconnected(MapleCharacter chr) 
    {
        try 
        {
            em.getIv().invokeFunction("playerDisconnected", this, chr);
        } 
        catch (ScriptException | NoSuchMethodException ex) 
        {
            Logger.getLogger(EventInstanceManager.class.getName()).log(Level.SEVERE, "Error found in event script " + em.getName(), ex);
        }
    }

    public void playerMapExit(MapleCharacter chr) 
    {
        try 
        {
            em.getIv().invokeFunction("playerMapExit", this, chr);
        } 
        catch (ScriptException | NoSuchMethodException ex) 
        {
            Logger.getLogger(EventInstanceManager.class.getName()).log(Level.SEVERE, "Error found in event script " + em.getName(), ex);
        }
    }

    public void monsterKilled(final MapleCharacter chr, final MapleMonster mob) 
    {
        try 
        {
            int inc = ((Double) em.getIv().invokeFunction("monsterValue", this, mob.getId())).intValue();
            if (chr == null)
                return;
            
            Integer kc = killCount.get(chr.getId());
            
            if (kc == null)
                kc = inc;
            else
                kc += inc;
            
            killCount.put(chr, kc);
            
            if (chr.getMonsterCarnival() != null && (mob.getStats().getPoint() > 0 || mob.getStats().getCP() > 0)) 
                em.getIv().invokeFunction("monsterKilled", this, chr, mob.getStats().getCP() > 0 ? mob.getStats().getCP() : mob.getStats().getPoint());
        } 
        catch (ScriptException | NoSuchMethodException ex) 
        {
            Logger.getLogger(EventInstanceManager.class.getName()).log(Level.SEVERE, "Error found in event script " + em.getName(), ex);
        }
    }
    
    public void monsterKilled(MapleCharacter chr, int mobId) 
    {
        try 
        {
            Integer kc = killCount.get(chr);
            int inc = ((Double) em.getIv().invokeFunction("monsterValue", this, mobId)).intValue();
            
            if (kc == null)
                kc = inc;
            else
                kc += inc;
                
            killCount.put(chr, kc);
        } 
        catch (ScriptException | NoSuchMethodException ex) 
        {
            Logger.getLogger(EventInstanceManager.class.getName()).log(Level.SEVERE, "Error found in event script " + em.getName(), ex);
        }
    }

    public int getKillCount(MapleCharacter chr) 
    {
        Integer kc = killCount.get(chr);
        
        if (kc == null)
            return 0;
        else
            return kc;
    }

    public void dispose() 
    {
        chars.clear();
        mobs.clear();
        killCount.clear();
        mapFactory = null;
        em.disposeInstance(name);
        em = null;
    }

    public MapleMapFactory getMapFactory() 
    {
        return mapFactory;
    }

    public ScheduledFuture<?> schedule(final String methodName, long delay) 
    {
        return TimerManager.getInstance().schedule(new Runnable() 
        {
            public void run() 
            {
                try {
                    em.getIv().invokeFunction(methodName, EventInstanceManager.this);
                } catch (NullPointerException npe) {
                } catch (ScriptException | NoSuchMethodException ex) {
                    Logger.getLogger(EventManager.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }, delay);
    }

    public String getName() 
    {
        return name;
    }

    public void saveWinner(MapleCharacter chr) 
    {
        try 
        {
            Connection con = DatabaseConnection.getConnection();
            PreparedStatement ps = con.prepareStatement("INSERT INTO eventstats (event, instance, characterid, channel) VALUES (?, ?, ?, ?)");
            ps.setString(1, em.getName());
            ps.setString(2, name);
            ps.setInt(3, chr.getId());
            ps.setInt(4, chr.getClient().getChannel());
            ps.executeUpdate();
            ps.close();
        }
        catch (SQLException ex) 
        {
            Logger.getLogger(EventInstanceManager.class.getName()).log(Level.SEVERE, "Error found in event script " + em.getName(), ex);
        }
    }

    public final MapleMap getMapInstance(int mapId) 
    {
        boolean instanced = false;
        int trueMapID;
        
        if (mapId >= mapIds.size()) 
            trueMapID = mapId;
        else 
        {
            trueMapID = mapIds.get(mapId);
            instanced = isInstanced.get(mapId);
        }
        
        final MapleMap map;
        if (!instanced) 
        {
            map = mapFactory.getMap(trueMapID);
            
            if (map == null)
                return null;
           
            if (map.countCharsOnMap() == 0x00000000)
                if (em.getProperty("shuffleReactors") != null && em.getProperty("shuffleReactors").equals("true"))
                    map.shuffleReactors();
        } 
        else 
        {
            map = mapFactory.getMap(trueMapID, true);
            
            if (map == null)
                return null;
            
            if (map.countCharsOnMap() == 0x00000000) 
                if (em.getProperty("shuffleReactors") != null && em.getProperty("shuffleReactors").equals("true"))
                    map.shuffleReactors();
        }
        return map;
    }

    public void setProperty(String key, String value) 
    {
        props.setProperty(key, value);
    }

    public Object setProperty(String key, String value, boolean prev) 
    {
        return props.setProperty(key, value);
    }

    public String getProperty(String key) 
    {
        return props.getProperty(key);
    }

    public void leftParty(MapleCharacter chr) 
    {
        try 
        {
            em.getIv().invokeFunction("leftParty", this, chr);
        } 
        catch (ScriptException | NoSuchMethodException ex) 
        {
            Logger.getLogger(EventInstanceManager.class.getName()).log(Level.SEVERE, "Error found in event script " + em.getName(), ex);
        }
    }

    public void disbandParty() 
    {
        try 
        {
            em.getIv().invokeFunction("disbandParty", this);
        } 
        catch (ScriptException | NoSuchMethodException ex) 
        {
            Logger.getLogger(EventInstanceManager.class.getName()).log(Level.SEVERE, "Error found in event script " + em.getName(), ex);
        }
    }

    //Separate function to warp players to a "finish" map, if applicable
    public void finishPQ() 
    {
        try 
        {
            em.getIv().invokeFunction("clearPQ", this);
        } 
        catch (ScriptException | NoSuchMethodException ex) 
        {
            Logger.getLogger(EventInstanceManager.class.getName()).log(Level.SEVERE, "Error found in event script " + em.getName(), ex);
        }
    }

    public void removePlayer(MapleCharacter chr) 
    {
        try 
        {
            em.getIv().invokeFunction("playerExit", this, chr);
        } 
        catch (ScriptException | NoSuchMethodException ex) 
        {
            Logger.getLogger(EventInstanceManager.class.getName()).log(Level.SEVERE, "Error found in event script " + em.getName(), ex);
        }
    }

    public boolean isPartyLeader(MapleCharacter chr) 
    {
        return chr.getParty().getLeader().getId() == chr.getId();
    }
    
    public boolean isSquadLeader(MapleCharacter chr, MapleSquadType mst) {
        return (chr.getClient().getChannelServer().getMapleSquad(mst).getLeader().equals(chr));
    }

    public void saveAllBossQuestPoints(int bossPoints) 
    {
        for (MapleCharacter character : chars) 
            character.setBossPoints(character.getBossPoints() + bossPoints);
    }

    public void saveBossQuestPoints(int bossPoints, MapleCharacter character) 
    {
        character.setBossPoints(character.getBossPoints() + bossPoints);
    }
}