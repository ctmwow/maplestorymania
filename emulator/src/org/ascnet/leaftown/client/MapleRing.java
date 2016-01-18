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

package org.ascnet.leaftown.client;

import org.ascnet.leaftown.database.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * @author Danny
 */
public class MapleRing implements Comparable<MapleRing> 
{
    private final int ringId;
    private final int ringId2;
    private final int partnerId;
    private final int itemId;
    private final String partnerName;

    private MapleRing(int id, int id2, int partnerId, int itemid, String partnername) 
    {
        ringId = id;
        ringId2 = id2;
        this.partnerId = partnerId;
        itemId = itemid;
        partnerName = partnername;
    }

    public static MapleRing loadFromDb(int ringId) 
    {
        try 
        {
            Connection con = DatabaseConnection.getConnection(); // Get a connection to the database

            PreparedStatement ps = con.prepareStatement("SELECT * FROM rings WHERE id = ?"); // Get ring details..

            ps.setInt(0x01, ringId);
            ResultSet rs = ps.executeQuery();

            rs.next();
            MapleRing ret = new MapleRing(ringId, rs.getInt("partnerRingId"), rs.getInt("partnerChrId"), rs.getInt("itemid"), rs.getString("partnerName"));

            rs.close();
            ps.close();

            return ret;
        }
        catch (SQLException ex) 
        {
        	ex.printStackTrace();
            return null;
        }
    }

    public static int createRing(int itemid, final MapleCharacter partner1, final MapleCharacter partner2) 
    {
        try 
        {
            if (partner1 == null) 
                return -0x02; // Partner Number 1 is not on the same channel.
            else if (partner2 == null) 
                return -0x01;

            int[] ringID = new int[0x02];
            Connection con = DatabaseConnection.getConnection();
            PreparedStatement ps = con.prepareStatement("INSERT INTO rings (itemid, partnerChrId, partnername) VALUES (?, ?, ?)", Statement.RETURN_GENERATED_KEYS);
            ps.setInt(1, itemid);
            ps.setInt(2, partner2.getId());
            ps.setString(3, partner2.getName());
            ps.executeUpdate();
            ResultSet rs = ps.getGeneratedKeys();
            rs.next();
            ringID[0] = rs.getInt(1);
            rs.close();
            ps.close();

            ps = con.prepareStatement("INSERT INTO rings (itemid, partnerRingId, partnerChrId, partnername) VALUES (?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS);
            ps.setInt(1, itemid);
            ps.setInt(2, ringID[0]);
            ps.setInt(3, partner1.getId());
            ps.setString(4, partner1.getName());
            ps.executeUpdate();
            rs = ps.getGeneratedKeys();
            rs.next();
            ringID[0x01] = rs.getInt(1);
            rs.close();
            ps.close();

            ps = con.prepareStatement("UPDATE rings SET partnerRingId = ? WHERE id = ?");
            ps.setInt(0x01, ringID[0x01]);
            ps.setInt(0x02, ringID[0x00]);
            ps.executeUpdate();
            ps.close();

            return ringID[0x00];
        }
        catch (SQLException ex) 
        {
        	ex.printStackTrace();
            return -0x01;
        }
    }

    public int getRingId()
    {
        return ringId;
    }

    public int getPartnerRingId() 
    {
        return ringId2;
    }

    public int getPartnerChrId() 
    {
        return partnerId;
    }

    public int getItemId() 
    {
        return itemId;
    }

    public String getPartnerName()
    {
        return partnerName;
    }

    @Override
    public boolean equals(Object o) 
    {
        if (o instanceof MapleRing) 
            return ((MapleRing) o).ringId == ringId;

        return false;
    }

    @Override
    public int hashCode() 
    {
        return 0x35 * (0x05) + ringId;
    }

    @Override
    public int compareTo(MapleRing other) 
    {
        if (ringId < other.ringId) 
            return -0x01;
        else if (ringId == other.ringId) 
            return 0x00;
        else 
            return 0x01;
    }

    public static boolean checkRingDB(MapleCharacter player) 
    {
        try 
        {
            Connection con = DatabaseConnection.getConnection();
            PreparedStatement ps = con.prepareStatement("SELECT id FROM rings WHERE partnerChrId = ?");
            ps.setInt(0x01, player.getId());
            ResultSet rs = ps.executeQuery();
            boolean ret = rs.next();
            rs.close();
            ps.close();
            return ret;
        } 
        catch (SQLException ex) 
        {
            return true;
        }
    }

    public static void removeRingFromDb(MapleCharacter player) 
    {
        try 
        {
            Connection con = DatabaseConnection.getConnection();
            int otherId;
            PreparedStatement ps = con.prepareStatement("SELECT partnerRingId FROM rings WHERE partnerChrId = ?");
            ps.setInt(0x01, player.getId());
            ResultSet rs = ps.executeQuery();
            rs.next();
            otherId = rs.getInt("partnerRingId");
            rs.close();
            ps.close();
            ps = con.prepareStatement("DELETE FROM rings WHERE partnerChrId = ?");
            ps.setInt(0x01, player.getId());
            ps.executeUpdate();
            ps.close();
            ps = con.prepareStatement("DELETE FROM rings WHERE partnerChrId = ?");
            ps.setInt(0x01, otherId);
            ps.executeUpdate();
            ps.close();
        }
        catch (SQLException ex) 
        {
        	ex.printStackTrace();
        }
    }

    public static void removeWeddingRing(MapleCharacter player)
    {
        int itemid = player.getMarriageRings().get(0x00).itemId;
        try 
        {
            Connection con = DatabaseConnection.getConnection();
            PreparedStatement ps = con.prepareStatement("DELETE from rings where partnercharid = ? and itemid = ?");
            ps.setInt(0x01, player.getId());
            ps.setInt(0x02, itemid);
            ps.executeUpdate();
            ps.close();
            ps = con.prepareStatement("DELETE FROM rings where partnercharid = ? and itemid = ?");
            ps.setInt(0x01, player.getPartnerId());
            ps.setInt(0x02, itemid);
            ps.executeUpdate();
            ps.close();
        }
        catch (SQLException ex) 
        {
        }
    }
}