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

package org.ascnet.leaftown.scripting;

import org.ascnet.leaftown.client.Equip;
import org.ascnet.leaftown.client.IItem;
import org.ascnet.leaftown.client.Item;
import org.ascnet.leaftown.client.MapleCharacter;
import org.ascnet.leaftown.client.MapleClient;
import org.ascnet.leaftown.client.MapleInventory;
import org.ascnet.leaftown.client.MapleInventoryType;
import org.ascnet.leaftown.client.MapleJob;
import org.ascnet.leaftown.client.MaplePet;
import org.ascnet.leaftown.client.MapleQuestStatus;
import org.ascnet.leaftown.client.SkillFactory;
import org.ascnet.leaftown.net.MaplePacket;
import org.ascnet.leaftown.net.world.MapleParty;
import org.ascnet.leaftown.net.world.guild.MapleGuild;
import org.ascnet.leaftown.scripting.event.EventManager;
import org.ascnet.leaftown.scripting.npc.NPCScriptManager;
import org.ascnet.leaftown.server.MapleInventoryManipulator;
import org.ascnet.leaftown.server.MapleItemInformationProvider;
import org.ascnet.leaftown.server.MaplePortal;
import org.ascnet.leaftown.server.TimerManager;
import org.ascnet.leaftown.server.maps.MapMonitor;
import org.ascnet.leaftown.server.maps.MapleMap;
import org.ascnet.leaftown.server.maps.MapleReactor;
import org.ascnet.leaftown.server.quest.MapleQuest;
import org.ascnet.leaftown.tools.MaplePacketCreator;
import org.ascnet.leaftown.tools.StringUtil;

import java.awt.Point;
import java.rmi.RemoteException;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class AbstractPlayerInteraction 
{
    private final MapleClient c;

    public AbstractPlayerInteraction(MapleClient c) 
    {
        this.c = c;
    }

    public MapleClient getClient() 
    {
        return c;
    }

    public MapleCharacter getPlayer() 
    {
        return c.getPlayer();
    }

    public EventManager getEventManager(String event) 
    {
        return c.getChannelServer().getEventSM().getEventManager(event);
    }

    public void warp(int mapId) 
    {
        warp(mapId, -0x01);
    }

    public void warp(int map, String portalName) 
    {
        warp(map, getWarpMap(map).getPortal(portalName).getId());
    } 

    public void warp(int mapId, int portal) 
    {
        MapleMap target = getWarpMap(mapId);
        
        if (target.canEnter() && getPlayer().getMap().canExit() || getPlayer().isGM()) 
            getPlayer().changeMap(target, portal != -0x01 && target.getPortal(portal) != null ? target.getPortal(portal) : target.getRandomSpawnPoint());
        else 
            c.sendPacket(MaplePacketCreator.serverNotice(0x05, "O mapa que você está tentando entrar não pode ser acessado ou você não pode sair do mapa atual! Portanto você não será teleportado.")); 
    }

    public void warp(MapleCharacter mc, int mapId, String ptl) 
    {
        MapleMap target = getWarpMap(mapId);
        
        if (target.canEnter() && mc.getMap().canExit() || mc.isGM()) 
            mc.changeMap(target, target.getPortal(ptl));
        else 
            mc.getClient().sendPacket(MaplePacketCreator.serverNotice(0x05, "O mapa que você está tentando entrar não pode ser acessado ou você não pode sair do mapa atual! Portanto você não será teleportado."));
    }

    private MapleMap getWarpMap(int map) 
    {
        MapleMap target;
        
        if (getPlayer().getEventInstance() == null) 
            target = c.getChannelServer().getMapFactory().getMap(map);
        else 
            target = getPlayer().getEventInstance().getMapInstance(map);

        return target;
    }

    public MapleMap getMap(int map) 
    {
        return getWarpMap(map);
    }

    public boolean haveItem(int itemid) 
    {
        return haveItem(itemid, 1);
    }

    public boolean haveItem(int itemid, int quantity) 
    {
        return haveItem(itemid, quantity, false, false);
    }

    public boolean haveItem(int itemid, int quantity, boolean checkEquipped, boolean exact) 
    {
        return getPlayer().haveItem(itemid, quantity, checkEquipped, exact);
    }

    public boolean canHold() 
    {
        return canHold(0x00, true);
    }

    public boolean canHold(int itemid) // Mainly for gach 
    { 
        return canHold(itemid, true);
    }

    public boolean canHold(int itemid, boolean fullInvent) 
    {
        if (!fullInvent) 
        {
            if (getPlayer().getInventory(MapleItemInformationProvider.getInstance().getInventoryType(itemid)).getNextFreeSlot() == -0x01)
                return false;
        }
        else 
        {
            for (int i = 0x01; i <= 0x05; i++) 
            {
                if (getPlayer().getInventory(MapleInventoryType.getByType((byte) i)).getNextFreeSlot() == -0x01)
                    return false;
            }
        }
        return true;
    }

    public boolean canHold(int[] items) 
    {
        return MapleInventoryManipulator.canHold(c, items);
    }

    public void showNPCAnimation(int npcId, String info) 
    {
        getPlayer().getMap().broadcastMessage(MaplePacketCreator.npcAnimation(getPlayer().getMap().getNPCbyID(npcId), info));
    }

    public void completeCustomQuest(int id) 
    {
        MapleQuest.getInstance(id).completeCustomQuest(getPlayer(), 0x00);
    }

    public MapleQuestStatus.Status getQuestStatus(int id) 
    {
        return getPlayer().getQuest(id).getStatus();
    }

    public int getQuestStatusId(int id) 
    {
        return getPlayer().getQuest(id).getStatus().getId();
    }

    public MapleJob getJob() 
    {
        return getPlayer().getJob();
    }

    public void gainItem(int id, short quantity) 
    {
        gainItem(id, quantity, false, getPlayer(), null);
    }

    public void gainItem(int id, short quantity, boolean r) 
    {
        gainItem(id, quantity, r, getPlayer(), null);
    }

    public int getQuestInfoInt(int id) 
    {
        return Integer.parseInt(getPlayer().getQuest(id).getQuestRecord());
    }

    public String getQuestInfo(int id, int startIndex) 
    {
        return getQuestInfo(id, startIndex, false);
    }

    public String getQuestInfo(int id, int startIndex, boolean questEx) 
    {
        if (questEx)
            return getPlayer().getQuestEx(id).getQuestRecord().substring(startIndex, startIndex + 0x01);
        
        return getPlayer().getQuest(id).getQuestRecord().substring(startIndex, startIndex + 0x01);
    }

    public String getQuestInfo(int id) 
    {
        return getPlayer().getQuest(id).getQuestRecord();
    }

    public void setQuestInfo(int id, String info) 
    {
        MapleQuest.getInstance(id).setQuestInfo(getPlayer(), info, true, false);
    }

    public void setQuestInfo(int id, int index, String info) 
    {
        setQuestInfo(id, index, info, false);
    }

    public void setQuestInfo(int id, int index, String info, boolean questEx) 
    {
        final StringBuilder newInfo = new StringBuilder();
    	final MapleQuest quest = MapleQuest.getInstance(id);
        final char[] originalInfo = questEx ? getPlayer().getQuestEx(id).getQuestRecord().toCharArray() : getPlayer().getQuest(id).getQuestRecord().toCharArray();
        
        originalInfo[index] = info.charAt(0x00);
        
        for (char element : originalInfo)
            newInfo.append(element);
        
        if (questEx)
            quest.setQuestRecordExInfo(getPlayer(), newInfo.toString());
        else
            quest.setQuestInfo(getPlayer(), newInfo.toString(), true, false);
    }

    public String getQuestExInfo(int id)
    {
        return getPlayer().getQuestEx(id).getQuestRecord();
    }

    public void setQuestExInfo(int id, String info) 
    {
        MapleQuest.getInstance(id).setQuestRecordExInfo(getPlayer(), info);
    }

    public void startNPC(int id) 
    {
        NPCScriptManager.getInstance().start(c, id);
    }

    public void setTimeOut(long time, final int mapId) 
    {
        TimerManager.getInstance().schedule(new Runnable() 
        {
            public void run() 
            {
                final MapleMap outMap = c.getChannelServer().getMapFactory().getMap(mapId);
                
                for (MapleCharacter player : getPlayer().getMap().getCharacters()) 
                    player.getClient().getPlayer().changeMap(outMap, outMap.getRandomSpawnPoint());
            }
        }, time);
    }

    /**
     * Gives item with the specified id or takes it if the quantity is negative. Note that this does NOT take items from the equipped inventory. randomStats for generating random stats on the generated equip.
     *
     * @param id
     * @param quantity
     * @param randomStats
     */
    public void gainItem(int id, short quantity, boolean randomStats, MapleCharacter player, MaplePet from) 
    {
        if (quantity >= 0x00) 
        {
            final MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
            IItem item = ii.getEquipById(id);
            MapleInventoryType type = ii.getInventoryType(id);
            StringBuilder logInfo = new StringBuilder(player.getName());
            logInfo.append(" received ");
            logInfo.append(quantity);
            logInfo.append(" from a scripted PlayerInteraction (");
            logInfo.append(toString());
            logInfo.append(")");
            
            
            MaplePet evolved = null;
            int petId = -1;
			if (id >= 5000000 && id <= 5000100) {
                petId = MaplePet.createPet(getPlayer().getId(), id).getUniqueId();
		                    
	            if(from != null) {
	                evolved = MaplePet.loadFromDb(id, petId);
	                
	                Point pos = getPlayer().getPosition();
	                pos.y -= 12;
	                evolved.setPos(pos);
	                evolved.setFh(getPlayer().getMap().getFootholds().findBelow(evolved.getPos()).getId());
	                evolved.setStance(0);
	                evolved.setSummoned(true);
	
	                evolved.setName(from.getName());
	                
	                if (from.getName().equals(MapleItemInformationProvider.getInstance().getName(from.getItemId())))
	                	evolved.setName(MapleItemInformationProvider.getInstance().getName(id));
	                else
	                	evolved.setName(from.getName());
	                
	                
	                evolved.setCloseness(from.getCloseness());
	                evolved.setFullness(from.getFullness());
	                evolved.setLevel(from.getLevel());
	                evolved.saveToDb();
	            }
		                    
				//MapleInventoryManipulator.addById(c, id, (short) 1, null, petId, expires == -1 ? -1 : System.currentTimeMillis() + expires);
			}
		            
            if (!MapleInventoryManipulator.checkSpace(player.getClient(), id, quantity, "")) 
            {
                c.sendPacket(MaplePacketCreator.serverNotice(0x01, "O seu inventário está cheio. Por favor, remova um item do seu " + type.name() + " inventory."));
                return;
            }
            
            if (type.equals(MapleInventoryType.EQUIP) && !ii.isThrowingStar(item.getItemId()) && !ii.isShootingBullet(item.getItemId())) 
            {
                if (randomStats) 
                    c.sendPacket(MaplePacketCreator.modifyInventory(true, MapleInventoryManipulator.addByItem(player.getClient(), ii.randomizeStats((Equip) item), logInfo.toString(), false)));
                else 
                    c.sendPacket(MaplePacketCreator.modifyInventory(true, MapleInventoryManipulator.addByItem(player.getClient(), item, logInfo.toString(), false)));
            }
            else 
            {
            	if (petId == -1)
            		MapleInventoryManipulator.addById(player.getClient(), id, quantity, logInfo.toString(), null, null);
            	else          
            		MapleInventoryManipulator.addById(player.getClient(), id, quantity, logInfo.toString(), getPlayer().getName(), evolved);
            }
        }
        else 
            MapleInventoryManipulator.removeById(player.getClient(), MapleItemInformationProvider.getInstance().getInventoryType(id), id, -quantity, true, false);

        player.getClient().sendPacket(MaplePacketCreator.getShowItemGain(id, quantity, true));
    }

    public void silentRemoveEquipped(short slot) 
    {
        c.sendPacket(MaplePacketCreator.modifyInventory(false, Collections.singletonList(MapleInventoryManipulator.removeItemFromSlot(c, MapleInventoryType.EQUIPPED, slot, (short) 0x01, false))));
    }

    public void changeMusic(String songName) 
    {
        getPlayer().getMap().broadcastMessage(MaplePacketCreator.musicChange(songName));
    }

    public void environmentChange(byte effect, String path) 
    {
        c.sendPacket(MaplePacketCreator.environmentChange(path, effect));
    }

    public void playerMessage(String message)
    {
        c.sendPacket(MaplePacketCreator.playerMessage(message));
    }
    
    public void mapMessage(String message)
    {
        mapMessage(0x05, message);
    }

    public void guildMessage(String message) 
    {
        guildMessage(0x05, message);
    }

    public void topMessage(String message) 
    {
        c.sendPacket(MaplePacketCreator.topMessage(message));
    }

    public void playerMessage(int type, String message) 
    {
        c.sendPacket(MaplePacketCreator.serverNotice(type, message));
    }

    public void mapMessage(int type, String message)
    {
        getPlayer().getMap().broadcastMessage(MaplePacketCreator.serverNotice(type, message));
    }

    public void guildMessage(int type, String message) 
    {
        MapleGuild guild = getGuild();
        
        if (guild != null) 
            guild.guildMessage(MaplePacketCreator.serverNotice(type, message));
    }

    public MapleGuild getGuild() 
    {
        try 
        {
            return c.getChannelServer().getWorldInterface().getGuild(getPlayer().getGuildId());
        }
        catch (RemoteException ex) 
        {
            Logger.getLogger(AbstractPlayerInteraction.class.getName()).log(Level.SEVERE, null, ex);
            c.getChannelServer().reconnectWorld();
        }
        return null;
    }
    
    public MapleGuild getGuild(int guildId) 
    {
        try 
        {
            return c.getChannelServer().getWorldInterface().getGuild(guildId);
        }
        catch (RemoteException ex) 
        {
            Logger.getLogger(AbstractPlayerInteraction.class.getName()).log(Level.SEVERE, null, ex);
            c.getChannelServer().reconnectWorld();
        }
        return null;
    }

    public void gainGP(int amount) 
    {
        try 
        {
            c.getChannelServer().getWorldInterface().gainGP(getPlayer().getGuildId(), amount);
        }
        catch (RemoteException e) 
        {
            c.getChannelServer().reconnectWorld();
        }
    }

    public MapleParty getParty() 
    {
        return getPlayer().getParty();
    }

    public boolean isPartyLeader() 
    {
        return getParty() != null && getParty().getLeader().getId() == getPlayer().getId();
    }

    /** PQ methods: give items/exp to all party members */
    public void givePartyItems(int id, short quantity, List<MapleCharacter> party) 
    {
        for (MapleCharacter chr : party) 
        {
            final MapleClient cl = chr.getClient();
            
            if (quantity >= 0x00) 
                MapleInventoryManipulator.addById(cl, id, quantity, cl.getPlayer().getName() + " received " + quantity + " from event " + chr.getEventInstance().getName(), null, null);
            else 
                MapleInventoryManipulator.removeById(cl, MapleItemInformationProvider.getInstance().getInventoryType(id), id, -quantity, true, false);
            
            cl.sendPacket(MaplePacketCreator.getShowItemGain(id, quantity, true));
        }
    }

    /** PQ gain EXP: Multiplied by channel rate here to allow global values to be input direct into NPCs */
    public void givePartyExp(int amount, List<MapleCharacter> party) 
    {
        for (MapleCharacter chr : party) 
        {
            chr.gainExp(amount * c.getChannelServer().getExpRate(), true, true);
        }
    }

    /** remove all items of type from party; combination of haveItem and gainItem */
    public void removeFromParty(int id, List<MapleCharacter> party) 
    {
        for (MapleCharacter chr : party) 
        {
            final MapleClient cl = chr.getClient();
            final int possessed = cl.getPlayer().getInventory(MapleItemInformationProvider.getInstance().getInventoryType(id)).countById(id);

            if (possessed > 0x00) 
            {
                MapleInventoryManipulator.removeById(cl, MapleItemInformationProvider.getInstance().getInventoryType(id), id, possessed, true, false);
                cl.sendPacket(MaplePacketCreator.getShowItemGain(id, (short) -possessed, true));
            }
        }
    }

    public void removeAll(int id) 
    {
        removeAll(id, c);
    }

    /** remove all items of type from character; combination of haveItem and gainItem */
    public void removeAll(int id, MapleClient cl) 
    {
        MapleInventoryType type = MapleItemInformationProvider.getInstance().getInventoryType(id);
        MapleInventory iv = cl.getPlayer().getInventory(type);
        int possessed = iv.countById(id);

        if (possessed > 0x00) 
        {
            MapleInventoryManipulator.removeById(cl, MapleItemInformationProvider.getInstance().getInventoryType(id), id, possessed, true, false);
            cl.sendPacket(MaplePacketCreator.getShowItemGain(id, (short) -possessed, true));
        }
    }

    public void gainCloseness(int closeness, int index) 
    {
        MaplePet pet = getPlayer().getPet(index);
        if (pet != null) 
        {
            pet.setCloseness(pet.getCloseness() + closeness);
            c.getPlayer().updatePet(pet);
        }
    }

    public void gainClosenessAll(int closeness) 
    {
        for (MaplePet pet : getPlayer().getPets()) 
        {
            pet.setCloseness(pet.getCloseness() + closeness);
            c.getPlayer().updatePet(pet);
        }
    }

    public int getMapId() 
    {
        return getPlayer().getMap().getId();
    }

    public int getPlayerCount(int mapid) 
    {
        return c.getChannelServer().getMapFactory().getMap(mapid).countCharsOnMap();
    }

    public int getCurrentPartyId(int mapid) 
    {
        for (MapleCharacter chr : getMap(mapid).getCharacters()) 
        {
            if (chr.getPartyId() != -0x01)
                return chr.getPartyId();
        }
        return -0x01;
    }

    public void sendPlayerHint(String hint, int width, int height) 
    {
        c.sendPacket(MaplePacketCreator.sendPlayerHint(hint, width, height));
    }

    public void worldMessage(int type, String message) 
    {
        MaplePacket packet = MaplePacketCreator.serverNotice(type, message);
        MapleCharacter chr = getPlayer();
        
        try 
        {
            chr.getClient().getChannelServer().getWorldInterface().broadcastMessage(chr.getName(), packet.getBytes());
        }
        catch (RemoteException e) 
        {
            chr.getClient().getChannelServer().reconnectWorld();
        }
    }
    
    public void mapEffect(String path) 
    {
    	c.sendPacket(MaplePacketCreator.mapEffect(path));
    }

    public void createMapMonitor(int mapId, boolean closePortal, int portalMap, String portalName, int reactorMap, int reactor) 
    {
        MaplePortal portal = null;
        if (closePortal) 
        {
            portal = c.getChannelServer().getMapFactory().getMap(portalMap).getPortal(portalName);
            portal.setPortalStatus(MaplePortal.CLOSED);
        }
        
        MapleReactor r = null;
        
        if (reactor > -0x01) 
        {
            r = c.getChannelServer().getMapFactory().getMap(reactorMap).getReactorById(reactor);
            r.setState((byte) 0x01);
            c.getChannelServer().getMapFactory().getMap(reactorMap).broadcastMessage(MaplePacketCreator.triggerReactor(r, 0x01));
        }
        new MapMonitor(c.getChannelServer().getMapFactory().getMap(mapId), closePortal ? portal : null, c.getChannel(), r);
    }

    public void createMapMonitor(int mapId, boolean closePortal, int portalMap, String portalName, int reactorMap, int reactor, long initialDelay) 
    {
        MaplePortal portal = null;
        
        if (closePortal) 
        {
            portal = c.getChannelServer().getMapFactory().getMap(portalMap).getPortal(portalName);
            portal.setPortalStatus(MaplePortal.CLOSED);
        }
        
        MapleReactor r = null;
        
        if (reactor > -0x01) 
        {
            r = c.getChannelServer().getMapFactory().getMap(reactorMap).getReactorById(reactor);
            r.setState((byte) 0x01);
            c.getChannelServer().getMapFactory().getMap(reactorMap).broadcastMessage(MaplePacketCreator.triggerReactor(r, 0x01));
        }
        
        new MapMonitor(c.getChannelServer().getMapFactory().getMap(mapId), closePortal ? portal : null, c.getChannel(), r, initialDelay);
    }

    public int getBossLog(String bossid)
    {
        return getPlayer().getBossLog(bossid);
    }

    public void setBossLog(String bossid) 
    {
        getPlayer().setBossLog(bossid);
    }

    public void showAnimationEffect(byte effect, String path)
    {
        if (effect == 18 && path.startsWith("Effect")) 
            c.getPlayer().setAutoChangeMapId(MapleItemInformationProvider.getInstance().getAutoChangeMapId(path));

        c.sendPacket(MaplePacketCreator.showAnimationEffect(effect, path));
    }

    public void showAnimationEffect(byte effect, String path, int num) 
    {
        c.sendPacket(MaplePacketCreator.showAnimationEffect(effect, path, num));
    }

    public void playPortalSE() 
    {
        c.sendPacket(MaplePacketCreator.showAnimationEffect((byte) 7));
    }

    public void hideUI(boolean enable) 
    {
        getPlayer().setUILocked(enable);
        c.sendPacket(MaplePacketCreator.hideUI(enable));
    }

    public void lockWindows(boolean enable) 
    {
        c.sendPacket(MaplePacketCreator.lockWindows(enable));
    }

    public void giveBuff(int itemId, boolean desc) 
    {
        MapleItemInformationProvider.getInstance().getItemEffect(itemId).applyTo(getPlayer());
        
        if (desc)
            c.sendPacket(MaplePacketCreator.buffInfo(itemId));
    }

    public String currentTime() 
    {
        Calendar cal = Calendar.getInstance();
        return StringUtil.getLeftPaddedStr(String.valueOf(cal.get(Calendar.YEAR) - 2000), '0', 2) + "/" + StringUtil.getLeftPaddedStr(String.valueOf(cal.get(Calendar.MONTH)), '0', 2) + "/" + StringUtil.getLeftPaddedStr(String.valueOf(cal.get(Calendar.DATE)), '0', 2) + "/" + StringUtil.getLeftPaddedStr(String.valueOf(cal.get(Calendar.HOUR_OF_DAY)), '0', 2) + "/" + StringUtil.getLeftPaddedStr(String.valueOf(cal.get(Calendar.MINUTE)), '0', 2);
    }

    public int compareTime(String time1, String time2) 
    {
        Calendar cal1 = Calendar.getInstance();
        Calendar cal2 = Calendar.getInstance();
        String[] time1Array = time1.split("/");
        String[] time2Array = time2.split("/");
        cal1.set(Integer.valueOf(2000 + time1Array[0]), Integer.valueOf(time1Array[1]), Integer.valueOf(time1Array[2]), Integer.valueOf(time1Array[3]), Integer.valueOf(time1Array[4]));
        cal2.set(Integer.valueOf(2000 + time2Array[0]), Integer.valueOf(time2Array[1]), Integer.valueOf(time2Array[2]), Integer.valueOf(time2Array[3]), Integer.valueOf(time2Array[4]));
        return (int) ((cal1.getTimeInMillis() - cal2.getTimeInMillis()) / 60000);
    }

    public void exchange(int meso, int[] items, boolean randomizeEquipStats) {
        MapleInventoryManipulator.exchange(c, meso, items, randomizeEquipStats);
    }

    public String getPartyVar(String name) {
        MapleParty party = getPlayer().getParty();
        return party != null ? party.getVar(name) : "";
    }

    public void addPartyVar(String name, String val) {
        MapleParty party = getPlayer().getParty();
        if (party != null)
            party.addVar(name, val);
    }
     
    public void removeEquipFromSlot(short slot) 
    {
		IItem tempItem = c.getPlayer().getInventory(MapleInventoryType.EQUIPPED).getItem(slot);
		
		if(tempItem != null)
			MapleInventoryManipulator.removeFromSlot(c, MapleInventoryType.EQUIPPED, slot, tempItem.getQuantity(), false, false, true);
    }
    
    public void sendClock(final MapleClient c, int time) 
    {
    	c.sendPacket(MaplePacketCreator.getClock(time));
    }
     
    public void useItem(int id) 
    {
		MapleItemInformationProvider.getInstance().getItemEffect(id).applyTo(c.getPlayer());
		c.sendPacket(MaplePacketCreator.showItemMessage(id));
    }

    public void removeNPC(int mapid, int npcid) {
        c.getChannelServer().getMapFactory().getMap(mapid).removeNPC(npcid);
    }

    public void removeNPC(int npcid) {
        c.getPlayer().getMap().removeNPC(npcid);
    }
 
    public void addNPC(int npcid, int x, int y, int mapid) 
    {
        c.getChannelServer().getMapFactory().getMap(mapid).addNPC(npcid, new Point(x, y));
    }

    public void addNPC(int npcid, int x, int y) 
    {
        c.getPlayer().getMap().addNPC(npcid, new Point(x, y));
    }
    
    public boolean isQuestStarted(int quest) 
    {
        try 
        {
            return getQuestStatus(quest) == MapleQuestStatus.Status.STARTED;
        } 
        catch (NullPointerException e) 
        {
            return false;
        }
    }
    
    public boolean isQuestCompleted(int quest) 
    {
        try 
        {
            return getQuestStatus(quest) == MapleQuestStatus.Status.COMPLETED;
        } 
        catch (NullPointerException e) 
        {
            return false;
        }
    }
    
    public void updateQuest(int questid, String data) 
    {
    	MapleQuest quest = MapleQuest.getInstance(questid);
    	quest.setQuestRecordExInfo(getPlayer(), data);
    	quest.start(getPlayer(), 0x00);
    	
    	getPlayer().updateQuest(getPlayer().getQuest(questid), true, true, false);
    }
    
    public void updateQuest(int questid, int npcId, String data) 
    {
		if(data.equals("1")) 
			MapleQuest.getInstance(questid).start(getPlayer(), npcId, false);
		else
			System.out.println("Not found updateQuest for data " + data);
    }
    
    public void showIntro(String path) 
    {
    	c.sendPacket(MaplePacketCreator.showIntro(path));
    }
    
    public void message(String data)
    {
    	c.getPlayer().dropMessage(0x05, data);
    }
    
    public boolean containsAreaInfo(short area, String info) 
    {
    	return c.getPlayer().containsAreaInfo(area, info);
    }
     
    public void updateAreaInfo(Short area, String info) 
    {
		c.getPlayer().updateAreaInfo(area, info);
		c.sendPacket(MaplePacketCreator.enableActions()); 
    } 
    
    public void teachSkill(int skillid, byte level, byte masterLevel, long expiration) 
    { 
        getPlayer().changeSkillLevel(SkillFactory.getSkill(skillid), level, masterLevel, expiration);
    }
     
    public void showInfo(String path) 
    {
    	c.sendPacket(MaplePacketCreator.showInfo(path));
        c.sendPacket(MaplePacketCreator.enableActions());
    }
    
    public void lockUI() 
    {
        c.sendPacket(MaplePacketCreator.hideUI(true));
        c.sendPacket(MaplePacketCreator.lockWindows(true));
    }
     
    public void unlockUI() 
    {
        c.sendPacket(MaplePacketCreator.hideUI(false)); 
        c.sendPacket(MaplePacketCreator.lockWindows(false));
    }
    
    public void playSound(String sound) 
    {
    	getPlayer().getMap().broadcastMessage(MaplePacketCreator.environmentChange(sound, 4));
    }
    
    public void spawnGuide() 
    {
        c.sendPacket(MaplePacketCreator.enableTutor(true));
    }

    public void removeGuide() 
    {
        c.sendPacket(MaplePacketCreator.enableTutor(false));
    }
    
    public void displayGuide(final int num) 
    {
        c.sendPacket(MaplePacketCreator.showInfo("UI/tutorial.img/" + num));
    }

    public void talkGuide(String message) 
    {
    	c.sendPacket(MaplePacketCreator.talkGuide(message));
    }

    public void guideHint(int hint) 
    { 
        c.sendPacket(MaplePacketCreator.showTutorActions((byte) 0x01, null, hint, 0x00001B58));
    }
    
    public void showInfoText(String msg) 
    {
        c.sendPacket(MaplePacketCreator.playerMessage(msg));
    }
    
    public final void openNpc(final int npc) 
    {
        openNpc(c, npc);
    }

    public final void openNpc(final String filename) 
    {
        openNpc(c, filename);
    }

    public final void openNpc(final int npc, final String filename) 
    {
        openNpc(c, npc, filename);
    }

    public final void openNpc(final MapleClient client, final int npc) 
    {
        openNpc(client, npc, null);
    }

    public final void openNpc(final MapleClient client, final String filename) 
    {
        openNpc(client, 0x00000000, filename);
    }

    public final void openNpc(final MapleClient client, final int npc, final String filename) 
    {
        NPCScriptManager.getInstance().start(client, npc, filename);
    }
    
    public void evolvePet(byte slot, int afterId) {
        MaplePet target = null;
        
        /*long period = 90;    //refreshes expiration date: 90 days
        period *= 24;
        period *= 60;
        period *= 60;
        period *= 1000;*/
        
        target = getPlayer().getPet(slot);
        
        if(target == null) {
            getPlayer().dropMessage("Pet could not be evolved...");
            return;
        }
        
        getPlayer().unequipPet(target, false);
        
        gainItem(afterId, (short)1, false, getPlayer(), target);
        MapleInventoryManipulator.removeFromSlot(c, MapleInventoryType.CASH, getPlayer().getInventory(MapleInventoryType.CASH).findById(target.getItemId()).getPosition(), (short) 1, false);
    }
}