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

package org.ascnet.leaftown.scripting.event;

import org.apache.log4j.Logger;
import org.ascnet.leaftown.net.channel.ChannelServer;
import org.ascnet.leaftown.scripting.AbstractScriptManager;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptException;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author Matze
 */
public class EventScriptManager extends AbstractScriptManager 
{
    private static class EventEntry 
    {
        public EventEntry(String script, Invocable iv, EventManager em) 
        {
            this.script = script;
            this.iv = iv;
            this.em = em;
        }

        public final String script;
        public final Invocable iv;
        public final EventManager em;
    }

    private final Map<String, EventEntry> events = new LinkedHashMap<>();

    public EventScriptManager(ChannelServer cserv, String[] scripts) 
    {
        super();
        
        for (String script : scripts) 
        {
            if (script.length() != 0x00) 
            {
                final Invocable iv = getInvocable("event/" + script + ".js", null);
                
                events.put(script, new EventEntry(script, iv, new EventManager(cserv, iv, script)));
            }
        }
    }

    public EventManager getEventManager(String event) 
    {
        final EventEntry entry = events.get(event);
        
        if (entry == null) 
            return null;

        return entry.em;
    }

    public void init() 
    {
        for (EventEntry entry : events.values()) 
        {
            try 
            { 
            	if(((ScriptEngine) entry.iv) == null)
            		Logger.getRootLogger().warn("Error initializing script: " + entry.script + "Reason: [Script file not found]");
            	else
            	{
                    ((ScriptEngine) entry.iv).put("em", entry.em);
                    entry.iv.invokeFunction("init", (Object) null);	
            	}
            } 
            catch (ScriptException ex) 
            {
            	Logger.getRootLogger().error("Error initializing Events Scripts", ex.getCause());
            }
            catch (NoSuchMethodException ex) 
            {
            	Logger.getRootLogger().error("Error initializing Events Scripts", ex.getCause());
            }
        }
    }

    public void cancel() 
    {
        for (EventEntry entry : events.values()) 
        {
        	if(entry != null)
        		entry.em.cancel();
        }
    }
}