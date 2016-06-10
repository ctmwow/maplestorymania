package org.ascnet.leaftown.client;

import java.io.Serializable;
import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import org.ascnet.leaftown.database.DatabaseConnection;
import org.ascnet.leaftown.net.MaplePacket;
import org.ascnet.leaftown.net.world.WorldRegistryImpl;
import org.ascnet.leaftown.tools.MaplePacketCreator;

import com.mysql.jdbc.Statement;

public class MapleFamily implements Serializable
{
    public static enum FCOp { NONE, DISBAND; }
    public static final long serialVersionUID = 6322150443228168192L;
    private final Map<Integer, MapleFamilyCharacterInfo> members = new ConcurrentHashMap<>();
    private String leadername = null, notice;
    private int id, leaderid;
    private boolean proper = true, bDirty = false, changed = false;

    public MapleFamily(int familyId) 
    {
    	 try  
    	 {
    		 if(familyId == -0x01)
    		 {
                 id = -0x01;
                 proper = false;
                 return; 
    		 }
    		 
             final Connection con = DatabaseConnection.getConnection();
             
             PreparedStatement ps = con.prepareStatement("SELECT characterLeaderId, familyNotice FROM family WHERE idFamily = ?");
             ps.setInt(0x01, familyId);
             
             ResultSet rs = ps.executeQuery();

             if (!rs.next()) 
             {
                 rs.close();
                 ps.close();
                 id = -0x01;
                 proper = false;
                 return;
             }
             
             id = familyId;
             leaderid = rs.getInt("characterLeaderId");
             leadername = MapleCharacter.getNameById(leaderid, 0x00); // TODO HAS MORE THEN 1 WORLD????
             notice = rs.getString("familyNotice");
             
             rs.close();
             ps.close();
             
             //does not need to be in any order
             ps = con.prepareStatement("SELECT idCharacter, currentReputation, totalReputation, todayEarnedReputation, seniorId, junior1, junior2 FROM characterFamilyInfo WHERE idFamily = ?", ResultSet.CONCUR_UPDATABLE);
             ps.setInt(0x01, familyId);
             rs = ps.executeQuery();
             
             while (rs.next()) 
             {
                 members.put(rs.getInt("idCharacter"), 
                 new MapleFamilyCharacterInfo(rs.getInt("idCharacter"), familyId, rs.getInt("senior"), rs.getInt("junior1"), rs.getInt("junior2"), rs.getInt("currentReputation"), rs.getInt("totalReputation"), rs.getInt("todayReputation")));
             }
             
             rs.close();
             ps.close();

             if (leadername == null || members.size() < 0x02) 
             {
                 System.err.println("Leader " + leaderid + " isn't in family " + id + ". Members: " + members.size() + ".  Impossible... family is disbanding.");
                 writeToDB(true);
                 proper = false;
                 return;
             }
             
             for (final MapleFamilyCharacterInfo mfc : members.values())
             {
                 if (mfc.getJunior1() > 0x00 && (getMFC(mfc.getJunior1()) == null || mfc.getCharacterId() == mfc.getJunior1())) 
                     mfc.setJunior1(0x00);
                 if (mfc.getJunior2() > 0x00 && (getMFC(mfc.getJunior2()) == null || mfc.getCharacterId() == mfc.getJunior2() || mfc.getJunior1() == mfc.getJunior2())) 
                     mfc.setJunior2(0x00);
                 if (mfc.getSenior() > 0x00 && (getMFC(mfc.getSenior()) == null || mfc.getCharacterId() == mfc.getSenior())) 
                     mfc.setSenior(0x00);
                 
                 if (mfc.getJunior2() > 0x00 && mfc.getJunior1() <= 0x00) 
                 {
                     mfc.setJunior1(mfc.getJunior2());
                     mfc.setJunior2(0x00);
                 }
                 
                 if (mfc.getJunior1() > 0x00) 
                 {
                     final MapleFamilyCharacterInfo mfc2 = getMFC(mfc.getJunior1());
                     
                     if (mfc2.getJunior1() == mfc.getCharacterId()) 
                         mfc2.setJunior1(0);
                     if (mfc2.getJunior2() == mfc.getCharacterId())
                         mfc2.setJunior2(0);
                     if (mfc2.getSenior() != mfc.getCharacterId()) 
                         mfc2.setSenior(mfc.getCharacterId());
                 }
                 
                 if (mfc.getJunior2() > 0x00) 
                 {
                     final MapleFamilyCharacterInfo mfc2 = getMFC(mfc.getJunior2());
                     
                     if (mfc2.getJunior1() == mfc.getCharacterId()) 
                         mfc2.setJunior1(0x00);
                     if (mfc2.getJunior2() == mfc.getCharacterId()) 
                         mfc2.setJunior2(0x00);
                     if (mfc2.getSenior() != mfc.getCharacterId()) 
                         mfc2.setSenior(mfc.getCharacterId());
                 }
             }

             resetPedigree();
             resetDescendants(); 
         } 
    	 catch (SQLException se) 
         {
             System.err.println("unable to read family information from sql");
         }
    }
    
    public Integer getId()
    {
    	return id;
    }
    
    public String getLeaderName() 
    {
		return leadername;
	}

	public void setLeaderName(String leadername) 
	{
		this.leadername = leadername;
	}

	public int getLeaderId() 
	{
		return leaderid;
	}

	public void setLeaderId(int leaderid) 
	{
		this.leaderid = leaderid;
	}

	public String getNotice() 
	{
		return notice;
	}

	public void setNotice(String notice) 
	{
		this.notice = notice;
	}
	
	public boolean isProper()
	{
		return proper;
	}

	public final MapleFamilyCharacterInfo getMFC(final int cid) 
    {
        return members.get(cid);
    }
	
    public final void setOnline(final int cid, final boolean online, final int channel) 
    {
        final MapleFamilyCharacterInfo mgc = getMFC(cid);
        
        if (mgc != null && mgc.getFamily().getId() == id) 
        {
            if (mgc.isOnline() != online)
            {
            	try 
            	{
					broadcast(MaplePacketCreator.familyLoggedIn(online, MapleCharacter.getNameById(mgc.getCharacterId(), 0x00)), cid, mgc.getCharacterId() == leaderid ? null : mgc.getPedigree());
				}
            	catch (SQLException e) 
            	{
					e.printStackTrace();
				}	
            }
            
            mgc.setOnline(online);
        }
        bDirty = true; // member formation has changed, update notifications
    }
	
    public final void writeToDB(final boolean bDisband) 
    {
        try 
        {
            Connection con = DatabaseConnection.getConnection();
            if (!bDisband) 
            {
                if (changed) 
                {
                    try (PreparedStatement ps = con.prepareStatement("UPDATE family SET notice = ? WHERE idFamily = ?")) 
                    {
                        ps.setString(0x01, notice);
                        ps.setInt(0x02, id);
                        ps.execute();
                    }
                }
                changed = false;
            }
            else 
            {
                if (leadername == null || members.size() < 2) 
                	broadcast(null, -1, FCOp.DISBAND, null);
                
                try (PreparedStatement ps = con.prepareStatement("DELETE FROM family WHERE idFamily = ?")) 
                {
                    ps.setInt(0x01, id);
                    ps.execute();
                }
            }
        }
        catch (SQLException se) 
        {
            System.err.println("Error saving family to SQL");
        }
    }
    
    public static void updateCharacterFamilyInfo(int familyid, int seniorid, int junior1, int junior2, int currentrep, int totalrep, int cid) 
    {
        try 
        {
            final java.sql.Connection con = DatabaseConnection.getConnection();
            
            if(con.createStatement().executeQuery("SELECT idFamily from characterFamilyInfo WHERE idCharacter = " + cid).next())
            {
                try (java.sql.PreparedStatement ps = con.prepareStatement("UPDATE characterFamilyInfo SET idFamily = ?, seniorId = ?, junior1 = ?, junior2 = ?, currentReputation = ?, totalReputation = ? WHERE idCharacter = ?")) 
                {
                    ps.setInt(0x01, familyid);
                    ps.setInt(0x02, seniorid);
                    ps.setInt(0x03, junior1);
                    ps.setInt(0x04, junior2);
                    ps.setInt(0x05, currentrep);
                    ps.setInt(0x06, totalrep);
                    ps.setInt(0x07, cid);
                    ps.execute();
                }	
            }
            else
            {
                try (java.sql.PreparedStatement ps = con.prepareStatement("INSERT INTO characterFamilyInfo (idFamily, idCharacter, seniorId, junior1, junior2, currentReputation, totalReputation) VALUES (?,?,?,?,?,?,?) ")) 
                {
                    ps.setInt(0x01, familyid);
                    ps.setInt(0x02, cid);
                    ps.setInt(0x03, seniorid);
                    ps.setInt(0x04, junior1);
                    ps.setInt(0x05, junior2);
                    ps.setInt(0x06, currentrep);
                    ps.setInt(0x07, totalrep);
                    ps.execute();
                }	
            }
        } 
        catch (SQLException se) 
        {
            System.out.println("SQLException: " + se.getLocalizedMessage());
        }
    }
	
    public static int createFamily(int leaderId) 
    {
        try 
        {
        	final Connection con = DatabaseConnection.getConnection();

        	PreparedStatement ps = con.prepareStatement("INSERT INTO family (`characterLeaderId`, `familyName`) VALUES (?, ?)", Statement.RETURN_GENERATED_KEYS);
            ps.setInt(0x01, leaderId);
            ps.setString(0x02, MapleCharacter.getNameById(leaderId, 0x00));
            ps.executeUpdate();
             
            ResultSet rs = ps.getGeneratedKeys();
            		
    		if (!rs.next()) 
                return 0x00;   
            
            return rs.getInt(0x01);
        }
        catch (SQLException e) 
        {
        	e.printStackTrace();
            return 0x00;
        }
    }
    
    public void resetPedigree() 
    {
        for (MapleFamilyCharacterInfo mfc : members.values()) 
            mfc.resetPedigree(this);
        
        bDirty = true;
    }
    
    public void resetDescendants()
    {
        MapleFamilyCharacterInfo mfc = getMFC(leaderid);
        
        if (mfc != null)
            mfc.resetDescendants(this);
        
        bDirty = true;
    }
    
    public final void leaveFamily(final int id) 
    {
        leaveFamily(getMFC(id), true);
    }

    public final void leaveFamily(final MapleFamilyCharacterInfo mgc, final boolean skipLeader) 
    {
        bDirty = true;
        
        if (mgc.getCharacterId() == leaderid && !skipLeader) 
        {
            leadername = null;
           	WorldRegistryImpl.getInstance().removeFamily(id);
        }
        else 
        {
            if (mgc.getJunior1() > 0x00)
            {
                MapleFamilyCharacterInfo j = getMFC(mgc.getJunior1());
                
                if (j != null) 
                {
                    j.setSenior(0x00);
                    splitFamily(j.getCharacterId(), j);
                }
            }
            if (mgc.getJunior2() > 0x00) 
            {
                MapleFamilyCharacterInfo j = getMFC(mgc.getJunior2());
                
                if (j != null) 
                {
                    j.setSenior(0x00);
                    splitFamily(j.getCharacterId(), j);
                }
            }
            if (mgc.getSenior() > 0x00)
            {
            	MapleFamilyCharacterInfo mfc = getMFC(mgc.getSenior());
            	
                if (mfc != null) 
                {
                    if (mfc.getJunior1() == mgc.getCharacterId()) 
                        mfc.setJunior1(0x00);
                    else 
                        mfc.setJunior2(0x00);
                }
            }
            
            broadcast(null, -0x01, FCOp.DISBAND, Arrays.asList(mgc.getCharacterId()));
            resetPedigree(); 
        }
        members.remove(mgc.getCharacterId());
        bDirty = true;
    }
    
    public final void disbandFamily() 
    {
        writeToDB(true);
    }
	
    public boolean splitFamily(int splitId, MapleFamilyCharacterInfo def) 
    {
        MapleFamilyCharacterInfo leader = getMFC(splitId);
        
        if (leader == null) 
        {
            leader = def;
            
            if (leader == null) 
                return false;
        }
        try 
        {
            final List<MapleFamilyCharacterInfo> all = leader.getAllJuniors(this); 
            
            if (all.size() <= 1) 
            {
                leaveFamily(leader, false);
                return true;
            }
            
            final int newId = createFamily(leader.getCharacterId());
            final MapleFamily newfam = WorldRegistryImpl.getInstance().getFamily(newId);
            
            if (newId <= 0x00) 
                return false;
            
            for (MapleFamilyCharacterInfo mgc : all)
            {
            	mgc.setFamily(newfam);
                updateCharacterFamilyInfo(newId, mgc.getSenior(), mgc.getJunior1(), mgc.getJunior2(), mgc.getReputation(), mgc.getTotalReputation(), mgc.getCharacterId());
                members.remove(mgc.getCharacterId());
            }
            
            for (MapleFamilyCharacterInfo mgc : all) 
            {
            	if(WorldRegistryImpl.getInstance().find(mgc.getCharacterId()) > -0x01)
            		WorldRegistryImpl.getInstance().setFamily(newfam, mgc.getCharacterId());
            }
        }
        catch (RemoteException e) 
        {
			e.printStackTrace();
		} 
        finally 
        {
            if (members.size() <= 0x01)
            {
               	WorldRegistryImpl.getInstance().removeFamily(id);
                return true;
            }
        }
        bDirty = true;
        return false;
    }
    
    private void buildNotifications() 
    {
        if (!bDirty) 
            return;
        
        final Iterator<Entry<Integer, MapleFamilyCharacterInfo>> toRemove = members.entrySet().iterator();
        
        while (toRemove.hasNext()) 
        {
            MapleFamilyCharacterInfo mfc = toRemove.next().getValue();
            
            if (mfc.getJunior1() > 0x00 && getMFC(mfc.getJunior1()) == null) 
                mfc.setJunior1(0x00);
            if (mfc.getJunior2() > 0x00 && getMFC(mfc.getJunior2()) == null) 
                mfc.setJunior2(0x00);
            if (mfc.getSenior() > 0x00 && getMFC(mfc.getSenior()) == null)
                mfc.setSenior(0x00);
            
            if (mfc.getFamily().id != id) 
                toRemove.remove();
        }
        
        if (members.size() < 0x02)
           	WorldRegistryImpl.getInstance().removeFamily(id);
        
        bDirty = false;
    }

    
    public final void broadcast(final MaplePacket packet, List<Integer> cids) 
    {
        broadcast(packet, -0x01, FCOp.NONE, cids);
    }

    public final void broadcast(final MaplePacket packet, final int exception, List<Integer> cids) 
    {
        broadcast(packet, exception, FCOp.NONE, cids);
    }

    public final void broadcast(final MaplePacket packet, final int exceptionId, final FCOp bcop, List<Integer> cids) 
    {	
        buildNotifications();
        
        if (members.size() < 0x02) 
        {
            bDirty = true;
            return;
        }
        
        for (MapleFamilyCharacterInfo mgc : members.values()) 
        {
            if (cids == null || cids.contains(mgc.getCharacterId())) 
            {
                if (bcop == FCOp.DISBAND) 
                {
                    if (mgc.isOnline()) 
                    {
                    	try 
                    	{
                    		updateCharacterFamilyInfo(0x00, 0x00, 0x00, 0x00, mgc.getReputation(), mgc.getTotalReputation(), mgc.getCharacterId());
							WorldRegistryImpl.getInstance().getChannel(WorldRegistryImpl.getInstance().find(mgc.getCharacterId())).playerStorage().getCharacterById(mgc.getCharacterId()).setMapleFamily(null);
						}
                    	catch (RemoteException e) 
						{
							e.printStackTrace();
						}
                    }
                    else 
                        updateCharacterFamilyInfo(0x00, 0x00, 0x00, 0x00, mgc.getReputation(), mgc.getTotalReputation(), mgc.getCharacterId());
                } 
                else if (mgc.isOnline() && mgc.getCharacterId() != exceptionId) 
                {
                	try 
                	{
						WorldRegistryImpl.getInstance().getChannel(WorldRegistryImpl.getInstance().find(mgc.getCharacterId())).sendPacket(Arrays.asList(mgc.getCharacterId()), packet, exceptionId);
					}
                	catch (RemoteException e) 
                	{
						e.printStackTrace();
					}
                }
            }
        }
    }
    
    public static MapleFamily getMyFamile(final int characterId)
    {
    	try
    	{
        	PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement("SELECT idFamily FROM characterFamilyInfo WHERE idCharacter = ?", ResultSet.CONCUR_UPDATABLE);
            ps.setInt(0x01, characterId);
            
            ResultSet rs = ps.executeQuery();
            
            if(rs.next())
            	return new MapleFamily(rs.getInt("idFamily"));
            else
            	return null;
    	}
    	catch(Exception ex)
    	{
    		ex.printStackTrace();
    		return null;
    	}
    }
}