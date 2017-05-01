/**
 * @author: Eric
 * @script: MCrevive<(mapid%1000)/100>
 * @func: warps players back to arena and fixes their score back to normal
*/

importPackage(java.lang);

function enter(pi) 
{
    if (pi.getPlayer().getTeam() == 0)
		pi.warp(pi.getMapId() - 1, "red_revive");
    else
		pi.warp(pi.getMapId() - 1, "blue_revive");
    
	pi.getPlayer().gainCP(0); //force update cp score
	pi.getPlayer().getMonsterCarnival().sendClock();
	return true;
}