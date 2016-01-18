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
import org.ascnet.leaftown.server.MapleInventoryManipulator;
import org.ascnet.leaftown.tools.MaplePacketCreator;
import org.ascnet.leaftown.tools.data.input.SeekableLittleEndianAccessor;

import java.sql.SQLException;

/**
 * @author Penguins (Acrylic)
 */
public class CouponCodeHandler extends AbstractMaplePacketHandler {

    @Override
    public void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        slea.skip(2);
        String code = slea.readMapleAsciiString();
        boolean validcode = false;
        int type = -1;
        int item = -1;

        try {
            validcode = c.getPlayer().getNXCodeValid(code.toUpperCase());
        } catch (SQLException e) {
        }

        if (validcode) {
            try {
                type = c.getPlayer().getNXCodeType(code);
            } catch (SQLException e) {
            }
            try {
                item = c.getPlayer().getNXCodeItem(code);
            } catch (SQLException e) {
            }
            if (type != 5) {
                try {
                    c.getPlayer().setNXCodeUsed(code);
                } catch (SQLException e) {
                }
            }
            /*
			 * Explanation of type!
			 * Basically, this makes coupon codes do
			 * different things!
			 *
			 * Type 1: PayPal/Pay by Cash
			 * Type 2: Maple Points
			 * Type 3: Item
			 * Type 4: Nexon Game Card Cash
			 * Type 5: NX Coupon that can be used over and over
			 *
			 * When using Types 1, 2 or 4 the item is the amount
			 * of Paypal Cash, Nexon Game Card Cash or Maple Points you get.
			 * When using Type 3 the item is the ID of the item you get.
			 * Enjoy!
			 */
            switch (type) {
                case 1:
                case 2:
                case 4:
                    c.getPlayer().getCashShop().gainCash(type, item);
                    break;
                case 3:
                    MapleInventoryManipulator.addById(c, item, (short) 1, "An item was obtain from a coupon.", null, null);
                    c.sendPacket(MaplePacketCreator.showCouponRedeemedItem(item));
                    break;
                case 5:
                    c.getPlayer().getCashShop().gainCash(1, item);
                    break;
            }
            c.sendPacket(MaplePacketCreator.showNXMapleTokens(c.getPlayer()));
        } else {
            c.sendPacket(MaplePacketCreator.wrongCouponCode());
        }
    }
}