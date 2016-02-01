function enter(pi) 
{
	if (pi.getPlayer().isRideFinished())
	{
		pi.warp(101000400, 0);
		return true;
	}
	else
		return false;
}