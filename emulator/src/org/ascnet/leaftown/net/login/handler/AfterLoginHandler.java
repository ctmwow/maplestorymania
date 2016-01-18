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

package org.ascnet.leaftown.net.login.handler;

import org.ascnet.leaftown.client.MapleClient;
import org.ascnet.leaftown.net.AbstractMaplePacketHandler;
import org.ascnet.leaftown.tools.MaplePacketCreator;
import org.ascnet.leaftown.tools.data.input.SeekableLittleEndianAccessor;

public class AfterLoginHandler extends AbstractMaplePacketHandler 
{
    @Override
    public void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) 
    {
        final byte action = slea.readByte();
        if (action == 0) 
        {
            c.updateGenderandPin();
            c.updateLoginState(MapleClient.LOGIN_NOTLOGGEDIN);
        }
        else if ((action == 1 || action == 2) && c.getLoginState() == MapleClient.ENTERING_PIN) 
        {
            final byte action2 = slea.readByte();
            final String pin = slea.readMapleAsciiString();
            if (action == 1) 
            {
                if (action2 == 0) 
                {
                    if (Integer.parseInt(pin) == c.getPin()) 
                    {
                        c.updateLoginState(MapleClient.PIN_CORRECT);
                        c.sendPacket(MaplePacketCreator.pinOperation((byte) 0));
                    } 
                    else 
                    {
                        c.setPinTries((byte) (c.getPinTries() + 1));
                        if (c.getPinTries() == 5) 
                        {
                            c.authenticationFailureBan();
                            c.disconnect();
                        }
                        c.sendPacket(MaplePacketCreator.pinOperation((byte) 2));
                    }
                }
                else if (action2 == 1) 
                {
                    if (c.getPin() == 10000) 
                        c.sendPacket(MaplePacketCreator.pinOperation((byte) 1));
                    else 
                        c.sendPacket(MaplePacketCreator.pinOperation((byte) 4));
                }
            } 
            else if (action2 == 0) 
            {
                if (Integer.parseInt(pin) == c.getPin()) 
                {
                    c.updateLoginState(MapleClient.PIN_CORRECT);
                    c.sendPacket(MaplePacketCreator.pinOperation((byte) 1));
                }
                else 
                {
                    c.setPinTries((byte) (c.getPinTries() + 1));
                    if (c.getPinTries() == 5) 
                    {
                        c.authenticationFailureBan();
                        c.disconnect();
                    }
                    c.sendPacket(MaplePacketCreator.pinOperation((byte) 2));
                }
            }
        }
    }
}