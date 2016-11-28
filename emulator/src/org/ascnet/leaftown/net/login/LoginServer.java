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

package org.ascnet.leaftown.net.login;

import org.ascnet.leaftown.client.MapleCharacterUtil;
import org.ascnet.leaftown.database.DatabaseConnection;
import org.ascnet.leaftown.net.PacketProcessor;
import org.ascnet.leaftown.net.Server;
import org.ascnet.leaftown.net.login.remote.LoginWorldInterface;
import org.ascnet.leaftown.net.world.remote.WorldLoginInterface;
import org.ascnet.leaftown.net.world.remote.WorldRegistry;
import org.ascnet.leaftown.provider.text.ServerMessages;
import org.ascnet.leaftown.server.TimerManager;

import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.rmi.ssl.SslRMIClientSocketFactory;
import java.io.FileReader;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.net.InetSocketAddress;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

public class LoginServer implements Runnable, LoginServerMBean {

    public static final int PORT = 8484;
    private Server server;
    static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(LoginServer.class);
    private static WorldRegistry worldRegistry = null;
    private final Map<Integer, String> channelServer = new HashMap<>();
    private LoginWorldInterface lwi;
    private WorldLoginInterface wli;
    private Properties prop = new Properties();
    private final Properties initialProp = new Properties();
    private Boolean worldReady = Boolean.TRUE;
    private final Properties subnetInfo = new Properties();
    private Map<Integer, Integer> load = new HashMap<>();
    private String serverName;
    private String eventMessage;
    private int flag;
    private int maxCharacters;
    //private Map<String, Integer> connectedIps = new HashMap<String, Integer>();
    private int userLimit;
    private int loginInterval;
    private static final LoginServer instance = new LoginServer();

    static 
    {
        MBeanServer mBeanServer = ManagementFactory.getPlatformMBeanServer();
        try 
        {
            mBeanServer.registerMBean(instance, new ObjectName("org.ascnet.leaftown.net.login:type=LoginServer,name=LoginServer"));
        }
        catch (Exception e) 
        {
            log.error("MBEAN ERROR", e);
        }
    }

    private LoginServer() 
    {
    }

    public static LoginServer getInstance() 
    {
        return instance;
    }

    public Set<Integer> getChannels() 
    {
        return channelServer.keySet();
    }

    public void addChannel(int channel, String ip) 
    {
        channelServer.put(channel, ip);
        load.put(channel, 0);
    }

    public void removeChannel(int channel) 
    {
        channelServer.remove(channel);
        load.remove(channel);
    }

    public String getIP(int channel) 
    {
        return channelServer.get(channel);
    }

    public int getPossibleLogins() {
        int ret = 0;
        try {
            Connection con = DatabaseConnection.getConnection();
            PreparedStatement limitCheck = con.prepareStatement("SELECT COUNT(*) FROM accounts WHERE loggedin > 1 AND gm = 0");
            ResultSet rs = limitCheck.executeQuery();
            if (rs.next()) {
                int usersOn = rs.getInt(1);
                if (usersOn < userLimit) {
                    ret = userLimit - usersOn;
                }
            }
            rs.close();
            limitCheck.close();
        } catch (Exception ex) {
            log.error("loginlimit error", ex);
        }
        return ret;
    }

    public void reconnectWorld() {
        try {
            wli.isAvailable(); // check if the connection is really gone
        } catch (RemoteException ex) {
            synchronized (worldReady) {
                worldReady = Boolean.FALSE;
            }
            synchronized (lwi) {
                synchronized (worldReady) {
                    if (worldReady)
                        return;
                }
                log.warn("Reconnecting to world server");
                synchronized (wli) {
                    // completely re-establish the rmi connection
                    try {
                        FileReader fileReader = new FileReader(System.getProperty("org.ascnet.leaftown.login.config"));
                        initialProp.load(fileReader);
                        fileReader.close();
                        Registry registry = LocateRegistry.getRegistry(initialProp.getProperty("org.ascnet.leaftown.world.host"), Registry.REGISTRY_PORT, new SslRMIClientSocketFactory());
                        worldRegistry = (WorldRegistry) registry.lookup("WorldRegistry");
                        lwi = new LoginWorldInterfaceImpl();
                        wli = worldRegistry.registerLoginServer(initialProp.getProperty("org.ascnet.leaftown.login.key"), lwi);
                        Properties dbProp = new Properties();
                        fileReader = new FileReader("db.properties");
                        dbProp.load(fileReader);
                        fileReader.close();
                        DatabaseConnection.setProps(dbProp);
                        DatabaseConnection.getConnection();
                        prop = wli.getWorldProperties();
                        userLimit = Integer.parseInt(prop.getProperty("org.ascnet.leaftown.login.userlimit"));
                        serverName = prop.getProperty("org.ascnet.leaftown.login.serverName");
                        eventMessage = prop.getProperty("org.ascnet.leaftown.login.eventMessage");
                        flag = Integer.parseInt(prop.getProperty("org.ascnet.leaftown.login.flag"));
                        maxCharacters = Integer.parseInt(prop.getProperty("org.ascnet.leaftown.login.maxCharacters"));
                        try {
                            fileReader = new FileReader("subnet.properties");
                            subnetInfo.load(fileReader);
                            fileReader.close();
                        } catch (Exception e) {
                            log.info("Could not load subnet configuration, falling back to world defaults", e);
                        }
                    } catch (Exception e) {
                        log.error("Reconnecting failed", e);
                    }
                    worldReady = Boolean.TRUE;
                }
            }
            synchronized (worldReady) {
                worldReady.notifyAll();
            }
        }
    }

    @Override
    public void run() 
    {
        try 
        {
            FileReader fileReader = new FileReader(System.getProperty("org.ascnet.leaftown.login.config"));
            initialProp.load(fileReader);
            fileReader.close();
            Registry registry = LocateRegistry.getRegistry(initialProp.getProperty("org.ascnet.leaftown.world.host"), Registry.REGISTRY_PORT, new SslRMIClientSocketFactory());
            worldRegistry = (WorldRegistry) registry.lookup("WorldRegistry");
            lwi = new LoginWorldInterfaceImpl();
            wli = worldRegistry.registerLoginServer(initialProp.getProperty("org.ascnet.leaftown.login.key"), lwi);
            Properties dbProp = new Properties();
            fileReader = new FileReader(System.getProperty("br.com.maplestorymania.db.properties"));
            dbProp.load(fileReader);
            fileReader.close();
            DatabaseConnection.setProps(dbProp);
            DatabaseConnection.getConnection();
            prop = wli.getWorldProperties();
            userLimit = Integer.parseInt(prop.getProperty("org.ascnet.leaftown.login.userlimit"));
            serverName = prop.getProperty("org.ascnet.leaftown.login.serverName");
            eventMessage = prop.getProperty("org.ascnet.leaftown.login.eventMessage");
            flag = Integer.parseInt(prop.getProperty("org.ascnet.leaftown.login.flag"));
            maxCharacters = Integer.parseInt(prop.getProperty("org.ascnet.leaftown.login.maxCharacters"));
            
            try 
            {
                fileReader = new FileReader("subnet.properties");
                subnetInfo.load(fileReader);
                fileReader.close();
            } 
            catch (Exception e) 
            {
                log.trace("Could not load subnet configuration, falling back to world defaults", e);
            }
        }
        catch (Exception e) 
        {
            throw new RuntimeException("Could not connect to world server.", e);
        }

        PacketProcessor.initialise(PacketProcessor.Mode.LOGINSERVER);
        TimerManager tMan = TimerManager.getInstance();
        tMan.start();
        loginInterval = Integer.parseInt(prop.getProperty("org.ascnet.leaftown.login.interval"));
        CharCreationInformationProvider.getInstance().loadValidValues();
        MapleCharacterUtil.isBanned("");

        server = new Server(new InetSocketAddress(PORT));
        server.run();
        log.info("Login Server Listening on port {}", PORT);
    }

    public void shutdown() 
    {
        log.info("Shutting down server...");
        try 
        {
            worldRegistry.deregisterLoginServer(lwi);
        }
        catch (RemoteException e) 
        {
            log.info("RemoteException encountered when shutting down server... but it shouldn't matter.");
        }
        TimerManager.getInstance().stop();
    }

    public WorldLoginInterface getWorldInterface() 
    {
        synchronized (worldReady) 
        {
            while (!worldReady) 
            {
                try 
                {
                    worldReady.wait();
                } catch (InterruptedException e) 
                {
                }
            }
        }
        return wli;
    }

    public static void main(String args[]) 
    {
        FileReader fileReader = null;
        try 
        {
        	ServerMessages.getInstance().load(new Locale("ptBR", "BR"));
        	
            Properties dbProp = new Properties();
            fileReader = new FileReader(System.getProperty("br.com.maplestorymania.db.properties"));
            dbProp.load(fileReader);
            fileReader.close();
            DatabaseConnection.setProps(dbProp);
            Connection c = DatabaseConnection.getConnection();
            PreparedStatement ps = c.prepareStatement("UPDATE accounts SET loggedin = 0");
            ps.executeUpdate();
            ps.close();
            ps = c.prepareStatement("UPDATE characters SET loggedin = 0, muted = 0, HasMerchant = 0, loggedinstate = 0");
            ps.executeUpdate();
            ps.close();
            ps = c.prepareStatement("UPDATE hiredmerchant SET onSale = false");
            ps.executeUpdate();
            ps.close();
            ps = c.prepareStatement("TRUNCATE TABLE accounts_ip");
            ps.executeUpdate();
            ps.close(); 
            ps = c.prepareStatement("TRUNCATE TABLE cooldowns");
            ps.executeUpdate();
            ps.close();
        }
        catch (Exception ex) 
        {
            log.error("Could not reset databases", ex);
        } finally 
        {
            try 
            {
                fileReader.close();
            }
            catch (IOException ex) 
            {
                Logger.getLogger(LoginServer.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        try 
        {
            LoginServer.getInstance().run();
        }
        catch (Exception ex) 
        {
            log.error("Error initializing loginserver", ex);
        }

        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() 
        {
            @Override
            public void run() 
            {
                log.info("Shutting down.");
                LoginServer.getInstance().shutdown();
                log.info("Shutdown complete; exiting.");
            }
        }));
    }

    public int getLoginInterval() 
    {
        return loginInterval;
    }

    public Properties getSubnetInfo() 
    {
        return subnetInfo;
    }

    public int getUserLimit() 
    {
        return userLimit;
    }

    public String getServerName() 
    {
        return serverName;
    }

    @Override
    public String getEventMessage() 
    {
        return eventMessage;
    }

    @Override
    public int getFlag() 
    {
        return flag;
    }

    public int getMaxCharacters() 
    {
        return maxCharacters;
    }

    public Map<Integer, Integer> getLoad() 
    {
        return load;
    }

    public void setLoad(Map<Integer, Integer> load) 
    {
        this.load = load;
    }

	/*public void addConnectedIP(String ip) {
        if (connectedIps.containsKey(ip)) {
			int connections = connectedIps.get(ip);
			connectedIps.remove(ip);
			connectedIps.put(ip, connections + 1);
		} else { // first connection from ip
			connectedIps.put(ip, 1);
		}
		System.out.println("Add - Connected IPs: " + connectedIps.get(ip));
	}

	public void removeConnectedIp(String ip) {
		if (connectedIps.containsKey(ip)) {
			int connections = connectedIps.get(ip);
			connectedIps.remove(ip);
			if (connections - 1 != 0)
				connectedIps.put(ip, connections - 1);
			System.out.println("Remove - Connected IPs: " + (connections - 1));
		}
	}

	public boolean ipCanConnect(String ip) {
		if (connectedIps.containsKey(ip)) {
			if (connectedIps.get(ip) > 1) {
				System.out.println("Check - Connected IPs: " + connectedIps.get(ip));
				return false;
			}
		}
		return true;
	}*/

    @Override
    public void setEventMessage(String newMessage) {
        eventMessage = newMessage;
    }

    @Override
    public void setFlag(int newflag) {
        flag = newflag;
    }

    @Override
    public void setUserLimit(int newLimit) {
        userLimit = newLimit;
    }
}