/*
	* Phoenix Points NPC
	* MaplePhoenix
	* Dante
*/
var status;
var option;
var ppoption;
var soption;
var moption;
var inv;
var notenoughmm = "I'm afraid you don't have enough Maple Marbles. Please come back when you have ";
var notenoughpp = "I'm afraid you don't have enough Phoenix Points. Please come back when you have ";
var slot = Array();
var selected;
var str;
var dex;
var int_;
var luk;
var hp;
var mp;
var speed;
var jump;
var acc;
var avoid;

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
			cm.sendSimple("Hi, I exchange Phoenix Points here at MaplePhoenix. You can get them by doing special things in game, or donating on our website!\r\n\r\n#b#L0#I'd like to to exchange PP#l\r\n#L1#I'd like to have Everlasting NX Clothes#l\r\n#L2#I'd like to have an Eternal Pet#l\r\n#L3#I'd like to have a Permanent Store#l\r\n#L4#Tell me more about Phoenix Points#l");
		} else if (status == 1) {
			if (selection == 0) {
				option = "pp";
				cm.sendSimple("Okay, these are the things you can currently exchange Phoenix Points for. You currently have #b" + cm.getPP() + " #kPhoenix Points.\r\n\r\n#b#L0#5 PP for 20,000 NX#l\r\n#L1#5 PP for 5 Maple Marbles#l\r\n#L2#10 PP for 11 Maple Marbles#l\r\n#L3#10 PP for a Donor Medal#l\r\n#L4#20 PP for 25 Maple Marbles#l\r\n#L5#25 PP for 110,000 NX#l");
				// has name change option (for later) cm.sendSimple("Okay, these are the things you can currently exchange Phoenix Points for.\r\n\r\n#b#L0#5 PP for 20,000 NX#l\r\n#L1#5 PP for 5 Maple Marbles#l\r\n#L2#10 PP for a Name Change#l\r\n#L3#10 PP for 11 Maple Marbles#l\r\n#L4#20 PP for 25 Maple Marbles#l\r\n#L5#25 PP for 110,000 NX#l");
			} else if (selection == 1) {
				option = "equips";
				cm.sendYesNo("I've been given the ability to remove the expiration date on NX Clothes and make them last forever! The catch is I need a Maple Marble in order to make it work.\r\n\r\n#i4031456# 1x Maple Marble\r\n\r\nWould you like me to remove an expiration date?");
			} else if (selection == 2) {
				option = "pets";
				cm.sendYesNo("I've been given the ability make pets last forever! Well more like the year 2078, but that's kind of like forever right? The catch is I need two Maple Marbles in order to make it work.\r\n\r\n#i4031456# 2x Maple Marbles\r\n\r\nWould you like me to immortalize your pet?");
			} else if (selection == 3) {
				option = "store";
				cm.sendYesNo("I've been given the authority to give out permanent merchant houses for the Free Market. I need five Maple Marbles per merchant, and please remember that the permits are non-transferable.\r\n\r\n#i4031456# 5x Maple Marbles\r\n\r\nWould you like a permanent store permit?");
			} else if (selection == 4) {
				cm.sendOk("Phoenix Points are a premium currency players can exchange for perks in game. You can obtain Phoenix points either by donating on our website, or getting them in game by doing a few different things. Please check our forum for ways to get Phoenix Points by playing!");
				cm.dispose();
			}
		} else if (status == 2) {
			if (option == "pp") {
				if (selection == 0) {
					ppoption = "20kNX";
					cm.sendYesNo("So you want to exchange 5 Phoenix Points for 20,000 NX?");
				} else if (selection == 1) {
					ppoption = "5marbles";
					cm.sendYesNo("So you want to exchange 5 Phoenix Points for 5 Maple Marbles?");
				// } else if (selection == 2) {
					// ppoption = "namechange";
					// cm.sendYesNo("So you want to exchange 10 Phoenix Points for a Name Change?");
				} else if (selection == 2) {
					ppoption = "11marbles";
					cm.sendYesNo("So you want to exchange 10 Phoenix Points for 11 Maple Marbles?");
				} else if (selection == 3) {
					ppoption = "medal";
					cm.sendSimple("Here is a list of Donation Medals we current have. Please note that they expire after 30 days.\r\n\r\n#b#L0#Perion Donation Medal#l\r\n#L1#Ellinia Donation Medal#l\r\n#L2#Henesys Donation Medal#l\r\n#L3#Kerning City Donation Medal#l\r\n#L4#Nautilus Harbour Donation Medal#l\r\n#L5#Sleepywood Donation Medal#l");
				} else if (selection == 4) {
					ppoption = "25marbles";
					cm.sendYesNo("So you want to exchange 20 Phoenix Points for 25 Maple Marbles?");
				} else if (selection == 5) {
					ppoption = "110kNX";
					cm.sendYesNo("So you want to exchange 25 Phoenix Points for 110,000 NX?");
				}
			} else if (option == "equips") {
				inv = 1;
				var avail = "";
				for (var i = 1; i <= 96; i++) {
					if (cm.getInventory(inv).getItem(i) != null) {
						var item = cm.getInventory(inv).getItem(i).getItemId();
						if (cm.isCash(item) == true && cm.getInventory(inv).getItem(i).getExpiration() > 0) {
							avail += "#L" + i + "##t" + item + "##l\r\n";
						}
					}
					slot.push(i);
				}
				if (avail == "") {
					cm.sendOk("There's nothing to remove the expiration date from! Make sure you unequip the item you want to last forever.");
					cm.dispose();
				} else {
					cm.sendSimple("Which item?\r\n#b" + avail);
				}
			} else if (option == "pets") {
				inv = 5;
				var avail = "";
				for (var i = 1; i <= 96; i++) {
					if (cm.getInventory(inv).getItem(i) != null) {
						var item = cm.getInventory(inv).getItem(i).getItemId();
						if (Math.floor(item / 1000) == 5000 && cm.getInventory(inv).getItem(i).getExpiration() > 0 && cm.getInventory(inv).getItem(i).getExpiration() < 3439770000000) {
							avail += "#L" + i + "##t" + item + "##l\r\n";
						}
					}
					slot.push(i);
				}
				if (avail == "") {
					cm.sendOk("You don't have any mortal pets!");
					cm.dispose();
				} else {
					cm.sendSimple("Which pet?\r\n#b" + avail);
				}
			} else if (option == "store") {
				cm.sendSimple("These are the permanent stores we offer, which one would you like?\r\n\r\n#b#L0##i5030000# Mushroom House Elf#l\r\n#L1##i5030002# Teddy Bear Clerk#l\r\n#L2##i5030004# The Robot Stand#l\r\n#L3##i5030008# Homely Coffeehouse#l\r\n#L4##i5030010# Granny's Food Stand#l\r\n#L5##i5030012# Tiki Torch Store#l");
			}
		} else if (status == 3) {
			var text = "Here is your requested reward, thanks for helping support our server."
			if (option == "equips" || option == "pets") {
				selected = selection - 1;
				cm.sendYesNo("So you wish me to remove the expiration date from your\r\n#b#t" + cm.getInventory(inv).getItem(slot[selected]).getItemId() + "##k?");
			} else if (option == "store") {
				if (selection == 0) {
					soption = 5030000;
					cm.sendYesNo("So you would like a permanent #i5030000# Mushroom House Elf for 5 Maple Marbles?");
				} else if (selection == 1) {
					soption = 5030002;
					cm.sendYesNo("So you would like a permanent #i5030002# Teddy Bear Clerk for 5 Maple Marbles?");
				} else if (selection == 2) {
					soption = 5030004;
					cm.sendYesNo("So you would like a permanent #i5030004# Robot Stand for 5 Maple Marbles?");
				} else if (selection == 3) {
					soption = 5030008;
					cm.sendYesNo("So you would like a permanent #i5030008# Homely Coffeehouse for 5 Maple Marbles?");
				} else if (selection == 4) {
					soption = 5030010;
					cm.sendYesNo("So you would like a permanent #i5030010# Granny's Food Stand for 5 Maple Marbles?");
				} else if (selection == 5) {
					soption = 5030012;
					cm.sendYesNo("So you would like a permanent #i5030012# Tiki Torch Store for 5 Maple Marbles?");
				}
			} else if (ppoption == "20kNX") {
				if (cm.getPP() < 5) {
					cm.sendOk(notenoughpp + "5.");
					cm.dispose();
				} else {
					cm.gainPP(-5);
					cm.message("You have lost 5 Phoenix Points");
					cm.gainCash(4, 20000);
					cm.message("You have gained 20,000 NX");
					cm.sendOk(text);
					cm.dispose();
				}
			} else if (ppoption == "110kNX") {
				if (cm.getPP() < 25) {
					cm.sendOk(notenoughpp + "25.");
					cm.dispose();
				} else {
					cm.gainPP(-25);
					cm.message("You have lost 25 Phoenix Points");
					cm.gainCash(4, 110000);
					cm.message("You have gained 110,000 NX");
					cm.sendOk(text);
					cm.dispose();
				}
			// } else if (ppoption == "namechange") {
				// -10 PP
				// cm.sendOk("bleh.");
				// cm.sendOk(text);
				// cm.dispose();
			} else if (ppoption == "5marbles") {
				if (cm.getPP() < 5) {
					cm.sendOk(notenoughpp + "5.");
					cm.dispose();
				} else {
					cm.gainPP(-5);
					cm.message("You have lost 5 Phoenix Points");
					cm.gainItem(4031456, 5, true);
					cm.sendOk(text);
					cm.dispose();
				}
			} else if (ppoption == "11marbles") {
				if (cm.getPP() < 10) {
					cm.sendOk(notenoughpp + "10.");
					cm.dispose();
				} else {
					cm.gainPP(-10);
					cm.message("You have lost 10 Phoenix Points");
					cm.gainItem(4031456, 11, true);
					cm.sendOk(text);
					cm.dispose();
				}
			} else if (ppoption == "25marbles") {
				if (cm.getPP() < 20) {
					cm.sendOk(notenoughpp + "20.");
					cm.dispose();
				} else {
					cm.gainPP(-20);
					cm.message("You have lost 20 Phoenix Points");
					cm.gainItem(4031456, 25, true);
					cm.sendOk(text);
					cm.dispose();
				}
			} else if (ppoption == "medal") {
				if (selection == 0) {
					moption = 1142016;
					str = 10;
					dex = 10;
					int_ = 5;
					luk = 5;
					hp = 500;
					mp = 500;
					speed = 10;
					jump = 10;
					acc = 10;
					avoid = 0;
					cm.sendYesNo("This medal is designed for Warriors. It gives the following stats:\r\n\r\n+ 10 Str & Dex\r\n+ 5 Int & Luk\r\n+ 500 HP & MP\r\n+ 10 Speed & Jump\r\n+ 10 Accuracy\r\n\r\nDo you want to exchange 10 Phoenix Points for this medal?");
				}
				if (selection == 1) {
					moption = 1142015;
					str = 5;
					dex = 5;
					int_ = 10;
					luk = 10;
					hp = 500;
					mp = 500;
					speed = 10;
					jump = 10;
					acc = 0;
					avoid = 10;
					cm.sendYesNo("This medal is designed for Magicians. It gives the following stats:\r\n\r\n+ 10 Int & Luk\r\n+ 5 Str & Dex\r\n+ 500 HP & MP\r\n+ 10 Speed & Jump\r\n+ 10 Avoid\r\n\r\nDo you want to exchange 10 Phoenix Points for this medal?");
				}
				if (selection == 2) {
					moption = 1142014;
					str = 10;
					dex = 10;
					int_ = 5;
					luk = 5;
					hp = 500;
					mp = 500;
					speed = 10;
					jump = 10;
					acc = 0;
					avoid = 10;
					cm.sendYesNo("This medal is designed for Archers. It gives the following stats:\r\n\r\n+ 10 Str & Dex\r\n+ 5 Int & Luk\r\n+ 500 HP & MP\r\n+ 10 Speed & Jump\r\n+ 10 Avoid\r\n\r\nDo you want to exchange 10 Phoenix Points for this medal?");
				}
				if (selection == 3) {
					moption = 1142017;
					str = 5;
					dex = 10;
					int_ = 5;
					luk = 10;
					hp = 500;
					mp = 500;
					speed = 10;
					jump = 10;
					acc = 0;
					avoid = 10;
					cm.sendYesNo("This medal is designed for Thieves. It gives the following stats:\r\n\r\n+ 10 Dex & Luk\r\n+ 5 Str & Int\r\n+ 500 HP & MP\r\n+ 10 Speed & Jump\r\n+ 10 Avoid\r\n\r\nDo you want to exchange 10 Phoenix Points for this medal?");
				}
				if (selection == 4) {
					moption = 1142019;
					str = 10;
					dex = 10;
					int_ = 5;
					luk = 5;
					hp = 500;
					mp = 500;
					speed = 10;
					jump = 10;
					acc = 5;
					avoid = 5;
					cm.sendYesNo("This medal is designed for Pirates. It gives the following stats:\r\n\r\n+ 10 Str & Dex\r\n+ 5 Int & Luk\r\n+ 500 HP & MP\r\n+ 10 Speed & Jump\r\n+ 5 Accuracy & Avoid\r\n\r\nDo you want to exchange 10 Phoenix Points for this medal?");
				}
				if (selection == 5) {
					moption = 1142018;
					str = 0;
					dex = 0;
					int_ = 0;
					luk = 0;
					hp = 1000;
					mp = 1000;
					speed = 20;
					jump = 20;
					acc = 20;
					avoid = 20;
					cm.sendYesNo("This medal is designed for all Jobs. It gives the following stats:\r\n\r\n+ 1000 HP & MP\r\n+ 20 Speed & Jump\r\n+ 20 Accuracy & Avoid\r\n\r\nDo you want to exchange 10 Phoenix Points for this medal?");
				}
			}
		} else if (status == 4) {
			if (option == "equips") {
				if (cm.itemQuantity(4031456) < 1) {
					cm.sendOk(notenoughmm + "1.");
					cm.dispose();
				} else {
					cm.gainItem(4031456, -1, true);
					cm.getInventory(inv).getItem(slot[selected]).setExpiration(-1);
					cm.forceUpdateItem(cm.getInventory(inv).getItem(slot[selected]));
					cm.sendOk("Here you go, thanks!");
					cm.dispose();
				}
			}
			if (option == "pets") {
				if (cm.itemQuantity(4031456) < 2) {
					cm.sendOk(notenoughmm + "2.");
					cm.dispose();
				} else {
					cm.gainItem(4031456, -2, true);
					cm.getInventory(inv).getItem(slot[selected]).setExpiration(3439770000000);
					cm.forceUpdateItem(cm.getInventory(inv).getItem(slot[selected]));
					cm.sendOk("Okay, all done!");
					cm.dispose();
				}
			}
			if (option == "store") {
				if (cm.itemQuantity(4031456) < 5) {
					cm.sendOk(notenoughmm + "5.");
					cm.dispose();
				} else if (cm.canHold(soption) == false) {
					cm.sendOk("You don't have enough room in your cash inventory!");
					cm.dispose();
				} else {
					cm.gainItem(4031456, -5, true);
					cm.gainItem(soption, 1, true);
					cm.sendOk("Thank you very much. Good luck in the Free Market!");
					cm.dispose();
				}
			}
			if (ppoption == "medal") {
				if (cm.getPP() < 10) {
					cm.sendOk(notenoughpp + "10.");
					cm.dispose();
				} else if (cm.canHold(moption) == false) {
					cm.sendOk("You don't have enough room in your equip inventory!");
					cm.dispose();
				} else if (cm.haveItem(moption) == true) {
					cm.sendOk("You already have a #t" + moption + "#!");
					cm.dispose();
				} else {
					cm.gainPP(-10);
					cm.message("You have lost 10 Phoenix Points");
					cm.gainItem(moption, 1, false, true, 2678400000);
					cm.sendOk("Here you go, thanks for supporting Maple Phoenix!");
					var mslot = 0;
					for (var i = 1; i <= 96; i++) {
						if (cm.getInventory(1).getItem(i) != null) {
							var item = cm.getInventory(1).getItem(i).getItemId();
							if (item == moption) {
								mslot = i;
								break;
							}
						}
					}
					cm.changeStat(mslot, 0, str);
					cm.changeStat(mslot, 1, dex);
					cm.changeStat(mslot, 2, int_);
					cm.changeStat(mslot, 3, luk);
					cm.changeStat(mslot, 4, hp);
					cm.changeStat(mslot, 5, mp);
					cm.changeStat(mslot, 10, acc);
					cm.changeStat(mslot, 11, avoid);
					cm.changeStat(mslot, 13, speed);
					cm.changeStat(mslot, 14, jump);
					cm.dispose();
				}
			}
		}
	}
}