/**
 * @author: Eric
 * @npc Spiegelmann
 * @func: Monster Carnival
*/

var status = 0;

function start() {
    status = -1;
    action(1, 0, 0);
}


function action(mode, type, selection) 
{
    (mode == 1 ? status++ : cm.dispose());
    if (status == 0)
		cm.sendSimple("#e<Competição: Festival de Monstros>#n\r\nSe você está ansioso por alguma ação, então o Festival de Monstros é o lugar para você!#b\r\n\r\n#L0#Quero participar do Festival de Monstros.\r\n#L1#Conte-me mais sobre o Festival de Monstros.\r\n#L2#Quero trocar minhas brilhantes moedas de Maple.");
	else if (status == 1) 
	{
		if (selection == 0) 
		{
			// gMS v1.51: cm.sendOk("The Monster Carnival's had to close its doors for a bit. Why don't you go find something else to entertain you for now?");
			var selStr = "Inscreva-se para o Festival de Monstros!#b";
			var found = false;
			for (var i = 0; i < 6; i++)
			{
				if (getCPQField(i+1) != "") 
				{
					selStr += "\r\n#L" + i + "# " + getCPQField(i+1) + "#l";
					found = true;
				}
			}
			if (cm.getParty() == null) 
			{
				cm.sendOk("Você não está em um grupo!");
				cm.dispose();
			} 
			else 
			{
				if (cm.isLeader()) 
				{
					if (found)
						cm.sendSimple(selStr);
					else 
					{
						cm.sendOk("Não existem salas disponíveis no momento.");
						cm.dispose();
					}
				} 
				else 
				{
					cm.sendOk("Por favor, diga ao seu líder do grupo para falar comigo.");
					cm.dispose();
				}
			}
		} 
		else if (selection == 1) 
		{
			status = 4;
			cm.sendOk("O #bFestival de Monstros#k é um lugar mágico onde você se junta com seu grupo para destruir as hordas de monstros mais rápido do que os outros grupos.");
		} 
		else if (selection == 2) 
		{
			if (!cm.haveItem(4001254)) 
			{
				cm.sendOk("What? You don't even have a single Shiny Maple Coin! If you want #i1102556# Spiegelmann's Mighty Mustache or #i1012270# Spiegelmann's Cape of Moxy, #i1122162# Spiegelmann's Mighty Bow Tie, then bring me more #i4001254# #bShiny Maple Coin!#k");
				cm.dispose();
			} 
			else 
			{
				status = 2;
				cm.sendSimple("\r\n#b#L0#50 Maple Coin = Spiegelmann Necklace#l\r\n#L1#30 Maple Coin = Spiegelmann Marble#l\r\n#L2#50 Sparkling Maple Coin = Spiegelmann Necklace of Chaos#l#k");
			}
		}
    } 
    else if (status == 2) 
    {
		if (selection >= 0 && selection < 9) 
		{
			var mapid = 980000000+((selection+1)*100);
			if (cm.getEventManager("CarnivalPQ").getInstance("CarnivalPQ"+mapid) == null) 
			{
				if ((cm.getParty() != null && 1 < cm.getParty().getMembers().size() && cm.getParty().getMembers().size() < (selection == 4 || selection == 5 || selection == 8 ? 4 : 3)) || cm.getPlayer().isGM()) 
				{
					if (checkLevelsAndMap(30, 255) == 1) 
						cm.sendOk("Um jogador em seu grupo não está em um nível apropriado.");
					else if (checkLevelsAndMap(30, 255) == 2) 
						cm.sendOk("Um ou mais jogadores do seu grupo não estão no mapa");
					else
						cm.getEventManager("CarnivalPQ").startInstance(mapid, cm.getPlayer());
				} 
				else 
					cm.sendOk("Seu grupo não tem um tamanho apropriado! Tente escolher outra sala.");
			}
			else if (cm.getEventManager("CarnivalPQ").getInstance("CarnivalPQ"+mapid).isEventStarted())
			{
				cm.sendOk("O evento já está em andamento!");
			}
			else if (cm.getParty() != null && cm.getEventManager("CarnivalPQ").getInstance("CarnivalPQ"+mapid).getPlayerCount() == cm.getParty().getMembers().size()) 
			{
				if (checkLevelsAndMap(30, 255) == 1) 
					cm.sendOk("Um jogador em seu grupo não está em um nível apropriado.");
				else if (checkLevelsAndMap(30, 255) == 2) 
					cm.sendOk("Um ou mais jogadores do seu grupo não estão no mapa");
				else 
				{
					var owner = cm.getEventManager("CarnivalPQ").getChannelServer().getPlayerStorage().getCharacterByName(cm.getEventManager("CarnivalPQ").getInstance("CarnivalPQ"+mapid).getPlayers().get(0).getParty().getLeader().getName());
					owner.addCarnivalRequest(cm.getCarnivalChallenge(cm.getPlayer()));
					cm.openNpc(owner.getClient(), 2042001, "2042001");
					cm.sendOk("Seu desafio foi enviado.");
				}
			} 
			else
				cm.sendOk("Os dois grupos que participam do Festival de Monstros devem ter um número igual de membros");
				
			cm.dispose();
		}
	} 
	else if (status == 3) 
	{
	    if (selection == 0) 
	    {
			if (!cm.haveItem(4001129,50)) 
			{
				cm.sendOk("You have no items.");
			} 
			else if (!cm.canHold(1122007,1)) 
			{
				cm.sendOk("Please make room");
			} 
			else 
			{
				cm.gainItem(1122007,1);
				cm.gainItem(4001129,-50);
			}
			cm.dispose();
	    } 
	    else if (selection == 1) 
	    {
			if (!cm.haveItem(4001129,30)) 
			{
				cm.sendOk("You have no items.");
			} 
			else if (!cm.canHold(2041211,1)) 
			{
				cm.sendOk("Please make room");
			}
			else 
			{
				cm.gainItem(2041211,1);
				cm.gainItem(4001129,-30);
			}
			cm.dispose();
	    } 
	    else if (selection == 2) 
	    {
			if (!cm.haveItem(4001254,50)) 
			{
				cm.sendOk("You have no items.");
			} 
			else if (!cm.canHold(1122058,1)) 
			{
				cm.sendOk("Please make room");
			} 
			else 
			{
				cm.gainItem(1122058,1);
				cm.gainItem(4001254,-50);
			}
			cm.dispose();
	    }
	} 
	else if (status == 5) 
	{
		cm.sendNextPrev("Don't think you can do it alone? Worry not, my friend, I will enlist others to join you! All you have to tell me is, are you game? If you are, I'll give a holler when I have your group ready.\r\n - #eLevel#n: 110 - 130\r\n - #eRewards#n:\r\n#i1102556# Spiegelmann's Mighty Mustache\r\n#i1012270# Spiegelmann's Cape of Moxy\r\n#i1122162# Spiegelmann's Mighty Bow Tie");
		cm.dispose();
	}
}

function checkLevelsAndMap(lowestlevel, highestlevel) 
{
    var party = cm.getParty().getMembers();
    var mapId = cm.getMapId();
    var valid = 0;
    var inMap = 0;

    var it = party.iterator();
    while (it.hasNext()) 
    {
        var cPlayer = it.next().getPlayer();
        
        if (!(cPlayer.getLevel() >= lowestlevel && cPlayer.getLevel() <= highestlevel) && cPlayer.getJobId() != 900) 
            valid = 1;
        if (cPlayer.getMapId() != mapId)
            valid = 2;
    }
    return valid;
}

function getCPQField(fieldnumber) 
{
    var status = "";
    var event1 = cm.getEventManager("CarnivalPQ");
    
    if (event1 != null) 
    {
        var event = event1.getInstance("CarnivalPQ"+(980000000+(fieldnumber*100)));
        if (event == null && fieldnumber != 4 && fieldnumber != 5 && fieldnumber != 6) 
            status = "Sala do Festival "+fieldnumber+"(2~4ppl)";
        else if (event == null) 
            status = "Sala do Festival "+fieldnumber+"(3~6ppl)";
        else if (event != null && (event.getProperty("started").equals("false"))) 
        {
            var averagelevel = 0;
            
            for (i = 0; i < event.getPlayerCount(); i++)
                averagelevel += event.getPlayers().get(i).getLevel();
                
            averagelevel /= event.getPlayerCount();
            
            if(event.getPlayerCount() == 0 || event.getPlayers().get(0).getParty() == null)
            	status = "Sala temporáriamente indisponível";
            else
            	status = "Líder: " + event.getPlayers().get(0).getParty().getLeader().getName()+"/"+event.getPlayerCount()+" jogadore(s)/Média de nível: "+averagelevel;
        }
    }
    return status;
}
