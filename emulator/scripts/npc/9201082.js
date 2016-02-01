/*
 * ITCG
 * Credits by celo for this web
 * https://www.maplewiki.net/index.php?title=ITCG_forging
 * MaplePhoenix 
 */

var status;
var item;
 
function start() {
    status = -1;
    action(1, 0, 0);
}

function action(mode, type, selection) {
    if (mode == -1) {
	cm.dispose();
    } else {
	if (status >= 0 && mode == 0) {
            cm.dispose();
            return;
	}
	if (mode == 1) {
            status++;
	} else {
            status--;
	}
        if (status == 0) {
            if (cm.getLevel() < 50) {
		cm.sendOk("You must be at least level 50 to use iTCG forging.");
                cm.dispose(); 
		return;
            }
            if (cm.getLevel() >= 50) {
		cm.sendSimple("Hello and welcome to iTCG forging.\r\n #L0#Stormcaster Gloves#l\r\n #L1#Crystal Leaf Earrings#l\r\n #L2#Facestompers#l\r\n #L3#Crystal Ilbis#l\r\n #L4#Silver Deputy Star#l");
            }
        } else if (status == 1) {
            if (selection == 0) {
                if (cm.haveItem(4031824)) {
                    item = "scg";
                    cm.sendNext("I see you have a #bStormcaster	Gloves Forging Manual#k. To make this item you need the following materials:\r\n\r\n #i4031757# Antellion Relic\r\n #i4031759# Subani Ankh\r\n #i4031755# Taru Totem\r\n #i4000021# 15 Leathers\r\n #i4005000# 2 Power Crystals");
                } else {
                    cm.sendOk("You need a #bStormcaster	Gloves Forging Manual#k to make this item.");
                    cm.dispose();
                }
            }
            if (selection == 1) {
                if (cm.haveItem(4031825)) {
                    item = "cle";
                    cm.sendNext("I see you have a #bCrystal Leaf Earrings Forging Manual#k. To make this item you need the following materials:\r\n\r\n #i4031755# Taru Totem\r\n #i4031758# Naricain Jewel\r\n #i4031756# Mystic Astrolabe\r\n #i4021007# 2 Diamonds");
                } else {
                    cm.sendOk("You need a #bCrystal Leaf Earrings Forging Manual#k to make this item.");
                    cm.dispose();
                }
            }
            if (selection == 2) {
                if (cm.haveItem(4031911)) {
                    item = "fs";
                    cm.sendNext("I see you have a #bFacestompers Forging Manual#k. To make this item you need the following materials:\r\n\r\n #i4031755# Taru Totem\r\n #i4031913# Stone Tiger Head\r\n #i4011001# 50 Steel Plates\r\n #i4000021# 25 Leathers\r\n #i4000030# 50 Dragon Skins");
                } else {
                    cm.sendOk("You need a #bFacestompers Forging Manual#k to make this item.");
                    cm.dispose();
                }
            }
            if (selection == 3) {
                if (cm.haveItem(4031912)) {
                    item = "cilbi";
                    cm.sendNext("I see you have a #bCrystal Ilbi Forging Manual#k. To make this item you need the following materials:\r\n\r\n #i4031917# Crystal Shard\r\n #i4031758# Naricain Jewel\r\n #i4005003# 7 LUK Crystals\r\n #i4005004# Dark Crystal");
                } else {
                    cm.sendOk("You need a #bCrystal Ilbi Forging Manual#k to make this item.");
                    cm.dispose();
                }
            }
            if (selection == 4) {
                if (cm.haveItem(4032013)) {
                    item = "pendant";
                    cm.sendNext("I see you have a #bBigfoot's Toe#k. To make this item you need the following materials:\r\n\r\n #i4032013# 4 Bigfoot's Toe \r\n #i4031755# Taru Totem");
                } else {
                    cm.sendOk("You need 4 #bBigfoot's Toe#k to make this item.");
                    cm.dispose();
                }
            }
        } else if (status == 2) {
            if (item == "cilbi") {
                cm.sendYesNo("Would you like to make this item?");
            } else {
                cm.sendYesNo("The item will have random stats. Would you like to make this item?");
            }
        } else if (status == 3) {
            if (item == "scg") {
                if (cm.haveItem(4031757, 1) && cm.haveItem(4031759, 1) && cm.haveItem(4031755, 1) && cm.haveItem(4000021, 15) && cm.haveItem(4005000, 2)) {
                    cm.gainItem(4031824, -1, true);
                    cm.gainItem(4031757, -1, true);
                    cm.gainItem(4031759, -1, true);
                    cm.gainItem(4031755, -1, true);
                    cm.gainItem(4000021, -15, true);
                    cm.gainItem(4005000, -2, true);
                    cm.gainItem(1082223, 1, true, true);
                    cm.sendOk("There you go, all done.");
                    cm.dispose();
                } else {
                    cm.sendOk("Please check that you have all the required items.");
                    cm.dispose();
                }
            }
            if (item == "cle") {
                if (cm.haveItem(4031755, 1) && cm.haveItem(4031758, 1) && cm.haveItem(4031756, 1) && cm.haveItem(4021007, 2)) {
                    cm.gainItem(4031825, -1, true);
                    cm.gainItem(4031755, -1, true);
                    cm.gainItem(4031758, -1, true);
                    cm.gainItem(4031756, -1, true);
                    cm.gainItem(4021007, -2, true);
                    cm.gainItem(1032048, 1, true, true);
                    cm.sendOk("There you go, all done.");
                    cm.dispose();
                } else {
                    cm.sendOk("Please check that you have all the required items.");
                    cm.dispose();
		}
            }
            if (item == "fs") {
                if (cm.haveItem(4031755, 1) && cm.haveItem(4031913,1) && cm.haveItem(4011001, 50) && cm.haveItem(4000021, 25) && cm.haveItem(4000030, 50)) {
                    cm.gainItem(4031911, -1, true);
                    cm.gainItem(4031755, -1, true);
                    cm.gainItem(4031913, -1, true);
                    cm.gainItem(4011001, -50, true);
                    cm.gainItem(4000021, -25, true);
                    cm.gainItem(4000030, -50, true);
                    cm.gainItem(1072344, 1, true, true);
                    cm.sendOk("There you go, all done.");
                    cm.dispose();
		} else {
                    cm.sendOk("Please check that you have all the required items.");
                    cm.dispose();
		}
            }
            if (item == "pendant") {
                if (cm.haveItem(4032013, 4) && cm.haveItem(4031755,1)) {
                    cm.gainItem(4032013, -4, true);
                    cm.gainItem(4031755, -1, true);
                    cm.gainItem(1122014, 1, true, true);
                    cm.sendOk("There you go, all done.");
                    cm.dispose();
		} else {
                    cm.sendOk("Please check that you have all the required items.");
                    cm.dispose();
		}
            }
            if (item == "cilbi") {
                if (cm.haveItem(4031917, 1) && cm.haveItem(4031758, 1) && cm.haveItem(4005003, 7) && cm.haveItem(4005004, 1)) {
                    cm.gainItem(4031912, -1, true);
                    cm.gainItem(4031917, -1, true);
                    cm.gainItem(4031758, -1, true);
                    cm.gainItem(4005003, -7, true);
                    cm.gainItem(4005004, -1, true);
                    cm.gainItem(2070016, 1, true, true);
                    cm.sendOk("There you go, all done.");
                    cm.dispose();
                } else {
                    cm.sendOk("Please check that you have all the required items.");
                    cm.dispose();
		}
            }
	}
    }
}