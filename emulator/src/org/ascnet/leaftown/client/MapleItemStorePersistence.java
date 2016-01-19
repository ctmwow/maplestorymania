package org.ascnet.leaftown.client;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.ascnet.leaftown.database.DatabaseConnection;
import org.ascnet.leaftown.server.MapleItemInformationProvider;

public class MapleItemStorePersistence 
{   
    @SuppressWarnings("unused")
	private int accountId;
    
    private Connection con;
    
    public void setConnection(Connection con)
    {
    	this.con = con;
    }
    
    private MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
 
    public MapleItemStorePersistence() 
    {

    }

    public MapleItemStorePersistence(int accountId) 
    {
        this.accountId = accountId;
    }
    
    public List<IItem> loadItems(MapleInventoryType type, MapleCharacter character) throws SQLException
    {
    	if(con == null || con.isClosed())
    		con = DatabaseConnection.getConnection();
    	
    	List<IItem> itemList = new ArrayList<IItem>();
    	
    	if(type == MapleInventoryType.EQUIPPED)
    	{
    		PreparedStatement preparedStatment = con.prepareStatement("SELECT * FROM inventory_eqp WHERE CharacterID = ? AND Position < 1");
            preparedStatment.setInt(0x01, character.getId());
            
            ResultSet resultSet = preparedStatment.executeQuery();
            
            while(resultSet.next())
            {
            	itemList.add(buildEquip(resultSet));
            	
            	int RingId = resultSet.getInt("RingID");
            	
                if (RingId != -0x01)
                {
                	int ItemId = resultSet.getInt("ItemID");
                	
                    MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
                    MapleRing ring = MapleRing.loadFromDb(RingId);
                    if (ii.isCrushRing(ItemId)) 
                    {
                        if (ring != null)
                        	character.getCrushRings().add(ring);
                    }
                    else if (ii.isFriendshipRing(ItemId)) 
                    {
                        if (ring != null)
                        	character.getFriendshipRings().add(ring);
                    } 
                    else if (ii.isWeddingRing(ItemId))
                    {
                    	if (ring != null)
                        	character.getMarriageRings().add(ring);
                    }
                }
            }
    	}
    	else if(type == MapleInventoryType.EQUIP)
    	{
    		PreparedStatement preparedStatment = con.prepareStatement("SELECT * FROM inventory_eqp WHERE CharacterID = ? AND Position > 0");
            preparedStatment.setInt(0x01, character.getId());
            
            ResultSet resultSet = preparedStatment.executeQuery();
            
            while(resultSet.next())
            {
            	itemList.add(buildEquip(resultSet));
            	
            	int RingId = resultSet.getInt("RingID");
            	
                if (RingId != -0x01)
                {
                	int ItemId = resultSet.getInt("ItemID");
                	
                    MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
                    MapleRing ring = MapleRing.loadFromDb(RingId);
                    if (ii.isCrushRing(ItemId)) 
                    {
                        if (ring != null)
                        	character.getCrushRings().add(ring);
                    }
                    else if (ii.isFriendshipRing(ItemId)) 
                    {
                        if (ring != null)
                        	character.getFriendshipRings().add(ring);
                    } 
                    else if (ii.isWeddingRing(ItemId))
                    {
                    	if (ring != null)
                        	character.getMarriageRings().add(ring);
                    }
                }
            }    		
    	}
    	else if(type == MapleInventoryType.USE)
    	{
    		PreparedStatement preparedStatment = con.prepareStatement("SELECT * FROM inventory_use WHERE CharacterID = ?");
            preparedStatment.setInt(0x01, character.getId());
            
            ResultSet resultSet = preparedStatment.executeQuery();
            
            while(resultSet.next())
            	itemList.add(buildItem(resultSet));    		
    	}
    	else if(type == MapleInventoryType.SETUP)
    	{
    		PreparedStatement preparedStatment = con.prepareStatement("SELECT * FROM inventory_setup WHERE CharacterID = ?");
            preparedStatment.setInt(0x01, character.getId());
            
            ResultSet resultSet = preparedStatment.executeQuery();
            
            while(resultSet.next())
            	itemList.add(buildItem(resultSet));    		
    	}
    	else if(type == MapleInventoryType.ETC)
    	{
    		PreparedStatement preparedStatment = con.prepareStatement("SELECT * FROM inventory_etc WHERE CharacterID = ?");
            preparedStatment.setInt(0x01, character.getId());
            
            ResultSet resultSet = preparedStatment.executeQuery();
            
            while(resultSet.next())
            	itemList.add(buildItem(resultSet));    		
    	}
    	else if(type == MapleInventoryType.CASH)
    	{
    		PreparedStatement preparedStatment = con.prepareStatement("SELECT * FROM inventory_cash WHERE CharacterID = ?");
            preparedStatment.setInt(0x01, character.getId());
            
            ResultSet resultSet = preparedStatment.executeQuery();
            
            while(resultSet.next())
            {
            	IItem tmpItem = buildItem(resultSet);
            	
            	if (ii.isPet(tmpItem.getItemId())) 
                {
                    MaplePet pet = MaplePet.loadFromDb(tmpItem.getItemId(), resultSet.getInt("PetID"));

                    tmpItem.setPet(pet);
                     
                    if (pet != null && pet.isSummoned())
                        character.getPets().add(pet); 
                }
            	itemList.add(tmpItem);
            }
    	}

    	return itemList;
    }
    
    public List<IItem> loadItems(MapleStorageType type, int storageId) throws SQLException
    {
    	if(con == null || con.isClosed())
    		con = DatabaseConnection.getConnection();
    	
    	List<IItem> itemList = new ArrayList<IItem>();
    	
    	if(type == MapleStorageType.EQUIP)
    	{
    		PreparedStatement preparedStatment = con.prepareStatement("SELECT * FROM storage_eqp WHERE StorageID = ? ORDER BY StoragePosition");
            preparedStatment.setInt(0x01, storageId);
            
            ResultSet resultSet = preparedStatment.executeQuery();
            
            while(resultSet.next())
            	itemList.add(buildEquip(resultSet));		
    	}
    	else if(type == MapleStorageType.USE)
    	{
    		PreparedStatement preparedStatment = con.prepareStatement("SELECT * FROM storage_use WHERE StorageID = ? ORDER BY StoragePosition");
            preparedStatment.setInt(0x01, storageId);
            
            ResultSet resultSet = preparedStatment.executeQuery();
            
            while(resultSet.next())
            	itemList.add(buildItem(resultSet));    		
    	}
    	else if(type == MapleStorageType.SETUP)
    	{
    		PreparedStatement preparedStatment = con.prepareStatement("SELECT * FROM storage_setup WHERE StorageID = ? ORDER BY StoragePosition");
            preparedStatment.setInt(0x01, storageId);
            
            ResultSet resultSet = preparedStatment.executeQuery();
            
            while(resultSet.next())
            	itemList.add(buildItem(resultSet));    		
    	}
    	else if(type == MapleStorageType.ETC)
    	{
    		PreparedStatement preparedStatment = con.prepareStatement("SELECT * FROM storage_etc WHERE StorageID = ? ORDER BY StoragePosition");
            preparedStatment.setInt(0x01, storageId);
            
            ResultSet resultSet = preparedStatment.executeQuery();
            
            while(resultSet.next())
            	itemList.add(buildItem(resultSet));    		
    	}

    	return itemList;
    }
    
    public List<IItem> loadItems(CashInventoryType type, int accountId, int characterId, int jobType) throws SQLException
    {
    	if(con == null || con.isClosed())
    		con = DatabaseConnection.getConnection();
    	
    	List<IItem> itemList = new ArrayList<IItem>();
    	
    	if(type == CashInventoryType.EQUIP)
    	{
    		PreparedStatement preparedStatment = con.prepareStatement("select * from cashinventory_eqp cs " +
	    																"inner join characters chr ON chr.id = cs.CharacterId " +
	    																"where chr." + (jobType == 0x00 ? "accountId" : "id") + " = ?" + (jobType == 0x00 ? " and ((chr.job / 1000) < 1) " : ""));
            preparedStatment.setInt(0x01, jobType == 0x00 ? accountId : characterId);
            
            ResultSet resultSet = preparedStatment.executeQuery();
            
            while(resultSet.next())
            	itemList.add(buildEquip(resultSet));	
    	}
    	else if(type == CashInventoryType.USE)
    	{
    		PreparedStatement preparedStatment = con.prepareStatement("select * from cashinventory_use cs " +
	    																"inner join characters chr ON chr.id = cs.CharacterId " +
	    																"where chr." + (jobType == 0x00 ? "accountId" : "id") + " = ?" + (jobType == 0x00 ? " and ((chr.job / 1000) < 1) " : ""));
            preparedStatment.setInt(0x01, jobType == 0x00 ? accountId : characterId);
            
            ResultSet resultSet = preparedStatment.executeQuery();
             
            while(resultSet.next())
            	itemList.add(buildItem(resultSet));	
    	}
    	else if(type == CashInventoryType.SETUP)
    	{
    		PreparedStatement preparedStatment = con.prepareStatement("select * from cashinventory_setup cs " +
	    																"inner join characters chr ON chr.id = cs.CharacterId " +
	    																"where chr." + (jobType == 0x00 ? "accountId" : "id") + " = ?" + (jobType == 0x00 ? " and ((chr.job / 1000) < 1) " : ""));
            preparedStatment.setInt(0x01, jobType == 0x00 ? accountId : characterId);
            
            ResultSet resultSet = preparedStatment.executeQuery();
             
            while(resultSet.next())
            	itemList.add(buildItem(resultSet));	
    	}
    	else if(type == CashInventoryType.ETC)
    	{
    		PreparedStatement preparedStatment = con.prepareStatement("select * from cashinventory_etc cs " +
	    																"inner join characters chr ON chr.id = cs.CharacterId " +
	    																"where chr." + (jobType == 0x00 ? "accountId" : "id") + " = ?" + (jobType == 0x00 ? " and ((chr.job / 1000) < 1) " : ""));
            preparedStatment.setInt(0x01, jobType == 0x00 ? accountId : characterId);
            
            ResultSet resultSet = preparedStatment.executeQuery();
             
            while(resultSet.next())
            	itemList.add(buildItem(resultSet));	
    	}
    	else if(type == CashInventoryType.CASH)
    	{
    		PreparedStatement preparedStatment = con.prepareStatement("select * from cashinventory_cash cs " +
	    																"inner join characters chr ON chr.id = cs.CharacterId " +
	    																"where chr." + (jobType == 0x00 ? "accountId" : "id") + " = ?" + (jobType == 0x00 ? " and ((chr.job / 1000) < 1) " : ""));
            preparedStatment.setInt(0x01, jobType == 0x00 ? accountId : characterId);
            
            ResultSet resultSet = preparedStatment.executeQuery();
             
            while(resultSet.next())
            { 
            	IItem tmpItem = buildItem(resultSet);
            	
            	if (ii.isPet(tmpItem.getItemId())) 
                    tmpItem.setPet(MaplePet.loadFromDb(tmpItem.getItemId(), resultSet.getInt("PetID")));
            	
            	itemList.add(tmpItem);  
            }	
    	}
    	
    	return itemList;
    }
    
    public void saveItems(List<IItem> items, MapleInventoryType type, MapleCharacter character) throws SQLException
    {
    	if(con == null || con.isClosed())
    		con = DatabaseConnection.getConnection();
    	
    	if(type == MapleInventoryType.EQUIPPED)
    	{
            PreparedStatement preparedStatement = con.prepareStatement("INSERT INTO inventory_eqp (CharacterID, ItemID, Position, STR, DEX, `INT`, LUK, MaxHP, MaxMP, PAD, MAD, PDD, MDD, ACC, EVA, Hands, Speed, Jump, ViciousHammers, Level, RemainingSlots, ExpireDate, Owner, Flag, RingID, IsGM) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
            preparedStatement.setInt(0x01, character.getId());
            for (IItem item : items)
            {
            	if(MapleItemInformationProvider.getInstance().expiresOnLogOut(item.getItemId()))
            		continue;
            	
            	writeEquipOnStatement(preparedStatement, item, 0x02);
            	preparedStatement.addBatch();
            }    		
            preparedStatement.executeBatch();
            preparedStatement.close();
    	}
    	else if(type == MapleInventoryType.EQUIP)
    	{
            PreparedStatement preparedStatement = con.prepareStatement("INSERT INTO inventory_eqp (CharacterID, ItemID, Position, STR, DEX, `INT`, LUK, MaxHP, MaxMP, PAD, MAD, PDD, MDD, ACC, EVA, Hands, Speed, Jump, ViciousHammers, Level, RemainingSlots, ExpireDate, Owner, Flag, RingID, IsGM) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
            preparedStatement.setInt(0x01, character.getId());
            for (IItem item : items)
            {
            	writeEquipOnStatement(preparedStatement, item, 0x02);
            	preparedStatement.addBatch();
            }    		
            preparedStatement.executeBatch();
            preparedStatement.close();
    	}
    	else if(type == MapleInventoryType.USE)
    	{
            PreparedStatement preparedStatement = con.prepareStatement("INSERT INTO inventory_use (CharacterID, ItemID, Position, Quantity, ExpireDate, Owner, Flag, IsGM) VALUES (?, ?, ?, ?, ?, ?, ?, ?)");
            preparedStatement.setInt(0x01, character.getId());
            for (IItem item : items)
            {
            	writeItemOnStatement(preparedStatement, item, 0x02);
            	preparedStatement.addBatch();
            }    		
            preparedStatement.executeBatch();
            preparedStatement.close();
    	}
    	else if(type == MapleInventoryType.SETUP)
    	{
            PreparedStatement preparedStatement = con.prepareStatement("INSERT INTO inventory_setup (CharacterID, ItemID, Position, Quantity, ExpireDate, Owner, Flag, IsGM) VALUES (?, ?, ?, ?, ?, ?, ?, ?)");
            preparedStatement.setInt(0x01, character.getId());
            for (IItem item : items)
            {
            	writeItemOnStatement(preparedStatement, item, 0x02);
            	preparedStatement.addBatch();
            }    		
            preparedStatement.executeBatch();
            preparedStatement.close();
    	}
    	else if(type == MapleInventoryType.ETC)
    	{
            PreparedStatement preparedStatement = con.prepareStatement("INSERT INTO inventory_etc (CharacterID, ItemID, Position, Quantity, ExpireDate, Owner, Flag, IsGM) VALUES (?, ?, ?, ?, ?, ?, ?, ?)");
            preparedStatement.setInt(0x01, character.getId());
            for (IItem item : items)
            {
            	writeItemOnStatement(preparedStatement, item, 0x02);
            	preparedStatement.addBatch();
            }    		
            preparedStatement.executeBatch(); 
            preparedStatement.close();
    	}
    	else if(type == MapleInventoryType.CASH)
    	{
            PreparedStatement preparedStatement = con.prepareStatement("INSERT INTO inventory_cash (CharacterID, ItemID, Position, Quantity, ExpireDate, Owner, Flag, IsGM, PetID) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)");
            preparedStatement.setInt(0x01, character.getId());
            for (IItem item : items)
            {
            	writeItemOnStatement(preparedStatement, item, 0x02);
            	
            	if(ii.isPet(item.getItemId())) 
            		preparedStatement.setInt(0x09, item.getPetId() == 0x00 ? -0x01 : item.getPetId());
            	
            	preparedStatement.addBatch();
            }    		
            preparedStatement.executeBatch();
            preparedStatement.close();
    	} 
    }
    
    public void saveItems(List<IItem> items, MapleStorageType type, int storageId, byte position) throws SQLException
    {
    	if(con == null || con.isClosed())
    		con = DatabaseConnection.getConnection();
    	
    	if(type == MapleStorageType.EQUIP)
    	{
            PreparedStatement preparedStatement = con.prepareStatement("INSERT INTO storage_eqp (StorageID, StoragePosition, ItemID, Position, STR, DEX, `INT`, LUK, MaxHP, MaxMP, PAD, MAD, PDD, MDD, ACC, EVA, Hands, Speed, Jump, ViciousHammers, Level, RemainingSlots, ExpireDate, Owner, Flag, RingID, IsGM) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
            preparedStatement.setInt(0x01, storageId);
            
            for (IItem item : items) 
            {
                if (ii.getInventoryType(item.getItemId()).equals(MapleInventoryType.EQUIP))
                {
                	preparedStatement.setByte(0x02, position);
                    writeEquipOnStatement(preparedStatement, item, 0x03);
                    preparedStatement.addBatch();
                    position++;
                }
            }
            preparedStatement.executeBatch();
            preparedStatement.close();
    	}
    	else if (type == MapleStorageType.USE)
    	{
    		PreparedStatement preparedStatement = con.prepareStatement("INSERT INTO storage_use (StorageID, StoragePosition, ItemID, Position, Quantity, ExpireDate, Owner, Flag, IsGM) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)");
    		preparedStatement.setInt(0x01, storageId);
            
            for (IItem item : items) 
            {
                if (ii.getInventoryType(item.getItemId()).equals(MapleInventoryType.USE))
                {
                	preparedStatement.setByte(0x02, position);
                	writeItemOnStatement(preparedStatement, item, 0x03);
                	preparedStatement.addBatch();
                    position++;
                }
            }
            preparedStatement.executeBatch();
            preparedStatement.close();
    	}
    	else if (type == MapleStorageType.SETUP)
    	{
    		PreparedStatement preparedStatement = con.prepareStatement("INSERT INTO storage_setup (StorageID, StoragePosition, ItemID, Position, Quantity, ExpireDate, Owner, Flag, IsGM) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)");
    		preparedStatement.setInt(0x01, storageId);
            
            for (IItem item : items) 
            {
                if (ii.getInventoryType(item.getItemId()).equals(MapleInventoryType.SETUP))
                {
                	preparedStatement.setByte(0x02, position);
                	writeItemOnStatement(preparedStatement, item, 0x03);
                	preparedStatement.addBatch();
                    position++;
                }
            }
            preparedStatement.executeBatch();
            preparedStatement.close();
    	}
    	else if (type == MapleStorageType.ETC)
    	{
    		PreparedStatement preparedStatement = con.prepareStatement("INSERT INTO storage_etc (StorageID, StoragePosition, ItemID, Position, Quantity, ExpireDate, Owner, Flag, IsGM) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)");
    		preparedStatement.setInt(0x01, storageId);
            
            for (IItem item : items) 
            {
                if (ii.getInventoryType(item.getItemId()).equals(MapleInventoryType.ETC))
                {
                	preparedStatement.setByte(0x02, position);
                	writeItemOnStatement(preparedStatement, item, 0x03);
                	preparedStatement.addBatch();
                    position++;
                }
            }
            preparedStatement.executeBatch();
            preparedStatement.close();
    	}
    }

    public void saveItems(List<IItem> items, CashInventoryType type, int characterId, byte position) throws SQLException
    {
    	if(con == null || con.isClosed())
    		con = DatabaseConnection.getConnection();
    	
    	if(type == CashInventoryType.EQUIP) 
    	{
            PreparedStatement preparedStatement = con.prepareStatement("INSERT INTO cashinventory_eqp (CharacterId, CashInventoryPosition, ItemID, Position, STR, DEX, `INT`, LUK, MaxHP, MaxMP, PAD, MAD, PDD, MDD, ACC, EVA, Hands, Speed, Jump, ViciousHammers, Level, RemainingSlots, ExpireDate, Owner, Flag, RingID, IsGM) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
            preparedStatement.setInt(0x01, characterId);
            
            for (IItem item : items)  
            {
                if (ii.getInventoryType(item.getItemId()).equals(MapleInventoryType.EQUIP))
                {
                	preparedStatement.setByte(0x02, position);
                    writeEquipOnStatement(preparedStatement, item, 0x03);
                    preparedStatement.addBatch();
                    position++;
                }
            }
            preparedStatement.executeBatch();
            preparedStatement.close();
    	}
    	else if (type == CashInventoryType.USE)
    	{
    		PreparedStatement preparedStatement = con.prepareStatement("INSERT INTO cashinventory_use (CharacterId, CashInventoryPosition, ItemID, Position, Quantity, ExpireDate, Owner, Flag, IsGM) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)");
    		preparedStatement.setInt(0x01, characterId);
            
            for (IItem item : items) 
            {
                if (ii.getInventoryType(item.getItemId()).equals(MapleInventoryType.USE))
                {
                	preparedStatement.setByte(0x02, position);
                	writeItemOnStatement(preparedStatement, item, 0x03);
                	preparedStatement.addBatch();
                    position++;
                }
            }
            preparedStatement.executeBatch();
            preparedStatement.close();
    	}
    	else if (type == CashInventoryType.SETUP)
    	{
    		PreparedStatement preparedStatement = con.prepareStatement("INSERT INTO cashinventory_setup (CharacterId, CashInventoryPosition, ItemID, Position, Quantity, ExpireDate, Owner, Flag, IsGM) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)");
    		preparedStatement.setInt(0x01, characterId);
            
            for (IItem item : items) 
            {
                if (ii.getInventoryType(item.getItemId()).equals(MapleInventoryType.SETUP))
                {
                	preparedStatement.setByte(0x02, position);
                	writeItemOnStatement(preparedStatement, item, 0x03);
                	preparedStatement.addBatch(); 
                    position++;
                }
            }
            preparedStatement.executeBatch();
            preparedStatement.close();
    	}
    	else if (type == CashInventoryType.ETC)
    	{
    		PreparedStatement preparedStatement = con.prepareStatement("INSERT INTO cashinventory_etc (CharacterId, CashInventoryPosition, ItemID, Position, Quantity, ExpireDate, Owner, Flag, IsGM) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)");
    		preparedStatement.setInt(0x01, characterId);
            
            for (IItem item : items)  
            {
                if (ii.getInventoryType(item.getItemId()).equals(MapleInventoryType.ETC))
                {
                	preparedStatement.setByte(0x02, position);
                	writeItemOnStatement(preparedStatement, item, 0x03);
                	preparedStatement.addBatch();
                    position++;
                }
            }
            preparedStatement.executeBatch();
            preparedStatement.close();
    	}
    	else if (type == CashInventoryType.CASH)
    	{
    		PreparedStatement preparedStatement = con.prepareStatement("INSERT INTO cashinventory_cash (CharacterId, CashInventoryPosition, ItemID, Position, Quantity, ExpireDate, Owner, Flag, IsGM, PetId) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
    		preparedStatement.setInt(0x01, characterId);
            
            for (IItem item : items)   
            { 
                if (ii.getInventoryType(item.getItemId()).equals(MapleInventoryType.CASH))
                {
                	preparedStatement.setByte(0x02, position);
                	writeItemOnStatement(preparedStatement, item, 0x03);
                	
                	preparedStatement.setInt(0x0A, item.getPetId() == 0x00 ? -0x01 : item.getPetId());
                	
                	preparedStatement.addBatch();
                    position++;
                } 
            }
            preparedStatement.executeBatch();
            preparedStatement.close();
    	}
    }
    
    private void writeEquipOnStatement(PreparedStatement preparedStatement, IItem item, int firstIndex) throws SQLException
    { 
        IEquip equip = (IEquip) item; 
        preparedStatement.setInt(firstIndex, item.getItemId());
        preparedStatement.setShort(firstIndex + 0x01, item.getPosition());
        preparedStatement.setShort(firstIndex + 0x02, equip.getStr());
        preparedStatement.setShort(firstIndex + 0x03, equip.getDex());
        preparedStatement.setShort(firstIndex + 0x04, equip.getInt());
        preparedStatement.setShort(firstIndex + 0x05, equip.getLuk());
        preparedStatement.setShort(firstIndex + 0x06, equip.getHp());
        preparedStatement.setShort(firstIndex + 0x07, equip.getMp());
        preparedStatement.setShort(firstIndex + 0x08, equip.getWatk()); 
        preparedStatement.setShort(firstIndex + 0x09, equip.getMatk());
        preparedStatement.setShort(firstIndex + 0x0A, equip.getWdef());
        preparedStatement.setShort(firstIndex + 0x0B, equip.getMdef());
        preparedStatement.setShort(firstIndex + 0x0C, equip.getAcc());
        preparedStatement.setShort(firstIndex + 0x0D, equip.getAvoid());
        preparedStatement.setShort(firstIndex + 0x0E, equip.getHands());
        preparedStatement.setShort(firstIndex + 0x0F, equip.getSpeed());
        preparedStatement.setShort(firstIndex + 0x10, equip.getJump());
        preparedStatement.setByte(firstIndex + 0x11, (byte) equip.getViciousHammers());
        preparedStatement.setByte(firstIndex + 0x12, equip.getLevel());
        preparedStatement.setByte(firstIndex + 0x13, equip.getUpgradeSlots());
        preparedStatement.setTimestamp(firstIndex + 0x14, item.getExpiration());
        preparedStatement.setString(firstIndex + 0x15, item.getOwner());
        preparedStatement.setByte(firstIndex + 0x16, (byte) item.getFlag());
        preparedStatement.setInt(firstIndex + 0x17, equip.getRingId());
        preparedStatement.setByte(firstIndex + 0x18, (byte) (equip.isByGM() ? 0x01 : 0x00));
    }
    
    private void writeItemOnStatement(PreparedStatement preparedStatement, IItem item, int firstIndex) throws SQLException
    {
    	preparedStatement.setInt(firstIndex, item.getItemId());
    	preparedStatement.setShort(firstIndex + 0x01, item.getPosition());
    	preparedStatement.setShort(firstIndex + 0x02, item.getQuantity());
    	preparedStatement.setTimestamp(firstIndex + 0x03, item.getExpiration());
    	preparedStatement.setString(firstIndex + 0x04, item.getOwner());
    	preparedStatement.setByte(firstIndex + 0x05, (byte) item.getFlag());
    	preparedStatement.setByte(firstIndex + 0x06, (byte) (item.isByGM() ? 0x01 : 0x00));
    } 
    
    private Item buildItem(ResultSet resultSet) throws SQLException
    {
        Item item = new Item(resultSet.getInt("ItemID"), resultSet.getByte("Position"), resultSet.getShort("Quantity"), resultSet.getShort("Flag"));
        item.setOwner(resultSet.getString("Owner"));
        item.setExpiration(resultSet.getTimestamp("ExpireDate"));
        
        if (resultSet.getByte("IsGM") == 0x01)
            item.setGMFlag();
        
        return item;
    }
    
    private Equip buildEquip(ResultSet resultSet) throws SQLException
    {
        Equip equip = new Equip(resultSet.getInt("ItemID"), resultSet.getShort("Position"), resultSet.getInt("RingID"));
        equip.setStr(resultSet.getShort("STR"));
        equip.setDex(resultSet.getShort("DEX"));
        equip.setInt(resultSet.getShort("INT"));
        equip.setLuk(resultSet.getShort("LUK"));
        equip.setHp(resultSet.getShort("MaxHP"));
        equip.setMp(resultSet.getShort("MaxMP"));
        equip.setWatk(resultSet.getShort("PAD"));
        equip.setMatk(resultSet.getShort("MAD"));
        equip.setWdef(resultSet.getShort("PDD"));
        equip.setMdef(resultSet.getShort("MDD"));
        equip.setAcc(resultSet.getShort("ACC"));
        equip.setAvoid(resultSet.getShort("EVA")); 
        equip.setHands(resultSet.getShort("Hands"));
        equip.setSpeed(resultSet.getShort("Speed"));
        equip.setJump(resultSet.getShort("Jump"));
        equip.setViciousHammers(resultSet.getByte("ViciousHammers"));
        equip.setLevel(resultSet.getByte("Level"));
        equip.setUpgradeSlots(resultSet.getByte("RemainingSlots"));
        equip.setExpiration(resultSet.getTimestamp("ExpireDate"));
        equip.setOwner(resultSet.getString("Owner"));
        equip.setFlag(resultSet.getByte("Flag"));
        
        if (resultSet.getByte("IsGM") == 0x01)
            equip.setGMFlag();
        
        return equip;
    }
}
