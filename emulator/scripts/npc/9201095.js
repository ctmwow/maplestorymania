/*
NPC: Fiona
Author: Dante
*/

var status;
var option;
var weap;
var ndd;

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
			return
		}
		if (mode == 1) {
			status++;
		} else {
			status--;
		}

		if (status == 0) {
			cm.sendSimple("Our clan is known for its implements of death. Be warned, the price of such beauty does not come cheap.\r\n\r\n#L0#Upgrade Raven Weapons#l\r\n#L1#Make Crimson Weapons#l\r\n#L2#Make Balanced Fury#l");
		} else if (status == 1) {
			if (selection == 0) {
				option = 1;
				cm.sendSimple("Which of our weapons would you like me to enhance?\r\n\r\n#L0##i1462052# Raven's Eye#l\r\n#L1##i1332077# Raven's Beak#l\r\n#L2##i1472072# Raven's Claw#l\r\n#L3##i1402048# Raven's Wing#l");
			}
			if (selection == 1) {
				option = 2;
				cm.sendSimple("Which weapon would you like to create?\r\n\r\n#L0##i1382060# Crimson Arcanon#l\r\n#L1##i1442068# Crimson Arcglaive#l\r\n#L2##i1452060# Crimson Arclancer#l");
			}
			if (selection == 2) {
				option = 3;
				cm.sendNext("The Balanced Fury, an amazing prize for any Lord of the Night. It requires:\r\n\r\n#i4032015# Tao of Shadows\r\n#i4032016# Tao of Sight\r\n#i4032017# Tao of Harmony\r\n#i4021008# 100 Black Crystals\r\n#i4032005# 30 Typhon Feathers\r\n#i4031138# 150,000 Mesos"); 
			}
		}
		if (option == 1) {
			if (status == 2) {
				if (selection == 0) {
					weap = 1;
					cm.sendSimple("Ah yes, a crossbow silent as the wind and deadly as a raven. Which one of these would you like to upgrade it to?\r\n\r\n#L0##i1462053# Night Raven's Eye#l\r\n#L1##i1462054# Dawn Raven's Eye#l\r\n#L2##i1462055# Dusk Raven's Eye#l");
				}
				if (selection == 1) {
					weap = 2;
					cm.sendSimple("Ah yes, a dagger silent as the wind and deadly as a raven. Which one of these would you like to upgrade it to?\r\n\r\n#L0##i1332078# Night Raven's Beak#l\r\n#L1##i1332079# Dawn Raven's Beak#l\r\n#L2##i1332080# Dusk Raven's Beak#l");
				}
				if (selection == 2) {
					weap = 3;
					cm.sendSimple("Ah yes, a claw silent as the wind and deadly as a raven. Which one of these would you like to upgrade it to?\r\n\r\n#L0##i1472073# Night Raven's Claw#l\r\n#L1##i1472074# Dawn Raven's Claw#l\r\n#L2##i1472075# Dusk Raven's Claw#l");
				}
				if (selection == 3) {
					weap = 4;
					cm.sendSimple("Ah yes, a sword silent as the wind and deadly as a raven. Which one of these would you like to upgrade it to?\r\n\r\n#L0##i1402049# Night Raven's Wing#l\r\n#L1##i1402050# Dawn Raven's Wing#l\r\n#L2##i1402051# Dusk Raven's Wing#l");
				}
			} else if (status == 3) {
				if (selection == 0) {
					ndd = 1;
					if (weap == 1) {
						weap = 1462053;
					}
					if (weap == 2) {
						weap = 1332078;
					}
					if (weap == 3) {
						weap = 1472073;
					}
					if (weap == 4) {
						weap = 1402049;
					}
					cm.sendNext("Its dark colors easily blend into the black moonless night. A #b#t"+weap+"##k requires:\r\n\r\n#i"+(weap-1)+"# #t"+(weap-1)+"#\r\n#i4032015# Tao of Shadows\r\n#i4005002# 10 Dex Crystals\r\n#i4021008# 30 Black Crystals");
				}
				if (selection == 1) {
					ndd = 2;
					if (weap == 1) {
						weap = 1462054;
					}
					if (weap == 2) {
						weap = 1332079;
					}
					if (weap == 3) {
						weap = 1472074;
					}
					if (weap == 4) {
						weap = 1402050;
					}
					cm.sendNext("Indigo tipped like the sky of the rising sun. A #b#t"+weap+"##k requires:\r\n\r\n#i"+(weap-2)+"# #t"+(weap-2)+"#\r\n#i4032017# Tao of Harmony\r\n#i4005001# 10 Wisdom Crystals\r\n#i4021008# 20 Black Crystals");
				}
				if (selection == 2) {
					ndd = 3;
					if (weap == 1) {
						weap = 1462055;
					}
					if (weap == 2) {
						weap = 1332080;
					}
					if (weap == 3) {
						weap = 1472075;
					}
					if (weap == 4) {
						weap = 1402051;
					}
					cm.sendNext("A bloody red sheen like a midsummer's sunset. A #b#t"+weap+"##k requires:\r\n\r\n#i"+(weap-3)+"# #t"+(weap-3)+"#\r\n#i4032016# Tao of Sight\r\n#i4005000# 5 Power Crystals\r\n#i4021008# 20 Black Crystals");
				}
			} else if (status == 4) {
				cm.sendSimple("I can also make the new weapon stronger for an #i4250002# Advanced Diamond.\r\nWould you like to make a #d#t"+weap+"##k?\r\n\r\n#b#L0#I want one WTHOUT an Advanced Diamond#l\r\n#L1#I want one WITH an Advanced Diamond#l");
			} else if (status == 5) {
				if (cm.canHold(weap) == false) {
					cm.sendOk("You don't have enough room in your inventory.");
					cm.dispose();
				} else if (selection == 1) {
					if (!cm.haveItem(4250002)) {
						cm.sendOk("Please check that you have all the required items.");
						cm.dispose();
						return;
					}
				}
				if (ndd == 1) {
					if (cm.haveItem(weap-1, 1) && cm.haveItem(4032015, 1) && cm.haveItem(4005002, 10) && cm.haveItem(4021008, 30)) {
						cm.gainItem(weap-1, -1, true);
						cm.gainItem(4032015, -1, true);
						cm.gainItem(4005002, -10, true);
						cm.gainItem(4021008, -30, true);
						var slot = cm.getInventory(1).getNextFreeSlot();
						cm.gainItem(weap, 1, true, true);
						if (selection == 1) {
							cm.gainItem(4250002, -1, true);
							var atk = cm.getStat(slot, 6) + 3;
							cm.changeStat(slot, 6, atk);
						}
						cm.sendOk("There you go, enjoy.");
						cm.dispose();
					} else {
					cm.sendOk("Please check that you have all the required items.");
                    cm.dispose();
					}
				} else if (ndd == 2) {
					if (cm.haveItem(weap-2, 1) && cm.haveItem(4032017, 1) && cm.haveItem(4005001, 10) && cm.haveItem(4021008, 20)) {
						cm.gainItem(weap-2, -1, true);
						cm.gainItem(4032017, -1, true);
						cm.gainItem(4005001, -10, true);
						cm.gainItem(4021008, -20, true);
						var slot = cm.getInventory(1).getNextFreeSlot();
						cm.gainItem(weap, 1, true, true);
						if (selection == 1) {
							cm.gainItem(4250002, -1, true);
							var atk = cm.getStat(slot, 6) + 3;
							cm.changeStat(slot, 6, atk);
						}
						cm.sendOk("There you go, enjoy.");
						cm.dispose();
					} else {
					cm.sendOk("Please check that you have all the required items.");
                    cm.dispose();
					}
				} else if (ndd == 3) {
					if (cm.haveItem(weap-3, 1) && cm.haveItem(4032016, 1) && cm.haveItem(4005000, 5) && cm.haveItem(4021008, 20)) {
						cm.gainItem(weap-3, -1, true);
						cm.gainItem(4032016, -1, true);
						cm.gainItem(4005000, -5, true);
						cm.gainItem(4021008, -20, true);
						var slot = cm.getInventory(1).getNextFreeSlot();
						cm.gainItem(weap, 1, true, true);
						if (selection == 1) {
							cm.gainItem(4250002, -1, true);
							var atk = cm.getStat(slot, 6) + 3;
							cm.changeStat(slot, 6, atk);
						}
						cm.sendOk("There you go, enjoy.");
						cm.dispose();
					} else {
					cm.sendOk("Please check that you have all the required items.");
                    cm.dispose();
					}
				}
			}
		}
		if (option == 2) {
			if (status == 2) {
				if (selection == 0) {
					weap = 1382060;
					cm.sendNext("Crafted from crimson wood, it radiates with arcane power. A #bCrimson Arcanon#k requires:\r\n\r\n#i4032016# Tao of Sight\r\n#i4032017# Tao of Harmony\r\n#i4032004# 400 Crimson Wood\r\n#i4032005# 10 Typhon Feathers\r\n#i4032012# 30 Crimson Hearts\r\n#i4005001# 4 Wisdom Crystals");
				}
				if (selection == 1) {
					weap = 1442068;
					cm.sendNext("Crafted from crimson wood, its lust for blood knows no limits. A #bCrimson Arcglaive#k requires:\r\n\r\n#i4032015# Tao of Shadows\r\n#i4032017# Tao of Harmony\r\n#i4032004# 500 Crimson Wood\r\n#i4032005# 40 Typhon Feathers\r\n#i4032012# 20 Crimson Hearts\r\n#i4005000# 4 Power Crystals");
				}
				if (selection == 2) {
					weap = 1452060;
					cm.sendNext("Crafted from crimson wood, its arrows seek the heart of its victim. A #bCrimson Arclancer#k requires:\r\n\r\n#i4032015# Tao of Shadows\r\n#i4032016# Tao of Sight\r\n#i4032004# 300 Crimson Wood\r\n#i4032005# 75 Typhon Feathers\r\n#i4032012# 10 Crimson Hearts\r\n#i4005002# 4 Dex Crystals");
				}
			} else if (status == 3) {
				cm.sendSimple("I can also make the new weapon stronger for an #i4250002# Advanced Diamond.\r\nWould you like to make a #d#t"+weap+"##k?\r\n\r\n#b#L0#I want one WTHOUT an Advanced Diamond#l\r\n#L1#I want one WITH an Advanced Diamond#l");
			} else if (status == 4) {
				if (cm.canHold(weap) == false) {
					cm.sendOk("You don't have enough room in your inventory.");
					cm.dispose();
				} else if (selection == 1) {
					if (!cm.haveItem(4250002)) {
						cm.sendOk("Please check that you have all the required items.");
						cm.dispose();
						return;
					}
				}
				if (weap == 1382060) {
					if (cm.haveItem(4032016, 1) && cm.haveItem(4032017, 1) && cm.haveItem(4032004, 400) && cm.haveItem(4032005, 10) && cm.haveItem(4032012, 30) && cm.haveItem(4005001, 4)) {
						cm.gainItem(4032016, -1, true);
						cm.gainItem(4032017, -1, true);
						cm.gainItem(4032004, -400, true);
						cm.gainItem(4032005, -10, true);
						cm.gainItem(4032012, -30, true);
						cm.gainItem(4005001, -4, true);
						var slot = cm.getInventory(1).getNextFreeSlot();
						cm.gainItem(weap, 1, true, true);
						if (selection == 1) {
							cm.gainItem(4250002, -1, true);
							var atk = cm.getStat(slot, 6) + 3;
							cm.changeStat(slot, 6, atk);
						}
						cm.sendOk("There you go, enjoy.");
						cm.dispose();
					} else {
					cm.sendOk("Please check that you have all the required items.");
                    cm.dispose();
					}
				} else if (weap == 1442068) {
					if (cm.haveItem(4032015, 1) && cm.haveItem(4032017, 1) && cm.haveItem(4032004, 500) && cm.haveItem(4032005, 40) && cm.haveItem(4032012, 20) && cm.haveItem(4005000, 4)) {
						cm.gainItem(4032015, -1, true);
						cm.gainItem(4032017, -1, true);
						cm.gainItem(4032004, -500, true);
						cm.gainItem(4032005, -40, true);
						cm.gainItem(4032012, -20, true);
						cm.gainItem(4005000, -4, true);
						var slot = cm.getInventory(1).getNextFreeSlot();
						cm.gainItem(weap, 1, true, true);
						if (selection == 1) {
							cm.gainItem(4250002, -1, true);
							var atk = cm.getStat(slot, 6) + 3;
							cm.changeStat(slot, 6, atk);
						}
						cm.sendOk("There you go, enjoy.");
						cm.dispose();
					} else {
					cm.sendOk("Please check that you have all the required items.");
                    cm.dispose();
					}
				} else if (weap == 1452060) {
					if (cm.haveItem(4032015, 1) && cm.haveItem(4032016, 1) && cm.haveItem(4032004, 300) && cm.haveItem(4032005, 75) && cm.haveItem(4032012, 10) && cm.haveItem(4005002, 4)) {
						cm.gainItem(4032015, -1, true);
						cm.gainItem(4032016, -1, true);
						cm.gainItem(4032004, -300, true);
						cm.gainItem(4032005, -75, true);
						cm.gainItem(4032012, -10, true);
						cm.gainItem(4005002, -4, true);
						var slot = cm.getInventory(1).getNextFreeSlot();
						cm.gainItem(weap, 1, true, true);
						if (selection == 1) {
							cm.gainItem(4250002, -1, true);
							var atk = cm.getStat(slot, 6) + 3;
							cm.changeStat(slot, 6, atk);
						}
						cm.sendOk("There you go, enjoy.");
						cm.dispose();
					} else {
					cm.sendOk("Please check that you have all the required items.");
                    cm.dispose();
					}
				}
			}
		}
		if (option == 3) {
			if (status == 2) {
				cm.sendYesNo("Would you like to make some #dBalanced Furies#k?");
			} else if (status == 3) {
				if (cm.canHold(2070018) == false) {
					cm.sendOk("You don't have enough room in your inventory.");
					cm.dispose();
				} else if (cm.getMeso() < 150000) {
					cm.sendOk("You don't have enough mesos.");
					cm.dispose();
				} else if (cm.haveItem(4032015, 1) && cm.haveItem(4032016, 1) && cm.haveItem(4032017, 1) && cm.haveItem(4021008, 100) && cm.haveItem(4032005, 30)) {
					cm.gainItem(4032015, -1, true);
					cm.gainItem(4032016, -1, true);
					cm.gainItem(4032017, -1, true);
					cm.gainItem(4021008, -100, true);
					cm.gainItem(4032005, -30, true);
					cm.gainMeso(-150000);
					cm.gainItem(2070018, 1, true);
					cm.dispose();
				} else {
					cm.sendOk("Please check that you have all the required items.");
                    cm.dispose();
				}
			}
		}
	}
}