/*
* @Portal - move_EreOrb
* @description - Portal while on ship from Ereve to Orbis
*/

function enter(pi) {
	if (pi.getPlayer().isRideFinished())
	{
		pi.warp(200000161, 0);
		return true;
	}
	else
		return false;
}