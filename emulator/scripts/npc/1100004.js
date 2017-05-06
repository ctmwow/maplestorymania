/**
----------------------------------------------------------------------------------
	Skyferry Between Victoria Island, Ereve and Orbis.

	1100004 Kiru (To Orbis)

-------Credits:-------------------------------------------------------------------
	*MapleSanta 
	*MapleStory Mania
----------------------------------------------------------------------------------
**/

var menu = new Array("Orbis");
var method;

function start() {
    status = -1;
    action(1, 0, 0);
}

function action(mode, type, selection) {
    if (mode == -1) 
	{
        cm.dispose();
        return;
    } else 
	{
        if (mode == 0 && status == 0) 
		{
            cm.dispose();
            return;
        } 
		else if (mode == 0) 
		{
            cm.sendNext("Está bem. Se você mudar de ideia, por favor me avise.");
            cm.dispose();
            return;
        }
        status++;
        if (status == 0) 
		{
            for (var i = 0; i < menu.length; i++)
			{
                var display = "\r\n#L" + i + "##b Orbis (1000 mesos)#k";
            }
            cm.sendSimple("Hmm... Os ventos são favoráveis. Você está pensando em deixar Ereve e ir para algum lugar? Este barco voa à Orbis no continente de Ossyria. Você cuidou de tudo que precisa em Ereve? Se você está indo para #bOrbis#k posso levá-lo até lá. O que você irá fazer? Irá para Orbis?\r\n" + display);

        } 
		else if (status == 1) 
		{
            if (cm.getMeso() < 1000)
			{
                cm.sendNext("Hm... Tem certeza de que tem #b1000#k Mesos? Verifique o seu inventário e verifique se você tem o suficiente. Você deve pagar a taxa ou então não posso deixá-lo entrar...");
                cm.dispose();
            } 
			else
			{
                cm.gainMeso(-1000);
				cm.getPlayer().setArrivalTime(480);
                cm.warp(200090021);
				cm.sendClock(cm.getClient(), 480);
                cm.dispose();
            }
        }
    }
}