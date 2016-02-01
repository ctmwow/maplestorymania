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
import org.ascnet.leaftown.client.MapleInventoryType;
import org.ascnet.leaftown.net.AbstractMaplePacketHandler;
import org.ascnet.leaftown.server.MapleInventoryManipulator;
import org.ascnet.leaftown.server.MaplePortal;
import org.ascnet.leaftown.server.maps.MapleMap;
import org.ascnet.leaftown.tools.MaplePacketCreator;
import org.ascnet.leaftown.tools.data.input.SeekableLittleEndianAccessor;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class ChangeMapHandler extends AbstractMaplePacketHandler 
{
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ChangeMapHandler.class);

    @Override
    public void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) 
    {
        final MapleCharacter player = c.getPlayer();
        
        player.resetAfkTimer();
        
        /** EXIT CASHSHOP **/
        if (slea.available() == 0x00) 
        {
            player.saveToDB(true);
            player.getCashShop().open(false);
            player.setInMTS(false);
            player.cancelSavedBuffs();
            player.resetCooldowns();

            final String[] socket = c.getChannelServer().getIP(c.getChannel()).split(":");
            
            c.getChannelServer().removePlayer(player);
            c.updateLoginState(MapleClient.LOGIN_SERVER_TRANSITION);
            
            try 
            {
                MapleCharacter.setLoggedInState(player.getId(), 0x00);
                
                c.sendPacket(MaplePacketCreator.getChannelChange(InetAddress.getByName(socket[0x00]), Integer.parseInt(socket[0x01])));
            }
            catch (Exception e) 
            {
            	try 
            	{
					log.error("Client cannot be reconnected to channel. INET_ADDR {" + InetAddress.getByName(socket[0x00]) + "}:{" + Integer.parseInt(socket[0x01]) + "}", e);
				}
            	catch (UnknownHostException e1) 
            	{
            		log.error("Cannot resolve hostname {" + socket[0x00] + "}", e1);
				}
            }
        }
        else 
        {
            slea.skip(0x01); // 1 = from dying 2 = regular portal
            
            final int targetid = slea.readInt();
            final String startwp = slea.readMapleAsciiString();
            final MaplePortal portal = player.getMap().getPortal(startwp);
            
            slea.skip(0x01);
            
            final boolean useDeathItem = slea.readShort() == 0x01;

            if (targetid != -0x01 && !player.isAlive()) 
            {
                boolean executeStandardPath = true;
                boolean hasDeathItem = false;
                
                if (player.getEventInstance() != null) 
                    executeStandardPath = player.getEventInstance().revivePlayer(player);
                
                if (executeStandardPath) 
                {
                    player.cancelAllBuffs();
                    MapleMap to = player.getMap().getReturnMap();
                    
                    if (useDeathItem && player.getInventory(MapleInventoryType.CASH).countById(5510000) > 0x00) 
                    {
                        hasDeathItem = true;
                        to = player.getMap();
                    }
                    
                    player.setStance((byte) 0x00);
                    
                    if (player.getMap().canExit() && to != null && to.canEnter() || player.isGM()) 
                    {
                        player.setHp(0x32);
                        
                        if (hasDeathItem) 
                            MapleInventoryManipulator.removeById(c, MapleInventoryType.CASH, 5510000, 0x01, true, false);
                        
                        player.changeMap(to, to.getRandomSpawnPoint());
                        
                    } 
                    else 
                    {
                        c.sendPacket(MaplePacketCreator.serverNotice(0x05, "You will remain dead."));
                        c.sendPacket(MaplePacketCreator.enableActions());
                    }
                }
            }
            else if (targetid != -0x01 && (player.isGM() || targetid == player.getAutoChangeMapId())) 
            {
                final MapleMap to = c.getChannelServer().getMapFactory().getMap(targetid);
                
                if (player.getMapId() == 2010000)
                    player.changeMap(to, to.getPortal(0x05));
                else
                    player.changeMap(to, to.getRandomSpawnPoint());
            }
            else if (targetid != -0x01 && !player.isGM()) 
            {
                final int divi = player.getMapId() / 0x64;
                
                boolean warp = false;
                
                if (divi == 9140900) // Aran Introduction //TODO really necessary?
                { 
                    if (targetid == 914090011 || targetid == 914090012 || targetid == 914090013 || targetid == 140090000) 
                        warp = true;
                }
                
                if(!warp)
                	log.warn("Player {} attempted Map jumping without being a GM", player.getName());
                else
                {
                	final MapleMap to = c.getChannelServer().getMapFactory().getMap(targetid);
                	player.changeMap(to, to.getRandomSpawnPoint());
                }
            }
            else 
            {
                if (player.getEventInstance() != null && (player.getMapId() == 910510201 || player.getMapId() == 108000700)) 
                    player.getEventInstance().playerMapExit(player);
                
                if (portal != null) 
                    portal.enterPortal(c);
                else 
                {
                    log.warn(MapleClient.getLogMessage(c, "Portal {} not found on map {}", startwp, player.getMap().getId()));
                    c.disconnect();
                }
            }
        }
    }
}