importPackage(Packages.org.ascnet.leaftown.server.maps);

var map = 390009999;
var status = 0;
var minLevel = 10;
var maxLevel = 200;
var minPlayers = 1;
var maxPlayers = 6;

var PQItems = new Array(4001095, 4001096, 4001097, 4001098, 4001099, 40011000);
var RiceCake = 40001101;
/* Fim */


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
        if (mode == 1) {
            status++;
        } else {
            cm.dispose();
			return;
		}
		if (cm.getPlayer().getMapId() == 100000200){ 
		if (cm.getParty() == null) {
                        cm.sendOk("Você não está em grupo.");
                        cm.dispose();
                        return;
                    }
            if (!cm.getParty().getLeader().getName() == cm.getPlayer().getName()) {
                if (status == 0) {
                    cm.sendNext("Olá como vai? Sou Tory. Este lugar está envolvido com a misteriosa aura da lua cheia, e ninguém pode entrar aqui por conta própria.");
                } else if (status == 1) {
                    cm.sendSimple("Se você deseja entrar, o líder do seu grupo deve falar comigo. Fale com o líder do seu grupo sobre isto.");
                } 
            } else {
                if (status == 0) {
                    cm.sendNext("Sou Tory. Aqui dentro há uma bela colina onde as prímulas floram. Há um tigre que vive na colina, Growlie, e ele aparenta estar procurando por algo para comer.");
                } else if (status == 1) {
                    cm.sendSimple("Você gostaria de ir até a Colina das Prímulas e juntar forças com os membros do seu grupo para ajudar o Growlie?\r\n#b#L0# Sim, eu irei.#l");
                } else if (status == 2) {
                    if (cm.getParty() == null) {
                        cm.sendOk("Você não está em grupo.");
                        cm.dispose();
                        return;
                    } else if (!cm.isPartyLeader()) { // Not Party Leader
                        cm.sendOk("Se você deseja entrar, o líder do seu grupo deve falar comigo.");
                        cm.dispose();
                    } else {
					var party = cm.getParty().getMembers();
					var mapId = cm.getMapId();
					var next = true;
					var levelValid = 0;
					var inMap = 0;
					if (party.size() < minPlayers || party.size() > maxPlayers) 
						next = false;
					else {
						for (var i = 0; i < party.size() && next; i++) {
							if ((party.get(i).getLevel() >= minLevel) && (party.get(i).getLevel() <= maxLevel))
								levelValid += 1;
							if (party.get(i).getMapId() == mapId)
								inMap += 1;
						}
						if (levelValid < minPlayers || inMap < minPlayers)
							next = false;
					}  if (next) {
		                  var em = cm.getEventManager("HenesysPQ");
	                          if (em == null) {
	                          cm.sendOk("A missão de grupo está indisponível no momento.");
		                  } else {
		                  var prop = em.getProperty("state");
		                  if (prop.equals("0") || prop == null) {
                                cm.getClient().getChannelServer().getMapFactory().destroyMap(910010000);
								em.startInstance(cm.getParty(),cm.getPlayer().getMap());
                                party = cm.getPlayer().getEventInstance().getPlayers();
								cm.dispose();
		                    } else {
		            	      cm.sendOk("Existe outro grupo dentro da missão de grupo.");
                                      cm.dispose();
		                 }
		               }
	                 } else {
		    cm.sendOk("Seu grupo aparenta não ter os requisitos mínimos, verifique se seu grupo possui #rentre "+ minPlayers +" e "+ maxPlayers +" membros acima do nível 10#k e fale comigo novamente.");
                    cm.dispose();
				}
            }
        }
    }
     } else if(cm.getPlayer().getMapId() == 910010400){
              if (status == 0){
                  /*if (cm.getHPQClear() >= 10) {
                      for (var i = 0; i < PQItems.length; i++) {
                          cm.removeAll(PQItems[i]);
                      }
                      cm.gainItem(1002798, 1);
                      //cm.HPQClear(-10);
                      cm.warp(910000022, 0);
                      cm.playerMessage("Você ganhou uma recompensa por concluir a missão de grupo 10 vezes.");
                      cm.dispose();                     
                  } else {*/
               for (var i = 0; i < PQItems.length; i++) {
				cm.removeAll(PQItems[i]);
                            }
                cm.warp(100000200);
                //cm.playerMessage("Você foi levado para o Mercado <22>.");
                cm.dispose();
            }
        } else if (cm.getPlayer().getMapId() == 910010100) {
            if (status==0) {
                /*if (cm.getHPQClear() >= 10){
                    cm.sendYesNo("Parabéns por concluir a missão de grupo de Henesys 10 vezes, como recompensa estarei lhe dando um #t1002798# como recompensa! Deseja voltar para o #rParque de Henesys#k?");
                } else {*/
                cm.sendYesNo("Você deseja voltar para o #rParque de Henesys#k?");
            } else if (status == 1) {
                /*if (cm.getHPQClear() >= 10){
                    for (var i = 0; i < PQItems.length; i++) {
                        cm.removeAll(PQItems[i]);
                    } 
                cm.gainItem(1002798, 1);
                cm.HPQClear(-10);
                cm.warp(910000022, 0);
                cm.dispose();
                } else {*/
               for (var i = 0; i < PQItems.length; i++) {
				cm.removeAll(PQItems[i]);
                            } 
                cm.warp(100000200);
                cm.dispose();
            }
        }
    }
}
					
function checkLevelsAndMap(lowestlevel, highestlevel) {
    var party = cm.getParty().getMembers();
    var mapId = cm.getMapId();
    var valid = 0;
    var inMap = 0;

    var it = party.iterator();
    while (it.hasNext()) {
        var cPlayer = it.next();
        if (!(cPlayer.getLevel() >= lowestlevel && cPlayer.getLevel() <= highestlevel) && cPlayer.getJobId() != 900) {
            valid = 1;
        }
        if (cPlayer.getMapid() != mapId) {
            valid = 2;
        }
    }
    return valid;
}
		
                