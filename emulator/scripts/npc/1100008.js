/**
----------------------------------------------------------------------------------
	Skyferry Between Victoria Island, Ereve and Orbis.

	1100008 Kiru (Orbis Station)

-------Credits:-------------------------------------------------------------------
	*MapleSanta 
	*MapleStory Mania
----------------------------------------------------------------------------------
**/

var menu = new Array("Ereve");
var method;

function start() {
	status = -1;
	action(1, 0, 0);
}

function action(mode, type, selection) {
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
			cm.sendNext("Está bem. Se você mudar de ideia, por favor me avise.");
			cm.dispose();
			return;
		}
		status++;
		if (status == 0)
		{
			for(var i=0; i < menu.length; i++)
			{
					var display = "\r\n#L"+i+"##b Ereve (1000 mesos)#k";
			}			
			cm.sendSimple("Este navio irá para #bEreve#k, Uma ilha onde você encontrará as folhas carmesim absorvendo o sol, a brisa suave que desliza além do córrego, e a Imperatriz dos Cavaleiros de Cygnus. Se você está interessado em se juntar aos Cavaleiros de Cygnus, então você deve definitivamente pagar uma visita aqui. Você está interessado em visitar Ereve? A viagem vai custar-lhe #b1000#k Mesos\r\n"+display);			
		}
		else if(status == 1) 
		{
			if(cm.getMeso() < 1000)
			{
				cm.sendNext("Hm... Tem certeza de que tem #b1000#k Mesos? Verifique o seu inventário e verifique se você tem o suficiente. Você deve pagar a taxa ou então não posso deixá-lo entrar...");
				cm.dispose();
			} 
			else
			{
				cm.gainMeso(1000);
				cm.getPlayer().setArrivalTime(480);
				cm.warp(200090020);
				cm.sendClock(cm.getClient(), 480);
				cm.dispose();
			}
		}
	}
}