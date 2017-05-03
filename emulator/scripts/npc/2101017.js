/*2101014.js - Lobby and Entrance
 * @author Jvlaple
 * For Jvlaple's AriantPQ
 */
importPackage(java.lang);
importPackage(Packages.org.ascnet.leaftown.server);
 
 
var status = 0;
var toBan = -1;
var choice;
var arena;
var arenaName;
var type;
var map;

function start() {
	status = -1;
	action(1, 0, 0);
}

function action(mode, type, selection) {
	if (mode == -1) {
		cm.dispose();
	} else {
		if (mode == 0) {
			cm.dispose();
			return;
		}
		if (mode == 1) {
			status++;
		} else {
			status--;
		} 
	
	if (cm.getPlayer().getMapId() == 980010100 || cm.getPlayer().getMapId() == 980010200 || cm.getPlayer().getMapId() == 980010300) {
			if (status == 0) {
				switch (cm.getPlayer().getMapId()) {
					case 980010100:
						arena = MapleSquadType.ARIANT1;
						break;
					case 980010200:
						arena = MapleSquadType.ARIANT2;
						break;
					case 980010300:
						arena = MapleSquadType.ARIANT3;
						break;
					default :
						return;
				}
				if (cm.checkSquadLeader(arena)) {
					cm.sendSimple("O que você gostaria de fazer?#b\r\n\r\n#L1#Ver registro atual da arena!#l\r\n#L2#Comece a luta!#l\r\n#L3#Sair desta arena!#l");
                    status = 19;
				} else if (cm.isSquadMember(arena)) {
					var noOfChars = cm.numSquadMembers(arena);
                    var toSend = "Você tem atualmente essas pessoas em sua arena :\r\n#b";
					for (var i = 1; i <= noOfChars; i++) {
						toSend += "\r\n#L" + i + "#" + cm.getSquadMember(arena, i - 1).getName() + "#l";
					}
					cm.sendSimple(toSend);
					cm.dispose();
				} else {
					cm.sendOk("O que aconteceu?");
					cm.dispose();
				}
			} else if (status == 20) {
				switch (cm.getPlayer().getMapId()) {
						case 980010100:
							arena = MapleSquadType.ARIANT1;
							arenaName = "AriantPQ1";
							break;
						case 980010200:
							arena = MapleSquadType.ARIANT2;
							arenaName = "AriantPQ2";
							break;
						case 980010300:
							arena = MapleSquadType.ARIANT3;
							arenaName = "AriantPQ3";
							break;
						default :
							return;
					}
				if (selection == 1) {
					var noOfChars = cm.numSquadMembers(arena);
                    var toSend = "Voc tem atualmente essas pessoas em sua arena :\r\n#b";
					for (var i = 1; i <= noOfChars; i++) {
						toSend += "\r\n#L" + i + "#" + cm.getSquadMember(arena, i - 1).getName() + "#l";
					}
					cm.sendSimple(toSend);
					cm.dispose();
				} else if (selection == 2) {
					if (cm.numSquadMembers(arena) < 2 && !cm.getPlayer().isGM()) {
						cm.sendOk("Eu só posso deixá-lo lutar quando voce tem duas ou mais pessoas.");
						cm.dispose();
					} else {
						var em = cm.getEventManager(arenaName);
						if (em == null) {
							cm.sendOk("Evento não disponível no momento.");
							cm.dispose();
						}
						else {
							cm.setSquadState(arena, 2);
							em.startInstance(cm.getMapleSquad(arena), cm.getPlayer().getMap());
						}
						cm.dispose();
					}
				} else if (selection == 3) {
					cm.mapMessage("O líder da Arena saiu.");
					cm.warpSquadMembers(arena, 980010000)
					var squad = cm.getPlayer().getClient().getChannelServer().getMapleSquad(arena);
					cm.getPlayer().getClient().getChannelServer().removeMapleSquad(squad, arena);
					cm.dispose();
				}
			} 
                    } else if (cm.getPlayer().getMapId() == 980010101 || cm.getPlayer().getMapId() == 980010201 || cm.getPlayer().getMapId() == 980010301) {
			var eim = cm.getPlayer().getEventInstance();
			if (status == 0) {
				var gotTheBombs = eim.getProperty("gotBomb" + cm.getPlayer().getId());
				if (gotTheBombs != null) {
                                    cm.sendOk("Eu já lhe dei as bombas, por favor, mate os #eEscorpioes#n para conseguir mais na próxima!");
					cm.dispose();
				} else {
					cm.sendOk("Eu lhe dei (5) #b#eBombas#k#n e (50) #b#eRochas Elementais#k#n.\r\nUse as rochas elementais para capturar os escorpiões para Sra.#r#eSpirit Jewels#k#n!");
					eim.setProperty("gotBomb" + cm.getPlayer().getId(), "got");
					cm.gainItem(2270002, 50);
					cm.gainItem(2100067, 5);
					cm.dispose();
				}
			}
		} 
	}
}
