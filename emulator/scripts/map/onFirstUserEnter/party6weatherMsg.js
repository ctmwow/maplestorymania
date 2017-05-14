function start(ms) {
    switch (c.getPlayer().getMapId()) { 
		case 930000000: 
			c.getPlayer().getMap().startMapEffect("Step in the portal to be transformed.", 5120023); 
			break; 
		case 930000100: 
			c.getPlayer().getMap().startMapEffect("Defeat the poisoned monsters!", 5120023); 
			break; 
		case 930000200: 
			c.getPlayer().getMap().startMapEffect("Eliminate the spore that blocks the way by purifying the poison!", 5120023); 
			break; 
		case 930000300: 
			c.getPlayer().getMap().startMapEffect("Uh oh! The forest is too confusing! Find me, quick!", 5120023); 
			break; 
		case 930000400: 
			c.getPlayer().getMap().startMapEffect("Purify the monsters by getting Purification Marbles from me!", 5120023); 
			break; 
		case 930000500: 
			c.getPlayer().getMap().startMapEffect("Find the Purple Magic Stone!", 5120023); 
			break; 
		case 930000600: 
			c.getPlayer().getMap().startMapEffect("Place the Magic Stone on the altar!", 5120023); 
			break; 
	} 
}
