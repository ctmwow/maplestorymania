function enter(pi) 
{
	if (pi.getPlayer().isRideFinished())
	{
		pi.warp(130000210, 0);
		return true;
	}
	else
		return false;
}