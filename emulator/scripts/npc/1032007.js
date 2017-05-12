/* Author: Xterminator
	NPC Name: 		Joel
	Map(s): 		Victoria Road : Ellinia Station (101000300)
	Description: 		Ellinia Ticketing Usher
*/
var status = -1;

function start() {
    status = -1;
    action(1, 0, 0);
}

function action(mode, type, selection) {
    if (mode == 1) {
		status++;
    } else {
		cm.sendNext("Você deve ter algum negócio para cuidar fora daqui, certo?");
		cm.dispose();
		return;
    }
    if (status == 0) {
		cm.sendYesNo("Olá, eu estou encarregado de vender bilhetes para a viagem de navio para Estação de Orbis. A viagem a Orbis decola cada 15 minutos, começando na hora, e lhe custará #b5000 mesos#k. Tem certeza de que deseja comprar um #bBilhete para Orbis (Normal)#k?");
    } else if (status == 1) {
		if (cm.getMeso() < 5000) {
			cm.sendNext("Tem certeza de que tem #b5000 mesos? Se sim, então eu sugiro você a verificar o seu inventário 'ETC', e ver se ele está cheio ou não.");
			cm.dispose();
		} else {
			cm.gainMeso(-5000);
			cm.gainItem(4031045, 1);
			cm.dispose();
		}
    }
}