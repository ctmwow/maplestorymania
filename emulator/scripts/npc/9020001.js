importPackage(Packages.org.ascnet.leaftown.tools);
importPackage(Packages.org.ascnet.leaftown.server.life);
importPackage(java.awt);

var status;
var curMap;
var playerStatus;
var chatState;
var nx = Array(100);
var questions = Array("Tarefa. O número de cupons que você deve coletar é o mesmo número dos pontos de experiência necessários para avançar do #rnível 1 para o nível 2.",
			"Esta é a tarefa. O número de cupons que você deve coletar é o mesmo número do nível necessário para fazer o primeiro avanço na carreira como bruxo.",
			"Esta é a tarefa. O número de cupons que você deve coletar é o mesmo número do nível mínimo de FOR necessária para fazer o primeiro avanço na carreira como guerreiro.",
			"Esta é a tarefa. O número de cupons que você deve coletar é o mesmo número do nível mínimo de INT necessária para fazer o primeiro avanço na carreira como bruxo.",
			"Esta é a tarefa. O número de cupons que você deve coletar é o mesmo número do nível mínimo de DES necessária para fazer o primeiro avanço na carreira como arqueiro.",
			"Esta é a tarefa. O número de cupons que você deve coletar é o mesmo número do nível mínimo de DES necessária para fazer o primeiro avanço na carreira como gatuno.",
			"Esta é a tarefa. O número de cupons que você deve coletar é o mesmo número do nível necessário para fazer o primeiro avanço na carreira como guerreiro.",
			"Esta é a tarefa. O número de cupons que você deve coletar é o mesmo número do nível necessário para fazer o primeiro avanço na carreira como arqueiro.",
			"Esta é a tarefa. O número de cupons que você deve coletar é o mesmo número do nível necessário para fazer o primeiro avanço na carreira como gatuno.");
var qanswers = Array(15, 8, 35, 20, 25, 25, 10, 10, 10);
var party;
var preamble;
var stage2rects = Array(Rectangle(-770,-132,28,178),Rectangle(-733,-337,26,105),Rectangle(-601,-328,29,105),Rectangle(-495,-125,24,165));
var stage2combos = Array(Array(0,1,1,1),Array(1,0,1,1),Array(1,1,0,1),Array(1,1,1,0));
var stage3rects = Array(Rectangle(608,-180,140,50),Rectangle(791,-117,140,45),Rectangle(958,-180,140,50),Rectangle(876,-238,140,45),Rectangle(702,-238,140,45));
var stage3combos = Array(Array(0,0,1,1,1),Array(0,1,0,1,1),Array(0,1,1,0,1),Array(0,1,1,1,0),Array(1,0,0,1,1),Array(1,0,1,0,1),Array(1,0,1,1,0),Array(1,1,0,0,1),Array(1,1,0,1,0),Array(1,1,1,0,0));
var stage4rects = Array(Rectangle(910,-236,35,5),Rectangle(877,-184,35,5),Rectangle(946,-184,35,5),Rectangle(845,-132,35,5),Rectangle(910,-132,35,5),Rectangle(981,-132,35,5));
var stage4combos = Array(Array(0,0,0,1,1,1),Array(0,0,1,0,1,1),Array(0,0,1,1,0,1),Array(0,0,1,1,1,0),Array(0,1,0,0,1,1),Array(0,1,0,1,0,1),Array(0,1,0,1,1,0),Array(0,1,1,0,0,1),Array(0,1,1,0,1,0),Array(0,1,1,1,0,0),Array(1,0,0,0,1,1),Array(1,0,0,1,0,1),Array(1,0,0,1,1,0),Array(1,0,1,0,0,1),Array(1,0,1,0,1,0),Array(1,0,1,1,0,0),Array(1,1,0,0,0,1),Array(1,1,0,0,1,0),Array(1,1,0,1,0,0),Array(1,1,1,0,0,0));
var eye = 9300002;
var necki = 9300000;
var slime = 9300003;
var monsterIds = Array(eye, eye, eye, necki, necki, necki, necki, necki, necki, slime);
var prizeIdScroll = Array(2040502, 2040505,					// Overall DEX and DEF
			2040802,										// Gloves for DEX 
			2040002, 2040402, 2040602);						// Helmet, Topwear and Bottomwear for DEF
var prizeIdUse = Array(2000001, 2000002, 2000003, 2000006,	// Orange, White and Blue Potions and Mana Elixir
			2000004, 2022000, 2022003);						// Elixir, Pure Water and Unagi
var prizeQtyUse = Array(80, 80, 80, 50,
			5, 15, 15);
var prizeIdEquip = Array(1032004, 1032005, 1032009,			// Level 20-25 Earrings
			1032006, 1032007, 1032010,						// Level 30 Earrings
			1032002,										// Level 35 Earring
			1002026, 1002089, 1002090);						// Bamboo Hats
var prizeIdEtc = Array(4010000, 4010001, 4010002, 4010003,	// Mineral Ores
			4010004, 4010005, 4010006,						// Mineral Ores
			4020000, 4020001, 4020002, 4020003,				// Jewel Ores
			4020004, 4020005, 4020006,						// Jewel Ores
			4020007, 4020008, 4003000);						// Diamond and Black Crystal Ores and Screws	
var prizeQtyEtc = Array(15, 15, 15, 15,
			8, 8, 8,
			8, 8, 8, 8,
			8, 8, 8,
			3, 3, 30);
			
function start() {
	status = -1;
	mapId = cm.getPlayer().getMapId();
	if (mapId == 103000800)
		curMap = 1;
	else if (mapId == 103000801)
		curMap = 2;
	else if (mapId == 103000802)
		curMap = 3;
	else if (mapId == 103000803)
		curMap = 4;
	else if (mapId == 103000804)
		curMap = 5;
	playerStatus = cm.isPartyLeader();
	preamble = null;
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
	if (curMap == 1) { 
		if (playerStatus) { 
			if (status == 0) {
				var eim = cm.getPlayer().getEventInstance();
				party = eim.getPlayers();
				preamble = eim.getProperty("leader1stpreamble");
				if (preamble == null) {
					cm.sendNext("Olá. Bem-vindo ao primeiro estágio.  Olhe ao redor e você verá Jacarés andando por aí. Depois de derrotados, eles vão tossir um #bcupom#k. Fora o líder, cada membro do grupo deve falar comigo, ouvir a pergunta e juntar o mesmo número de #bcupons#k da resposta.\r\nEu darei o #bpasse#k ao jogador que coletar o número correto de #bcupons#k. Assim que todos os membros obtiverem os #bpasses#k e entregarem ao líder, este entregará os #bpasses#k para mim, completando o estágio. Quanto mais rápidos os estágios forem completados, mais estágios haverá para desafiar. Por isto, sugiro que dêem conta rápido de tudo. Bem, boa sorte para vocês.");
					eim.setProperty("leader1stpreamble","done");
					cm.dispose();
				} else {
					var complete = eim.getProperty(curMap.toString() + "stageclear");
					if (complete != null) {
						cm.sendNext("Você completou este estágio. Siga para o próximo estágio usando o portal. Cuidado...");
						cm.dispose();
					} else {
						var numpasses = party.size()-1;
						var passes = cm.haveItem(4001008,numpasses);
						var strpasses = "#b" + numpasses.toString() + " passes#k";
						if (!passes) {
							cm.sendNext("Você precisa me entregar o número correto de passes; deve ser o mesmo número de membros do seu grupo menos o líder. Nem mais nem menos. Por favor, verifique se você tem a quantidade correta."); 
							cm.dispose();
						} else {
							cm.sendNext("Você juntou #b" + strpasses + "#k! Parabéns por completar o estágio! Eu vou criar o portal que envia você para o próximo estágio. Há um limite de tempo para chegar lá, apresse-se. Boa sorte para todos vocês!");
							clear(1,eim,cm);
							cm.givePartyExp(100, party);
							cm.gainItem(4001008, -numpasses);
							cm.dispose();
						}
					}
				}
			}
		} else { 
			var eim = cm.getPlayer().getEventInstance();
			pstring = "member1stpreamble" + cm.getPlayer().getId().toString();
			preamble = eim.getProperty(pstring);
			if (status == 0 && preamble == null) {
				var qstring = "member1st" + cm.getPlayer().getId().toString();
				var question = eim.getProperty(qstring);
				if (question == null) {
					var questionNum = Math.floor(Math.random() * questions.length);
					eim.setProperty(qstring, questionNum.toString());
				}
					cm.sendNext("Olá. Bem-vindo ao primeiro estágio.  Olhe ao redor e você verá Jacarés andando por aí. Depois de derrotados, eles vão tossir um #bcupom#k. Fora o líder, cada membro do grupo deve falar comigo, ouvir a pergunta e juntar o mesmo número de #bcupons#k da resposta.\r\nEu darei o #bpasse#k ao jogador que coletar o número correto de #bcupons#k. Assim que todos os membros obtiverem os #bpasses#k e entregarem ao líder, este entregará os #bpasses#k para mim, completando o estágio. Quanto mais rápidos os estágios forem completados, mais estágios haverá para desafiar. Por isto, sugiro que dêem conta rápido de tudo. Bem, boa sorte para vocês.");
			} else if (status == 0) { 
				var complete = eim.getProperty(curMap.toString() + "stageclear");
				var qdone = "member1st" + cm.getPlayer().getId().toString();
				if (complete != null) {
					cm.sendNext("Você completou este estágio. Siga para o próximo estágio usando o portal. Cuidado...");
					cm.dispose();
				} else if (eim.getProperty(qdone) == "done") {
					cm.sendNext( "Uau, você respondeu bem ao meu desafio. Aqui está o passe para o grupo; entregue ao líder." ); 
					cm.dispose();
				} else {
					var qstring = "member1st" + cm.getPlayer().getId().toString();
					var question = parseInt(eim.getProperty(qstring));
					var numcoupons = qanswers[parseInt(eim.getProperty(qstring))];
					var qcorr = cm.haveItem(4001007,(numcoupons+1));
					var enough = false;
					if (!qcorr) { 
						qcorr = cm.haveItem(4001007,numcoupons);
						if (qcorr) { 
							cm.sendNext("Resposta correta! Você acaba de ganhar um #bpasse#k. Por favor, entregue-o para o líder do seu grupo.");
							cm.gainItem(4001007, -numcoupons);
							cm.gainItem(4001008, 1);
							eim.setProperty(qdone,"done");
							enough = true;
						}
					}
					if (!enough) {
						cm.sendNext("Resposta incorreta. Só posso entregar o passe se você coletar o número de #bcupons#k sugerido pela resposta à pergunta. Vou repetir a pergunta:\r\n" + questions[question]);
					}
					cm.dispose();
				}
			} else if (status == 1) {
				if (preamble == null) {
					var qstring = "member1st" + cm.getPlayer().getId().toString();
					var question = parseInt(eim.getProperty(qstring));
					cm.sendNextPrev(questions[question]);
				} else { 
					cm.dispose();
				}
			} else if (status == 2) { 
				eim.setProperty(pstring,"done");
				cm.dispose();
			} else { 
				eim.setProperty(pstring,"done");
				cm.dispose();
			}
		} 
	} else if (2 <= curMap && 4 >= curMap) {
		rectanglestages(cm);
	} else if (curMap == 5) {
		var eim = cm.getPlayer().getEventInstance();
		var stage5done = eim.getProperty("5stageclear");
		if (stage5done == null) {
			if (playerStatus) { 
				var map = eim.getMapInstance(cm.getPlayer().getMapId());
				var passes = cm.haveItem(4001008,10);
                                var randomnx = Math.floor(Math.random() * nx.length); //variáveis são legais mas esta não funciona
				if (passes) {
					cm.sendNext("Aqui está o portal que leva ao último estágio de bônus. É um estágio que permite derrotar monstros comuns um pouco mais facilmente. Você terá um tempo limite para derrotar o máximo possível deles, mas poderá sair do estágio quando quiser falando com o NPC. Mais uma vez, parabéns por completar todos os estágios. Cuidado..."); 
					party = eim.getPlayers();
					cm.gainItem(4001008, -10);
					clear(5,eim,cm);
					cm.givePartyExp(1500, party);
                    cm.givePartyNX(300, party);
                                      //  cm.givePartyQPoints(20, party);
					cm.dispose();
				} else { 
					cm.sendNext("Olá. Bem-vindo ao 5º estágio final. Ande pelo mapa e você poderá ver alguns Monstros Chefes. Derrote todos e junte 10 #bpasses#k para mim. Obtido o seu passe, o líder do seu grupo vai juntá-los e me entregar quanto tiver todos os 10. Os monstros podem parecer familiares, mas eles são muito mais fortes do que você pensa. Por isso, tenha cuidado. Boa sorte!"); 				
				}	
				cm.dispose();
			} else { 
				cm.sendNext("Olá. Bem-vindo ao 5º estágio final. Ande pelo mapa e você poderá ver alguns Monstros Chefes. Derrote todos e junte 10 #bpasses#k para mim. Obtido o seu passe, o líder do seu grupo vai juntá-los e me entregar quanto tiver todos os 10. Os monstros podem parecer familiares, mas eles são muito mais fortes do que você pensa. Por isso, tenha cuidado. Boa sorte!"); 				
				cm.dispose();
			}
		} else { 
			if (status == 0) {
				cm.sendNext("Incrível! Você completou todos os estágios para chegar até aqui. Aqui está uma pequena recompensa pelo trabalho bem-feito. Mas, antes de aceitar, verifique se você possui slots disponíveis nos inventários de uso e etc." ); 
			}
			if (status == 1) {
				getPrize(eim,cm);
				cm.dispose();
			}
		}
	} else { 
		cm.sendNext("REPORTE ESTE CÓDIGO NO FÓRUM: 9020001");
		cm.dispose();
		}
	}
}

function clear(stage, eim, cm) {
	eim.setProperty(stage.toString() + "stageclear","true");
	var packetef = MaplePacketCreator.showEffect("quest/party/clear");
	var packetsnd = MaplePacketCreator.playSound("Party1/Clear");
	var packetglow = MaplePacketCreator.environmentChange("gate",2);
	var map = eim.getMapInstance(cm.getPlayer().getMapId());
	map.broadcastMessage(packetef);
	map.broadcastMessage(packetsnd);
	map.broadcastMessage(packetglow);
	var mf = eim.getMapFactory();
	map = mf.getMap(103000800 + stage);
	var nextStage = eim.getMapInstance(103000800 + stage);
	var portal = nextStage.getPortal("next00");
	if (portal != null) {
		portal.setScriptName("kpq" + (stage+1).toString());
	}
}

function failstage(eim, cm) {
	var packetef = MaplePacketCreator.showEffect("quest/party/wrong_kor");
	var packetsnd = MaplePacketCreator.playSound("Party1/Failed");
	var map = eim.getMapInstance(cm.getPlayer().getMapId());
	map.broadcastMessage(packetef);
	map.broadcastMessage(packetsnd);
}

function rectanglestages (cm) {
	var debug = false;
	var eim = cm.getPlayer().getEventInstance();
	if (curMap == 2) {
		var nthtext = "2";
		var talk = "Oi. Bem-vindo ao 2º estágio. Você verá algumas cordas perto de mim. #b3 dessas cordas estarão conectadas ao portal que leva ao próximo estágio#k. Tudo o que você precisa é que #b3 membros do grupo encontrem as cordas e se segurem nelas#k.\r\nMAS isto não conta como resposta correta se você se pendurar muito embaixo. Por favor, suba o suficiente para a resposta ser considerada correta. E apenas 3 membros do seu grupo serão permitidos nas cordas. Quando isto acontecer, o líder do grupo deverá #bclicar duas vezes em mim para saber se a resposta está correta ou não#k. Agora, encontre as cordas certas para se pendurar!";
		var unable = "Parece que você ainda não encontrou as 3 cordas. Pense numa combinação diferente das cordas. Apenas 3 membros podem se pendurar nas cordas. E não se pendurem muito embaixo ou a resposta não irá contar. Continue!";
		var curcombo = stage2combos;
		var currect = stage2rects;
		var objset = [0,0,0,0];
	} else if (curMap == 3) {
		var nthtext = "3";
		var talk = "Olá. Bem-vindo ao 3º estágio. Em cima das plataformas, vocês verão alguns barris por perto com gatinhos dentro. Destas plataformas, #b3 levarão ao portal para o próximo estágio#k. #b3 membros do grupo precisam encontrar as plataformas corretas para subir e completar o estágio.\r\nMAS é preciso ficar firme no centro, e não na beira, para que a resposta seja considerada correta. E apenas 3 membros do seu grupo serão permitidos nas plataformas. Quando os membros estiverem nas plataformas, o líder do grupo deverá #bclicar duas vezes em mim para saber se a resposta está correta ou não#k. Agora, encontre as plataformas corretas~!"; 
		var unable = "Parece que você ainda não encontrou as 3 plataformas. Pense numa combinação diferente das plataformas. E lembre-se de que apenas 3 membros podem ficar nas plataformas, firmes no centro, para que a resposta seja válida. Continue!";
		var curcombo = stage3combos;
		var currect = stage3rects;
		var objset = [0,0,0,0,0];
	} else if (curMap == 4) {
		var nthtext = "4";
		var talk = "Oi. Bem-vindo ao 4º estágio. Você verá alguns barris por perto. 3 desses barris estarão conectados ao portal que leva ao próximo estágio. #b3 membros do grupo precisam encontrar os barris corretos e ficar em cima deles#k para completar o estágio. MAS, para a resposta contar, é preciso ficar bem firme no centro do barril, não na beira. E apenas 3 membros do seu grupo podem ficar em cima dos barris. Quando os membros estiverem em cima, o líder do grupo deverá #bclicar duas vezes em mim para saber se a resposta está correta ou não#k. Agora, encontre os barris corretos~!";
		var unable = "Parece que você ainda não encontrou os 3 barris. Pense numa combinação diferente dos barris. E não esqueça que apenas 3 membros podem ficar em cima dos barris, firmes no centro para que a resposta conte como correta. Continue!";
		var curcombo = stage4combos;
		var currect = stage4rects;
		var objset = [0,0,0,0,0,0];
	}
	if (playerStatus) { 
		if (status == 0) {
			party = eim.getPlayers();
			preamble = eim.getProperty("leader" + nthtext + "preamble");
			if (preamble == null) {
				cm.sendNext(talk);
				eim.setProperty("leader" + nthtext + "preamble","done");
				var sequenceNum = Math.floor(Math.random() * curcombo.length);
				eim.setProperty("stage" + nthtext + "combo",sequenceNum.toString());
				cm.dispose();
			} else {
				var complete = eim.getProperty(curMap.toString() + "stageclear");
				if (complete != null) {	
					var mapClear = curMap.toString() + "stageclear";
					eim.setProperty(mapClear,"true"); 
					cm.sendNext("Você completou este estágio. Siga para o próximo estágio usando o portal. Cuidado..." ); 
				} else { 
					var totplayers = 0;
					for (i = 0; i < objset.length; i++) {
						for (j = 0; j < party.size(); j++) {
							var present = currect[i].contains(party.get(j).getPosition());
							if (present) {
								objset[i] = objset[i] + 1;
								totplayers = totplayers + 1;
							}
						}
					}
			if (totplayers == 3 || debug) {
				var combo = curcombo[parseInt(eim.getProperty("stage" + nthtext + "combo"))];
				var testcombo = true;
				for (i = 0; i < objset.length; i++) {
					if (combo[i] != objset[i])
						testcombo = false;
				}
			if (testcombo || debug) {
				clear(curMap,eim,cm);
				var exp = (Math.pow(2,curMap) * 50);
				cm.givePartyExp(exp, party);
				cm.dispose();
			} else { 
				failstage(eim,cm);
				cm.dispose();
				}
			} else {
				if (debug) {
					var outstring = "Objetos contem:"
					for (i = 0; i < objset.length; i++) {
						outstring += "\r\n" + (i+1).toString() + ". " + objset[i].toString();
					}
					cm.sendNext(outstring); 
				} else
					cm.sendNext(unable);
					cm.dispose();
					}
				}
			}
		} else {
			var complete = eim.getProperty(curMap.toString() + "stageclear");
			if (complete != null) {
				var target = eim.getMapInstance(103000800 + curMap);
				var targetPortal = target.getPortal("st00");
				cm.getPlayer().changeMap(target, targetPortal);
			}
			cm.dispose();
		}
	} else { 
		if (status == 0) {
			var complete = eim.getProperty(curMap.toString() + "stageclear");
			if (complete != null) {
				cm.sendNext("Você completou este estágio. Siga para o próximo estágio usando o portal. Cuidado..." ); 
			} else {
				cm.sendNext("Por favor, peça ao líder do seu grupo para falar comigo.");
				cm.dispose();
			}
		} else {
			var complete = eim.getProperty(curMap.toString() + "stageclear");
			if (complete != null) {	
				var target = eim.getMapInstance(103000800 + curMap);
				var targetPortal = target.getPortal("st00");
				cm.getPlayer().changeMap(target, targetPortal);
			}
			cm.dispose();
		}
	}
}

function getPrize(eim,cm) {
	var itemSetSel = Math.random();
	var itemSet;
	var itemSetQty;
	var hasQty = false;
	if (itemSetSel < 0.3)
		itemSet = prizeIdScroll;
	else if (itemSetSel < 0.6)
		itemSet = prizeIdEquip;
	else if (itemSetSel < 0.9) {
		itemSet = prizeIdUse;
		itemSetQty = prizeQtyUse;
		hasQty = true;
	} else { 
		itemSet = prizeIdEtc;
		itemSetQty = prizeQtyEtc;
		hasQty = true;
	}
	var sel = Math.floor(Math.random()*itemSet.length);
	var qty = 1;
	if (hasQty)
	qty = itemSetQty[sel];
	cm.gainItem(itemSet[sel], qty);
	var map = eim.getMapInstance(103000805);
	var portal = map.getPortal("sp");
	cm.getPlayer().changeMap(map,portal);
}