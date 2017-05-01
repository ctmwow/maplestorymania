var status = 0;
var request;

importPackage(java.lang);

function start() 
{
    status = -1;
    action(1, 0, 0);
}

function action(mode, type, selection) 
{
	status++;
	
	if(status == 0)
	{
		cm.sendYesNo("Você deseja abandonar a sala?");
	}
	else
	{
		if(mode == 1)
		{
			cm.getPlayer().getEventInstance().unregisterPlayer(cm.getPlayer());
    		cm.warp(980000000, 4);
		}
		cm.dispose();
	}
}