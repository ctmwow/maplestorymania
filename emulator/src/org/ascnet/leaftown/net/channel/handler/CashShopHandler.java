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

import org.ascnet.leaftown.client.Equip;
import org.ascnet.leaftown.client.IItem;
import org.ascnet.leaftown.client.MapleCharacter;
import org.ascnet.leaftown.client.MapleClient;
import org.ascnet.leaftown.client.MapleInventory;
import org.ascnet.leaftown.client.MapleInventoryType;
import org.ascnet.leaftown.client.MaplePet;
import org.ascnet.leaftown.client.MapleRing;
import org.ascnet.leaftown.net.AbstractMaplePacketHandler;
import org.ascnet.leaftown.net.channel.ChannelServer;
import org.ascnet.leaftown.provider.text.ServerMessages;
import org.ascnet.leaftown.server.CashItemFactory;
import org.ascnet.leaftown.server.CashItemInfo;
import org.ascnet.leaftown.server.MapleInventoryManipulator;
import org.ascnet.leaftown.server.MapleItemInformationProvider;
import org.ascnet.leaftown.tools.MaplePacketCreator;
import org.ascnet.leaftown.tools.data.input.SeekableLittleEndianAccessor;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author Acrylic (Terry Han)
 */
public class CashShopHandler extends AbstractMaplePacketHandler
{
	private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(CashShopHandler.class);
    private final List<Integer> blockedItems = Arrays.asList(5000028, 5400000, 5510000, 5000029, 5000048);

    @Override
    public void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) 
    {
        final int action = slea.readByte();
        final int accountId = c.getAccID();
        
        if (action == 0x03) /** PURCHASE **/
        {
            slea.skip(0x01);
            
            final int cashType = slea.readInt();
            final int snCS = slea.readInt();
            
            final CashItemInfo item = CashItemFactory.getItem(snCS);
            final MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
            
            if (item == null || ii.getName(item.getItemId()).contains("2x") || ii.getName(item.getItemId()).contains("2 x") || blockedItems.contains(item.getItemId())) 
            { 
            	c.sendPacket(MaplePacketCreator.serverNotice(0x01, ServerMessages.getInstance().getString("ITEM_PURCHASE_NOT_AVALIABLE")));
                c.sendPacket(MaplePacketCreator.showNXMapleTokens(c.getPlayer()));
                c.sendPacket(MaplePacketCreator.enableActions());
                return;
            }

            if (!item.onSale() || item.getPrice() == 0x00)
            {
            	c.sendPacket(MaplePacketCreator.showCashShopMessage((byte) 0x00));
                c.sendPacket(MaplePacketCreator.showNXMapleTokens(c.getPlayer()));
            	c.sendPacket(MaplePacketCreator.enableActions()); 
            	return;
            }
            else if((item.getGender() != 2 && item.getGender() != c.getPlayer().getGender()))
            {
            	c.sendPacket(MaplePacketCreator.showCashShopMessage((byte) 0xAA));
                c.sendPacket(MaplePacketCreator.showNXMapleTokens(c.getPlayer()));
            	c.sendPacket(MaplePacketCreator.enableActions()); 
            	return;
            }
            else if(!c.getPlayer().getCashShop().removeCash(cashType, item.getPrice()))
            {
            	c.sendPacket(MaplePacketCreator.showCashShopMessage((byte) 0xA5));
                c.sendPacket(MaplePacketCreator.showNXMapleTokens(c.getPlayer()));
            	c.sendPacket(MaplePacketCreator.enableActions()); 
            	return;
            }
            
            final IItem realItem = item.toItem();
            
            if(MapleItemInformationProvider.getInstance().isPet(item.getItemId()))
            	realItem.setPet(MaplePet.createPet(c.getPlayer().getId(), item.getItemId())); 

            c.getPlayer().getCashShop().addToInventory(realItem);
            c.getPlayer().getCashShop().gainCash(cashType, -item.getPrice());
            c.sendPacket(MaplePacketCreator.showBoughtCSItem(accountId, realItem));
            c.sendPacket(MaplePacketCreator.showNXMapleTokens(c.getPlayer()));
        }
        else if (action == 0x04) 
        {
            final int idate = slea.readInt();
            final int snCS = slea.readInt();
            
            final String recipient = slea.readMapleAsciiString();

            if(!c.checkBirthDate(idate))
            {
                c.sendPacket(MaplePacketCreator.showCashShopMessage((byte) 0xC4));
                c.sendPacket(MaplePacketCreator.showNXMapleTokens(c.getPlayer()));
                c.sendPacket(MaplePacketCreator.enableActions());
                return;
            }
            
            final String message = slea.readMapleAsciiString();
            
            if(message.length() < 0x01 || message.length() > 0x49)
            {
            	c.sendPacket(MaplePacketCreator.showCashShopMessage((byte) 0xA6));
                c.sendPacket(MaplePacketCreator.showNXMapleTokens(c.getPlayer()));
                c.sendPacket(MaplePacketCreator.enableActions());
            	return;
            }

            final CashItemInfo item = CashItemFactory.getItem(snCS);
            final String itemName = MapleItemInformationProvider.getInstance().getName(item.getItemId());
            
            if (item == null || itemName != null && (itemName.contains("2x") || itemName.contains("2 x")) || blockedItems.contains(item.getItemId()) || !item.onSale() || item.getPrice() == 0x00) 
            {
            	c.sendPacket(MaplePacketCreator.serverNotice(0x01, ServerMessages.getInstance().getString("ITEM_PURCHASE_NOT_AVALIABLE")));
                c.sendPacket(MaplePacketCreator.showNXMapleTokens(c.getPlayer()));
                c.sendPacket(MaplePacketCreator.enableActions());
                return;
            }
            
            if(c.getPlayer().getCashShop().getCash(0x04) < item.getPrice())
            {
            	c.sendPacket(MaplePacketCreator.showCashShopMessage((byte) 0xA5));
                c.sendPacket(MaplePacketCreator.showNXMapleTokens(c.getPlayer()));
            	c.sendPacket(MaplePacketCreator.enableActions());
            	return;
            }

            MapleCharacter gifted = null;
            final int giftedid = MapleCharacter.getIdByName(recipient, 0x00);
            
            if(giftedid == -0x01)
            {
                c.sendPacket(MaplePacketCreator.showCashShopMessage((byte) 0xA9));
                c.sendPacket(MaplePacketCreator.showNXMapleTokens(c.getPlayer()));
                c.sendPacket(MaplePacketCreator.enableActions());
                return;
            }
            
            for (ChannelServer cserv : ChannelServer.getAllInstances()) 
            {
                gifted = cserv.getPlayerStorage().getCharacterById(giftedid);
                
                if (gifted != null)
                    break;
            }

            if(gifted != null && gifted.getAccountID() == c.getAccID())
            {
                c.sendPacket(MaplePacketCreator.showCashShopMessage((byte) 0xA8));
                c.sendPacket(MaplePacketCreator.showNXMapleTokens(c.getPlayer()));
                c.sendPacket(MaplePacketCreator.enableActions());
                return;
            }
            else if(gifted != null && gifted.isGM())
            {
                c.getPlayer().dropMessage(0x01, ServerMessages.getInstance().getString("GM_GIFT_MSG"));
                c.sendPacket(MaplePacketCreator.showNXMapleTokens(c.getPlayer()));
                c.sendPacket(MaplePacketCreator.enableActions());
                return;
            }
            else
            {
                if(!(item.getGender() == 0x02 || item.getGender() == c.getGender()))
                {
                	c.sendPacket(MaplePacketCreator.showCashShopMessage((byte) 0xAA));
                	c.sendPacket(MaplePacketCreator.showNXMapleTokens(c.getPlayer()));
                    c.sendPacket(MaplePacketCreator.enableActions());
                    return;	
                }

                try 
                {
                    c.getPlayer().getCashShop().gift(giftedid, c.getPlayer().getName(), message, item.getSN());
                }
                catch (Exception SQE) 
                {
                	log.debug("SQL Error with adding the cash shop gift into the database, error is:", SQE);
                	
                    c.getPlayer().dropMessage(ServerMessages.getInstance().getString("CS_OPERATION_FAIL"));
                    c.sendPacket(MaplePacketCreator.enableActions());
                    
                    return;
                }

                c.getPlayer().getCashShop().gainCash(0x04, -item.getPrice());
                c.sendPacket(MaplePacketCreator.showGiftSucceed(recipient, item));
                c.sendPacket(MaplePacketCreator.showNXMapleTokens(c.getPlayer()));
                
                try
                {
                    c.getPlayer().sendNote(recipient, c.getPlayer().getName() + " ".concat(ServerMessages.getInstance().getString("GIFT_MSG")), (byte) 0x00);
                    
                    if(gifted != null) gifted.showNote();
                }
                catch(Exception ex) 
                { 
                	log.error("SQL Error with sending gifted note, error is:", ex);
                }
            }
        }
        else if (action == 0x05) /** UPDATE WISH LIST **/
        {
        	c.getPlayer().getCashShop().clearWishList();
        	
            for (byte i = 0x00; i < 0x0A; i++) 
            {
                final int sn = slea.readInt();
                CashItemInfo cItem = CashItemFactory.getItem(sn);
                
                if (cItem != null && cItem.onSale() && sn != 0x00) 
                	c.getPlayer().getCashShop().addToWishList(sn); 
            }
            
            c.sendPacket(MaplePacketCreator.showWishList(c.getPlayer().getCashShop().getWishList(), true));
            c.sendPacket(MaplePacketCreator.showNXMapleTokens(c.getPlayer()));
        	c.sendPacket(MaplePacketCreator.enableActions());
        } 
        else if (action == 0x06) /** INCREASE INVENTORY SLOT **/ 
        {
            slea.skip(0x01);
            final int cash = slea.readInt();
            final byte mode = slea.readByte();
            
            if (mode == 0x00) 
            {
                final byte type = slea.readByte();
                
                if(c.getPlayer().getCashShop().getCash(cash) < 4000)
                {
                	c.sendPacket(MaplePacketCreator.showCashShopMessage((byte) 0xA5));
                    c.sendPacket(MaplePacketCreator.showNXMapleTokens(c.getPlayer()));
                	c.sendPacket(MaplePacketCreator.enableActions());
                	return;
                }
                
                if (c.getPlayer().gainSlots(type, 0x04, false)) 
                {
                    c.getPlayer().getCashShop().gainCash(cash, -4000);
                    c.sendPacket(MaplePacketCreator.showBoughtInventorySlots(type, c.getPlayer().getSlots(type)));   	
                }
                else
                {
                    c.getPlayer().dropMessage(ServerMessages.getInstance().getString("INVENTORY_SLOTS_ERROR"));
                    c.sendPacket(MaplePacketCreator.showNXMapleTokens(c.getPlayer()));
                    c.sendPacket(MaplePacketCreator.enableActions());
                    return;	
                }
            }
            else 
            {
                final CashItemInfo cItem = CashItemFactory.getItem(slea.readInt());
                final int type = (cItem.getItemId() - 9110000) / 1000;
                
                if(c.getPlayer().getCashShop().getCash(cash) < cItem.getPrice())
                {
                	c.sendPacket(MaplePacketCreator.showCashShopMessage((byte) 0xA5));
                    c.sendPacket(MaplePacketCreator.showNXMapleTokens(c.getPlayer()));
                	c.sendPacket(MaplePacketCreator.enableActions());
                	return;
                }

                if (c.getPlayer().gainSlots(type, 0x08, false)) 
                {
                    c.getPlayer().getCashShop().gainCash(cash, -cItem.getPrice());
                    c.sendPacket(MaplePacketCreator.showBoughtInventorySlots(type, c.getPlayer().getSlots(type)));
                }
                else
                {
                    c.getPlayer().dropMessage(ServerMessages.getInstance().getString("INVENTORY_SLOTS_ERROR"));
                    c.sendPacket(MaplePacketCreator.showNXMapleTokens(c.getPlayer()));
                    c.sendPacket(MaplePacketCreator.enableActions());
                    return;	
                }
            }
            
            c.sendPacket(MaplePacketCreator.showNXMapleTokens(c.getPlayer()));
        }
        else if (action == 0x07) /** INCREASE STORAGE SLOT **/ 
        { 
            slea.skip(0x01);
            
            final int cashType = slea.readInt();
            final byte mode = slea.readByte();
            
            if (mode == 0x00)
            {
                if(c.getPlayer().getCashShop().getCash(cashType) < 4000)
                {
                	c.sendPacket(MaplePacketCreator.showCashShopMessage((byte) 0xA5));
                    c.sendPacket(MaplePacketCreator.showNXMapleTokens(c.getPlayer()));
                	c.sendPacket(MaplePacketCreator.enableActions());
                	return;
                }
    
                if (c.getPlayer().getStorage().gainSlots(0x04))
                {
                    c.getPlayer().getCashShop().gainCash(cashType, -4000);
                    c.sendPacket(MaplePacketCreator.showBoughtStorageSlots(c.getPlayer().getStorage().getSlots()));	
                }
                else
                {
                    c.getPlayer().dropMessage(ServerMessages.getInstance().getString("STORAGE_SLOTS_ERROR"));
                    c.sendPacket(MaplePacketCreator.showNXMapleTokens(c.getPlayer()));
                    c.sendPacket(MaplePacketCreator.enableActions());
                    return;	
                }

                c.sendPacket(MaplePacketCreator.showNXMapleTokens(c.getPlayer()));
            }
            else 
            {
                CashItemInfo item = CashItemFactory.getItem(slea.readInt());
                
                if(c.getPlayer().getCashShop().getCash(cashType) < item.getPrice())
                {
                	c.sendPacket(MaplePacketCreator.showCashShopMessage((byte) 0xA5));
                    c.sendPacket(MaplePacketCreator.showNXMapleTokens(c.getPlayer()));
                	c.sendPacket(MaplePacketCreator.enableActions());
                	return;
                }
                
                
            	if(c.getPlayer().getStorage().gainSlots(0x08))
            	{
                    c.getPlayer().getCashShop().gainCash(cashType, -item.getPrice());
            		c.sendPacket(MaplePacketCreator.showBoughtStorageSlots(c.getPlayer().getStorage().getSlots()));	
            	}
            	else
            	{
                    c.getPlayer().dropMessage(ServerMessages.getInstance().getString("STORAGE_SLOTS_ERROR"));
                    c.sendPacket(MaplePacketCreator.showNXMapleTokens(c.getPlayer()));
                    c.sendPacket(MaplePacketCreator.enableActions());
                    return;	
            	}

                c.sendPacket(MaplePacketCreator.showNXMapleTokens(c.getPlayer()));
            }
        }
        else if (action == 0x0D) /** TAKE FROM CASH INVENTORY **/
        {  
            final IItem item = c.getPlayer().getCashShop().findByCashId(slea.readInt());
            
            if (item == null) 
                return;
            
            if (!(c.getPlayer().getInventory(MapleItemInformationProvider.getInstance().getInventoryType(item.getItemId())).getNextFreeSlot() > -0x01))
            {
            	c.sendPacket(MaplePacketCreator.showCashShopMessage((byte) 0xBB));
                c.sendPacket(MaplePacketCreator.showNXMapleTokens(c.getPlayer()));
                c.sendPacket(MaplePacketCreator.enableActions());
                return;
            }
            
            if (c.getPlayer().getInventory(MapleItemInformationProvider.getInstance().getInventoryType(item.getItemId())).addItem(item) != -0x01) 
            {
                c.getPlayer().getCashShop().removeFromInventory(item);
                c.sendPacket(MaplePacketCreator.takeFromCashInventory(item));
                
                if (item instanceof Equip) 
                {
                    final Equip equip = (Equip) item;
                    
                    if (equip.getRingId() >= 0x00) 
                    {
                        final MapleRing ring = MapleRing.loadFromDb(equip.getRingId());
                        
                        if (ring.getItemId() > 1112012) 
                        	c.getPlayer().getFriendshipRings().add(ring);
                        else 
                        	c.getPlayer().getCrushRings().add(ring);
                    }
                }
            }
            else
            {
            	log.error("PLAYER " + c.getAccID() + " ADD ITEM FAILED!!!");
            	
            	c.sendPacket(MaplePacketCreator.showCashShopMessage((byte) 0xA4));
                return;
            }
        }
        else if (action == 0x0E) /** PUT INTO CASH INVENTORY **/
        { 
            final int cashId = slea.readInt();
            slea.skip(0x04);
            
            MapleInventory mi = c.getPlayer().getInventory(MapleInventoryType.getByType(slea.readByte()));
            final IItem item = mi.findByCashId(cashId);
            
            if (item == null) 
            {
            	log.error("PLAYER " + c.getAccID() + " CASH ITEM NOT FOUND!!!  CASHID : " + cashId);
            	
            	c.sendPacket(MaplePacketCreator.showCashShopMessage((byte) 0xA4));
                return;
            }
            
            mi.removeSlot(item.getPosition());
            c.getPlayer().getCashShop().addToInventory(item);
            
            c.sendPacket(MaplePacketCreator.putIntoCashInventory(item, c.getAccID()));
            c.sendPacket(MaplePacketCreator.showNXMapleTokens(c.getPlayer()));
            c.sendPacket(MaplePacketCreator.enableActions());
        }
        else if (action == 0x1D) /** CRUSH RING **/ 
        { 
            if(!c.checkBirthDate(slea.readInt()))
            {
                c.sendPacket(MaplePacketCreator.showCashShopMessage((byte) 0xC4));
                c.sendPacket(MaplePacketCreator.showNXMapleTokens(c.getPlayer()));
                c.sendPacket(MaplePacketCreator.enableActions());
                return;
            }
        	
            final int cashType = slea.readInt();
            final int snCS = slea.readInt();
            
            CashItemInfo ring = CashItemFactory.getItem(snCS);
            
            if(c.getPlayer().getCashShop().getCash(cashType) < ring.getPrice())
            {
            	c.sendPacket(MaplePacketCreator.showCashShopMessage((byte) 0xA5));
                c.sendPacket(MaplePacketCreator.showNXMapleTokens(c.getPlayer()));
            	c.sendPacket(MaplePacketCreator.enableActions());
            	return;
            }
            
            final String sentTo = slea.readMapleAsciiString();
            final String text = slea.readMapleAsciiString();
            
            if(text.length() < 0x01 || text.length() > 0x49)
            {
            	c.sendPacket(MaplePacketCreator.showCashShopMessage((byte) 0xA6));
                c.sendPacket(MaplePacketCreator.showNXMapleTokens(c.getPlayer()));
                c.sendPacket(MaplePacketCreator.enableActions());
            	return;
            }
            
            MapleCharacter partnerChar = c.getChannelServer().getPlayerStorage().getCharacterByName(sentTo);
            
            if (partnerChar == null) 
            {
                c.sendPacket(MaplePacketCreator.showCashShopMessage((byte) 0xA9));
                c.sendPacket(MaplePacketCreator.showNXMapleTokens(c.getPlayer()));
                c.sendPacket(MaplePacketCreator.enableActions());
                return;	
            }
            else
            {
            	if(ring.toItem() instanceof Equip)
            	{
                	final Equip item = (Equip) ring.toItem();         	
                    item.setRingId(MapleRing.createRing(ring.getItemId(), c.getPlayer(), partnerChar));
                    
                    c.getPlayer().getCashShop().gainCash(cashType, -ring.getPrice());
                    c.getPlayer().getCashShop().addToInventory(item);
                    c.getPlayer().getCashShop().gift(partnerChar.getId(), c.getPlayer().getName(), text, item.getSN(), (item.getRingId() + 0x01));
                    c.sendPacket(MaplePacketCreator.showBoughtCSItem(accountId, (Equip) ring.toItem()));
                    
                    try 
                    {
                        c.getPlayer().sendNote(partnerChar.getName(), text, (byte) 0x01);
                        partnerChar.showNote();
                    } 
                    catch (SQLException ex) 
                    { 
                    	log.error("SQL Error with sending gifted note, error is:", ex);
                    }	
            	}
            	else
            	{
            		log.error("TRYING TO ADD A RING THAT IS NOT A EQUIP.  ITEM ID " + ring.getItemId());
            		
                    c.sendPacket(MaplePacketCreator.showCashShopMessage((byte) 0x00));
                    c.sendPacket(MaplePacketCreator.showNXMapleTokens(c.getPlayer()));
                    c.sendPacket(MaplePacketCreator.enableActions());
                    return;	
            	}                
            }
            c.sendPacket(MaplePacketCreator.enableActions());
            c.sendPacket(MaplePacketCreator.showNXMapleTokens(c.getPlayer()));
        }
        else if (action == 0x1E) /** PACKAGE PURCHASE **/
        {
            slea.skip(0x01);
            
            final int cashType = slea.readInt(); 
            final int snCS = slea.readInt();
            final CashItemInfo cashPackage = CashItemFactory.getItem(snCS);
            
            if (!cashPackage.onSale()) 
            {
            	c.sendPacket(MaplePacketCreator.showCashShopMessage((byte) 0x00));
                c.sendPacket(MaplePacketCreator.showNXMapleTokens(c.getPlayer()));
                c.sendPacket(MaplePacketCreator.enableActions());
                return;
            }
            
            if(c.getPlayer().getCashShop().getCash(cashType) < cashPackage.getPrice())
            {
            	c.sendPacket(MaplePacketCreator.showCashShopMessage((byte) 0xA5));
                c.sendPacket(MaplePacketCreator.showNXMapleTokens(c.getPlayer()));
                c.sendPacket(MaplePacketCreator.enableActions());
            	return;
            }
            
            List<IItem> listItem = new ArrayList<IItem>();
            
            for (CashItemInfo item : CashItemFactory.getPackageItems(cashPackage.getItemId())) 
            {
            	final IItem realItem = item.toItem();
            	
                if (realItem.getItemId() >= 5000000 && realItem.getItemId() <= 5000100)
                {
                    final MaplePet pet = MaplePet.createPet(c.getPlayer().getId(), item.getItemId());
                    
                    if (pet == null)
                    {
                    	c.sendPacket(MaplePacketCreator.showCashShopMessage((byte) 0x00));
                        c.sendPacket(MaplePacketCreator.enableActions());
                        c.sendPacket(MaplePacketCreator.showNXMapleTokens(c.getPlayer()));
                        
                        log.error("CANNOT CREATE PET FOR PACKAGE \r\n ACCID : " + c.getAccID());	
                    }
                    else
                    	realItem.setPet(pet);
                    
                    c.getPlayer().getCashShop().addToInventory(realItem);
                }
                else 
                	c.getPlayer().getCashShop().addToInventory(realItem);
                
                listItem.add(realItem);
            }
            
            c.getPlayer().getCashShop().gainCash(cashType, -cashPackage.getPrice());
            
            c.sendPacket(MaplePacketCreator.showBoughtCSPackage(accountId, listItem));
            c.sendPacket(MaplePacketCreator.showNXMapleTokens(c.getPlayer()));
            c.sendPacket(MaplePacketCreator.enableActions());
        }
        else if (action == 0x20) /** EVERYTHING IS ONE MESO? **/ //TODO rework
        {
            int snCS = slea.readInt();
            CashItemInfo item = CashItemFactory.getItem(snCS);
            if (item == null || !(c.getPlayer().getInventory(MapleItemInformationProvider.getInstance().getInventoryType(item.getItemId())).getNextFreeSlot() > -1)) 
            {
                c.sendPacket(MaplePacketCreator.enableActions());
                return;
            }
            if (c.getPlayer().getMeso() >= item.getPrice() && item.onSale() && item.getPrice() == 0x01) 
            {
                c.getPlayer().gainMeso(-item.getPrice(), false);
                MapleInventoryManipulator.addById(c, item.getItemId(), (short) item.getCount(), "Quest Item was purchased.", null, null);
                MapleInventory etcInventory = c.getPlayer().getInventory(MapleInventoryType.ETC);
                c.sendPacket(MaplePacketCreator.showBoughtCSQuestItem(etcInventory.findById(item.getItemId()).getPosition(), item.getItemId()));
            }
        } 
        else if (action == 0x23) /** FRIENDSHIP RING **/ 
        { 
            if(!c.checkBirthDate(slea.readInt()))
            {
                c.sendPacket(MaplePacketCreator.showCashShopMessage((byte) 0xC4));
                c.sendPacket(MaplePacketCreator.showNXMapleTokens(c.getPlayer()));
                c.sendPacket(MaplePacketCreator.enableActions());
                return;
            }
        	
            final int cashType = slea.readInt();
            final int snID = slea.readInt();
            
            CashItemInfo ring = CashItemFactory.getItem(snID);
            
            if(c.getPlayer().getCashShop().getCash(cashType) < ring.getPrice())
            {
            	c.sendPacket(MaplePacketCreator.showCashShopMessage((byte) 0xA5));
                c.sendPacket(MaplePacketCreator.showNXMapleTokens(c.getPlayer()));
                c.sendPacket(MaplePacketCreator.enableActions());
            	return;
            }
            
            final String sentTo = slea.readMapleAsciiString();
            final String text = slea.readMapleAsciiString();
            
            if(text.length() < 0x01 || text.length() > 0x49)
            {
            	c.sendPacket(MaplePacketCreator.showCashShopMessage((byte) 0xA6));
                c.sendPacket(MaplePacketCreator.showNXMapleTokens(c.getPlayer()));
                c.sendPacket(MaplePacketCreator.enableActions());
            	return;
            }
            
            MapleCharacter partner = c.getChannelServer().getPlayerStorage().getCharacterByName(sentTo);
            
            if (partner == null)
        	{
                c.sendPacket(MaplePacketCreator.showCashShopMessage((byte) 0xA9));
                c.sendPacket(MaplePacketCreator.showNXMapleTokens(c.getPlayer()));
                c.sendPacket(MaplePacketCreator.enableActions());
                return;	
        	}
            else
            {
            	if(ring.toItem() instanceof Equip)
            	{
            		final Equip item = (Equip) ring.toItem();
                    item.setRingId(MapleRing.createRing(ring.getItemId(), c.getPlayer(), partner));
                    
                    c.getPlayer().getCashShop().gainCash(cashType, -ring.getPrice());
                    c.getPlayer().getCashShop().addToInventory(item);
                    c.getPlayer().getCashShop().gift(partner.getId(), c.getPlayer().getName(), text, ring.getSN(), (item.getRingId() + 0x01));
                    
                    c.sendPacket(MaplePacketCreator.showBoughtCSItem(accountId, (Equip) ring.toItem()));
                    
                    try 
                    {
                        c.getPlayer().sendNote(partner.getName(), text, (byte) 0x01);
                        partner.showNote();
                    }
                    catch (SQLException ex) 
                    {
                    	log.error("SQL Error with sending gifted note, error is:", ex);
                    }
            	}
            	else
            	{
            		log.error("TRYING TO ADD A RING THAT IS NOT A EQUIP.  ITEM ID " + ring.getItemId());
            		
                    c.sendPacket(MaplePacketCreator.showCashShopMessage((byte) 0x00));
                    c.sendPacket(MaplePacketCreator.showNXMapleTokens(c.getPlayer()));
                    c.sendPacket(MaplePacketCreator.enableActions());
                    return;	
            	}
            }
            c.sendPacket(MaplePacketCreator.enableActions());
            c.sendPacket(MaplePacketCreator.showNXMapleTokens(c.getPlayer()));
        }
        else
        {
    		log.error("CASH SHOP ACTION NOT REGISTERED A:" + action);
    		
            c.sendPacket(MaplePacketCreator.showCashShopMessage((byte) 0x00));
            c.sendPacket(MaplePacketCreator.showNXMapleTokens(c.getPlayer()));
            c.sendPacket(MaplePacketCreator.enableActions());
            return;	
        }
    }
}