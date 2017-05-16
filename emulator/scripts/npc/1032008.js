function start() {
    if(cm.haveItem(4031045)){
        var em = cm.getEventManager("Boats");
        if (em.getProperty("entry") == "true")
            cm.sendYesNo("Você gostaria de ir para Orbis?");
        else{
            cm.sendOk("O barco para Orbis já está viajando, por favor, seja paciente para o próximo.");
            cm.dispose();
        }
    }else{
        cm.sendOk("Certifique-se que você tem um bilhete Orbis para viajar neste barco. Verifique seu inventário.");
        cm.dispose();
    }
}
function action(mode, type, selection) {
    if (mode <= 0) {
		cm.sendOk("Ok, fale comigo se mudar de idéia!");
		cm.dispose();
		return;
    }
	
	cm.warp(101000301);
	cm.gainItem(4031045, -1);
	cm.dispose();
}	