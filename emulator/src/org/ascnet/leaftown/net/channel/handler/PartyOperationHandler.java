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

package org.ascnet.leaftown.net.channel.handler;

import org.ascnet.leaftown.client.MapleCharacter;
import org.ascnet.leaftown.client.MapleClient;
import org.ascnet.leaftown.net.AbstractMaplePacketHandler;
import org.ascnet.leaftown.net.channel.ChannelServer;
import org.ascnet.leaftown.net.world.MapleParty;
import org.ascnet.leaftown.net.world.MaplePartyCharacter;
import org.ascnet.leaftown.net.world.PartyOperation;
import org.ascnet.leaftown.net.world.remote.WorldChannelInterface;
import org.ascnet.leaftown.tools.MaplePacketCreator;
import org.ascnet.leaftown.tools.data.input.SeekableLittleEndianAccessor;

import java.rmi.RemoteException;

public class PartyOperationHandler extends AbstractMaplePacketHandler 
{
    @Override
    public void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) 
    {
        final int operation = slea.readByte();
        final MapleCharacter player = c.getPlayer();
        final WorldChannelInterface wci = c.getChannelServer().getWorldInterface();
        MapleParty party = player.getParty();
        MaplePartyCharacter partyplayer = new MaplePartyCharacter(player);

        switch (operation) 
        {
            case 0x01: 
            {
                if (c.getPlayer().getParty() == null) 
                {
                    try 
                    {
                        party = wci.createParty(partyplayer);
                        player.setParty(party);
                    }
                    catch (RemoteException e) 
                    {
                        c.getChannelServer().reconnectWorld();
                    }
                    c.sendPacket(MaplePacketCreator.partyCreated(c.getPlayer().getPartyId()));
                } 
                else 
                    c.sendPacket(MaplePacketCreator.serverNotice(5, "You can't create a party as you are already in one"));
                break;
            }
            case 2: // LEAVE PARTY 
            {
                if (party != null) 
                {
                    try 
                    {
                        if (partyplayer.equals(party.getLeader())) // DISBAND 
                        {
                            wci.updateParty(party.getId(), PartyOperation.DISBAND, partyplayer);
                            
                            if (player.getEventInstance() != null) 
                                player.getEventInstance().disbandParty();
                        } 
                        else 
                        {
                            wci.updateParty(party.getId(), PartyOperation.LEAVE, partyplayer);
                            
                            if (player.getEventInstance() != null) 
                                player.getEventInstance().leftParty(player);
                        }
                    } catch (RemoteException e) 
                    {
                        c.getChannelServer().reconnectWorld();
                    }
                    player.setParty(null);
                }
                break;
            }
            case 0x03: 
            { // accept invitation
                int partyid = slea.readInt();
                if (!c.getPlayer().getPartyInvited())
                    return;
                if (c.getPlayer().getParty() == null) {
                    try {
                        party = wci.getParty(partyid);
                        if (party != null && party.getLeader().isOnline()) {
                            if (party.getMembers().size() < 6) {
                                wci.updateParty(party.getId(), PartyOperation.JOIN, partyplayer);
                                player.receivePartyMemberHP();
                                player.updatePartyMemberHP();
                            } else {
                                c.sendPacket(MaplePacketCreator.partyStatusMessage(17));
                            }
                        } else {
                            c.sendPacket(MaplePacketCreator.serverNotice(5, "The party you are trying to join does not exist"));
                        }
                        c.getPlayer().setPartyInvited(false);
                    } catch (RemoteException e) {
                        c.getChannelServer().reconnectWorld();
                    }
                } else {
                    c.sendPacket(MaplePacketCreator.serverNotice(5, "You can't join the party as you are already in one"));
                }
                break;
            }
            case 0x04: // PARTY INVITE 
            {
            	if (party == null) 
            	{
                    try 
                    {
						party = wci.createParty(partyplayer);
					}
                    catch (RemoteException e) 
                    {
						e.printStackTrace();
					}
                    player.setParty(party);
                    c.sendPacket(MaplePacketCreator.partyCreated(c.getPlayer().getPartyId()));
            	}
            	
                final MapleCharacter invited = c.getChannelServer().getPlayerStorage().getCharacterByName(slea.readMapleAsciiString());
                
                if (invited != null) 
                {
                    if (invited.getParty() == null) 
                    {
                        if (party.getMembers().size() < 6) 
                        {
                            invited.setPartyInvited(true);
                            invited.getClient().sendPacket(MaplePacketCreator.partyInvite(player));
                        }
                        else
                        	c.sendPacket(MaplePacketCreator.partyStatusMessage(17));
                    }
                    else 
                        c.sendPacket(MaplePacketCreator.partyStatusMessage(16));
                }
                else 
                    c.sendPacket(MaplePacketCreator.partyStatusMessage(18));
                break;
            }
            case 5: { // expel
                int cid = slea.readInt();
                if (party != null && partyplayer.equals(party.getLeader())) {
                    MaplePartyCharacter expelled = party.getMemberById(cid);
                    if (expelled != null && !expelled.equals(party.getLeader())) {
                        try {
                            wci.updateParty(party.getId(), PartyOperation.EXPEL, expelled);
                            if (player.getEventInstance() != null) {
                                /*if leader wants to boot someone, then the whole party gets expelled
								TODO: Find an easier way to get the character behind a MaplePartyCharacter
								possibly remove just the expel.*/
                                if (expelled.isOnline()) {
                                    MapleCharacter expellee = ChannelServer.getInstance(expelled.getChannel()).getPlayerStorage().getCharacterById(expelled.getId());
                                    if (expellee != null && expellee.getEventInstance() != null && expellee.getEventInstance().getName().equals(player.getEventInstance().getName())) {
                                        player.getEventInstance().disbandParty();
                                    }
                                }
                            }
                        } catch (RemoteException e) {
                            c.getChannelServer().reconnectWorld();
                        }
                    }
                }
                break;
            }
            case 6: { //change leader
                int nlid = slea.readInt();
                if (party != null) {
                    MaplePartyCharacter newleader = party.getMemberById(nlid);
                    if (newleader != null && partyplayer.equals(party.getLeader()) && newleader.isOnline()) {
                        try {
                            party.setLeader(newleader);
                            wci.updateParty(party.getId(), PartyOperation.CHANGE_LEADER, newleader);
                        } catch (RemoteException re) {
                            c.getChannelServer().reconnectWorld();
                        }
                    }
                }
                break;
            }
        }
    }
}