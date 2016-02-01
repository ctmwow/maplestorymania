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

package org.ascnet.leaftown.scripting.maps;

import org.ascnet.leaftown.client.MapleClient;

import javax.script.Compilable;
import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class MapScriptManager 
{
    protected static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(MapScriptManager.class);
    
    private static final MapScriptManager instance = new MapScriptManager();
    private final Map<String, MapScript> compiledScripts = new HashMap<>();
    private final ScriptEngineFactory sef;

    private MapScriptManager() 
    {
        sef = new ScriptEngineManager().getEngineByName("javascript").getFactory();
    }

    public static MapScriptManager getInstance() 
    {
        return instance;
    }

    public void getMapScript(MapleClient c, String scriptName, boolean firstUser) 
    {
        if (compiledScripts.containsKey(scriptName)) 
        {
        	compiledScripts.get(scriptName).start(new MapScriptMethods(c));
            return;
        }
        
        final File scriptFile = new File("scripts/map/" + (firstUser ? "onFirstUserEnter" : "onUserEnter") + "/" + scriptName + ".js");
        
        if (!scriptFile.exists()) 
        {
        	log.warn("[MAP] Script File " + scriptFile.getAbsolutePath() + " cannot be found!");
            return;
        }

        final ScriptEngine portal = sef.getScriptEngine();
        
        try 
        {
            ((Compilable) portal).compile(new FileReader(scriptFile)).eval();
        }
        catch (ScriptException | IOException e) 
        {
        	log.error("[MAP] Script " + scriptFile.getAbsolutePath() + " cannot by compiled!", e);
        }

        final MapScript script = ((Invocable) portal).getInterface(MapScript.class);
        script.start(new MapScriptMethods(c));
        
        compiledScripts.put(scriptName, script);
    }

    public void clearCompiledScripts() 
    {
    	compiledScripts.clear();
    }
}