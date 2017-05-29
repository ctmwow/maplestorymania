/*
 	OrpheusMS: MapleStory Private Server based on OdinMS
    Copyright (C) 2012 Aaron Weiss <aaron@deviant-core.net>
    				Patrick Huy <patrick.huy@frz.cc>
					Matthias Butz <matze@odinms.de>
					Jan Christian Meyer <vimes@odinms.de>

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as
    published by the Free Software Foundation, either version 3 of the
    License, or (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.ascnet.leaftown.net.channel.handler;


import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

import org.ascnet.leaftown.client.IItem;
import org.ascnet.leaftown.client.MapleCharacter;
import org.ascnet.leaftown.client.MapleClient;
import org.ascnet.leaftown.database.DatabaseConnection;
import org.ascnet.leaftown.net.AbstractMaplePacketHandler;
import org.ascnet.leaftown.server.MapleInventoryManipulator;
import org.ascnet.leaftown.server.playerinteractions.HiredMerchant;
import org.ascnet.leaftown.tools.MaplePacketCreator;
import org.ascnet.leaftown.tools.data.input.SeekableLittleEndianAccessor;

/**
 * 
 * @author kevintjuh93
 */
public class FredrickHandler extends AbstractMaplePacketHandler 
{
	public void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) 
	{
		MapleCharacter chr = c.getPlayer();
		byte operation = slea.readByte();

		switch (operation) 
		{
			case 0x19: // Will never come...
				// c.announce(MaplePacketCreator.getFredrick((byte) 0x24));
				break;
			case 0x1A:
				try 
				{
					List<IItem> items = HiredMerchant.loadStoragedItems(c.getPlayer().getId());
					
					if (!check(chr, items)) 
					{
						c.sendPacket(MaplePacketCreator.fredrickMessage((byte) 0x21));
						return;
					}

					chr.gainMeso(chr.getMerchantMesos(), false);
					chr.setMerchantMesos(0);
					
					if (deleteItems(chr)) 
					{
						for (int i = 0; i < items.size(); i++) 
							MapleInventoryManipulator.addByItem(c, items.get(i), "Adicionado pelo Fredrick", true);
						
						c.sendPacket(MaplePacketCreator.fredrickMessage((byte) 0x1E));
					}
					else 
					{
						return;
					}
					break;
				} 
				catch (SQLException ex) 
				{
					ex.printStackTrace();
				}
				break;
			case 0x1C: // Exit
				break;
			default:

		}
	}

	private static boolean check(MapleCharacter chr, List<IItem> items) 
	{
		if (chr.getMeso() + chr.getMerchantMesos() < 0) 
			return false;
		
		for (IItem entry : items) 
		{
			if (!MapleInventoryManipulator.checkSpace(chr.getClient(), entry.getItemId(), entry.getQuantity(), entry.getOwner()))
				return false;
		}

		return true;
	}

	private static boolean deleteItems(MapleCharacter chr) 
	{
		try 
		{
			Connection con = DatabaseConnection.getConnection();
			PreparedStatement ps = con.prepareStatement("DELETE FROM `hiredmerchant` WHERE `ownerid` = ?");

			ps.setInt(1, chr.getId());
			ps.execute();
			ps.close();
			return true;
		} 
		catch (SQLException e) 
		{
			return false;
		}
	}
}