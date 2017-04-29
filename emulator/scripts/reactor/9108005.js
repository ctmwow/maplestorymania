importPackage(Packages.org.ascnet.leaftown.tools);
importPackage(Packages.org.ascnet.leaftown.server);
importPackage(Packages.org.ascnet.leaftown.server.life);
importPackage(Packages.org.ascnet.leaftown.server.maps);


function act() 
{
	rm.mapMessage(6, "Uma das sementes foi plantada.");
	var em = rm.getEventManager("HenesysPQ");
	
	if (em != null) 
	{
		var react = rm.getPlayer().getMap().getReactorByName("fullmoon");
		em.setProperty("semente", parseInt(em.getProperty("semente")) + 1);
		react.forceHitReactor(react.getState() + 1);
		
		if (em.getProperty("semente").equals("6") && rm.getPlayer().getMap().getMonsterByOid(9300061) == null) 
		{
			var eim = rm.getPlayer().getEventInstance();
			var tehMap = eim.getMapInstance(910010000);
			var bunny = MapleLifeFactory.getMonster(9300061);
			
			tehMap.spawnMonsterOnGroundBelow(bunny, new java.awt.Point(-187, -186));
							
			rm.getPlayer().getMap().setSpawns(true);
			
			eim.registerMonster(bunny);
			eim.setProperty("shouldDrop", "true");
			
			rm.playerMessage("Proteja o Coelhinho da Lua!");
		}
	}
}