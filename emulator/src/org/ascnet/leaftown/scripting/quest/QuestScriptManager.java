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

package org.ascnet.leaftown.scripting.quest;

import org.ascnet.leaftown.client.MapleClient;
import org.ascnet.leaftown.scripting.AbstractScriptManager;

import javax.script.Invocable;
import java.util.HashMap;
import java.util.Map;

/**
 * @author RMZero213
 */
public class QuestScriptManager extends AbstractScriptManager 
{
    private final Map<MapleClient, QuestActionManager> qms = new HashMap<>();
    private final Map<MapleClient, Invocable> scripts = new HashMap<>();
    
    private static final QuestScriptManager instance = new QuestScriptManager();

    public synchronized static QuestScriptManager getInstance() 
    {
        return instance;
    }

    public void start(MapleClient c, int npc, int quest) 
    {
        try 
        {
            final QuestActionManager qm = new QuestActionManager(c, npc, quest, true);
            
            if (qms.containsKey(c)) 
                return;

            qms.put(c, qm);
            
            final Invocable iv = getInvocable("quest/" + quest + ".js", c);
            
            if (iv == null) 
            {
                qm.dispose();
                return;
            }
            
            engine.put("qm", qm);
            
            scripts.put(c, iv);
            
            iv.invokeFunction("start", (byte) 0x01, (byte) 0x00, 0x00);
        } 
        catch (Exception e) 
        {
            log.error("Error executing Quest script. (" + quest + ")", e);
            dispose(c);
        }
    }

    public void start(MapleClient c, byte mode, byte type, int selection) 
    {
        final Invocable qs = scripts.get(c);
        
        if (qs != null) 
        {
            try 
            {
                qs.invokeFunction("start", mode, type, selection);
            }
            catch (Exception e) 
            {
                log.error("Error executing Quest script. (" + c.getQM().getQuest() + ")", e);
                dispose(c);
            }
        }
    }

    public void end(MapleClient c, int npc, int quest) 
    {
        try 
        {
            final QuestActionManager qm = new QuestActionManager(c, npc, quest, false);
            
            if (qms.containsKey(c)) 
                return;
            
            qms.put(c, qm);
            
            final Invocable iv = getInvocable("quest/" + quest + ".js", c);
            
            if (iv == null) 
            {
                qm.dispose();
                return; 
            }
            
            engine.put("qm", qm);
            scripts.put(c, iv);
            
            iv.invokeFunction("end", (byte) 0x01, (byte) 0x00, 0x00); // start it off as something
        } 
        catch (Exception e) 
        {
            log.error("Error executing Quest script. (" + quest + ")", e);
            dispose(c);
        }
    }

    public void end(MapleClient c, byte mode, byte type, int selection) 
    {
        final Invocable qs = scripts.get(c);
        
        if (qs != null) 
        {
            try 
            {
                qs.invokeFunction("end", mode, type, selection);
            }
            catch (Exception e) 
            {
                log.error("Error executing Quest script. (" + c.getQM().getQuest() + ")", e);
                dispose(c);
            }
        }
    }

    public void dispose(QuestActionManager qm, MapleClient c) 
    {
        qms.remove(c);
        scripts.remove(c);
        
        resetContext("quest/" + qm.getQuest() + ".js", c);
    }

    public void dispose(MapleClient c) 
    {
        final QuestActionManager qm = qms.get(c);
        
        if (qm != null) 
            dispose(qm, c);
    }

    public QuestActionManager getQM(MapleClient c)
    {
        return qms.get(c);
    }
}