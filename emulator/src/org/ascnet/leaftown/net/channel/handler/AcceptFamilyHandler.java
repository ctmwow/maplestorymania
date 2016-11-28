package org.ascnet.leaftown.net.channel.handler;

import java.sql.Connection;
import java.sql.SQLException;

import org.ascnet.leaftown.client.MapleCharacter;
import org.ascnet.leaftown.client.MapleClient;
import org.ascnet.leaftown.client.MapleFamily;
import org.ascnet.leaftown.client.MapleFamilyCharacterInfo;
import org.ascnet.leaftown.database.DatabaseConnection;
import org.ascnet.leaftown.net.AbstractMaplePacketHandler;
import org.ascnet.leaftown.tools.MaplePacketCreator;
import org.ascnet.leaftown.tools.data.input.SeekableLittleEndianAccessor;

public final class AcceptFamilyHandler extends AbstractMaplePacketHandler
{
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(AcceptFamilyHandler.class);
    
    @Override
    public final void handlePacket(final SeekableLittleEndianAccessor slea, final MapleClient c) 
    {
    	 final MapleCharacter inviter = c.getPlayer().getMap().getCharacterById(slea.readInt());
    	 Connection con = null;
    	 
    	 try
    	 {
    		 if (inviter != null && c.getPlayer().getMFC().getSenior() == 0 && (c.getPlayer().isGM() || !inviter.isHidden()) && inviter.getLevel() - 20 <= c.getPlayer().getLevel() && 
            		 inviter.getLevel() >= 10 && inviter.getName().equals(slea.readMapleAsciiString()) && inviter.getMFC().getNoJuniors() < 2 && c.getPlayer().getLevel() >= 10) 
             {
                 final boolean accepted = slea.readByte() > 0x000000;
                 
                 inviter.getClient().sendPacket(MaplePacketCreator.sendFamilyJoinResponse(accepted, c.getPlayer().getName()));
                 
                 if (accepted) 
                 {
                	 con = DatabaseConnection.getConnection();
                	 con.setAutoCommit(false);
                	 
                     c.sendPacket(MaplePacketCreator.getSeniorMessage(inviter.getName()));
                     
                     final int old = c.getPlayer().getMFC() == null ? 0x000000 : c.getPlayer().getMFC().getFamily().getId();
                     final int oldj1 = c.getPlayer().getMFC() == null ? 0x000000 : c.getPlayer().getMFC().getJunior1();
                     final int oldj2 = c.getPlayer().getMFC() == null ? 0x000000 : c.getPlayer().getMFC().getJunior2();
                     
                     final MapleFamily fam = c.getChannelServer().getWorldInterface().getFamily(inviter.getMapleFamily().getId());
                     
                     if (fam != null && fam.getId() > 0x000000) 
                     {
                         final MapleFamilyCharacterInfo mf = inviter.getMFC();
                         
                         if (mf.getJunior1() > 0x000000) 
                             mf.setJunior2(c.getPlayer().getId()); 
                         else 
                             mf.setJunior1(c.getPlayer().getId());

                         mf.writeOnDB(con);
                         
                         final MapleFamily oldFamily = c.getChannelServer().getWorldInterface().getFamily(old);
                         
                         if (old > 0x000000 && oldFamily != null) 
                         { 
                             //MapleFamily.mergeFamily(fam, oldFamily); TODO MERGE
                         }
                         else
                         {
                        	 c.getChannelServer().getWorldInterface().setFamily(fam, c.getPlayer().getId());
                        	 
                        	 MapleFamily.updateCharacterFamilyInfo(con, fam.getId(), inviter.getId(), oldj1 <= 0 ? 0 : oldj1, oldj2 <= 0 ? 0 : oldj2, c.getPlayer().getMFC().getReputation(), c.getPlayer().getMFC().getTotalReputation(), c.getPlayer().getId());
                             fam.setOnline(c.getPlayer().getId(), true, c.getChannel());
                         }
                         
                         if (fam != null) 
                         {
                             if (inviter.getMFC().getNoJuniors() == 0x000001 || old > 0x000000)
                                 fam.resetDescendants();

                             fam.resetPedigree();
                         }
                     } 
                     else 
                     {
                         final int id = MapleFamily.createFamily(con, inviter.getId());
                         
                         if (id > 0x000000) 
                         {
                             MapleFamily.updateCharacterFamilyInfo(con, id, 0x000000, c.getPlayer().getId(), 0x000000, inviter.getMFC().getReputation(), inviter.getMFC().getTotalReputation(), inviter.getId());
                             MapleFamily.updateCharacterFamilyInfo(con, id, inviter.getId(), oldj1 <= 0x000000 ? 0x000000 : oldj1, oldj2 <= 0x000000 ? 0x000000 : oldj2, c.getPlayer().getMFC().getReputation(), c.getPlayer().getMFC().getTotalReputation(), c.getPlayer().getId());
                             
                             MapleFamily fam_ = c.getChannelServer().getWorldInterface().getFamily(id);
                             
                             c.getChannelServer().getWorldInterface().setFamily(fam_, c.getPlayer().getId());
                             c.getChannelServer().getWorldInterface().setFamily(fam_, inviter.getId());

                             fam.setOnline(inviter.getId(), true, inviter.getClient().getChannel());
                             
                             if (old > 0x000000 && c.getChannelServer().getWorldInterface().getFamily(old) != null) 
                             {
                                 //MapleFamily.mergeFamily(fam_, World.Family.getFamily(old)); // TODO MERGE
                             }
                             else 
                                 fam.setOnline(c.getPlayer().getId(), true, c.getChannel());
                             
                             fam_.resetDescendants();
                             fam_.resetPedigree();

                         }
                     }
                     c.sendPacket(MaplePacketCreator.getFamilyInfo(c.getPlayer().getMFC()));
                     c.getChannelServer().getWorldInterface().updateFamily(fam);
                 }
                 
             } 
    		 con.commit();
    	 }
    	 catch(Exception ex)
    	 {
    		 log.error("Não foi possível completar o pacote AcceptFamilyHandler do Lider " + inviter.getName() + " para o Jogador " + c.getPlayer().getName(), ex);
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
}