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

package org.ascnet.leaftown.server;

import org.ascnet.leaftown.client.IItem;
import org.ascnet.leaftown.database.DatabaseConnection;
import org.ascnet.leaftown.provider.DataUtil;
import org.ascnet.leaftown.provider.MapleData;
import org.ascnet.leaftown.provider.MapleDataProviderFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * @author Lerk
 */
public class CashItemFactory
{
    private final static Map<Integer, CashItemInfo> itemStats = new HashMap<>(11922);
    private final static Map<Integer, List<CashItemInfo>> cashPackages = new HashMap<>();
    private final static MapleData packageInformationProvider = MapleDataProviderFactory.getDataProvider("Etc").getData("CashPackage.img");
    private final static MapleData commodityInformationProvider = MapleDataProviderFactory.getDataProvider("Etc").getData("Commodity.img");

    static
    {
        try 
        {
        	Iterator<MapleData> it = commodityInformationProvider.iterator();
        	while(it.hasNext())
        	{
        		MapleData data = it.next();
        		  
        		int sn = DataUtil.toInt(data.resolve("SN"));
        		int itemId = DataUtil.toInt(data.resolve("ItemId"));
        		int price = DataUtil.toInt(data.resolve("Price"), 0x00);
        		int period = DataUtil.toInt(data.resolve("Price"), 0x00);
        		int gender = DataUtil.toInt(data.resolve("Gender"), 0x02);
        		short count = (short) DataUtil.toInt(data.resolve("Count"), 0x01);
        		boolean onSale = DataUtil.toInt(data.resolve("OnSale"), 0x01) == 0x01;
        		
        		itemStats.put(sn, new CashItemInfo(sn, itemId, count, price, period, gender, onSale));
        	}
        }
        catch (Exception e) 
        {
        	e.printStackTrace();
        } 
        finally 
        {
        }
    }
    
    public static List<IItem> getPackage(int itemId) 
    {
        List<IItem> cashPackage = new ArrayList<>();

        for (CashItemInfo cashItemInfo : cashPackages.get(itemId))
            cashPackage.add(cashItemInfo.toItem());
        
        return cashPackage;
    }

    public static CashItemInfo getItem(int sn) 
    {
        return itemStats.get(sn);
    }
    
    public static boolean isPackage(int itemId)
    {
        return cashPackages.containsKey(itemId);
    }

    public static List<CashItemInfo> getPackageItems(int itemId)
    {
        if (cashPackages.containsKey(itemId)) 
            return cashPackages.get(itemId);

        final ArrayList<CashItemInfo> packageItems = new ArrayList<>();
        final MapleData packageItem = packageInformationProvider.resolve(itemId + "/SN");
        
        if (packageItem != null)
        {
            for (MapleData item : packageItem)
                packageItems.add(getItem(DataUtil.toInt(packageItem.resolve(item.getName()))));
        }
        
        packageItems.trimToSize();
        cashPackages.put(itemId, packageItems);
        return packageItems;
    }
}