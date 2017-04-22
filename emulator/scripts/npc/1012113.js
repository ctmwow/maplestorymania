var status = 0;
var PQItems = new Array(4001095, 4001096, 4001097, 4001098, 4001099, 4001100, 4001101, 4001101);

importPackage(Packages.org.ascnet.leaftown.client);

function start() {
	status = -1;
	action(1, 0, 0);
}

function action(mode, type, selection) {
	if (mode == -1) {
		cm.dispose();
	} else {
		if (status >= 0 && mode == 0) {
			cm.dispose();
			return;
		}
		if (mode == 1)
			status++;
		else
			status--;
        var eim = cm.getPlayer().getEventInstance(); 
		if(cm.getMapId()==910010300){
			if (status==0) {
				cm.sendNext("Azarado né? Tente novamente mais tarde!");						
			}else if (status == 1){
				for (var i = 0; i < PQItems.length; i++) {
					cm.removeAll(PQItems[i]);
				}
				cm.warp(100000200);
				cm.dispose();
			}
		} if(cm.getMapId()== 910010200){
			if (status==0) {
				cm.sendNext("Deseja mesmo sair? Lembrando que não poderás voltar.");				
			}else if (status == 1){
                for (var i = 0; i < PQItems.length; i++) {
					cm.removeAll(PQItems[i]);
                }
				eim.leftParty(cm.getPlayer());
				cm.dispose();
			}
		} else if (cm.getPlayer().getMapId() == 910010100) {
			if (status == 0) {
				cm.sendYesNo("Gostaria de ir para a #rVila dos Porcos#k? É uma cidade onde os porcos estão por toda parte, você pode encontrar alguns itens valiosos lá!");
			} else if (status == 1) {
                if (!cm.isPartyLeader() || cm.getParty() == null) { // Not Party Leader
                    cm.sendOk("Se você deseja entrar, o líder do seu grupo deve falar comigo.");
                     cm.dispose();
                } else {
					var em = cm.getEventManager("PigTown");
						if (em == null) {
							cm.sendOk("Evento indisponível.");
							cm.dispose();
						} else {
							em.startInstance(cm.getParty(),cm.getPlayer().getMap());
							party = cm.getPlayer().getEventInstance().getPlayers();
						}
					cm.dispose();
                }
            }
        }
    }
}