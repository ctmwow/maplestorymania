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
import org.ascnet.leaftown.net.netty.ByteBufAccessor;
import org.ascnet.leaftown.provider.DataUtil;
import org.ascnet.leaftown.tools.MaplePacketCreator;
import org.ascnet.leaftown.tools.data.input.LittleEndianAccessor;
import org.ascnet.leaftown.tools.data.input.SeekableLittleEndianAccessor;

import io.netty.buffer.Unpooled;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.Charset;
import java.sql.SQLException;
import javax.naming.AuthenticationNotSupportedException;

/**
 * @author Coal
 */
public class NoteActionHandler extends AbstractMaplePacketHandler {

	public static void main(String[] args) throws Exception
	{
		System.out.println("Olá".getBytes(Charset.forName("US-ASCII")).length);
		System.out.println("Olá".getBytes(Charset.forName("UTF-8")).length);
	}
	
    @Override
    public void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) 
    {
        int action = slea.readByte();

        if (action == 0x00 && c.getPlayer().getCashShop().getAvailableNotes() > 0x00) 
        {
            String charname = slea.readMapleAsciiString();
            String message = slea.readMapleAsciiString();
            
            System.out.println(slea.available());
            
            System.out.println(slea.toString());
            
            try 
            {
                if (c.getPlayer().getCashShop().isOpened())
                    c.sendPacket(MaplePacketCreator.showCashInventory(c));
                
                c.getPlayer().getCashShop().decreaseNotes();
                c.getPlayer().sendNote(charname, message, (byte) 0x01);
                MapleCharacter partner = c.getChannelServer().getPlayerStorage().getCharacterByName(charname);
                if(partner != null) partner.showNote();
            }
            catch (SQLException e) 
            {
                e.printStackTrace();
            }
        }
        else if (action == 0x01)
        {
            int num = slea.readByte();
            int fame = 0x00;
            
            slea.readByte();
            slea.readByte();
            
            for (int i = 0x00; i < num; i++) 
            {
                int id = slea.readInt();

                try 
                {
                	fame += slea.readByte();
                    c.getPlayer().deleteNote(id);
                }
                catch (SQLException e) 
                {
                }
            }
            
            if (fame > 0x00) 
            {
                c.getPlayer().addFame(fame);
                c.sendPacket(MaplePacketCreator.getShowFameGain(fame));
            }
        }
    }
}