/**
	Rolly - Ludibirum Maze PQ
* */
importPackage(Packages.org.ascnet.leaftown.client);
importPackage(Packages.org.ascnet.leaftown.server.maps);

var status = 0;
var minlvl = 51;
var maxlvl = 200;
var minPlayers = 1;
var maxPlayers = 6;
var time = 15;
var open = true;

function start() {
    status = -1;
    action(1, 0, 0);
}
var PQItems = new Array(4001106);

function action(mode, type, selection) {
    if (mode == 0) {
	cm.dispose();
    } else {
	if (mode == 1)
	    status++;
	else
	    status--;
		
	if (status == 0) {
	    cm.sendSimple("Esta é a entrada do Labirinto de Ludibrium. Divirta-se!\r\n#b#L0#Entrar no Labirinto de Lubidrium#l\r\n#L1#O que é o Labirinto de Ludibrium?"); 	
	} else if (status == 1) {
	    var em = cm.getEventManager("LudiMazePQ");
            var prop = em.getProperty("state");
	    if(selection == 0) {//ENTER THE PQ
			if (!hasParty()) {//NO PARTY
				cm.sendOk("Tente desafiar o Labirinto com seu grupo. Se você deseja entrar, crie um grupo e recrute pelo menos 3 pessoas entre os níveis 50 e 100!");
			} else if (!isPartyLeader()) {//NOT LEADER
				cm.sendOk("Tente desafiar o Labirinto com seu grupo. Se você deseja entrar, por favor peça que seu líder fale comigo!");
			} else if (!checkPartySize()) {//PARTY SIZE WRONG
				cm.sendOk("Seu grupo deve possuir pelo menos " + minPlayers + " membros para poder desafiar o labirinto.");
			} else if (!checkPartyLevels()) {//WRONG LEVELS
				cm.sendOk("Um dos membros do seu grupo não está entre os níveis " + minlvl + "~" + maxlvl + ".");
			} else if (em == null) {//EVENT ERROR
				cm.sendOk("ERROR IN EVENT");
			} else if (prop.equals("1") || prop == null){
				cm.sendOk("Há algum grupo dentro do labirinto no momento.");
			} else {
				em.startInstance(cm.getParty(),cm.getPlayer().getMap());
				party = cm.getPlayer().getEventInstance().getPlayers();
				cm.removeFromParty(4001106, party);
			}//4001106
			cm.dispose();
			} else if(selection == 1) {
				cm.sendOk("Esta missão está disponível para todos os grupos com 3 ou mais membros, e todos os participantes devem estar entre os nívis "+minlvl+"~"+maxlvl+". Você terá 15 minutos para escapar do labirinto. No centro de cada sala, haverá um portal que lhe leva para uma sala diferente. Estes portais irão transportar você para outras salas onde você poderá com sorte) encontrar a saída.  Pietri estará lhe esperando na saída, e tudo que você precisa fazer é falar com ele, e ele irá lhe remover.  Quebre todas as caixas localizadas nas salas, e um monstro dentro da caixa irá derrubar um cupom.  Após escapar o labirinto, você será recompensado com EXP e Cash baseado no número de cupons coletados.  Adicionalmente, se o líder possuir pelo menos 30 cupons, um presente especial será dado ao seu grupo. Se você não conseguir escapar do labirinto dentro do tempo de " + time +" minutos, você não irá receber nada. Se você desconectar enquanto no labirinto, você será automaticamente removido. Mesmo se os membros do seu grupo sairem no meio da missão, os membros restantes ainda poderão concluir a missão. Se você tiver numa condição crítica e não poder derrotar os monstros, você pode evitar eles e se salvar. Seu espirito de luta e seu fôlego serão testados! Boa sorte!");
				cm.dispose();
			}
		}
    }
}
     
function getPartySize(){
    if (cm.getParty() == null) {
		return 0;
    } else {
		return (cm.getParty().getMembers().size());
    }
}

function isPartyLeader(){
    return cm.isPartyLeader();
}

function checkPartySize(){
    var size = 0;
    if (cm.getParty() == null){
		size = 0;
    } else {
		size = (cm.getParty().getMembers().size());
    }
    if (size < minPlayers  || size > maxPlayers) {
		return false;
    } else {
		return true;
    }
}

function checkPartyLevels(){
    var pass = true;
    var party = cm.getParty().getMembers();
    if (cm.getParty() == null) {
		pass = false;
    } else {
		for (var i = 0; i < party.size() && pass; i++) {
			if ((party.get(i).getLevel() < 51) || (party.get(i).getLevel() > 200) || (party.get(i).getMapid() != cm.getMapId())) {
				pass = false;
			}
		}
    }
    return pass;
}

function hasParty(){
    if(cm.getParty() == null){
		return false;
    } else {
		return true;
    }
}