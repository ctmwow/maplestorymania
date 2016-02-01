/*
NPC: Shadow Dude
Author: Dante
*/

var status;

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
			cm.sendSimple("In the shadow of death,\r\non a full moon's delight,\r\na crimson rain shall fall,\r\nas my blades take flight.\r\n\r\n#L0#Nice Dragon Khanjar you have there#l");
		} else if (status == 1) {
			cm.sendSimple("It was forged in the blood of a thousand dragons, the Cornian's still shiver at night as they dream of the massacre.\r\n\r\n#L0#Can you help me make one?#l");
		} else if (status == 2) {
			cm.sendNext("Certainly. Go to mountains of the Minar Forest and stain the ground with much Cornian and Wyvern blood. Return when you have collected:\r\n\r\n#i4260006# 20 Advanced Monster Crystal 1\r\n#i4260007# 10 Advanced Monster Crystal 2\r\n#i4000244# 15 Dragon Spirits\r\n #i4000245# 15 Dragon Scales\r\n#i1092050# Khanjar\r\n\r\nIf you lack a Khanjar, I hear that fool Duey put a few in the Gachapon Machine back in Kerning.");
		} else if (status == 3) {
			cm.sendSimple("If you also bring me a Advanced Black Crystal, I can infuse the blade with the power of chaos.\r\nWould you like a #dDragon Khanjar#k?\r\n\r\n#b#L0#Make a normal Dragon Khanjar#l\r\n#L1#Make a Dragon Khanjar with an Advanced Black Crystal#l");
		} else if (status == 4) {
			if (cm.canHold(1092049) == false) {
				cm.sendOk("You don't have enough room in your inventory.");
				cm.dispose();
			} else if (selection == 1) {
				if (!cm.haveItem(4251302)) {
					cm.sendOk("I am no fool. Bring me the required items if you wish to wield a Dragon Khanjar.");
					cm.dispose();
					return;
				}
			}
			if (cm.haveItem(4260006, 20) && cm.haveItem(4260007, 10) && cm.haveItem(4000244, 15) && cm.haveItem(4000245, 15) && cm.haveItem(1092050, 1)) {
				cm.gainItem(4260006, -20, true);
				cm.gainItem(4260007, -10, true);
				cm.gainItem(4000244, -15, true);
				cm.gainItem(4000245, -15, true);
				cm.gainItem(1092050, -1, true);
				var slot = cm.getInventory(1).getNextFreeSlot();
				cm.gainItem(1092049, 1, true);
				if (selection == 1) {
					cm.gainItem(4251302, -1, true);
					var atk = cm.getStat(slot, 6) + ((Math.floor(Math.random() * 2) == 1) ? Math.floor((Math.random() * 3) - 3) : Math.floor((Math.random() * 3) + 1));
					cm.changeStat(slot, 6, atk);
				}
				cm.sendOk("It is done.");
				cm.dispose();
			} else {
			cm.sendOk("I am no fool. Bring me the required items if you wish to wield a Dragon Khanjar.");
			cm.dispose();
			}
		}
	}
}