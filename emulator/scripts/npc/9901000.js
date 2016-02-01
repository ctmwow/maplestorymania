/* 
	* NX Remover NPC
	* MaplePhoenix
	* Dante
*/

var status;
var inv;
var slot = Array();
var selected
var item_
var text;

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
			cm.sendSimple("I am the mighty NX remover! Talk to me if you would like me to remove a NX item from your inventory.\r\n\r\n#b#L0#I'd like a NX equip removed.#l\r\n#L1#I'd like a NX item removed.#l");
		} else if (status == 1) {
			if (selection == 0) {
				inv = 1;
			} else if (selection == 1) {
				inv = 5;
			}
			var avail = "";
			for (var i = 1; i <= 96; i++) {
				if (cm.getInventory(inv).getItem(i) != null) {
					var item = cm.getInventory(inv).getItem(i).getItemId();
					if (cm.isCash(item) == true) {
						avail += "#L" + i + "##t" + item + "##l\r\n";
					}
				}
				slot.push(i);
			}
			if (avail == "") {
				txt = (inv == 1) ? "I only remove unequipped items." : "";
				cm.sendOk("There is nothing for me to remove! " + txt);
				cm.dispose();
			} else {
				cm.sendSimple("Which item would you like me to remove?\r\n#b" + avail);
			}
		} else if (status == 2) {
			selected = selection - 1;
			item_ = cm.getInventory(inv).getItem(slot[selected]);
			text = ((item_.getExpiration() < 0) || (item_.getExpiration() == 3439770000000)) ? "#epermanent#n " : "";
			cm.sendYesNo("Removed items #ecannot#n be recovered, they will be #edeleted forever#n!\r\n\r\nAre you sure you want to delete your " + text + "\r\n#b#t" + item_.getItemId() + "##k?");
		} else if (status == 3) {
			cm.sendYesNo("There will be #eno refunds#n!\r\n\r\nAre you double sure you want to delete your " + text + "\r\n#b#t" + item_.getItemId() + "##k?");
		} else if (status == 4) {
			cm.removeFromSlot(inv, slot[selected], 1, true);
			cm.sendOk("It is done.");
			cm.dispose();
		}
	}
}