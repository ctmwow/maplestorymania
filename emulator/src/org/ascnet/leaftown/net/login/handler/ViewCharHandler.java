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

import org.ascnet.leaftown.client.MapleCharacter;
import org.ascnet.leaftown.client.MapleClient;
import org.ascnet.leaftown.database.DatabaseConnection;
import org.ascnet.leaftown.net.AbstractMaplePacketHandler;
import org.ascnet.leaftown.tools.MaplePacketCreator;
import org.ascnet.leaftown.tools.data.input.SeekableLittleEndianAccessor;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class ViewCharHandler extends AbstractMaplePacketHandler {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ViewCharHandler.class);

    @Override
    public void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        final Connection con = DatabaseConnection.getConnection();
        c.sendPacket(MaplePacketCreator.reauthenticateClient(c));
        try {
            final PreparedStatement ps = con.prepareStatement("SELECT id, world FROM characters WHERE accountid = ?");
            ps.setInt(1, c.getAccID());
            int charsNum = 0;
            final List<Integer> worlds = new ArrayList<>();
            final List<MapleCharacter> chars = new ArrayList<>();
            final ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                final int cworld = rs.getInt("world");
                boolean inside = false;
                for (int w : worlds) {
                    if (w == cworld)
                        inside = true;
                }

                if (!inside)
                    worlds.add(cworld);

                final MapleCharacter chr = MapleCharacter.loadCharFromDB(rs.getInt("id"), c, false);
                chars.add(chr);
                charsNum++;
            }
            rs.close();
            ps.close();
            if (charsNum == 0) {
                c.sendPacket(MaplePacketCreator.showAllCharacterStatus((byte) 4));
                return;
            }
            c.sendPacket(MaplePacketCreator.showAllCharacter(3 + worlds.size(), 7 + charsNum));
            for (int i = 0; i < 3; i++) {
                c.sendPacket(MaplePacketCreator.showAllCharacterStatus((byte) 5));
            }
            for (int w : worlds) {
                final List<MapleCharacter> chrsinworld = new ArrayList<>();
                for (MapleCharacter chr : chars) {
                    if (chr.getWorld() == w)
                        chrsinworld.add(chr);
                }
                c.sendPacket(MaplePacketCreator.showAllCharacterInfo(w, chrsinworld));
            }
        } catch (Exception e) {
            log.error("Viewing all chars failed", e);
        }
    }
}