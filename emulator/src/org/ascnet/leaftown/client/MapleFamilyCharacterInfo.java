package org.ascnet.leaftown.client;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.ascnet.leaftown.database.DatabaseConnection;
import org.ascnet.leaftown.net.channel.ChannelServer;

public class MapleFamilyCharacterInfo implements Serializable
{
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(MapleFamilyCharacterInfo.class);
    
	private static final long serialVersionUID = 2582617925553587955L;
	private MapleFamily family;
    private int characterId, characterLevel, characterJob, characterChannel;
    private String characterName;
    private int totalJuniors = 0x02;
    private int reputation, totalReputation, todayReputation, senior, junior1, junior2;
    private final List<Integer> pedigree = new ArrayList<>();
    private int descendants = 0x00;
    private boolean online = false;

    public MapleFamilyCharacterInfo(int _id, int _level, int _job, int _channel, String _name, final MapleFamily family, int _sid, int _jr1, int _jr2, int _crep, int _trep, int _torep) 
    {
        characterId = _id;
        characterLevel = _level;
        characterJob = _job;
        characterName = _name;
        characterChannel = _channel;
        this.family = family;
        reputation = _crep;
        totalReputation = _trep;
        senior = _sid;
        junior1 = _jr1;
        junior2 = _jr2;
    }

	public MapleFamily getFamily() 
	{
		return family;
	}

	public void setFamily(MapleFamily family) 
	{
		this.family = family;
	}

	public boolean isOnline() 
	{
		return online;
	}

	public void setOnline(boolean online) 
	{
		this.online = online;
	}

	public int getCharacterId() 
	{
		return characterId;
	}

	public int getCharacterLevel() 
	{
		return characterLevel;
	}

	public int getCharacterJob() 
	{
		return characterJob;
	}

	public int getCharacterChannel()
	{
		return characterChannel;
	}

	public String getCharacterName() 
	{
		return characterName;
	}

	public int getTotalJuniors()
	{
		return totalJuniors;
	}

	public void setTotalJuniors(int totalJuniors) 
	{
		this.totalJuniors = totalJuniors;
	}

	public int getReputation()
	{
		return reputation;
	}

	public void setReputation(int reputation)
	{
		this.reputation = reputation;
	}

	public int getTotalReputation()
	{
		return totalReputation;
	}

	public void setTotalReputation(int totalReputation)
	{
		this.totalReputation = totalReputation;
	}

	public int getTodayReputation()
	{
		return todayReputation;
	}

	public void setTodayReputation(int todayReputation)
	{
		this.todayReputation = todayReputation;
	}

	public int getSenior()
	{
		return senior;
	}

	public void setSenior(int senior) 
	{
		this.senior = senior;
	}

	public int getJunior1()
	{
		return junior1;
	}

	public void setJunior1(int junior1) 
	{
		this.junior1 = junior1;
	}

	public int getJunior2()
	{
		return junior2;
	}

	public void setJunior2(int junior2) 
	{
		this.junior2 = junior2;
	}
	
	public void writeOnDB(Connection con) throws SQLException
	{
    	boolean ownConnection = false;
    	
        try 
        {
    		if((ownConnection = (con == null || con.isClosed())))
    			con = DatabaseConnection.getConnection();
    		
            if(con.createStatement().executeQuery("SELECT idFamily from characterFamilyInfo WHERE idCharacter = " + characterId).next())
            {
                try (java.sql.PreparedStatement ps = con.prepareStatement("UPDATE characterFamilyInfo SET idFamily = ?, seniorId = ?, junior1 = ?, junior2 = ?, currentReputation = ?, totalReputation = ? WHERE idCharacter = ?")) 
                {
                    ps.setInt(0x000001, family.getId());
                    ps.setInt(0x000002, senior);
                    ps.setInt(0x000003, junior1);
                    ps.setInt(0x000004, junior2);
                    ps.setInt(0x000005, reputation);
                    ps.setInt(0x000006, totalReputation);
                    ps.setInt(0x000007, characterId);
                    ps.execute();
                }	
            }
            else
            {
                try (java.sql.PreparedStatement ps = con.prepareStatement("INSERT INTO characterFamilyInfo (idFamily, idCharacter, seniorId, junior1, junior2, currentReputation, totalReputation) VALUES (?,?,?,?,?,?,?) ")) 
                {
                    ps.setInt(0x000001, family.getId());
                    ps.setInt(0x000002, characterId);
                    ps.setInt(0x000003, senior);
                    ps.setInt(0x000004, junior1);
                    ps.setInt(0x000005, junior2);
                    ps.setInt(0x000006, reputation);
                    ps.setInt(0x000007, totalReputation);
                    ps.execute();
                }	
            }
        } 
        catch (SQLException se) 
        {
            log.error("Não foi possível escrever as alterações do MapleFamilyCharacterInfo do Jogador " + characterId, se);
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
	
    public List<MapleFamilyCharacterInfo> getAllJuniors(final MapleFamily fam) 
    {
        final List<MapleFamilyCharacterInfo> ret = new ArrayList<>();
        
        ret.add(this);
        
        if (junior1 > 0x00) 
        {
        	final MapleFamilyCharacterInfo chr = fam.getMFC(junior1);
            
            if (chr != null) 
                ret.addAll(chr.getAllJuniors(fam));
        }
        
        if (junior2 > 0) 
        {
            final MapleFamilyCharacterInfo chr = fam.getMFC(junior2);
            
            if (chr != null)
                ret.addAll(chr.getAllJuniors(fam));
        }
        return ret;
    }
	
	public final boolean juniorOnline(final int junior)
	{
        for (ChannelServer cserv : ChannelServer.getAllInstances()) 
        {
            if(cserv.getPlayerStorage().getCharacterById(junior) != null)
            	return true;
        }
        return false;
	}
	
    public final List<MapleFamilyCharacterInfo> getOnlineJuniors(MapleFamily fam) 
    {
        final List<MapleFamilyCharacterInfo> ret = new ArrayList<>();
        
        ret.add(this);
        
        if (junior1 > 0x00)
        {
            MapleFamilyCharacterInfo chr = fam.getMFC(junior1);
            
            if (chr != null) 
            {
                if (juniorOnline(chr.getCharacterId()))
                    ret.add(chr);

                if (chr.getJunior1() > 0x00) 
                {
                	final MapleFamilyCharacterInfo chr2 = fam.getMFC(chr.getJunior1());
                    
                    if (chr2 != null && juniorOnline(chr2.getCharacterId()))
                        ret.add(chr2);
                }
                if (chr.getJunior2() > 0x00) 
                {
                	final MapleFamilyCharacterInfo chr2 = fam.getMFC(chr.getJunior2());
                    
                    if (chr2 != null && juniorOnline(chr2.getCharacterId()))
                        ret.add(chr2);
                }
            }
        }
        
        if (junior2 > 0x00) 
        {
            final MapleFamilyCharacterInfo chr = fam.getMFC(junior2);
            
            if (chr != null) 
            {
                if (juniorOnline(chr.getCharacterId()))
                    ret.add(chr);
                
                if (chr.getJunior1() > 0x00) 
                {
                	final MapleFamilyCharacterInfo chr2 = fam.getMFC(chr.getJunior1());
                    
                    if (chr2 != null && juniorOnline(chr2.getCharacterId()))
                        ret.add(chr2);
                }
                
                if (chr.getJunior2() > 0x00) 
                {
                	final MapleFamilyCharacterInfo chr2 = fam.getMFC(chr.getJunior2());
                    
                    if (chr2 != null && juniorOnline(chr2.getCharacterId()))
                        ret.add(chr2);
                }
            }
        }
        return ret;
    }
	
    public final List<Integer> getPedigree() 
    {
        return pedigree;
    }
	
    public void resetPedigree(MapleFamily fam) 
    {
        pedigree.clear();
        
        pedigree.add(characterId);
        
        if (senior > 0x00) 
        {
        	final MapleFamilyCharacterInfo chr = fam.getMFC(senior);
            
            if (chr != null) 
            {
                pedigree.add(senior);
                
                if (chr.getSenior() > 0x00) 
                    pedigree.add(chr.getSenior());
                if (chr.getJunior1() > 0x00 && chr.getJunior1() != characterId) 
                    pedigree.add(chr.getJunior1());
                else if (chr.getJunior2() > 0x00 && chr.getJunior2() != characterId) 
                    pedigree.add(chr.getJunior2());
            }
        }
        
        if (junior1 > 0x00) 
        {
        	final MapleFamilyCharacterInfo chr = fam.getMFC(junior1);
            
            if (chr != null) 
            {
                pedigree.add(junior1);
                if (chr.getJunior1() > 0x00) 
                    pedigree.add(chr.getJunior1());
                if (chr.getJunior2() > 0x00) 
                    pedigree.add(chr.getJunior2());
            }
        }
        
        if (junior2 > 0x00) 
        {
            final MapleFamilyCharacterInfo chr = fam.getMFC(junior2);
            
            if (chr != null) 
            {
                pedigree.add(junior2);
                
                if (chr.getJunior1() > 0x00) 
                    pedigree.add(chr.getJunior1());
                if (chr.getJunior2() > 0x00) 
                    pedigree.add(chr.getJunior2());
            }
        }

    }

    public int getDescendants() 
    {
        return descendants;
    }

    public final int resetDescendants(MapleFamily fam) 
    {
        descendants = 0x00;
        
        if (junior1 > 0x00) 
        {
            final MapleFamilyCharacterInfo chr = fam.getMFC(junior1);
            
            if (chr != null) 
                descendants += 0x01 + chr.resetDescendants(fam);
        }
        
        if (junior2 > 0x00) 
        {
        	final MapleFamilyCharacterInfo chr = fam.getMFC(junior2);
            
            if (chr != null)
                descendants += 0x01 + chr.resetDescendants(fam);
        }
        
        return descendants;
    }

    public final int getNoJuniors() 
    {
        int ret = 0x00;
        
        if (junior1 > 0x00) 
            ret++;
        if (junior2 > 0x00) 
            ret++;
        
        return ret;
    }

    @Override
    public int hashCode() 
    {
        return 0x1F + characterId;
    }
}