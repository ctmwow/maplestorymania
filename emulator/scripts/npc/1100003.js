/**
----------------------------------------------------------------------------------
	Skyferry Between Victoria Island, Ereve and Orbis.

	1100003 Kiriru (To Victoria Island From Ereve)

-------Credits:-------------------------------------------------------------------
	*MapleSanta 
----------------------------------------------------------------------------------
**/

var menu = new Array("Victoria Island");
var method;

function start() 
{
	status = -1;
	action(1, 0, 0);
}

function action(mode, type, selection) 
{
	if(mode == -1) 
	{
		cm.dispose();
		return;
	}
	else 
	{
		if(mode == 0 && status == 0) 
		{
			cm.dispose();
			return;
		}
		else if(mode == 0) 
		{
			cm.sendNext("Se você não está interessado, tudo bem.");
			cm.dispose();
			return;
		}
		status++;
		if (status == 0)
		{
			for(var i=0; i < menu.length; i++) 
			{
				var display = "\r\n#L"+i+"##b Victoria Island (1000 mesos)#k";
			}			
			cm.sendSimple("Eh, Olá...novamente. Você deseja deixar Ereve e ir para outro lugar? Se sim, você veio ao lugar certo! Eu opero a balsa que vai de #bEreve#k à #bVictoria Island#k, eu posso leva-lo até #bVictoria Island#k se você quiser... Você vai ter que pagar uma taxa de #b1000#k Mesos.\r\n"+display);
			
		} 
		else if(status == 1) 
		{
			 if(cm.getMeso() < 1000) 
			 {
				cm.sendNext("Hmm... Tem certeza que você possui #b1000#k Mesos? Verifique o seu inventário e certifique-se que você possui o necessário. Você deve pagar a taxa ou eu não posso deixar você vir...");
				cm.dispose();
			} 
			else
		    {
				cm.gainMeso(-1000);
				cm.getPlayer().setArrivalTime(120);
				cm.warp(200090031);
				cm.sendClock(cm.getClient(), 120);
				cm.dispose();
		    }
		}
	}
}