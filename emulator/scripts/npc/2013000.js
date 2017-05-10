/*
 * Wonky The Fairy
 * Orbis PQ
 * Zero
 */

var status = 0;
var mySelection = -1;
var foodSelection = -1;
var minLevel = 51;
var maxLevel = 200;
var minPlayers = 1;
var maxPlayers = 6;
var blessingArray = Array(2022090, 2022091, 2022092, 2022093); //Blessing IDs
var warriors = Array(100, 110, 111, 112, 120, 121, 122, 130, 131, 132);
var mages = Array(200, 210, 211, 212, 220, 221, 222, 230, 231, 232);
var rangerz = Array(300, 310, 311, 312, 320, 321, 322);
var theifs = Array(400, 410, 411, 412, 420, 421, 422);
var priatez = Array(500, 510, 511, 512, 520, 521, 522);
var numOfWarriors = 0;
var numOfMages = 0;
var numOfBowmans = 0;
var numOfThiefs = 0;
var numOfPirates = 0;

function start() {
    status = -1;
    action(1, 0, 0);
}

function action(mode, type, selection) {
    if (mode == -1) {
	cm.dispose();
    } else {
	if (mode == 0 && status == 0) {
            cm.dispose();
            return;
        }
        if (mode == 1)
            status++;
        else
            status--;
	if (cm.getPlayer().getMapId() == 200080101) {
            if (status == 0) {
                cm.sendSimple("Hello I am Wonky the Fairy. What would you like to do today?#b\r\n#L0#Enter OrbisPQ.#l");
            } else if (status == 1 && selection == 0) {
                if (cm.getParty() == null) {
                    cm.sendOk("Please create a party.");
                    cm.dispose();
                    return;
                }
                if (!cm.isPartyLeader()) { 
                    cm.sendOk("Please ask your party leader to talk to me.");
                    cm.dispose();
                } else { 
                    var party = cm.getParty().getMembers();
                    var inMap = cm.partyMembersInMap();
                    var next = true;
                    var levelValid = 0;
                    
                    if (party.size() < minPlayers || party.size() > maxPlayers) {
                        next = false;
                    } else {
                        for (var i = 0; i < party.size() && next; i++) {
                            if ((party.get(i).getLevel() >= minLevel) && (party.get(i).getLevel() <= maxLevel))
                                levelValid += 1; inMap += 1;
                            if (party.get(i).getJobId() == 100 || party.get(i).getJobId() == 110 || party.get(i).getJobId() == 111 || party.get(i).getJobId() == 112 ||
                                party.get(i).getJobId() == 120 || party.get(i).getJobId() == 121 || party.get(i).getJobId() == 122 ||  
                                party.get(i).getJobId() == 130 || party.get(i).getJobId() == 131 || party.get(i).getJobId() == 132) { 
                                numOfWarriors += 1;
                            }
                            if (party.get(i).getJobId() == 200 || party.get(i).getJobId() == 210 || party.get(i).getJobId() == 211 || party.get(i).getJobId() == 212 ||
                                party.get(i).getJobId() == 220 || party.get(i).getJobId() == 221 || party.get(i).getJobId() == 212 ||
                                party.get(i).getJobId() == 230 || party.get(i).getJobId() == 231 || party.get(i).getJobId() == 232) {
                                numOfMages += 1;
                            }
                            if (party.get(i).getJobId() == 300 || party.get(i).getJobId() == 310 || party.get(i).getJobId() == 311 || 
                                party.get(i).getJobId() == 320 || party.get(i).getJobId() == 321) {
                                numOfBowmans += 1;
                            }
                            if (party.get(i).getJobId() == 400 || party.get(i).getJobId() == 410 || party.get(i).getJobId() == 411 || party.get(i).getJobId() == 412 ||
                                party.get(i).getJobId() == 420 || party.get(i).getJobId() == 421 || party.get(i).getJobId() == 422) {
                                numOfThiefs += 1;
                            }
                            if (party.get(i).getJobId() == 500 || party.get(i).getJobId() == 510 || party.get(i).getJobId() == 511 || party.get(i).getJobId() == 512 ||
                                party.get(i).getJobId() == 520 || party.get(i).getJobId() == 521 || party.get(i).getJobId() == 522) {
                                numOfPirates += 1;
                            }
			}
                        if (levelValid < party.size() || inMap < party.size())
                            next = false;
                    } 	//Slate says nothing here, just warps you in.

                    if (next) {
                        var em = cm.getEventManager("OrbisPQ");
			if (em == null) {
                            cm.sendOk("Orbis PQ is not available at the moment, please contact any GM for more infomations.");
                            cm.dispose();
			} else { // start orbis pq here?
                            em.startInstance(cm.getParty(), cm.getPlayer().getMap());
                            if ((numOfWarriors >= 1 && numOfMages >= 1 && numOfBowmans >= 1 && numOfThiefs >= 1 && numOfPirates >= 1)) {
                                for (var ii = 0; ii < party.size(); ii++) {
                                    var randmm = Math.floor(Math.random() * blessingArray.length);
                                    var buffToGivee = blessingArray[randmm];
                                    party.get(ii).giveItemBuff(buffToGivee);
								}
                            }
                            cm.dispose();
                        }
                        cm.dispose();
                    } else {
                        cm.sendOk("Please check your party level ranges between level 51 ~ 200 & ensure that everyone is in the same map now.");
			cm.dispose();
                    }
		}
            }
        } else if (cm.getPlayer().getMapId() == 920010000) {
            if (status == 0) {
                cm.sendYesNo("Would you like to exit the Party Quest?\r\nYou will have to start again next time...");
            } else if (status == 1) {
                var eim = cm.getPlayer().getEventInstance();
		var party = cm.getPlayer().getEventInstance().getPlayers();
		var exitMapz = cm.getPlayer().getClient().getChannelServer().getMapFactory().getMap(920011200);
		for (var outt = 0; outt<party.size(); outt++) {
                    party.get(outt).changeMap(exitMapz, exitMapz.getPortal(0));
                    eim.unregisterPlayer(party.get(outt));
                }
                cm.dispose();
            }
        }
    }
}					
					
