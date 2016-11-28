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

package org.ascnet.leaftown.server.playerinteractions;

import org.ascnet.leaftown.client.Equip;
import org.ascnet.leaftown.client.IItem;
import org.ascnet.leaftown.client.Item;
import org.ascnet.leaftown.client.MapleCharacter;
import org.ascnet.leaftown.client.MapleClient;
import org.ascnet.leaftown.database.DatabaseConnection;
import org.ascnet.leaftown.server.MapleInventoryManipulator;
import org.ascnet.leaftown.server.MapleItemInformationProvider;
import org.ascnet.leaftown.server.TimerManager;
import org.ascnet.leaftown.server.maps.MapleMap;
import org.ascnet.leaftown.server.maps.MapleMapObjectType;
import org.ascnet.leaftown.tools.MaplePacketCreator;
import org.ascnet.leaftown.tools.Pair;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ScheduledFuture;

/**
 * @author XoticStory
 */
public class HiredMerchant extends AbstractPlayerStore 
{
    private static final HashMap<Integer, HiredMerchant> allMerchants = new HashMap<>(); //Used for Minerva and Remote Shop Controller
    
    private boolean open;
    public ScheduledFuture<?> schedule = null;
    private final MapleMap map;
    private final MapleCharacter owner;
    private final int channel;

    public static HiredMerchant getMerchantByOwner(MapleCharacter chr) 
    {
        if (allMerchants.containsKey(chr.getId()))
            return allMerchants.get(chr.getId());
        
        return null;
    }

    public static HiredMerchant getMerchByOwner(int id) 
    {
        if (allMerchants.containsKey(id))
            return allMerchants.get(id);
        return null;
    }

    public static HashMap<Integer, HiredMerchant> getAllMerchants() 
    {
        return allMerchants;
    }

    public static List<Pair<HiredMerchant, MaplePlayerShopItem>> getMerchsByIId(int itemid, boolean onSale) 
    {
        final List<Pair<HiredMerchant, MaplePlayerShopItem>> ret = new ArrayList<>();
        for (HiredMerchant merch : allMerchants.values()) 
        {
            for (MaplePlayerShopItem item : merch.items) 
            {
                if (item.getItem().getItemId() == itemid && (!onSale || item.getBundles() > 0))
                    ret.add(new Pair<>(merch, item));
            }
        }
        return ret;
    }

    public static void clearMerchants() 
    {
        allMerchants.clear();
    }

    public int getChannel() 
    {
        return channel;
    }

    public HiredMerchant(MapleCharacter owner, int itemId, String desc) 
    {
        super(owner, itemId, desc);
        map = owner.getMap();
        this.owner = owner;
        channel = owner.getClient().getChannel();
        allMerchants.put(owner.getId(), this);
        schedule = TimerManager.getInstance().schedule(new Runnable() 
        {
            @Override
            public void run() 
            {
                HiredMerchant.this.closeShop();
                HiredMerchant.this.makeAvailableAtFred();
            }
        }, 1000 * 60 * 60 * 24);
    }

    public List<MaplePlayerShopItem> getItemsById(int itemid) 
    {
        ArrayList<MaplePlayerShopItem> ret = new ArrayList<>();

        for (MaplePlayerShopItem mpsi : items) 
            if (mpsi.getItem().getItemId() == itemid) 
                ret.add(mpsi);

        return ret;
    }

    public byte getShopType() 
    {
        return IMaplePlayerShop.HIRED_MERCHANT;
    }

    @Override
    public void buy(MapleClient c, int item, short quantity) 
    {
        MaplePlayerShopItem pItem = items.get(item);
        if (pItem.getBundles() > 0) 
        {
            synchronized (items) 
            {
                IItem newItem = pItem.getItem().copy();
                int perBundle = newItem.getQuantity();
                short final2 = (short) (quantity * perBundle);
                
                if (final2 < 0) 
                {
                    c.getPlayer().dropMessage(1, "You cannot buy so many. Please buy less.");
                    return;
                }
                
                if (MapleItemInformationProvider.getInstance().canHaveOnlyOne(newItem.getItemId()) && c.getPlayer().haveItem(newItem.getItemId(), 1, true, false)) 
                {
                    c.getPlayer().dropMessage(1, "You already have this item and it's ONE of A KIND!");
                    return;
                }
                
                newItem.setQuantity(final2);
                
                if (c.getPlayer().getMeso() >= pItem.getPrice() * quantity) 
                {
                    if (MapleInventoryManipulator.checkSpace(c, newItem.getItemId(), quantity, "")) 
                    {
                        MapleInventoryManipulator.addByItem(c, newItem, "", true);
                        Connection con = DatabaseConnection.getConnection();
                        
                        try 
                        {
                            PreparedStatement ps = con.prepareStatement("UPDATE characters set MerchantMesos = MerchantMesos + " + pItem.getPrice() * quantity + " where id = ?");
                            ps.setInt(1, getOwnerId());
                            ps.executeUpdate();
                            ps.close();
                        }
                        catch (SQLException se) 
                        {
                            se.printStackTrace();
                        }
                        c.getPlayer().gainMeso(-pItem.getPrice() * quantity, false);
                        pItem.setBundles((short) (pItem.getBundles() - quantity));
                        try 
                        {
                            updateItem(pItem);
                        }
                        catch (SQLException se) 
                        {
                        }
                    }
                    else 
                        c.getPlayer().dropMessage(1, "Your inventory is full.");
                } 
                else 
                    c.getPlayer().dropMessage(1, "You do not have enough mesos.");
            }
            if (isSoldOut()) 
                closeShop();
        }
    }
    
    public static List<IItem> loadStoragedItems(final int playerId) throws SQLException
    {
    	final List<IItem> tmp = new ArrayList<IItem>();
    	
        PreparedStatement ps;
        Connection con = DatabaseConnection.getConnection();
        
        ps = con.prepareStatement("SELECT * FROM hiredMerchant WHERE ownerid = ?;");
        ps.setInt(0x00000001, playerId);

        final ResultSet rs = ps.executeQuery();
        
        final MapleItemInformationProvider informationProvider = MapleItemInformationProvider.getInstance();
        
        while(rs.next())
        {
        	final int itemId = rs.getInt("itemid");
    
        	if(informationProvider.isEquip(itemId))
        	{
                final Equip equip = new Equip(rs.getInt("itemid"), (short) (tmp.size() + 0x01), rs.getInt("quantity"));
                equip.setStr(rs.getShort("STR"));
                equip.setDex(rs.getShort("DEX"));
                equip.setInt(rs.getShort("INT"));
                equip.setLuk(rs.getShort("LUK"));
                equip.setHp(rs.getShort("hp"));
                equip.setMp(rs.getShort("mp"));
                equip.setWatk(rs.getShort("watk"));
                equip.setMatk(rs.getShort("matk"));
                equip.setWdef(rs.getShort("wdef"));
                equip.setMdef(rs.getShort("mdef"));
                equip.setAcc(rs.getShort("acc")); 
                equip.setAvoid(rs.getShort("avoid")); 
                equip.setHands(rs.getShort("hands"));
                equip.setSpeed(rs.getShort("Speed"));
                equip.setJump(rs.getShort("Jump"));
                equip.setViciousHammers(rs.getByte("hammer"));
                equip.setLevel(rs.getByte("Level"));
                equip.setExpiration(rs.getTimestamp("ExpireDate"));
                equip.setOwner(rs.getString("Owner"));
                equip.setFlag(rs.getByte("Flag"));
                equip.setRingId(null);
                equip.setUpgradeSlots(rs.getByte("upgradeslots"));
                tmp.add((IItem) equip);
        	}
        	else
        	{
            	final IItem item = new Item(itemId, (short) (tmp.size() + 0x01), rs.getInt("flag"));
                item.setOwner(rs.getString("Owner"));
                item.setExpiration(rs.getTimestamp("ExpireDate"));
                item.setQuantity(rs.getShort("quantity"));
                item.setFlag(rs.getShort("flag"));
                 
                tmp.add(item);
        	}
        }
        
        rs.close();
        ps.close();
        
        return tmp;
    }

    public void closeShop() 
    {
        map.removeMapObject(this);
        map.broadcastMessage(MaplePacketCreator.destroyHiredMerchant(getOwnerId()));
        allMerchants.remove(getOwnerId());
        
        Connection con = DatabaseConnection.getConnection();
        
        try 
        {
            PreparedStatement ps = con.prepareStatement("UPDATE characters SET HasMerchant = 0 WHERE id = ?");
            ps.setInt(1, getOwnerId());
            ps.executeUpdate();
            ps.close();
        } 
        catch (SQLException se) 
        {
        }
        
        if (schedule != null) 
        {
            schedule.cancel(false);
            schedule = null;
        }
        
        if (owner != null)
            owner.setHasMerchant(false);
    }

    public void cancelShop() 
    {
        try 
        {
            Connection con = DatabaseConnection.getConnection();
            PreparedStatement ps = con.prepareStatement("UPDATE characters SET HasMerchant = 0 WHERE id = ?");
            ps.setInt(1, getOwnerId());
            ps.executeUpdate();
            ps.close();
        }
        catch (SQLException se) 
        {
        }
        
        if (owner != null)
            owner.setHasMerchant(false);
        
        if (schedule != null) 
        {
            schedule.cancel(false);
            schedule = null;
        }
    }

    public boolean isOpen() 
    {
        return open;
    }

    public void setOpen(boolean set) 
    {
        if (set && owner != null)
            owner.setHasMerchant(true);
        
        open = set;
    }

    public MapleMap getMap() 
    {
        return map;
    }

    @Override
    public void sendDestroyData(MapleClient client) 
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public MapleMapObjectType getType() 
    {
        return MapleMapObjectType.HIRED_MERCHANT;
    }

    @Override
    public void sendSpawnData(MapleClient client) 
    {
        client.sendPacket(MaplePacketCreator.spawnHiredMerchant(this));
    }
}