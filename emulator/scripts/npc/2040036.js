/**
 * @author: Eric
 * @npc: Red Balloon - LudiPQ 1st stage NPC
*/

var status = 0;

function start() {
    status = -1;
    action(1, 0, 0);
}

function action(mode, type, selection) {
	(mode == 1 ? status++ : mode == 0 ? stauts-- : cm.dispose());
	var eim = cm.getPlayer().getEventInstance();
	var stage1status = eim.getProperty("stage1status");
	if (status == 0) {
		if (stage1status == null) {
			if (cm.isPartyLeader()) { // Leader
				var stage1leader = eim.getProperty("stage1leader");
				if (stage1leader == "done") { // not in gms anymore because i just tested this
					if (cm.haveItem(4001022, 25) && cm.getPlayer().getMap().getAllMonsters().size() == 0) {
						status = 0;
						cm.sendNext("Wow! Congratulations on clearing the quests for this stage.\r\nPlease use the portal you see over there and move on to the next stage. Best of luck to you!");
					} else { // Not done yet
						cm.sendOk("In the first stage, you'll find Ratz and Black Rats from Another Dimension, who are nibbling away at the Dimensional Schism. If you gather up 20 passes that the Ratz and Black Ratz have stolen, I'll open the way to the next stage. Good luck!");
						cm.dispose();
					}
				} else {
					cm.sendNext("In the first stage, you'll find Ratz and Black Rats from Another Dimension, who are nibbling away at the Dimensional Schism. If you gather up 20 passes that the Ratz and Black Ratz have stolen, I'll open the way to the next stage. Good luck!");
					eim.setProperty("stage1leader","done");
					cm.dispose();
				}
			} else { // Members
				cm.sendNext("In the first stage, you'll find Ratz and Black Rats from Another Dimension, who are nibbling away at the Dimensional Schism. If you gather up 20 passes that the Ratz and Black Ratz have stolen, I'll open the way to the next stage. Good luck!");
				cm.dispose();
			}
		} else {
			cm.sendNext("Wow! Congratulations on clearing the quests for this stage.\r\nPlease use the portal you see over there and move on to the next stage. Best of luck to you!");
			cm.dispose();
		}
	} else if (status == 1) {
		if (eim.getProperty("stage1status") == null) {
			cm.removeAll(4001022);
			clear(1, eim, cm);
			cm.givePartyExp(210, eim.getPlayers()); // handled automatically now.
		}
		cm.dispose();
	}
}

function clear(stage, eim, cm) {
    eim.setProperty("stage" + stage.toString() + "status", "clear");
    cm.showEffect("quest/party/clear");
    cm.playSound("Party1/Clear");
	cm.mapMessage(-1, "A portal to the next stage has opened.");
    cm.environmentChange(2, "gate");
}