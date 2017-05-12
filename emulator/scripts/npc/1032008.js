/* 
	NPC Name: 		Cherry
	Map(s): 		Victoria Road : Ellinia Station (101000300)
	Description: 		Ellinia Ticketing Usher
*/
var status = 0;

function start() {
    status = -1;
    boat = cm.getEventManager("Boats");
    action(1, 0, 0);
}

function action(mode, type, selection) {
    status++;
    if(mode == 0) {
		cm.sendNext("Você deve ter algum negócio para cuidar fora daqui, certo?");
		cm.dispose();
		return;
    }
    if (status == 0) {
		if(boat == null) {
			cm.sendNext("Erro de evento, reinicie o servidor para solução.");
			cm.dispose();
		} else if(boat.getProperty("entry").equals("true")) {
			cm.sendYesNo("Parece que há espaço suficiente para esta viagem, por favor, tenha o seu bilhete pronto para que eu possa deixá-lo entrar. A viagem será longa, mas você vai chegar ao seu destino muito bem. O que você acha? Você quer começar esta viagem?");
		} else if(boat.getProperty("entry").equals("false") && boat.getProperty("docked").equals("true")) {
			cm.sendNext("O barco está se preparando para a decolagem. Desculpa, mas terá de ir na próxima viagem. A agenda de viagem está disponível através do porta-voz na cabine de bilheteira.");
			cm.dispose();
		} else {
			cm.sendNext("Começaremos a embarcar 1 minuto antes da decolagem. Por favor, seja paciente e aguarde alguns minutos. Esteja ciente de que o barco vai decolar na hora, e nós pararemos de receber bilhetes 1 minuto antes disso, e por isso certifique-se de estar aqui a tempo.");
			cm.dispose();
		}
    } else if(status == 1) {
		if(!cm.haveItem(4031045)) {
			cm.sendNext("Oh não... Eu não acho que você tem o bilhete com você. Eu não posso deixar você entrar sem ele. Por favor, compre o bilhete na cabine de bilheteira.");
		} else {
			cm.gainItem(4031045, -1);
			cm.warp(101000301, 0);
		}
		cm.dispose();
    }
}