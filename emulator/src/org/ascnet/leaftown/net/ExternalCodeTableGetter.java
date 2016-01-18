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

package org.ascnet.leaftown.net;

import org.ascnet.leaftown.tools.HexTool;
import org.ascnet.leaftown.tools.MutableValueHolder;
import org.ascnet.leaftown.tools.ValueHolder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Properties;

public class ExternalCodeTableGetter {

    final Properties props;

    public ExternalCodeTableGetter(Properties properties) {
        props = properties;
    }

    private static <T extends Enum<? extends ValueHolder<Integer>> & ValueHolder<Integer>> T valueOf(String name, T[] values) {
        for (T val : values) {
            if (val.name().equals(name)) {
                return val;
            }
        }
        return null;
    }

    private <T extends Enum<? extends ValueHolder<Integer>> & ValueHolder<Integer>> int getValue(String name, T[] values, int def) {
        String prop = props.getProperty(name);
        if (prop != null && prop.length() > 0) {
            String trimmed = prop.trim();
            String[] args = trimmed.split(" ");
            int base = 0;
            String offset;
            if (args.length == 2) {
                base = valueOf(args[0], values).getValue();
                if (base == def) {
                    base = getValue(args[0], values, def);
                }
                offset = args[1];
            } else {
                offset = args[0];
            }
            if (offset.length() > 2 && offset.substring(0, 2).equals("0x")) {
                return Integer.parseInt(offset.substring(2), 16) + base;
            } else {
                return Integer.parseInt(offset) + base;
            }
        }
        return def;
    }

    public static <T extends Enum<? extends MutableValueHolder<Integer>> & MutableValueHolder<Integer>> String getOpcodeTable(T[] enumeration) {
        StringBuilder enumVals = new StringBuilder();
        List<T> all = new ArrayList<>(); // need a mutable list plawks
        all.addAll(Arrays.asList(enumeration));
        Collections.sort(all, new Comparator<ValueHolder<Integer>>() {

            @Override
            public int compare(ValueHolder<Integer> o1, ValueHolder<Integer> o2) {
                return o1.getValue().compareTo(o2.getValue());
            }
        });
        for (T code : all) {
            enumVals.append(code.name());
            enumVals.append(" = ");
            enumVals.append("0x");
            enumVals.append(HexTool.toString(code.getValue()));
            enumVals.append(" (");
            enumVals.append(code.getValue());
            enumVals.append(")\n");
        }
        return enumVals.toString();
    }

    public static <T extends Enum<? extends MutableValueHolder<Integer>> & MutableValueHolder<Integer>> void populateValues(Properties properties, T[] values) {
        ExternalCodeTableGetter exc = new ExternalCodeTableGetter(properties);
        for (T code : values) {
            code.setValue(exc.getValue(code.name(), values, -2));
        }
        org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ExternalCodeTableGetter.class);
        if (log.isTraceEnabled()) { // generics - copy pasted between send and recv current?
            log.trace(getOpcodeTable(values));
        }
    }
}