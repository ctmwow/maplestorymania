/*
function start() {
    status = -1;
    action(1, 0, 0);
}

function action(mode, type, selection) {
    if (mode == 1) {
	status++;
    } else {
	if (status == 0) {
	    cm.dispose();
	}
	status--;
    }
    if (status == 0) {
		cm.sendSimple("#b#L3#Head to Lith Harbor#l\r\n#L4#Stay in Maple Island#l");
    } else if (status == 1) {
	if (selection == 3) {
	    cm.warp(104000000);
	}
	cm.dispose();
    }
}
*/
 
function start() {
    cm.sendYesNo("Would you like to skip the tutorial and head straight to Lith Harbor?");
}

function action(mode, type, selection) {
	if (mode == -1) {
		cm.dispose();
	}
	if (mode == 0) {
		cm.sendOk("Enjoy your trip.");
		cm.dispose();
	}
	if (mode == 1) {
	cm.warp(104000000);
		cm.dispose();
	}
}