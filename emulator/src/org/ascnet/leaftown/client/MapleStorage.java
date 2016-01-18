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

package org.ascnet.leaftown.client;

import org.ascnet.leaftown.client.IItem;
import org.ascnet.leaftown.client.MapleClient;
import org.ascnet.leaftown.client.MapleInventoryType;
import org.ascnet.leaftown.database.DatabaseConnection;
import org.ascnet.leaftown.server.MapleItemInformationProvider;
import org.ascnet.leaftown.tools.MaplePacketCreator;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * @author Matze
 */
public class MapleStorage 
{
    private final int id;
    private final List<IItem> items;
    private int meso;
    private byte slots;
    
    private final MapleItemStorePersistence storagePersistence = new MapleItemStorePersistence();
    private final Map<MapleInventoryType, List<IItem>> typeItems = new EnumMap<>(MapleInventoryType.class);
    
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(MapleStorage.class);

    private MapleStorage(int id, byte slots, int meso) 
    {
        this.id = id;
        this.slots = slots; 
        items = new LinkedList<>();
        this.meso = meso;
    }

    public static MapleStorage create(int id)
    {
        try 
        {
            Connection con = DatabaseConnection.getConnection();
            PreparedStatement ps = con.prepareStatement("INSERT INTO storages (accountid, slots, meso) VALUES (?, ?, ?)");
            ps.setInt(0x01, id);
            ps.setInt(0x02, 0x10);
            ps.setInt(0x03, 0x00);
            ps.executeUpdate();
            ps.close();
        }
        catch (SQLException ex) 
        {
            log.error("Error creating storage", ex);
        }
        return loadOrCreateFromDB(id);
    }

    public static MapleStorage loadOrCreateFromDB(int id)
    {
    	Connection con = null;
        MapleStorage ret = null;
        int storageID;
        
        try
        {
            con = DatabaseConnection.getConnection();
            PreparedStatement ps = con.prepareStatement("SELECT * FROM storages WHERE accountid = ?");
            ps.setInt(0x01, id);
            ResultSet rs = ps.executeQuery();
            
            if (!rs.next()) 
            {
                rs.close();
                ps.close();
                return create(id);
            } 
            else
            {
                storageID = rs.getInt("storageid");
                
                ret = new MapleStorage(storageID, (byte) rs.getInt("slots"), rs.getInt("meso"));
                
                ret.storagePersistence.setConnection(con);
                ret.items.addAll(ret.storagePersistence.loadItems(MapleStorageType.EQUIP, storageID));
                ret.items.addAll(ret.storagePersistence.loadItems(MapleStorageType.USE, storageID));
                ret.items.addAll(ret.storagePersistence.loadItems(MapleStorageType.SETUP, storageID));
                ret.items.addAll(ret.storagePersistence.loadItems(MapleStorageType.ETC, storageID));
                ret.storagePersistence.setConnection(null);
            }
        } 
        catch (SQLException ex) 
        {
            log.error("Error loading storage", ex);
        }
        return ret;
    }
    
    public void saveToDB()
    {
    	saveToDB(null);
    }

    public void saveToDB(Connection con) 
    {
        byte position = 0x00;
        
        try 
        {
        	if(con == null)
        		con = DatabaseConnection.getConnection();
        	
            storagePersistence.setConnection(con);
            
            PreparedStatement ps = con.prepareStatement("UPDATE storages SET slots = ?, meso = ? WHERE storageid = ?");
            ps.setInt(0x01, slots);
            ps.setInt(0x02, meso);
            ps.setInt(0x03, id);
            ps.executeUpdate();
            ps.close();
            
            ps = con.prepareStatement("DELETE FROM storage_eqp WHERE StorageID = ?");
            ps.setInt(0x01, id);
            ps.executeUpdate();
            ps.close();

            storagePersistence.saveItems(items, MapleStorageType.EQUIP, id, position);

            ps = con.prepareStatement("DELETE FROM storage_use WHERE StorageID = ?");
            ps.setInt(0x01, id);
            ps.executeUpdate();
            ps.close();
            
            storagePersistence.saveItems(items, MapleStorageType.USE, id, position);

            ps = con.prepareStatement("DELETE FROM storage_setup WHERE StorageID = ?");
            ps.setInt(0x01, id);
            ps.executeUpdate();
            ps.close();
            
            storagePersistence.saveItems(items, MapleStorageType.SETUP, id, position);

            ps = con.prepareStatement("DELETE FROM storage_etc WHERE StorageID = ?");
            ps.setInt(0x01, id);
            ps.executeUpdate();
            ps.close();

            storagePersistence.saveItems(items, MapleStorageType.ETC, id, position);
            
            storagePersistence.setConnection(null);
        }
        catch (SQLException ex)
        {
            log.error("Error saving storage", ex);
        }
    }

    public IItem takeOut(byte slot) 
    {
        MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
        IItem ret = items.remove(slot);
        MapleInventoryType type = ii.getInventoryType(ret.getItemId());
        typeItems.put(type, new ArrayList<>(filterItems(type)));
        return ret;
    }

    public IItem itemAt(byte slot)
    {
        return items.get(slot);
    }

    public void store(IItem item)
    {
        MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
        items.add(item);
        MapleInventoryType type = ii.getInventoryType(item.getItemId());
        typeItems.put(type, new ArrayList<>(filterItems(type)));
    }

    public List<IItem> getItems()
    {
        return Collections.unmodifiableList(items);
    }

    private List<IItem> filterItems(MapleInventoryType type)
    {
        List<IItem> ret = new LinkedList<>();
        MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
        for (IItem item : items) 
        {
            if (ii.getInventoryType(item.getItemId()) == type) 
                ret.add(item);
        }
        return ret;
    }

    public byte getSlot(MapleInventoryType type, byte slot) 
    {
        byte ret = 0x00;
        for (IItem item : items) 
        {
            if (item == typeItems.get(type).get(slot)) 
                return ret;
            
            ret++;
        }
        return -0x01;
    }

    public void sendStorage(MapleClient c, int npcId)
    {
        final MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
        
        Collections.sort(items, new Comparator<IItem>()
        {
            public int compare(IItem o1, IItem o2)
            {
                if (ii.getInventoryType(o1.getItemId()).getType() < ii.getInventoryType(o2.getItemId()).getType()) 
                    return -0x01;
                else if (ii.getInventoryType(o1.getItemId()) == ii.getInventoryType(o2.getItemId())) 
                    return 0x00;
                else 
                    return 0x01;
            }
        });
        
        for (MapleInventoryType type : MapleInventoryType.values()) 
            typeItems.put(type, new ArrayList<>(items));

        c.sendPacket(MaplePacketCreator.getStorage(npcId, slots, items, meso));
    }

    public void sendStored(MapleClient c, MapleInventoryType type)
    {
        c.sendPacket(MaplePacketCreator.storeStorage(slots, type, typeItems.get(type)));
    }

    public void sendTakenOut(MapleClient c, MapleInventoryType type) 
    {
        c.sendPacket(MaplePacketCreator.takeOutStorage(slots, type, typeItems.get(type)));
    }

    public int getMeso()
    {
        return meso;
    }

    public void setMeso(int meso) 
    {
        if (meso < 0x00) 
            throw new RuntimeException();
        
        this.meso = meso;
    }

    public void sendMeso(MapleClient c)
    {
        c.sendPacket(MaplePacketCreator.mesoStorage(slots, meso));
    }

    public boolean isFull() 
    {
        return items.size() >= slots;
    }
    
    public byte getSlots()
    {
    	return slots;
    }
    
    public boolean gainSlots(int slots) 
    {
        slots += this.slots;

        if (slots <= 0x30) 
        {
            this.slots = (byte) slots;
            return true;
        }

        return false;
    }

    public void close() 
    {
        typeItems.clear();
    }
}