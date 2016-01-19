package org.ascnet.leaftown.client;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.ascnet.leaftown.database.DatabaseConnection;
import org.ascnet.leaftown.server.CashItemFactory;
import org.ascnet.leaftown.server.CashItemInfo;
import org.ascnet.leaftown.server.MapleItemInformationProvider;
import org.ascnet.leaftown.tools.Pair;

public class CashShop
{
    private int accountId, characterId, paypalCash, maplePoint, gameCardCash;
    private boolean opened;
    private final MapleItemStorePersistence cashInventoryPersistence = new MapleItemStorePersistence();
    
    private List<IItem> inventory = new ArrayList<IItem>();
    private List<Integer> wishList = new ArrayList<Integer>();
    private int notes = 0;

    public CashShop(int accountId, int characterId, int jobType) throws SQLException 
    {
        this.accountId = accountId;
        this.characterId = characterId;
        
        for(IItem item : cashInventoryPersistence.loadItems(CashInventoryType.SETUP, accountId, characterId, jobType))
        	inventory.add(item);
        for(IItem item : cashInventoryPersistence.loadItems(CashInventoryType.EQUIP, accountId, characterId, jobType))
        	inventory.add(item);
        for(IItem item : cashInventoryPersistence.loadItems(CashInventoryType.ETC, accountId, characterId, jobType))
        	inventory.add(item);
        for(IItem item : cashInventoryPersistence.loadItems(CashInventoryType.USE, accountId, characterId, jobType))
        	inventory.add(item);
        for(IItem item : cashInventoryPersistence.loadItems(CashInventoryType.CASH, accountId, characterId, jobType))
        	inventory.add(item);
        
        Connection con = DatabaseConnection.getConnection();
        PreparedStatement ps = null;
        ResultSet rs = null;
        
        ps = con.prepareStatement("SELECT `paypalCash`, `maplePoints`, `gameCardCash` FROM `accounts` WHERE `id` = ?");
        ps.setInt(1, accountId);
        rs = ps.executeQuery();
        
        if (rs.next()) 
        {
            this.paypalCash = rs.getInt("paypalCash");
            this.maplePoint = rs.getInt("maplePoints");
            this.gameCardCash = rs.getInt("gameCardCash");
        }
        
        rs.close();
        ps.close();

        ps = con.prepareStatement("SELECT `sn` FROM `wishlist` WHERE `charid` = ?");
        ps.setInt(1, characterId);
        rs = ps.executeQuery();
        
        while (rs.next())
            wishList.add(rs.getInt("sn"));
        
        rs.close();
        ps.close();
    }
  
    public int getCash(int type) 
    {
        switch (type)
        {
            case 0x01:
                return paypalCash;
            case 0x02:
                return maplePoint;
            case 0x04:
                return gameCardCash;
        }

        return 0x00;
    }
    
    public boolean removeCash(int type, int cash)
    {
    	return gainCash(type, cash * -0x01);
    }

    public boolean gainCash(int type, int cash) 
    {
        switch (type) 
        {
            case 0x01:
            	if((paypalCash + cash) < 0x00)
            		return false;
            	
            	paypalCash += cash;
                break;
            case 0x02:
            	if((maplePoint + cash) < 0x00)
            		return false;
            	
                maplePoint += cash;
                break;
            case 0x04:
            	if((gameCardCash + cash) < 0x00)
            		return false;
            	
            	gameCardCash += cash;
                break;
        }
        return true;
    }

    public boolean isOpened()
    {
        return opened;
    }

    public void open(boolean b) 
    {
        opened = b;
    }

    public List<IItem> getInventory() 
    {
        return inventory;
    }

    public IItem findByCashId(int cashId) 
    {
        boolean isRing = false;
        Equip equip = null;
        for (IItem item : inventory)
        {
            if (item.getType() == 1)
            {
                equip = (Equip) item;
                isRing = equip.getRingId() > -1;
            }
            
            if ((item.getPetId() > -1 ? item.getPetId() : isRing ? equip.getRingId() : item.getCashId()) == cashId) 
                return item;
        }

        return null;
    }

    public void addToInventory(IItem item) 
    {
        inventory.add(item);
    }

    public void removeFromInventory(IItem item)
    {
        inventory.remove(item);
    }

    public List<Integer> getWishList() 
    {
        return wishList;
    }

    public void clearWishList()
    {
        wishList.clear();
    }

    public void addToWishList(int sn)
    {
        wishList.add(sn);
    }

    public void gift(int recipient, String from, String message, int sn)
    {
        gift(recipient, from, message, sn, -1);
    }

    public void gift(int recipient, String from, String message, int sn, int ringid) 
    {
        PreparedStatement ps = null;
        try 
        {
            ps = DatabaseConnection.getConnection().prepareStatement("INSERT INTO `csgifts` (id, sender, recipient, sn, quantity, message, ringId) VALUES (DEFAULT, ?, ?, ?, ?, ?, ?)");
            ps.setString(0x01, from);
            ps.setInt(0x02, recipient);
            ps.setInt(0x03, sn);
            ps.setInt(0x04, 0x01);
            ps.setString(0x05, message);
            ps.setInt(0x06, ringid);
            ps.executeUpdate();
        } 
        catch (SQLException sqle) 
        {
            sqle.printStackTrace();
        }
        finally
        {
            try 
            {
                if (ps != null) ps.close();
            } 
            catch (SQLException ex)  { }
        }
    }

    public List<Pair<IItem, String>> loadGifts()
    {
        List<Pair<IItem, String>> gifts = new ArrayList<>();
        Connection con = DatabaseConnection.getConnection();

        try 
        {
            PreparedStatement ps = con.prepareStatement("SELECT * FROM `csgifts` WHERE `recipient` = ?");
            ps.setInt(0x01, characterId);
            ResultSet rs = ps.executeQuery();

            while (rs.next())
            {
                notes++;
                CashItemInfo cItem = CashItemFactory.getItem(rs.getInt("SN"));
                IItem item = cItem.toItem();
                Equip equip = null; 

                item.setGiftFrom(rs.getString("sender"));
                
                if (item.getType() == MapleInventoryType.EQUIP.getType())
                {
                    equip = (Equip) item;
                    equip.setRingId(rs.getInt("ringid"));
                    gifts.add(new Pair<IItem, String>(equip, rs.getString("message")));
                }
                else
                    gifts.add(new Pair<>(item, rs.getString("message")));

                if (CashItemFactory.isPackage(cItem.getItemId())) //Packages never contains a ring
                { 
                    for (IItem packageItem : CashItemFactory.getPackage(cItem.getItemId())) 
                    {       	
                        packageItem.setGiftFrom(rs.getString("sender"));
                        addToInventory(packageItem);
                    }
                }
                else
                    addToInventory(equip == null ? item : equip);
            }

            rs.close();
            ps.close();
            ps = con.prepareStatement("DELETE FROM `csgifts` WHERE `recipient` = ?");
            ps.setInt(0x01, characterId);
            ps.executeUpdate();
            ps.close();
        } 
        catch (SQLException sqle) 
        {
            sqle.printStackTrace();
        }

        return gifts;
    }

    public int getAvailableNotes()
    {
        return notes;
    }

    public void decreaseNotes() 
    {
        notes--;
    }

    public void save(Connection con) throws SQLException 
    {
        List<IItem> equips = new ArrayList<IItem>();
        List<IItem> setup = new ArrayList<IItem>();
        List<IItem> use = new ArrayList<IItem>();
        List<IItem> etc = new ArrayList<IItem>();
        List<IItem> cash = new ArrayList<IItem>();

        for (IItem item : inventory) 
        {
        	if(MapleItemInformationProvider.getInstance().getInventoryType(item.getItemId()) == MapleInventoryType.EQUIP)
        		equips.add(item);
        	else if(MapleItemInformationProvider.getInstance().getInventoryType(item.getItemId()) == MapleInventoryType.SETUP)
        		setup.add(item);
        	else if(MapleItemInformationProvider.getInstance().getInventoryType(item.getItemId()) == MapleInventoryType.ETC)
        		etc.add(item);
        	else if(MapleItemInformationProvider.getInstance().getInventoryType(item.getItemId()) == MapleInventoryType.USE)
        		use.add(item);
        	else if(MapleItemInformationProvider.getInstance().getInventoryType(item.getItemId()) == MapleInventoryType.CASH)
        		cash.add(item);
        }
        
        int jobType = (MapleCharacter.getJobIdById(characterId, 0x00) / 1000);
        
        PreparedStatement ps = con.prepareStatement("DELETE FROM `cashinventory_eqp` WHERE `CharacterId` in (" + (jobType == 0x00 ? "select id from characters where ((job/1000) < 1) and accountID = ?" : "?") + ")");
        ps.setInt(0x01, jobType == 0x00 ? accountId : characterId);
        ps.executeUpdate();
        ps.close();
        
        ps = con.prepareStatement("DELETE FROM `cashinventory_setup` WHERE `CharacterId` in (" + (jobType == 0x00 ? "select id from characters where ((job/1000) < 1) and accountID = ?" : "?") + ")");
        ps.setInt(0x01, jobType == 0x00 ? accountId : characterId);
        ps.executeUpdate();
        ps.close();
        
        ps = con.prepareStatement("DELETE FROM `cashinventory_etc` WHERE `CharacterId` in (" + (jobType == 0x00 ? "select id from characters where ((job/1000) < 1) and accountID = ?" : "?") + ")");
        ps.setInt(0x01, jobType == 0x00 ? accountId : characterId);
        ps.executeUpdate();
        ps.close();
        
        ps = con.prepareStatement("DELETE FROM `cashinventory_use` WHERE `CharacterId` in (" + (jobType == 0x00 ? "select id from characters where ((job/1000) < 1) and accountID = ?" : "?") + ")");
        ps.setInt(0x01, jobType == 0x00 ? accountId : characterId);
        ps.executeUpdate();
        ps.close();
        
        ps = con.prepareStatement("DELETE FROM `cashinventory_cash` WHERE `CharacterId` in (" + (jobType == 0x00 ? "select id from characters where ((job/1000) < 1) and accountID = ?" : "?") + ")");
        ps.setInt(0x01, jobType == 0x00 ? accountId : characterId);
        ps.executeUpdate(); 
        ps.close();
         
        byte position = 0x00;
         
        cashInventoryPersistence.saveItems(equips, CashInventoryType.EQUIP, characterId, position);
        cashInventoryPersistence.saveItems(setup, CashInventoryType.SETUP, characterId, position);
        cashInventoryPersistence.saveItems(etc, CashInventoryType.ETC, characterId, position);
        cashInventoryPersistence.saveItems(use, CashInventoryType.USE, characterId, position);
        cashInventoryPersistence.saveItems(cash, CashInventoryType.CASH, characterId, position);
        
        ps = con.prepareStatement("UPDATE `accounts` SET `paypalCash` = ?, `maplePoints` = ?, `gameCardCash` = ? WHERE `id` = ?");
        ps.setInt(1, paypalCash);
        ps.setInt(2, maplePoint);
        ps.setInt(3, gameCardCash);
        ps.setInt(4, accountId); 
        ps.executeUpdate();
        ps.close();
         
        ps = con.prepareStatement("DELETE FROM `wishlist` WHERE `charid` = ?");
        ps.setInt(1, characterId);
        ps.executeUpdate();
        ps.close();
        
        ps = con.prepareStatement("INSERT INTO `wishlist` VALUES (DEFAULT, ?, ?)");
        ps.setInt(1, characterId);

        for (int sn : wishList) 
        {
            ps.setInt(2, sn);
            ps.executeUpdate();
        }
        ps.close();
    }
}
