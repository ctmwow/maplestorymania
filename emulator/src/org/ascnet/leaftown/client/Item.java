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

import org.ascnet.leaftown.tools.FileTimeUtil;

import java.sql.Timestamp;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

public class Item implements IItem 
{
    private final int id;
    private int cashId;
    private short prevPosition = -0x01;
    private short position = Byte.MAX_VALUE;
    private short quantity;
    private int petid = -0x01;
    private String owner = "";
    protected List<String> log;
    private Timestamp expiration = FileTimeUtil.getDefaultTimestamp();
    private int uniqueid;
    private int sn;
    private short flag = 0x00;
    private boolean isByGM = false;
    private byte storagePosition = 0x00;
    private String giftFrom = ""; 
    private MaplePet pet = null;

    public Item(int id, short position, short quantity) 
    {
        super();
        this.id = id;
        this.position = position;
        this.quantity = quantity;
        log = new LinkedList<>();
    }
    
    public Item(int id, short position, int petId) 
    {
        super();
        this.id = id;
        this.position = position;
        this.quantity = 0x01;
        this.petid = petId;
        log = new LinkedList<>();
    }

    public Item(int id, short position, short quantity, short flag)
    {
        super();
        this.id = id;
        this.position = position;
        this.quantity = quantity;
        this.flag = flag;
        log = new LinkedList<>();
    }

    public IItem copy() 
    {
        Item ret = new Item(id, position, quantity, flag);
        ret.isByGM = isByGM;
        ret.owner = owner;
        ret.expiration = expiration;
        ret.prevPosition = prevPosition;
        ret.petid = petid;
        ret.pet = pet;
        ret.log = new LinkedList<>(log);
        return ret;
    }
    
    public boolean isFromOwner(final String owner)
    {
    	if(owner == null && this.owner == null)
    		return true;
    	if(owner == null && this.owner.equals(""))
    		return true;
    	
    	if(this.owner == null)
    		return false;
    	if(owner == null)
    		return false;
    	
    	return this.owner.equals(owner);
    }

    public void setPosition(short position) 
    {
        if (this.position != Byte.MAX_VALUE)
            prevPosition = this.position;
        this.position = position;
    }

    public void setPrevPosition(short position) 
    {
        prevPosition = position;
    }

    public void setQuantity(short quantity) 
    {
        this.quantity = quantity;
    }

    @Override
    public int getItemId()
    {
        return id;
    }
    
    public int getCashId()
    {
        if (cashId == 0x00) 
            cashId = new Random().nextInt(Integer.MAX_VALUE) + 1;
        
        return cashId;
    }

    @Override
    public short getPosition()
    {
        return position;
    }

    @Override
    public short getPrevPosition() 
    {
        return prevPosition;
    }

    @Override
    public short getQuantity()
    {
        return quantity;
    }

    @Override
    public byte getType() 
    {
        return IItem.ITEM;
    }

    @Override
    public String getOwner()
    {
    	if(owner != null && owner.trim().length() == 0x00)
    		owner = null;
    	
        return owner;
    }

    public void setOwner(String owner) 
    {
        this.owner = owner;
    }

    @Override
    public int getPetId() 
    {
        return petid;
    }

    @Override
    public short getFlag() 
    {
        if (isSSOneOfAKind())
            return (short) (flag ^ 0x20);
        return flag;
    }

    @Override
    public void setFlag(short flag)
    {
        boolean ooak = isSSOneOfAKind();
        this.flag = flag;
        if (ooak && !isSSOneOfAKind())
            this.flag |= 0x20;
    }

    @Override
    public int compareTo(IItem other) 
    {
        if (Math.abs(position) < Math.abs(other.getPosition()))
            return -0x01;
        else if (Math.abs(position) == Math.abs(other.getPosition()))
            return 0x00;
        else
            return 0x01;
    }

    @Override
    public String toString() 
    {
        return "Item: " + id + " quantity: " + quantity;
    }

    // no op for now as it eats too much ram :( once we have persistent inventoryids we can reenable it in some form.
    @Override
    public void log(String msg, boolean fromDB)
    {
		/*if (!fromDB) {
			StringBuilder toLog = new StringBuilder("[");
			toLog.append(Calendar.getInstance().getTime().toString());
			toLog.append("] ");
			toLog.append(msg);
			log.add(toLog.toString());
		} else {
			log.add(msg);
		}*/
    }

    @Override
    public List<String> getLog()
    {
        return Collections.unmodifiableList(log);
    }

    @Override
    public Timestamp getExpiration()
    {
        return expiration;
    }

    @Override
    public void setExpiration(Timestamp expire)
    {
        expiration = expire;
    }

    @Override
    public int getSN() 
    {
        return sn;
    }

    @Override
    public void setSN(int sn) 
    {
        this.sn = sn;
    }

    @Override
    public int getUniqueId()
    {
        return uniqueid;
    }

    @Override
    public void setGMFlag()
    {
        isByGM = true;
    }

    @Override
    public void setUniqueId(int id)
    {
        uniqueid = id;
    }

    @Override
    public boolean isByGM()
    {
        return isByGM;
    }

    @Override
    public void setSSOneOfAKind(boolean sets) 
    {
        if (isSSOneOfAKind() && !sets) 
            flag ^= 0x20;
        if (!isSSOneOfAKind() && sets) 
            flag |= 0x20;
    }

    @Override
    public boolean isSSOneOfAKind() 
    {
        return (flag & 0x20) == 0x20;
    }

    @Override
    public void setStoragePosition(byte position) 
    {
        storagePosition = position;
    }

    @Override
    public byte getStoragePosition() 
    {
        return storagePosition;
    }

    @Override
    public void setPet(MaplePet pet) 
    {
        this.pet = pet;
        petid = pet.getUniqueId();
    }

    @Override
    public MaplePet getPet() 
    {
        return pet;
    }
    
    public String getGiftFrom()
    {
    	return giftFrom;
    }
    
    public void setGiftFrom(String from)
    {
    	giftFrom = from;
    }
}