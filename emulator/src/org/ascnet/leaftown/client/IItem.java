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

import java.sql.Timestamp;
import java.util.List;

public interface IItem extends Comparable<IItem> 
{
    public final int ITEM = 0x02;
    public final int EQUIP = 0x01;

    byte getType();

    short getPosition();

    void setPosition(short position);

    short getPrevPosition();

    void setPrevPosition(short position);

    int getItemId();
    
    int getCashId();

    short getQuantity();

    String getOwner();

    int getPetId();

    IItem copy();
    
    boolean isFromOwner(final String owner);

    void setOwner(String owner);

    void setQuantity(short quantity);

    public void log(String msg, boolean fromDB);

    List<String> getLog();

    Timestamp getExpiration();

    void setExpiration(Timestamp expire);

    int getSN();

    int getUniqueId();

    void setUniqueId(int id);

    void setSN(int sn);

    short getFlag();

    void setFlag(short flag);

    boolean isByGM();

    void setGMFlag();

    boolean isSSOneOfAKind();

    void setSSOneOfAKind(boolean sets);

    void setStoragePosition(byte position);

    byte getStoragePosition();
    
    void setGiftFrom(String from);
    
    String getGiftFrom();

    MaplePet getPet();

    void setPet(MaplePet pet);
}