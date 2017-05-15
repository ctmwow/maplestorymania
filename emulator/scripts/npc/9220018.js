function start() {
    status = -1;
    action(1, 0, 0);
}

function action(mode, type, selection) {
	cm.removeAll(4032248);
	    if (cm.getPlayer().getParty() == null || !cm.isLeader()) {
		cm.sendOk("O líder do grupo deve estar aqui.");
	    } else {
		var party = cm.getPlayer().getParty().getMembers();
		var mapId = cm.getPlayer().getMapId();
		var next = true;
		var size = 0;
		var it = party.iterator();
		while (it.hasNext()) {
			var cPlayer = it.next();
			var ccPlayer = cm.getPlayer().getMap().getCharacterById(cPlayer.getId());
			if (ccPlayer == null || ccPlayer.getLevel() < 8) {
				next = false;
				break;
			}
			size += (ccPlayer.isGM() ? 2 : 1);
		}	
		if (next && size >= 2) {
			var em = cm.getEventManager("MV");
			if (em == null) {
				cm.sendOk("Por favor, tente novamente mais tarde.");
			} else {
				em.startInstance(cm.getPlayer().getParty(), cm.getPlayer().getMap());
			}
		} else {
			cm.sendOk("Todos os membros do seu grupo devem estar aqui e acima do nível 8.");
		}
	    }
	cm.dispose();
}