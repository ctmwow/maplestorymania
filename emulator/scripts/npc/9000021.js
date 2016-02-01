var status;
var text;
var upgrade = "Keep up the good work and thanks for playing MaplePhoenix!";

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
			cm.sendSimple("Hello and welcome to MaplePhoenix!\r\nLet me tell you about our grand opening Mark of Beta event.\r\n #L1# #bInformation#l\r\n #L2# Mark of the Beta#l\r\n #L3# Upgrade#l");
		} else if (status == 1) {
			if (selection == 1) {
				text = 1;
				cm.sendNext("Until the 18th of July, we will be doing our Mark of the Beta event. During this time you will be able to acquire a Mark of the Beta #i1002419# and upgrade it based upon the level you achieve.");
			} else if (selection == 2) {
				if (cm.haveItem(1002419)) {
					cm.sendOk("I see you already have a Mark of the Beta. Thank you for playing MaplePhoenix!");
					cm.dispose();
				} else {
					cm.sendOk("Here you go, have a free Mark of the Beta on us. Thank you for playing MaplePhoenix!");
					cm.gainItem(1002419,1);
					cm.dispose();
				}
			} else if (selection == 3) {
				if (cm.getPlayer().getLevel() < 10) {
					cm.sendOk("You have to be level 10 to make your first upgrade.");
					cm.dispose();
				} else {
					if (cm.itemQuantity(1002419) == 1) {
						cm.sendOk("Please equip your Mark of the Beta so it can be upgraded!");
						cm.dispose();
					} else if (cm.itemQuantity(1002419) == 0 && cm.haveItem(1002419)) {
						text = 2;
						cm.sendNext("Okay, I'll make some upgrades to your Mark of the Beta.");
					} else {
						cm.sendOk("You don't even have a Mark of the Beta yet!");
						cm.dispose();
					}
				}
			}
		} else if (status == 2) {
			if (text == 1) {
				cm.sendOk("The higher level you reach, the more powerful it will become. For every 10 levels you gain, it will gain\r\n+2 Str/Dex/Int/Luk, +10 HP/MP, and +5 WDef/MDef. It will cap at level 100.\r\n\r\nWe hope you can make the most out of our gift to you, and thank you for playing MaplePhoenix!");
				cm.dispose();
			} else if (text == 2) {
				if (cm.getLevel() >= 10 && cm.getLevel() <= 19) {
					if (cm.getStat(-1, 0) == 2) {
						cm.sendOk("You already have the level 10 upgrade!");
						cm.dispose();
					} else {
						cm.changeStat(-1, 0, 2);
						cm.changeStat(-1, 1, 2);
						cm.changeStat(-1, 2, 2);
						cm.changeStat(-1, 3, 2);
						cm.changeStat(-1, 4, 10);
						cm.changeStat(-1, 5, 10);
						cm.changeStat(-1, 8, 5);
						cm.changeStat(-1, 9, 5);
						cm.changeStat(-1, 10, 2);
						cm.changeStat(-1, 15, 0);
						cm.changeStat(-1, 16, 2);
						cm.changeStat(-1, 17, 0);
						cm.sendOk("There we go, level 10 upgrade done. " + upgrade);
					}
				} else if (cm.getLevel() >= 20 && cm.getLevel() <=29) {
					if (cm.getStat(-1, 0) == 4) {
						cm.sendOk("You already have the level 20 upgrade!");
						cm.dispose();
					} else {
						cm.changeStat(-1, 0, 4);
						cm.changeStat(-1, 1, 4);
						cm.changeStat(-1, 2, 4);
						cm.changeStat(-1, 3, 4);
						cm.changeStat(-1, 4, 20);
						cm.changeStat(-1, 5, 20);
						cm.changeStat(-1, 8, 10);
						cm.changeStat(-1, 9, 10);
						cm.changeStat(-1, 10, 4);
						cm.changeStat(-1, 15, 0);
						cm.changeStat(-1, 16, 2);
						cm.changeStat(-1, 17, 0);
						cm.sendOk("There we go, level 20 upgrade done. " + upgrade);
					}
				} else if (cm.getLevel() >= 30 && cm.getLevel() <=39) {
					if (cm.getStat(-1, 0) == 6) {
						cm.sendOk("You already have the level 30 upgrade!");
						cm.dispose();
					} else {
						cm.changeStat(-1, 0, 6);
						cm.changeStat(-1, 1, 6);
						cm.changeStat(-1, 2, 6);
						cm.changeStat(-1, 3, 6);
						cm.changeStat(-1, 4, 30);
						cm.changeStat(-1, 5, 30);
						cm.changeStat(-1, 8, 15);
						cm.changeStat(-1, 9, 15);
						cm.changeStat(-1, 10, 6);
						cm.changeStat(-1, 15, 0);
						cm.changeStat(-1, 16, 2);
						cm.changeStat(-1, 17, 0);
						cm.sendOk("There we go, level 30 upgrade done. " + upgrade);
					}
				} else if (cm.getLevel() >= 40 && cm.getLevel() <=49) {
					if (cm.getStat(-1, 0) == 8) {
						cm.sendOk("You already have the level 40 upgrade!");
						cm.dispose();
					} else {
						cm.changeStat(-1, 0, 8);
						cm.changeStat(-1, 1, 8);
						cm.changeStat(-1, 2, 8);
						cm.changeStat(-1, 3, 8);
						cm.changeStat(-1, 4, 40);
						cm.changeStat(-1, 5, 40);
						cm.changeStat(-1, 8, 20);
						cm.changeStat(-1, 9, 20);
						cm.changeStat(-1, 10, 8);
						cm.changeStat(-1, 15, 0);
						cm.changeStat(-1, 16, 2);
						cm.changeStat(-1, 17, 0);
						cm.sendOk("There we go, level 40 upgrade done. " + upgrade);
					}
				} else if (cm.getLevel() >= 50 && cm.getLevel() <=59) {
					if (cm.getStat(-1, 0) == 10) {
						cm.sendOk("You already have the level 50 upgrade!");
						cm.dispose();
					} else {
						cm.changeStat(-1, 0, 10);
						cm.changeStat(-1, 1, 10);
						cm.changeStat(-1, 2, 10);
						cm.changeStat(-1, 3, 10);
						cm.changeStat(-1, 4, 50);
						cm.changeStat(-1, 5, 50);
						cm.changeStat(-1, 8, 25);
						cm.changeStat(-1, 9, 25);
						cm.changeStat(-1, 10, 10);
						cm.changeStat(-1, 15, 0);
						cm.changeStat(-1, 16, 2);
						cm.changeStat(-1, 17, 0);
						cm.sendOk("There we go, level 50 upgrade done. " + upgrade);
					}
				} else if (cm.getLevel() >= 60 && cm.getLevel() <=69) {
					if (cm.getStat(-1, 0) == 12) {
						cm.sendOk("You already have the level 60 upgrade!");
						cm.dispose();
					} else {
						cm.changeStat(-1, 0, 12);
						cm.changeStat(-1, 1, 12);
						cm.changeStat(-1, 2, 12);
						cm.changeStat(-1, 3, 12);
						cm.changeStat(-1, 4, 60);
						cm.changeStat(-1, 5, 60);
						cm.changeStat(-1, 8, 30);
						cm.changeStat(-1, 9, 30);
						cm.changeStat(-1, 10, 12);
						cm.changeStat(-1, 15, 0);
						cm.changeStat(-1, 16, 2);
						cm.changeStat(-1, 17, 0);
						cm.sendOk("There we go, level 60 upgrade done. " + upgrade);
					}
				} else if (cm.getLevel() >= 70 && cm.getLevel() <=79) {
					if (cm.getStat(-1, 0) == 14) {
						cm.sendOk("You already have the level 70 upgrade!");
						cm.dispose();
					} else {
						cm.changeStat(-1, 0, 14);
						cm.changeStat(-1, 1, 14);
						cm.changeStat(-1, 2, 14);
						cm.changeStat(-1, 3, 14);
						cm.changeStat(-1, 4, 70);
						cm.changeStat(-1, 5, 70);
						cm.changeStat(-1, 8, 35);
						cm.changeStat(-1, 9, 35);
						cm.changeStat(-1, 10, 14);
						cm.changeStat(-1, 15, 0);
						cm.changeStat(-1, 16, 2);
						cm.changeStat(-1, 17, 0);
						cm.sendOk("There we go, level 70 upgrade done. " + upgrade);
					}
				} else if (cm.getLevel() >= 80 && cm.getLevel() <=89) {
					if (cm.getStat(-1, 0) == 16) {
						cm.sendOk("You already have the level 80 upgrade!");
						cm.dispose();
					} else {
						cm.changeStat(-1, 0, 16);
						cm.changeStat(-1, 1, 16);
						cm.changeStat(-1, 2, 16);
						cm.changeStat(-1, 3, 16);
						cm.changeStat(-1, 4, 80);
						cm.changeStat(-1, 5, 80);
						cm.changeStat(-1, 8, 40);
						cm.changeStat(-1, 9, 40);
						cm.changeStat(-1, 10, 16);
						cm.changeStat(-1, 15, 0);
						cm.changeStat(-1, 16, 2);
						cm.changeStat(-1, 17, 0);
						cm.sendOk("There we go, level 80 upgrade done. " + upgrade);
					}
				} else if (cm.getLevel() >= 90 && cm.getLevel() <=99) {
					if (cm.getStat(-1, 0) == 18) {
						cm.sendOk("You already have the level 90 upgrade!");
						cm.dispose();
					} else {
						cm.changeStat(-1, 0, 18);
						cm.changeStat(-1, 1, 18);
						cm.changeStat(-1, 2, 18);
						cm.changeStat(-1, 3, 18);
						cm.changeStat(-1, 4, 90);
						cm.changeStat(-1, 5, 90);
						cm.changeStat(-1, 8, 45);
						cm.changeStat(-1, 9, 45);
						cm.changeStat(-1, 10, 18);
						cm.changeStat(-1, 15, 0);
						cm.changeStat(-1, 16, 2);
						cm.changeStat(-1, 17, 0);
						cm.sendOk("There we go, level 90 upgrade done. " + upgrade);
					}
				} else if (cm.getLevel() >= 100) {
					if (cm.getStat(-1, 0) == 20) {
						cm.sendOk("You already have all the upgrades!");
						cm.dispose();
					} else {
						cm.changeStat(-1, 0, 20);
						cm.changeStat(-1, 1, 20);
						cm.changeStat(-1, 2, 20);
						cm.changeStat(-1, 3, 20);
						cm.changeStat(-1, 4, 100);
						cm.changeStat(-1, 5, 100);
						cm.changeStat(-1, 8, 50);
						cm.changeStat(-1, 9, 50);
						cm.changeStat(-1, 10, 20);
						cm.changeStat(-1, 15, 0);
						cm.changeStat(-1, 16, 2);
						cm.changeStat(-1, 17, 0);
						cm.sendOk("There we go, level 100 upgrade done. " + upgrade);
					}
				}
				cm.dispose();
			}
		}
	}
}