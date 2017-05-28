function start(ms) {
	switch (c.getPlayer().getMapId()) { 
		case 925100000: 
			ms.getPlayer().getMap().startMapEffect("Defeat the monsters outside of the ship to advance!", 5120020); 
			break; 
		case 925100100: 
			ms.getPlayer().getMap().startMapEffect("We must prove ourselves! Get me Pirate Medals!", 5120020); 
			break; 
		case 925100200: 
			ms.getPlayer().getMap().startMapEffect("Defeat the guards here to pass!", 5120020); 
			break; 
		case 925100300: 
			ms.getPlayer().getMap().startMapEffect("Eliminate the guards here to pass!", 5120020); 
			break; 
		case 925100400: 
			ms.getPlayer().getMap().startMapEffect("Lock the doors! Seal the root of the Ship's power!", 5120020); 
			break; 
		case 925100500: 
			ms.getPlayer().getMap().startMapEffect("Destroy the Lord Pirate!", 5120020); 
			break; 
	} 
	final EventManager em = ms.getChannelServer().getEventSM().getEventManager("Pirate"); 
	if (c.getPlayer().getMapId() == 925100500 && em != null && em.getProperty("stage5") != null) { 
		int mobId = Randomizer.nextBoolean() ? 9300107 : 9300119; //lord pirate 
		final int st = Integer.parseInt(em.getProperty("stage5")); 
		switch (st) { 
			case 1: 
				mobId = Randomizer.nextBoolean() ? 9300119 : 9300105; //angry 
				break; 
			case 2: 
				mobId = Randomizer.nextBoolean() ? 9300106 : 9300105; //enraged 
				break; 
		} 
		final MapleMonster shammos = MapleLifeFactory.getMonster(mobId); 
		if (c.getPlayer().getEventInstance() != null) { 
			c.getPlayer().getEventInstance().registerMonster(shammos); 
		} 
		c.getPlayer().getMap().spawnMonsterOnGroundBelow(shammos, new Point(411, 236)); 
	} 
	break;
}