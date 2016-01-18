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
import org.ascnet.leaftown.client.MapleInventoryType;
import org.ascnet.leaftown.client.MaplePet;
import org.ascnet.leaftown.client.anticheat.CheatingOffense;
import org.ascnet.leaftown.net.AbstractMaplePacketHandler;
import org.ascnet.leaftown.net.channel.ChannelServer;
import org.ascnet.leaftown.net.world.MaplePartyCharacter;
import org.ascnet.leaftown.server.MapleInventoryManipulator;
import org.ascnet.leaftown.server.MapleItemInformationProvider;
import org.ascnet.leaftown.server.maps.MapleMapItem;
import org.ascnet.leaftown.server.maps.MapleMapObject;
import org.ascnet.leaftown.tools.MaplePacketCreator;
import org.ascnet.leaftown.tools.data.input.SeekableLittleEndianAccessor;

/**
 * @author Raz
 */
public class PetLootHandler extends AbstractMaplePacketHandler {

    @Override
    public void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        if (c.getPlayer().getPets().isEmpty() || !c.getPlayer().getMap().isLootable() || c.getPlayer().getTrade() != null) {
            c.sendPacket(MaplePacketCreator.enableActions());
            return;
        }
        MaplePet pet = c.getPlayer().getPet(c.getPlayer().getPetIndex(slea.readInt()));
        slea.skip(13);
        int oid = slea.readInt();
        MapleMapObject ob = c.getPlayer().getMap().getMapObject(oid);
        MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
        if (ob == null || pet == null) {
            c.sendPacket(MaplePacketCreator.enableActions());
            return;
        }
        if (c.getPlayer().getInventory(MapleInventoryType.EQUIPPED).findById(1812000) == null && c.getPlayer().getInventory(MapleInventoryType.EQUIPPED).findById(1812001) == null) {
            c.sendPacket(MaplePacketCreator.enableActions());
            return;
        }
        if (ob instanceof MapleMapItem) {
            MapleMapItem mapitem = (MapleMapItem) ob;
            synchronized (mapitem) {
                if (mapitem.isPickedUp()) {
                    c.sendPacket(MaplePacketCreator.enableActions());
                    return;
                }
                double distance = pet.getPos().distanceSq(mapitem.getPosition());
                c.getPlayer().getCheatTracker().checkPickupAgain();
                if (distance > 90000.0) { // 300^2, 550 is approximatly the range of ultis
                    c.getPlayer().getCheatTracker().registerOffense(CheatingOffense.ITEMVAC);
                } else if (distance > 22500.0) {
                    c.getPlayer().getCheatTracker().registerOffense(CheatingOffense.SHORT_ITEMVAC);
                }
                int mesos = mapitem.getMeso();
                if (mesos > 0 && c.getPlayer().getInventory(MapleInventoryType.EQUIPPED).findById(1812000) != null) {
                    if (c.getPlayer().isIgnoredItem(Integer.MAX_VALUE)) {
                        c.sendPacket(MaplePacketCreator.enableActions());
                        return;
                    }
                    if (c.getPlayer().getParty() != null) {
                        final ChannelServer cserv = c.getChannelServer();
                        int partynum = 0;
                        for (MaplePartyCharacter partymem : c.getPlayer().getParty().getMembers()) {
                            if (partymem.isOnline() && partymem.getChannel() == c.getChannel() && partymem.getMapId() == c.getPlayer().getMap().getId() && partymem.getPlayer() != null && !partymem.getPlayer().getCashShop().isOpened() && !partymem.getPlayer().inMTS()) {
                                partynum++;
                            }
                        }
                        if (partynum == 0)
                            partynum = 1;
                        for (MaplePartyCharacter partymem : c.getPlayer().getParty().getMembers()) {
                            if (partymem.isOnline() && partymem.getChannel() == c.getChannel() && partymem.getMapId() == c.getPlayer().getMap().getId() && partymem.getPlayer() != null && !partymem.getPlayer().getCashShop().isOpened() && !partymem.getPlayer().inMTS()) {
                                MapleCharacter somecharacter = cserv.getPlayerStorage().getCharacterById(partymem.getId());
                                if (somecharacter != null) {
                                    somecharacter.gainMeso(mesos / partynum, true, false, false);
                                    removeItem(c.getPlayer(), pet, mapitem, ob);
                                }
                            }
                        }
                    } else {
                        c.getPlayer().gainMeso(mesos, true, false, false);
                        removeItem(c.getPlayer(), pet, mapitem, ob);
                    }
                } else if (mapitem.getItem() != null && c.getPlayer().getInventory(MapleInventoryType.EQUIPPED).findById(1812001) != null) {
                    int itemId = mapitem.getItem().getItemId();
                    if (itemId == 2022428 && c.getPlayer().haveItem(2022428, 1, false, false)) { // custom one of a kind (pink box)
                        c.sendPacket(MaplePacketCreator.enableActions());
                        return;
                    }
                    if (c.getPlayer().isIgnoredItem(itemId)) {
                        c.sendPacket(MaplePacketCreator.enableActions());
                        return;
                    }
                    if (ii.isPet(itemId)) {
                        MaplePet nPet = mapitem.getItem().getPet();
                        int petId = nPet != null ? nPet.getUniqueId() : -1;
                        if (nPet != null && mapitem.getOwner() != c.getPlayer())
                            nPet = MaplePet.updateExisting(c.getPlayer().getId(), mapitem.getItem().getPet());
                        else if (petId == -1) {
                            nPet = MaplePet.createPet(c.getPlayer().getId(), itemId);
                        }
                        MapleInventoryManipulator.addById(c, itemId, mapitem.getItem().getQuantity(), "Pet was picked up", null, nPet);
                        c.sendPacket(MaplePacketCreator.enableActions());
                        removeItem(c.getPlayer(), pet, mapitem, ob);
                    } else if (ii.isConsumeOnPickup(itemId)) {
                        if (ii.isMonsterCard(itemId) && !c.getPlayer().getMonsterBook().isCardMaxed(itemId)) {
                            c.getPlayer().getMonsterBook().addCard(c, itemId);
                        }
                        MapleItemInformationProvider.getInstance().getItemEffect(itemId).applyTo(c.getPlayer());
                        c.sendPacket(MaplePacketCreator.getShowItemGain(itemId, mapitem.getItem().getQuantity()));
                        removeItem(c.getPlayer(), pet, mapitem, ob);
                    } else {
                        if (!c.getPlayer().canSeeItem(ob)) {
                            c.sendPacket(MaplePacketCreator.enableActions());
                            c.sendPacket(MaplePacketCreator.showItemUnavailable());
                            return;
                        }
                        if (!MapleInventoryManipulator.addByItem(c, mapitem.getItem(), "Picked up by " + c.getPlayer().getName(), true).isEmpty()) {
                            removeItem(c.getPlayer(), pet, mapitem, ob);
                        } else {
                            c.getPlayer().getCheatTracker().pickupComplete();
                        }
                    }
                }
            }
        }
    }

    private void removeItem(MapleCharacter chr, MaplePet pet, MapleMapItem mapitem, MapleMapObject ob) {
        chr.getMap().broadcastMessage(MaplePacketCreator.removeItemFromMap(mapitem.getObjectId(), 5, chr.getId(), true, chr.getPetIndex(pet)), mapitem.getPosition());
        chr.getCheatTracker().pickupComplete();
        chr.getMap().removeMapObject(ob);
        mapitem.setPickedUp(true);
    }
}