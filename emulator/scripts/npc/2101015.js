var status = 0;

importPackage(Packages.client);

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
		if (status == 0) {
			apqpoints = cm.getPlayer().getAPQPoints();
			if (apqpoints < 100) {
				  cm.sendOk("A sua Pontuação de Arena de Batalha é #b" + apqpoints + "#k Pontos. Você precisa ultrapassar os #b100 Pontos#k para que eu possa lhe dar a #bCadeira de Praia com Palmeira#k.Estou ocupado, então fale comigo quando você tiver pontos suficientes e fale comigo novamente.")
				  cm.dispose();
			}
			if (apqpoints > 99) {
				cm.sendNext("Wow, parece que você conseguiu os #b100 pontos#k necessários para troca, vamos lá?!");
			}
		} else if (status == 1) {
			cm.getPlayer().gainAPQPoints(-100);
			cm.gainItem(3010018, 1);
			cm.dispose();
	 }
    }
}