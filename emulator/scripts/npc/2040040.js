/*
	Green Balloon - LudiPQ 5th stage NPC
**/

var exp = 3770;
var status = 0;

function start() {
    status = -1;
    action(1, 0, 0);
}

function action(mode, type, selection) {
    var eim = cm.getPlayer().getEventInstance();
    var stage5status = eim.getProperty("stage5status");

    if (stage5status == null) {
	if (cm.isPartyLeader()) { // Leader
	    var stage5leader = eim.getProperty("stage5leader");
	    if (stage5leader == "done") {

		if (cm.haveItem(4001022,24)) { // Clear stage
		    cm.sendNext("Congratulations! You've passed the 5th stage. Hurry on now, to the 6th stage.");
		    cm.removeAll(4001022);
		    clear(5,eim,cm);
		    cm.givePartyExp(exp, eim.getPlayers());
		} else { // Not done yet
		    cm.sendNext("Are you sure you've brought me #r24 Passes of Dimension#k? Please check again.");
		}
		cm.dispose();
	    } else {
		cm.sendOk("Welcome to the 5th stage. Go around, and collect #r24 Passes of Dimension#k from the boxes in the other maps. Once you're done, get your party members to hand all the #rPasses#k to you, then talk to me again.");
		eim.setProperty("stage5leader","done");
		cm.dispose();
	    }
	} else { // Members
	    cm.sendNext("Welcome to the 5th stage. Go around, and collect #rPasses of Dimension#k from the boxes in the other maps. Once you're done, hand all the #rPasses#k to your party leader.");
	    cm.dispose();
	}
    } else {
	cm.sendNext("Congratulations! You've passed the 5th stage. Hurry on now, to the 6th stage.");
	cm.dispose();
    }
}

function clear(stage, eim, cm) {
    eim.setProperty("stage" + stage.toString() + "status","clear");

    cm.showEffect("quest/party/clear");
    cm.playSound("Party1/Clear");
    cm.environmentChange(2, "gate");
}