/**
 * @author: Eric
 * @npc: Lime Balloon - LudiPQ 2nd (was 4th) stage NPC
*/

var status = 0;

function start() {
    status = -1;
    action(1, 0, 0);
}

function action(mode, type, selection) {
	(mode == 1 ? status++ : mode == 0 ? stauts-- : cm.dispose());
	var eim = cm.getPlayer().getEventInstance();
	var stage4status = eim.getProperty("stage4status");
	if (status == 0) {
		if (stage4status == null) {
			if (cm.isPartyLeader()) { // Leader
				var stage4leader = eim.getProperty("stage4leader");
				if (stage4leader == "done") { // not in gms anymore because i just tested this
					if (cm.getMap(922010401).getAllMonsters().size() == 0 && cm.getMap(922010402).getAllMonsters().size() == 0 && cm.getMap(922010403).getAllMonsters().size() == 0 && cm.getMap(922010404).getAllMonsters().size() == 0 && cm.getMap(922010405).getAllMonsters().size() == 0) {
						status = 0;
						cm.sendNext("Congratulations on clearing the quests for this stage. Please use the portal you see over there and move on to the next stage.");
					} else { // Not done yet
						cm.sendNext("In the second stage, the Dimensional Schism has spawned a place of pure darkness. Monsters called #b#o9300008##k have hidden themselves in the darkness. Defeat all of them, and then talk to me to proceed to the next stage.");
						cm.dispose();
					}
				} else {
					cm.sendNext("In the second stage, the Dimensional Schism has spawned a place of pure darkness. Monsters called #b#o9300008##k have hidden themselves in the darkness. Defeat all of them, and then talk to me to proceed to the next stage.");
					eim.setProperty("stage4leader","done");
					cm.dispose();
				}
			} else { // Members
				cm.sendNext("In the second stage, the Dimensional Schism has spawned a place of pure darkness. Monsters called #b#o9300008##k have hidden themselves in the darkness. Defeat all of them, and then talk to me to proceed to the next stage.");
				cm.dispose();
			}
		} else {
			cm.sendNext("Congratulations on clearing the quests for this stage. Please use the portal you see over there and move on to the next stage.");
			cm.dispose();
		}
	} else if (status == 1) {
		if (eim.getProperty("stage4status") == null) {
			cm.removeAll(4001022); // not even dropped in this stage but whatever
			clear(4, eim, cm);
			cm.givePartyExp(3360, eim.getPlayers());
		}
		cm.dispose();
	}
}

function clear(stage, eim, cm) {
    eim.setProperty("stage" + stage.toString() + "status","clear");
    cm.showEffect("quest/party/clear");
    cm.playSound("Party1/Clear");
    cm.environmentChange(2, "gate");
}