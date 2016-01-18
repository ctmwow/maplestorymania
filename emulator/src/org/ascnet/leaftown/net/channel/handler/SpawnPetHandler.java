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

import org.ascnet.leaftown.client.IItem;
import org.ascnet.leaftown.client.MapleCharacter;
import org.ascnet.leaftown.client.MapleClient;
import org.ascnet.leaftown.client.MapleInventoryType;
import org.ascnet.leaftown.client.MaplePet;
import org.ascnet.leaftown.client.PetDataFactory;
import org.ascnet.leaftown.client.SkillFactory;
import org.ascnet.leaftown.client.messages.ServernoticeMapleClientMessageCallback;
import org.ascnet.leaftown.net.AbstractMaplePacketHandler;
import org.ascnet.leaftown.server.maps.MapleFoothold;
import org.ascnet.leaftown.tools.MaplePacketCreator;
import org.ascnet.leaftown.tools.data.input.SeekableLittleEndianAccessor;

import java.awt.Point;

public class SpawnPetHandler extends AbstractMaplePacketHandler {

    @Override
    public void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) 
    {
        slea.skip(4);
        final short slot = slea.readShort();
        final boolean lead = slea.readByte() == 1;

        MapleCharacter player = c.getPlayer();
        final IItem item = player.getInventory(MapleInventoryType.CASH).getItem(slot);
        if (item == null)
            return;
        
        // Handle Dragons
        if (item.getItemId() == 5000028) 
        {
            new ServernoticeMapleClientMessageCallback(5, c).dropMessage("Dragon eggs currently cannot be hatched.");
            c.sendPacket(MaplePacketCreator.enableActions());
            return;
        }
        
        // Handle Robo Pets
        if (item.getItemId() == 5000047)
        {
            new ServernoticeMapleClientMessageCallback(5, c).dropMessage("Robo eggs currently cannot be hatched.");
            c.sendPacket(MaplePacketCreator.enableActions());
            return;
        }

        final MaplePet pet = item.getPet();
        if (player.getPets().contains(pet))
            player.unequipPet(pet, false);
        else 
        {
            if (player.getSkillLevel(SkillFactory.getSkill(8)) == 0 && player.getPet(0) != null)
                player.unequipPet(player.getPet(0), false);

            final Point pos = player.getPosition();
            pos.y -= 12;
            pet.setPos(pos);
            final MapleFoothold fh = player.getMap().getFootholds().findBelow(pos);
            pet.setFh(fh != null ? fh.getId() : 0);
            pet.setStance(0);
            pet.setSummoned(true);
            pet.saveToDb();
            player.addPet(pet, lead);
            player.getMap().broadcastMessage(player, MaplePacketCreator.showPet(player, pet, false, false, true), true);
            c.sendPacket(MaplePacketCreator.petStatUpdate(player));
            c.sendPacket(MaplePacketCreator.enableActions());
            player.startFullnessSchedule(PetDataFactory.getHunger(pet.getItemId()), pet, player.getPetIndex(pet));
        }
    }
}