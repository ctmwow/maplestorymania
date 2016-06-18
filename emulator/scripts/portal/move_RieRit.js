/*
* @Portal - move_RieRit
* @description - Portal while on ship from Rien to Lith Harbor.
*/

function enter(pi) {
	if (pi.getPlayer().isRideFinished())
	{
		pi.warp(104000000, 0);
		return true;
	}
	else
		return false;
}