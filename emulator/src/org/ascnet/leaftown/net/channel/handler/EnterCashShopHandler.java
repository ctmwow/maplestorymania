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

import org.ascnet.leaftown.client.MapleClient;
import org.ascnet.leaftown.net.AbstractMaplePacketHandler;
import org.ascnet.leaftown.net.world.remote.WorldChannelInterface;
import org.ascnet.leaftown.server.playerinteractions.MapleTrade;
import org.ascnet.leaftown.tools.MaplePacketCreator;
import org.ascnet.leaftown.tools.data.input.SeekableLittleEndianAccessor;

import java.rmi.RemoteException;

/**
 * @author Acrylic (Terry Han)
 */
public class EnterCashShopHandler extends AbstractMaplePacketHandler 
{
    @Override
    public void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c)
    {
        if (c.getChannelServer().allowCashshop() || c.getPlayer().getMap().getForcedReturnId() != 999999999) 
        {
            if (c.getPlayer().getCashShop().getCash(0x01) < 0x00 || c.getPlayer().getCashShop().getCash(0x02) < 0x00 || c.getPlayer().getCashShop().getCash(0x04) < 0x00) 
            {
                c.getPlayer().dropMessage("You can not enter the cash shop due to having a negative amount of NX cash.");
                c.sendPacket(MaplePacketCreator.enableActions());
                return;
            }
            
            if (c.getPlayer().getTrade() != null) 
                MapleTrade.cancelTrade(c.getPlayer());

            try 
            {
                WorldChannelInterface wci = c.getChannelServer().getWorldInterface();
                wci.addBuffsToStorage(c.getPlayer().getId(), c.getPlayer().getAllBuffs());
            } 
            catch (RemoteException e) 
            {
                c.getChannelServer().reconnectWorld();
            }
            
            c.getPlayer().stopAllTimers();
            c.getPlayer().getMap().removePlayer(c.getPlayer());
            c.sendPacket(MaplePacketCreator.warpCS(c));
            c.getPlayer().getCashShop().open(true);
            c.sendPacket(MaplePacketCreator.enableCSUse0());
            c.sendPacket(MaplePacketCreator.showCashInventory(c));
            c.sendPacket(MaplePacketCreator.showGifts(c.getPlayer().getCashShop().loadGifts()));
            c.sendPacket(MaplePacketCreator.showWishList(c.getPlayer().getCashShop().getWishList(), false));
            c.sendPacket(MaplePacketCreator.showNXMapleTokens(c.getPlayer()));
            c.getPlayer().saveToDB(true);
        }
        else 
        {
            c.getPlayer().dropMessage("The Cash Shop is currently unavailable.");
            c.sendPacket(MaplePacketCreator.enableActions());
        }
    }
}