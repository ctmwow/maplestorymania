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

package org.ascnet.leaftown.net.world.remote;

import java.rmi.RemoteException;
import java.util.List;

public interface WorldChannelCommonOperations 
{
    public boolean isConnected(String charName, boolean removePlayer) throws RemoteException;

    public void broadcastMessage(String sender, final byte[] message, boolean smega) throws RemoteException;

    public void broadcastMessage(String sender, final byte[] message) throws RemoteException;

    public void whisper(String sender, String target, int channel, String message) throws RemoteException;

    public void shutdown(int time) throws RemoteException;

    public void broadcastWorldMessage(String message) throws RemoteException;

    public void loggedOn(String name, int characterId, int channel, int[] buddies) throws RemoteException;

    public void loggedOff(String name, int characterId, int channel, int[] buddies) throws RemoteException;

    public List<CheaterData> getCheaters() throws RemoteException;

    public void buddyChat(int[] recipientCharacterIds, int cidFrom, String nameFrom, String chattext) throws RemoteException;

    public void messengerInvite(String sender, int messengerid, String target, int fromchannel) throws RemoteException;

    public void spouseChat(String sender, String target, String message) throws RemoteException;

    public void broadcastGMMessage(String sender, final byte[] message) throws RemoteException;
}