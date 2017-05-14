function start(ms) {
    switch (ms.getPlayer().getMapId()) { 
		case 930000000: 
			ms.getPlayer().getMap().startMapEffect("Entre no portal para ser transformado.", 5120023); 
			break; 
		case 930000100: 
			ms.getPlayer().getMap().startMapEffect("Derrote os monstros envenenados!", 5120023); 
			break; 
		case 930000200: 
			ms.getPlayer().getMap().startMapEffect("Elimine o esporo que bloqueia o caminho purificando o veneno!", 5120023); 
			break; 
		case 930000300: 
			ms.getPlayer().getMap().startMapEffect("Aaaah! A floresta é muito confusa! Encontre-me rápido!", 5120023); 
			break; 
		case 930000400: 
			ms.getPlayer().getMap().startMapEffect("Purifique os monstros obtendo Mármore de Monstro para mim!", 5120023); 
			break; 
		case 930000500: 
			ms.getPlayer().getMap().startMapEffect("Encontre a Pedra Mágica Roxa!", 5120023); 
			break; 
		case 930000600: 
			ms.getPlayer().getMap().startMapEffect("Coloque a Pedra Mágica no altar!", 5120023); 
			break; 
	} 
}
