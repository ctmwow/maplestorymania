/*
* @Portal - move_OrbEre
* @description - Portal while on ship from Orbis to Ereve
*/

function enter(pi) {
	if (pi.getPlayer().isRideFinished())
	{
		pi.warp(130000210, 0);
		return true;
	}
	else
		return false;
}