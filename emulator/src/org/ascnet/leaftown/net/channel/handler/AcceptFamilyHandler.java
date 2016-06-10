package org.ascnet.leaftown.net.channel.handler;

import org.ascnet.leaftown.client.MapleCharacter;
import org.ascnet.leaftown.client.MapleClient;
import org.ascnet.leaftown.client.MapleFamily;
import org.ascnet.leaftown.client.MapleFamilyCharacterInfo;
import org.ascnet.leaftown.net.AbstractMaplePacketHandler;
import org.ascnet.leaftown.tools.MaplePacketCreator;
import org.ascnet.leaftown.tools.data.input.SeekableLittleEndianAccessor;

public final class AcceptFamilyHandler extends AbstractMaplePacketHandler
{
    @Override
    public final void handlePacket(final SeekableLittleEndianAccessor slea, final MapleClient c) 
    {
    	 final MapleCharacter inviter = c.getPlayer().getMap().getCharacterById(slea.readInt());
    	 
    	 try
    	 {
    		 if (inviter != null && c.getPlayer().getMFC().getSenior() == 0 && (c.getPlayer().isGM() || !inviter.isHidden()) && inviter.getLevel() - 20 <= c.getPlayer().getLevel() && 
            		 inviter.getLevel() >= 10 && inviter.getName().equals(slea.readMapleAsciiString()) && inviter.getMFC().getNoJuniors() < 2 && c.getPlayer().getLevel() >= 10) 
             {
                 boolean accepted = slea.readByte() > 0;
                 inviter.getClient().sendPacket(MaplePacketCreator.sendFamilyJoinResponse(accepted, c.getPlayer().getName()));
                 if (accepted) 
                 {
                     c.sendPacket(MaplePacketCreator.getSeniorMessage(inviter.getName()));
                     int old = c.getPlayer().getMFC() == null ? 0 : c.getPlayer().getMFC().getFamily().getId();
                     int oldj1 = c.getPlayer().getMFC() == null ? 0 : c.getPlayer().getMFC().getJunior1();
                     int oldj2 = c.getPlayer().getMFC() == null ? 0 : c.getPlayer().getMFC().getJunior2();
                     
                     final MapleFamily fam = c.getChannelServer().getWorldInterface().getFamily(inviter.getMapleFamilyId());
                     
                     if (inviter.getMapleFamilyId() > 0x00 && fam != null) 
                     {
                         
                         //if old isn't null, don't set the familyid yet, mergeFamily will take care of it
                         //c.getPlayer().setFamily(old <= 0 ? inviter.getMapleFamilyId() : old, inviter.getId(), oldj1 <= 0 ? 0 : oldj1, oldj2 <= 0 ? 0 : oldj2);
                         
                         final MapleFamilyCharacterInfo mf = inviter.getMFC();
                         
                         if (mf.getJunior1() > 0x00) 
                             mf.setJunior2(c.getPlayer().getId()); 
                         else 
                             mf.setJunior1(c.getPlayer().getId());

                         mf.writeOnDB();
                         
                         final MapleFamily oldFamily = c.getChannelServer().getWorldInterface().getFamily(old);
                         
                         if (old > 0x00 && oldFamily != null) 
                         { 
                             //MapleFamily.mergeFamily(fam, oldFamily); TODO MERGE
                         }
                         else
                         {
                        	 c.getChannelServer().getWorldInterface().setFamily(fam, c.getPlayer().getId());
                        	 
                        	 MapleFamily.updateCharacterFamilyInfo(fam.getId(), inviter.getId(), oldj1 <= 0 ? 0 : oldj1, oldj2 <= 0 ? 0 : oldj2, c.getPlayer().getMFC().getReputation(), c.getPlayer().getMFC().getTotalReputation(), c.getPlayer().getId());
                             fam.setOnline(c.getPlayer().getId(), true, c.getChannel());
                         }
                         
                         if (fam != null) 
                         {
                             if (inviter.getMFC().getNoJuniors() == 0x01 || old > 0x00)
                                 fam.resetDescendants();

                             fam.resetPedigree(); //is this necessary? really necessary???? TODO
                         }
                     } 
                     else 
                     {
                         final int id = MapleFamily.createFamily(inviter.getId());
                         
                         if (id > 0x00) 
                         {
                             //before loading the family, set sql
                             MapleFamily.updateCharacterFamilyInfo(id, 0x00, c.getPlayer().getId(), 0x00, inviter.getMFC().getReputation(), inviter.getMFC().getTotalReputation(), inviter.getId());
                             MapleFamily.updateCharacterFamilyInfo(id, inviter.getId(), oldj1 <= 0 ? 0 : oldj1, oldj2 <= 0 ? 0 : oldj2, c.getPlayer().getMFC().getReputation(), c.getPlayer().getMFC().getTotalReputation(), c.getPlayer().getId());
                             
                             MapleFamily fam_ = c.getChannelServer().getWorldInterface().getFamily(id);
                             
                             c.getChannelServer().getWorldInterface().setFamily(fam_, c.getPlayer().getId());
                             c.getChannelServer().getWorldInterface().setFamily(fam_, inviter.getId());

                             fam.setOnline(inviter.getId(), true, inviter.getClient().getChannel());
                             
                             if (old > 0 && c.getChannelServer().getWorldInterface().getFamily(old) != null) 
                             {
                                 //MapleFamily.mergeFamily(fam_, World.Family.getFamily(old));
                             }
                             else 
                                 fam.setOnline(c.getPlayer().getId(), true, c.getChannel());
                             
                             fam_.resetDescendants();
                             fam_.resetPedigree();

                         }
                     }
                     c.sendPacket(MaplePacketCreator.getFamilyInfo(c.getPlayer().getMFC()));
                 }
             } 
    	 }
    	 catch(Exception ex)
    	 {
    		 ex.printStackTrace();
    	 }
    }
}