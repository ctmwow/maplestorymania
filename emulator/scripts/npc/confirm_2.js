var status = 0;

function start() 
{
	cm.sendYesNo(param0);
}

function action(mode, type, selection)
{
	status++;

    if (mode == 1)
    	cm.getPlayer().confirmationCallback();
    else
    	cm.getPlayer().confirmationFailCallback();
     
    cm.dispose();
}