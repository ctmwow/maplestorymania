/*
NPC: Joko
Author: Belmont
*/

var status; 

function start() { // starts the NPC 
    status = -1; // sets the status of the NPC to -1 
    action(1, 0, 0); // sets the mode to 1, type to 0, and selection to 0 for the NPC 
} // closes off the start function 

function action(mode, type, selection) { // calls what you set above in function start, almost all actions are done here 
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
			cm.sendSimple("Hi, I'm Joko. If you help us eliminate the monsters around here, I can give trade you some items. Just bring me 25 badges from Windraider, Stormbreaker, Firebrand, and Nightshadow.\r\n#L0#I have the badges#l\r\n#L1#I'll go collect some#l");
		} else if (status == 1) {
			if (selection == 0) {
				if (cm.haveItem(4032006, 25) && cm.haveItem (4032007, 25) && cm.haveItem (4032008, 25) && cm.haveItem (4032009, 25)) {
					cm.sendNext("Awesome. Which item do you want from our supply?");
				} else {
					cm.sendOk("You are missing some, come back when you have 25 of each.");
					cm.dispose();
				}
			}
			if (selection == 1) {
				cm.sendOk("I'll be here waiting for you.");
				cm.dispose();
			} 
		} else if (status == 2) {
			cm.sendSimple("\r\n#L0##i1002801# Raven's Ninja Bandana#l\r\n#L1##i1462052# Raven's Eye#l\r\n#L2##i1332077# Raven's Beak#l\r\n#L3##i1472072# Raven's Claw#l\r\n#L4##i1402048# Raven's Wing#l\r\n#L5##i4032015# Tao of Shadow#l\r\n#L6##i4032016# Tao of Sight#l\r\n#L7##i4032017# Tao of Harmony#l\r\n#L8##i4032004# 100 Crimson Wood#l");
		} else if (status == 3) {
			cm.gainItem(4032006, -25, true);
			cm.gainItem(4032007, -25, true);
			cm.gainItem(4032008, -25, true);
			cm.gainItem(4032009, -25, true);
			if (selection == 0) { // Raven's Ninja Bandanna
				cm.gainItem(1002801, 1, true, true);
			}
			if (selection == 1) { // Raven's Eye
				cm.gainItem(1462052, 1, true, true);
			}
			if (selection == 2) { // Raven's Beak
				cm.gainItem(1332077, 1, true, true);
			}
			if (selection == 3) { // Raven's Claw
				cm.gainItem(1472072, 1, true, true);
			}
			if (selection == 4) { // Raven's Wing
				cm.gainItem(1402048, 1, true, true);
			}
			if (selection == 5) { // Tao of Shadow
				cm.gainItem(4032015, 1, true);
			}
			if (selection == 6) { // Tao of Sight
				cm.gainItem(4032016, 1, true);
			}
			if (selection == 7) { // Tao of Harmony
				cm.gainItem(4032017, 1, true);
			}
			if (selection == 8) { // 100 Crimson Wood
				cm.gainItem(4032004, 100, true);
			}
			cm.dispose();
		}
	}
}