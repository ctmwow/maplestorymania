/*
* @Portal - move_RieRit
* @description - Portal while on ship from Lith Harbor to Riet
*/

function enter(pi) {
	if (pi.getPlayer().isRideFinished())
	{
		pi.warp(140020300, 0);
		return true;
	}
	else
		return false;
}