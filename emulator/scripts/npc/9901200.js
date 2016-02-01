/* 
	* Masked Man
	* Event NPC
	* MaplePhoenix
	* Dante
*/

var status;
var coins;
var itemid;
var quantity = 1;
var expiry = 0;
			var gram = 0;
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
			cm.sendSimple("Salutations. I'm from Magatia looking for a masked associate of mine. He is an alchemist specializing in combustion, and seems rather determined to destroy Henesys. If you would do me the honor of stopping him and retrieving the Golden Maple Leaf he carries, I shall handsomely reward you.\r\n\r\n#b#L0#He's been defeated, I have his Golden Maple Leaf.#l\r\n#L1#I'd like to exchange some Gullivera Coins with you.#l\r\n#L2#I'll keep an eye out for him.#l");
		} else if (status == 1) {
			if (selection == 0) {
				if (cm.haveItem(4001168)) {
					cm.gainItem(4001168, -1 , true);
					cm.gainItem(4031682, 1 , true);
					cm.sendOk("Excellent, such service cannot go unrewarded. Take this Gullivera Coin for your troubles. If you gather enough of them, I'd happily exchange them with you for some magnificent bounty.");
					cm.dispose();
				} else {
					cm.sendOk("Oh dear, this won't do at all. I need some evidence that he has been apprehended. Please come back when you have obtained his Golden Maple Leaf.");
					cm.dispose();
				}
			} else if (selection == 1) {
				cm.sendSimple("Wonderful! Simply Wonderful! I can offer you the following items in exchange for Gullivera Coins.\r\n\r\n#b#L0#1 Coin - 5 Fireworks (20 WAtk / MAtk for 10 minutes)#l\r\n#L1#1 Coin - 1 Day Dreamer Medal#l\r\n#L2#1 Coins - Onyx Apple#l\r\n#L3#2 Coins - Ap Reset#l\r\n#L4#2 Coins - 5 Day Dreamer Medal#l\r\n#L5#2 Coins - Mercury Gloves (NX Gloves)#l\r\n#L6#2 Coins - Mercury Lighning (NX Boots)#l\r\n#L7#2 Coins - Tania Gloves (NX Gloves)#l\r\n#L8#2 Coins - Tania En Fuego (NX Boots)#l\r\n#L31#3 Coins - Wheel of Destiny#l\r\n#L21#3 Coins - Maple Marble#l\r\n#L9#3 Coins - 10 Day Dreamer Medal#l\r\n#L10#3 Coins - Mercury Leather Jacket (Male NX Top)#l\r\n#L11#3 Coins - Mercury Leather Jacket (Female NX Top)#l\r\n#L12#3 Coins - Mercury Washed Jeans (Male NX Bottom)#l\r\n#L13#3 Coins - Mercury Jean Skirt (Female NX Bottom)#l\r\n#L14#3 Coins - Tania Tailored Jacket (Male NX Top)#l\r\n#L15#3 Coins - Tania's Bolero (Female NX Top)#l\r\n#L16#3 Coins - Tania Tartan Pants (Male NX Bottom)#l\r\n#L17#3 Coins - Tania Tartan Skirt (Female NX Bottom)#l\r\n#L18#4 Coins - 15 Day Dreamer Medal#l\r\n#L19#4 Coins - Mercury Cloak (NX Cape)#l\r\n#L20#4 Coins - Tania's Cloak (NX Cape)#l\r\n#L25#5 Coins - Alchemist Boots (NX Boots)#l\r\n#L22#5 Coins - 10 Gachapon Tickets#l\r\n#L23#5 Coins - Mercury Sword (NX Weapon)#l\r\n#L24#5 Coins - Tania's Sword (NX Weapon)#l\r\n#L28#8 Coins - Moon and Star Cushion#l\r\n#L27#8 Coins - Alchemist Overall (NX Overall)#l\r\n#L26#10 Coins - Ring of the Alchemist#l");
			} else if (selection == 2) {
				cm.sendOk("Very good, beware though he's very dangerous and also a skilled conjurer.");
				cm.dispose();
			}
		} else if (status == 2) {

			if (selection == 0) {
				coins = 1;
				itemid = 2022185;
				quantity = 5;
			} else if (selection == 1) {
				coins = 1;
				itemid = 1142085;
				gram = 1;
				expiry = 86400000;
			} else if (selection == 2) {
				coins = 1;
				itemid = 2022179;
				gram = 2;
			} else if (selection == 3) {
				coins = 2;
				itemid = 5050000;
				gram = 2;
			} else if (selection == 4) {
				coins = 2;
				itemid = 1142089;
				gram = 1;
				expiry = 432000000;
			} else if (selection == 5) {
				coins = 2;
				itemid = 1082225;
				expiry = 7776000000;
			} else if (selection == 6) {
				coins = 2;
				itemid = 1072328;
				gram = 1;
				expiry = 7776000000;
			} else if (selection == 7) {
				coins = 2;
				itemid = 1082224;
				expiry = 7776000000;
			} else if (selection == 8) {
				coins = 2;
				itemid = 1072327;
				gram = 1;
				expiry = 7776000000;
			} else if (selection == 9) {
				coins = 3;
				itemid = 1142094;
				gram = 1;
				expiry = 864000000;
			} else if (selection == 10) {
				coins = 3;
				itemid = 1040138;
				gram = 1;
				expiry = 7776000000;
			} else if (selection == 11) {
				coins = 3;
				itemid = 1041139;
				gram = 1;
				expiry = 7776000000;
			} else if (selection == 12) {
				coins = 3;
				itemid = 1060121;
				expiry = 7776000000;
			} else if (selection == 13) {
				coins = 3;
				itemid = 1061142;
				gram = 1;
				expiry = 7776000000;
			} else if (selection == 14) {
				coins = 3;
				itemid = 1040137;
				gram = 1;
				expiry = 7776000000;
			} else if (selection == 15) {
				coins = 3;
				itemid = 1041138;
				gram = 1;
				expiry = 7776000000;
			} else if (selection == 16) {
				coins = 3;
				itemid = 1060120;
				expiry = 7776000000;
			} else if (selection == 17) {
				coins = 3;
				itemid = 1061141;
				gram = 1;
				expiry = 7776000000;
			} else if (selection == 18) {
				coins = 4;
				itemid = 1142099;
				gram = 1;
				expiry = 1296000000;
			} else if (selection == 19) {
				coins = 4;
				itemid = 1102149;
				gram = 1;
				expiry = 7776000000;
			} else if (selection == 20) {
				coins = 4;
				itemid = 1102148;
				gram = 1;
				expiry = 7776000000;
			} else if (selection == 21) {
				coins = 3;
				itemid = 4031456;
				gram = 1;
                         } else if (selection == 31) {
				coins = 3;
				itemid = 5510000;
				gram = 1;
			} else if (selection == 22) {
				coins = 5;
				itemid = 5220000;
				quantity = 10;
			} else if (selection == 23) {
				coins = 5;
				itemid = 1702150;
				gram = 1;
				expiry = 7776000000;
			} else if (selection == 24) {
				coins = 5;
				itemid = 1702149;
				gram = 1;
				expiry = 7776000000;
			} else if (selection == 25) {
				coins = 5;
				itemid = 1072404;
				expiry = 7776000000;
			} else if (selection == 26) {
				coins = 10;
				itemid = 1112400;
				gram = 1;
			} else if (selection == 27) {
				coins = 8;
				itemid = 1052210;
				gram = 2;
				expiry = 7776000000;
			} else if (selection == 28) {
				coins = 8;
				itemid = 3010063;
				gram = 1;
			}
			var t1 = (gram == 0) ? "" : (gram == 1) ? "a" : "an";
			var t2 = (quantity == 1) ? "" : quantity;
			var t3 = (quantity == 10) ? "s" : "";
			var t4 = (coins == 1) ? "" : "s";
			cm.sendYesNo("So you desire " + t1 + " #i" + itemid + "# " + t2 + " #t" + itemid + "#" + t3 + " for " + coins + " Gullivera Coin" + t4 + "?");
		} else if (status == 3) {
			if (cm.itemQuantity(4031682) < coins) {
				cm.sendOk("I may be wearing a mask, but I can still see. Please come back when you have enough coins.");
				cm.dispose();
			} else if (cm.canHold(itemid) == false) {
				cm.sendOk("You don't have enough room in your inventory. Please come back when you have enough room.");
				cm.dispose();
			} else {
				var text = "Thank you again for stopping that madman, a pleasure doing business with you.";
				cm.gainItem(4031682, -coins, true);
				if (expiry != 0) {
					if (cm.haveItem(itemid) == true) {
						cm.sendOk("You already have a #t" + itemid + "#");
						cm.dispose();
					} else {
						cm.gainItem(itemid, quantity, false, true, expiry);
						if ((itemid == 1142085) || (itemid == 1142089) || (itemid == 1142094) || (itemid == 1142099)) {
							var slot = 0;
							for (var i = 1; i <= 96; i++) {
								if (cm.getInventory(1).getItem(i) != null) {
									var item = cm.getInventory(1).getItem(i).getItemId();
									if (item == itemid) {
										slot = i;
										break;
									}
								}
							}
							cm.changeStat(slot, 0, 5);
							cm.changeStat(slot, 1, 5);
							cm.changeStat(slot, 2, 5);
							cm.changeStat(slot, 3, 5);
							cm.changeStat(slot, 6, 3);
							cm.changeStat(slot, 7, 3);
							cm.changeStat(slot, 10, 0);
							cm.changeStat(slot, 11, 0);
							cm.sendOk(text);
						}
						cm.dispose();
					}
				} else {
					cm.gainItem(itemid, quantity, true);
					cm.sendOk(text);
					cm.dispose();
				}
			}
		}
	}
}