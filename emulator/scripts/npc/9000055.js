/* 
	* Aramia
	* Henesys fireworks NPC
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
                    if (cm.getMonsterCount(100000200) >= 1) {
                        cm.sendOk("We have collected enough Powder Kegs, the strange man is here!!");
                        cm.dispose();
                    } else {
			cm.sendNext("Hi There~ I'm Aramia. A strange man asked me to gather some Powder Kegs to throw a firework celebration. He sure wanted a lot though, I guess he really wants to light things up! Please get all the Powder Kegs you can.");
                    }
            } else if (status == 1) {
			cm.sendSimple("Each time users collect enough Powder Kegs, we can get some fireworks!\r\n#b#L0#I brought some Powder Kegs.#l\r\n#L1# Please show me the current status on collecting the Powder Kegs.#l");
		} else if (status == 2) {
			var text = (cm.getKegs() >= 2000) ? "We have collected enough Powder Kegs, the strange man is here!" : "If we collect enough, the strange man will show up and start his performance!";
			if (selection == 0) {
				if (cm.getKegs() >= 2000) {
					cm.sendOk(text);
					cm.dispose();
				} else {
					cm.sendGetNumber("Did you bring the Powder Kegs with you? Please give me all of the #bPowder Kegs#k you have. If you give me 500 at a time I'll reward you with a shiny coin. How many are you willing to give me? \r\n#b< Number of Powder Keg in inventory: " + cm.itemQuantity(4001128) + " >#k", 0, 0, 10000);
				}
			} else if (selection == 1) {
				cm.sendOk("Status of the Powder Keg	 Collection:\r\n\r\n#B" + cm.getKegsPercentage() + "#  " + cm.getKegs() + " / 2000\r\n\r\n" + text);
				cm.dispose();
			}
		} else if (status == 3) {
			var num = selection;
			if (num == 0) {
				cm.sendOk("Are you sure you don't have any Powder Kegs to give me?\r\nPlease come back when you have some.");
			} else if (cm.haveItem(4001128, num)) {
				cm.gainItem(4001128, -num, true);
				cm.giveKegs(num);
				cm.gainItem(4031682, Math.floor(num/500), true);
				cm.sendOk("Thank you! Please come back if you get more!");
			} else {
				cm.sendOk("You don't have that many Powder Kegs!");
			}
			cm.dispose();
		}
	}
}