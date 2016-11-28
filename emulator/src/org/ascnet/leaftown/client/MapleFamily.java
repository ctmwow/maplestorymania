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
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(MapleFamily.class);
    
    public static enum FCOp { NONE, UPDATE, DISBAND; }
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
             ps.setInt(0x000001, familyId);
             
             ResultSet rs = ps.executeQuery();

             if (!rs.next()) 
             {
                 rs.close();
                 ps.close();
                 id = -0x000001;
                 proper = false;
                 return;
             }
             
             id = familyId;
             leaderid = rs.getInt("characterLeaderId");
             leadername = MapleCharacter.getNameById(leaderid, 0x000000); // TODO HAS MORE THEN 1 WORLD????
             notice = rs.getString("familyNotice");
             
             rs.close();
             ps.close();

             ps = con.prepareStatement("SELECT characterFamilyInfo.idCharacter, characterFamilyInfo.currentReputation, characterFamilyInfo.totalReputation, "
             		+ "characterFamilyInfo.todayEarnedReputation, characterFamilyInfo.seniorId, characterFamilyInfo.junior1, characterFamilyInfo.junior2, "
             		+ "characters.level, characters.name, characters.job " 
             		+ "FROM characterFamilyInfo "
             		+ "INNER JOIN characters ON characters.id = characterFamilyInfo.idCharacter "
             		+ "WHERE characterFamilyInfo.idFamily = ?", ResultSet.CONCUR_UPDATABLE);
             ps.setInt(0x000001, familyId);
             rs = ps.executeQuery();
             
             while (rs.next()) 
             {
                 members.put(rs.getInt("characterFamilyInfo.idCharacter"), 
                 new MapleFamilyCharacterInfo(rs.getInt("characterFamilyInfo.idCharacter"), rs.getInt("characters.level"), rs.getInt("characters.job"), 0x000 /** channel */, rs.getString("characters.name"),
                		 this, rs.getInt("characterFamilyInfo.seniorId"), rs.getInt("characterFamilyInfo.junior1"), rs.getInt("characterFamilyInfo.junior2"), 
                		 rs.getInt("characterFamilyInfo.currentReputation"), rs.getInt("characterFamilyInfo.totalReputation"), rs.getInt("characterFamilyInfo.todayEarnedReputation")));
             }
             
             rs.close();
             ps.close();

             if (leadername == null || members.size() < 0x000002) 
             {
                 log.error("Leader " + leaderid + " isn't in family " + id + ". Members: " + members.size() + ".  Impossible... family is disbanding.");
                 writeToDB(true);
                 proper = false;
                 return;
             }
             
             for (final MapleFamilyCharacterInfo mfc : members.values())
             {
                 if (mfc.getJunior1() > 0x000000 && (getMFC(mfc.getJunior1()) == null || mfc.getCharacterId() == mfc.getJunior1())) 
                     mfc.setJunior1(0x000000);
                 if (mfc.getJunior2() > 0x000000 && (getMFC(mfc.getJunior2()) == null || mfc.getCharacterId() == mfc.getJunior2() || mfc.getJunior1() == mfc.getJunior2())) 
                     mfc.setJunior2(0x000000);
                 if (mfc.getSenior() > 0x000000 && (getMFC(mfc.getSenior()) == null || mfc.getCharacterId() == mfc.getSenior())) 
                     mfc.setSenior(0x000000);
                 
                 if (mfc.getJunior2() > 0x000000 && mfc.getJunior1() <= 0x000000) 
                 {
                     mfc.setJunior1(mfc.getJunior2());
                     mfc.setJunior2(0x000000);
                 }
                 
                 if (mfc.getJunior1() > 0x000000) 
                 {
                     final MapleFamilyCharacterInfo mfc2 = getMFC(mfc.getJunior1());
                     
                     if (mfc2.getJunior1() == mfc.getCharacterId()) 
                         mfc2.setJunior1(0x000000);
                     if (mfc2.getJunior2() == mfc.getCharacterId())
                         mfc2.setJunior2(0x000000);
                     if (mfc2.getSenior() != mfc.getCharacterId()) 
                         mfc2.setSenior(mfc.getCharacterId());
                 }
                 
                 if (mfc.getJunior2() > 0x000000) 
                 {
                     final MapleFamilyCharacterInfo mfc2 = getMFC(mfc.getJunior2());
                     
                     if (mfc2.getJunior1() == mfc.getCharacterId()) 
                         mfc2.setJunior1(0x000000);
                     if (mfc2.getJunior2() == mfc.getCharacterId()) 
                         mfc2.setJunior2(0x000000);
                     if (mfc2.getSenior() != mfc.getCharacterId()) 
                         mfc2.setSenior(mfc.getCharacterId());
                 }
             }

             resetPedigree();
             resetDescendants(); 
         } 
    	 catch (SQLException se) 
         {
    		 log.error("Não foi possível carregar a familia " + familyId, se);
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
		changed = true;
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
                mgc.setOnline(online);
                members.put(cid, mgc);

            	try 
            	{
					broadcast(MaplePacketCreator.familyLoggedIn(online, MapleCharacter.getNameById(mgc.getCharacterId(), 0x00)), cid, mgc.getCharacterId() == leaderid ? null : mgc.getPedigree());
				}
            	catch (SQLException e) 
            	{
					e.printStackTrace();
				}	
            }
        }
        bDirty = true; // member formation has changed, update notifications
    }
	
    public final void writeToDB(final boolean bDisband) 
    {
        Connection con = DatabaseConnection.getConnection();
        try 
        {
            if (!bDisband) 
            {
                if (changed) 
                {
                    try (PreparedStatement ps = con.prepareStatement("UPDATE family SET familyNotice = ? WHERE idFamily = ?")) 
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
        	log.error("Não foi possível gravar as informações da família!", se);
        }
        finally
        {
        	try 
        	{
        		con.close();
			}
        	catch (SQLException e) 
        	{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        }
    }
    
    public static void updateCharacterFamilyInfo(Connection con, int familyid, int seniorid, int junior1, int junior2, int currentrep, int totalrep, int cid) throws SQLException
    {
    	boolean ownConnection = false;
    	
        try 
        {
    		if((ownConnection = (con == null || con.isClosed())))
    			con = DatabaseConnection.getConnection();
    		
            if(con.createStatement().executeQuery("SELECT idFamily from characterFamilyInfo WHERE idCharacter = " + cid).next())
            {
                try (java.sql.PreparedStatement ps = con.prepareStatement("UPDATE characterFamilyInfo SET idFamily = ?, seniorId = ?, junior1 = ?, junior2 = ?, currentReputation = ?, totalReputation = ? WHERE idCharacter = ?")) 
                {
                    ps.setInt(0x000001, familyid);
                    ps.setInt(0x000002, seniorid);
                    ps.setInt(0x000003, junior1);
                    ps.setInt(0x000004, junior2);
                    ps.setInt(0x000005, currentrep);
                    ps.setInt(0x000006, totalrep);
                    ps.setInt(0x000007, cid);
                    ps.execute();
                }	
            }
            else
            {
                try (java.sql.PreparedStatement ps = con.prepareStatement("INSERT INTO characterFamilyInfo (idFamily, idCharacter, seniorId, junior1, junior2, currentReputation, totalReputation) VALUES (?,?,?,?,?,?,?) ")) 
                {
                    ps.setInt(0x000001, familyid);
                    ps.setInt(0x000002, cid);
                    ps.setInt(0x000003, seniorid);
                    ps.setInt(0x000004, junior1);
                    ps.setInt(0x000005, junior2);
                    ps.setInt(0x000006, currentrep);
                    ps.setInt(0x000007, totalrep);
                    ps.execute();
                }	
            }
        } 
        catch (SQLException se) 
        {
            log.error("Não foi possível atualizar o CharacterFamilyInfo do Player " + cid, se);
            throw se;
        }
        finally
        {
        	if(ownConnection)
        	{
            	try
            	{
    				con.close();
    			}
            	catch (SQLException e) 
            	{
					log.warn("Não foi possível fechar a conexão", e);
    			}	
        	}
        }
    }
	
    public static int createFamily(Connection con, int leaderId) throws SQLException
    {
    	boolean ownConnection = false;
    	
        try 
        {
    		if((ownConnection = (con == null || con.isClosed())))
    			con = DatabaseConnection.getConnection();
    		
        	PreparedStatement ps = con.prepareStatement("INSERT INTO family (`characterLeaderId`, `familyName`) VALUES (?, ?)", Statement.RETURN_GENERATED_KEYS);
            ps.setInt(0x000001, leaderId);
            ps.setString(0x000002, MapleCharacter.getNameById(leaderId, 0x00));
            ps.executeUpdate();
             
            ResultSet rs = ps.getGeneratedKeys();
            		
    		if (!rs.next()) 
                return 0x000000;   
            
            return rs.getInt(0x000001);
        }
        catch (SQLException e) 
        {
    		log.error("Não foi possível carregar a familia do Jogador " + leaderId, e);
            throw e;
        }
        finally
        {
    		if(ownConnection)
    		{
				try 
				{
					con.close();
				}
				catch (SQLException e) 
				{
					log.warn("Não foi possível fechar a conexão", e);
				}
    		}
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
        
    	final Connection con = DatabaseConnection.getConnection();
    	
        try 
        {
        	con.setAutoCommit(false);
        	
            final List<MapleFamilyCharacterInfo> all = leader.getAllJuniors(this); 
            
            if (all.size() <= 1) 
            {
                leaveFamily(leader, false);
                return true;
            }
            
            final int newId = createFamily(con, leader.getCharacterId());
            final MapleFamily newfam = WorldRegistryImpl.getInstance().getFamily(newId);
            
            if (newId <= 0x00) 
                return false;
            
            for (MapleFamilyCharacterInfo mgc : all)
            {
            	mgc.setFamily(newfam);
                members.remove(mgc.getCharacterId());
                updateCharacterFamilyInfo(con, newId, mgc.getSenior(), mgc.getJunior1(), mgc.getJunior2(), mgc.getReputation(), mgc.getTotalReputation(), mgc.getCharacterId());
            }
            
            for (MapleFamilyCharacterInfo mgc : members.values())
            {
                updateCharacterFamilyInfo(con, newId, mgc.getSenior() == splitId ? 0 : mgc.getSenior(), mgc.getJunior1() == splitId ? 0 : mgc.getJunior1(), 
                		mgc.getJunior2() == splitId ? 0 : mgc.getJunior2(), mgc.getReputation(), mgc.getTotalReputation(), mgc.getCharacterId());
            }
            
            resetDescendants();
            resetPedigree();
            
            WorldRegistryImpl.getInstance().updateFamily(newfam);
            WorldRegistryImpl.getInstance().updateFamily(this);
            
            newfam.broadcast(null, 0, FCOp.UPDATE, null);
            broadcast(null, 0, FCOp.UPDATE, null);
            
            con.commit();
        }
        catch (SQLException e) 
        {
			log.error("Não foi possível dividir a familia " + id, e);
			
			try
			{
				con.rollback();
			} 
			catch (SQLException e1) 
			{
				log.error("Não foi possível fazer o rollback da conexão", e1);
			}
		} 
        finally 
        {
        	try 
        	{
				con.close();
			} 
        	catch (SQLException e)
        	{
				log.warn("Não foi possível fechar a conexão", e);
			}
        	
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
        
        final Connection con = DatabaseConnection.getConnection();
        
        try
        {
        	con.setAutoCommit(false);
            
            for (final MapleFamilyCharacterInfo mgc : members.values()) 
            {
                if (cids == null || cids.contains(mgc.getCharacterId())) 
                {
                    if (bcop == FCOp.DISBAND) 
                    {
                        if (mgc.isOnline()) 
                        {
                        	try 
                        	{
                        		updateCharacterFamilyInfo(con, 0x00, 0x00, 0x00, 0x00, mgc.getReputation(), mgc.getTotalReputation(), mgc.getCharacterId());
    							WorldRegistryImpl.getInstance().getChannel(WorldRegistryImpl.getInstance().find(mgc.getCharacterId())).playerStorage().getCharacterById(mgc.getCharacterId()).setMapleFamily(null);
    						}
                        	catch (RemoteException e) 
    						{
    							log.warn("Não foi possível atualizar a familia do Jogador" + mgc.getCharacterId(), e);
    						}
                        }
                        else 
                            updateCharacterFamilyInfo(con, 0x00, 0x00, 0x00, 0x00, mgc.getReputation(), mgc.getTotalReputation(), mgc.getCharacterId());
                    } 
                    else if(bcop == FCOp.UPDATE)
                    {
                    	if (mgc.isOnline()) 
                    	{
                    		mgc.setFamily(this);
                    		WorldRegistryImpl.getInstance().getChannel(WorldRegistryImpl.getInstance().find(mgc.getCharacterId())).playerStorage().getCharacterById(mgc.getCharacterId()).setMapleFamily(this);
                    	}
                    }
                    else if (mgc.isOnline() && mgc.getCharacterId() != exceptionId) 
                    {
                    	try 
                    	{
    						WorldRegistryImpl.getInstance().getChannel(WorldRegistryImpl.getInstance().find(mgc.getCharacterId())).sendPacket(Arrays.asList(mgc.getCharacterId()), packet, exceptionId);
    						WorldRegistryImpl.getInstance().getChannel(WorldRegistryImpl.getInstance().find(mgc.getCharacterId())).playerStorage().getCharacterById(mgc.getCharacterId()).setMapleFamily(this);
    					}
                    	catch (RemoteException e) 
                    	{
							log.warn("Não foi possível enviar o pacote para o Jogador " + mgc.getCharacterId(), e);
    					}
                    }
                }
            }
        }
        catch(Exception ex)
        {
        	log.error("Não foi possível fazer o broadcast do pacote para a familia " + id, ex);
        	try 
        	{
        		con.rollback();
        	}
        	catch (SQLException e) 
        	{
        		log.error("Não foi possível fazer o rollback da conexão", e);
        	}
        }
        finally
        {
        	try 
        	{
        		con.close();
        	}
        	catch (SQLException e) 
        	{
        		log.warn("Não foi possível fechar a conexão", e);
        	}
        }
    }
    
    public static MapleFamily getMyFamily(Connection con, final int characterId)
    {
    	boolean ownConnection = false;
    	
    	try
    	{
    		if((ownConnection = (con == null || con.isClosed())))
    			con = DatabaseConnection.getConnection();
    		
        	final PreparedStatement ps = con.prepareStatement("SELECT idFamily FROM characterFamilyInfo WHERE idCharacter = ?", ResultSet.CONCUR_UPDATABLE);
            ps.setInt(0x000001, characterId);
            
            final ResultSet rs = ps.executeQuery();
            
            if(rs.next())
            	return new MapleFamily(rs.getInt("idFamily"));
            else
            	return null;
    	}
    	catch(Exception ex)
    	{
    		log.error("Não foi possível carregar a familia do Jogador " + characterId, ex);
    		return null;
    	}
    	finally
    	{
    		if(ownConnection)
    		{
				try 
				{
					con.close();
				}
				catch (SQLException e) 
				{
					log.warn("Não foi possível fechar a conexão", e);
				}
    		}
    	}
    }
}