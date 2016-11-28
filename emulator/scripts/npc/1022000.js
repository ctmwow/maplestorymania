/*
	This file is part of the OdinMS Maple Story Server
    Copyright (C) 2008 Patrick Huy <patrick.huy@frz.cc>
		       Matthias Butz <matze@odinms.de>
		       Jan Christian Meyer <vimes@odinms.de>

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as
    published by the Free Software Foundation version 3 as published by
    the Free Software Foundation. You may not use, modify or distribute
    this program under any other version of the GNU Affero General Public
    License.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/
/* Dances with Balrog
	Warrior Job Advancement
	Victoria Road : Warriors' Sanctuary (102000003)

	Custom Quest 100003, 100005
*/

status = -1;
actionx = {"1stJob" : false, "2ndjob" : false, "3thJobI" : false, "3thJobC" : false};
job = 110;

function start() 
{
    if (cm.getJobId() == 0) 
    {
        actionx["1stJob"] = true;
        if (cm.getLevel() >= 10)
            cm.sendNext("Você quer se tornar um guerreiro? Você precisa atender a alguns critérios, a fim de fazê-lo. #b Você deve estar pelo menos no nível 10, com pelo menos 35 em STR#k. Vamos ver...");
        else 
        {
            cm.sendOk("Treinar um pouco mais e eu posso mostrar-lhe o caminho do #rGuerreiro#k.");
            cm.dispose();
        }
    }
    else if (cm.getLevel() >= 30 && cm.getJobId() == 100) 
    {
        actionx["2ndJob"] = true;
        if (cm.haveItem(4031012))
            cm.sendNext("Vejo que você tem feito bem. Eu irei lhe permitir dar o próximo passo na sua longa estrada.");
        else if (cm.haveItem(4031008))
        {
            cm.sendOk("Vá e veja o #b#p1072000##k.");
            cm.dispose();
        }
         else
            cm.sendNext("O progresso que você fez é surpreendente.");
    }
     else if (actionx["3thJobI"] || (cm.getPlayer().gotPartQuestItem("JB3") && cm.getLevel() >= 70 && (cm.getJobId() % 10 == 0 && parseInt(cm.getJobId() / 100) == 1 && !cm.getPlayer().gotPartQuestItem("JBP")))) 
     {
        actionx["3thJobI"] = true;
        cm.sendNext("Eu estava esperando por você. Poucos dias atrás, eu ouvi sobre você de #b#p2020008##k em Ossyria. Bem ... eu gostaria de testar sua força. Há uma passagem secreta perto do túnel da formiga. Ninguém fora você pode ir para essa passagem. Se você for para a passagem, você irá conhecer meu outro eu. Vença-o e traga #b#t4031059##k para mim.");
    }
	else if (cm.getPlayer().gotPartQuestItem("JBP") && !cm.haveItem(4031059))
	{
        cm.sendNext("Por favor, traga-me o #b#t4031059##k.");
        cm.dispose();
    } 
    else if (cm.haveItem(4031059) && cm.getPlayer().gotPartQuestItem("JBP"))
    {
        actionx["3thJobC"] = true;
        cm.sendNext("Nossa, você venceu meu outro eu e trouxe o #b#t4031059##k para mim. Bom! isso certamente demonstra a sua força. Em termos de força, você está pronto para avançar para o 3th Job. Como eu prometi, vou dar #b#t4031057##k pra você. Dê esse colar para #b#p2020008##k em Ossyria e você será capaz de tomar o segundo teste do terceiro avanço do trabalho. Boa sorte~");
    }
     else 
    {
        cm.sendOk("Você escolheu sabiamente");
        cm.dispose();
    }
}

function action(mode, type, selection) 
{
    status++;
    if (mode == 0 && type != 1)
        status -= 2;
    if (status == -1)
    {
        start();
        return;
    } 
    else if (mode != 1 || status == 7 && type != 1 || (actionx["1stJob"] && status == 4) || (cm.haveItem(4031008) && status == 2) || (actionx["3thJob"] && status == 1)){
        if (mode == 0 && status == 2 && type == 1)
            cm.sendOk("Make up your mind and visit me again.");
        if (!(mode == 0 && type != 1))
        {
            cm.dispose();
            return;
        }
    }
    if (actionx["1stJob"])
    {
        if (status == 0)
            cm.sendNextPrev("É uma escolha importante e final. Você não será capaz de voltar atrás.");
        else if (status == 1)
        {
            if (cm.canHold(1302077))
            {
                if (cm.getJobId() == 0)
                {
                    cm.changeJobById(100);
                    cm.gainItem(1302077, 1);
                    cm.resetStats();
                }
				cm.dispose();
                //cm.sendNext("From here on out, you are going to the (Incomplete)");
            }
             else 
            {
                cm.sendNext("Faça algum espaço em seu inventário e volte a falar comigo");
                cm.dispose();
            }
        } else if (status == 2) 
            cm.sendNextPrev("Você se tornou muito mais forte agora. Além disso, cada um de seus estoques adicionou slots. Toda uma fila, para ser exato. Vai ver por você mesmo. Eu apenas dei-lhe um pouco de #bSP#k. Quando você abrir o menu #bSkill#k no canto inferior esquerdo da tela, existem habilidades que você pode aprender usando SP. Um aviso : Você não pode aprender todos juntos de uma vez. Há também habilidades que você pode adquirir somente depois de ter aprendido um par de habilidades primeiro.");
        else if (status == 3)
            cm.sendNextPrev("Mais uma advertência. Depois de ter escolhido você (incompleto)");
    } 
    else if(actionx["2ndJob"])
    {
        if (status == 0)
	{
            if (cm.haveItem(4031012))
                cm.sendSimple("Alright, when you have made your decision, click on [I'll choose my occupation] at the bottom.#b\r\n#L0#Please explain to me what being the Fighter is all about.\r\n#L1#Please explain to me what being the Page is all about.\r\n#L2#Please explain to me what being the Spearman is all about.\r\n#L3#I'll choose my occupation!");
            else
                cm.sendNext("Good decision. You look strong, but I need to see if you really are strong enough to pass the test, it's not a difficult test, so you'll do just fine. Here, take my letter first... make sure you don't lose it!");
        } 
		else if (status == 1)
		{
			if (!cm.haveItem(4031012))
			{
				if (cm.canHold(4031008))
				{
					if(!cm.haveItem(4031008))
						cm.gainItem(4031008, 1);
					cm.sendNextPrev("Por favor, entregue essa carta para #b#p1072000##k que está ao redor #b#m102020300##k próximo à Perion. Ele está fazendo o trabalho de instrutor no meu lugar. Dê a carta à ele. Boa sorte.");
			
				} 
				else 
				{
					cm.sendNext("Por favor, libere um espaço em seu inventário.");
					cm.dispose();
				}
			}
			else
			{
				if (selection < 3)
				{
					cm.sendNext("Nada feito.");
					status -= 2;
				} 
				else
					cm.sendSimple("Por favor, escolha o trabalho que você gostaria de selecionar para o seu segundo avanço do trabalho. #b\r\n#L0#Fighter\r\n#L1#Page\r\n#L2#Spearman");
			}
		}
		else if (status == 1)
		{
			if (cm.haveItem(4031008))
            {
                cm.dispose();
                return;
            }
            job += selection * 10;
            cm.sendYesNo("Então você quer fazer o segundo avanço do trabalho como " + (job == 110 ? "#bFighter#k" : job == 120 ? "#bPage#k" : "#bSpearman#k") + "? Você sabe que você não será capaz de escolher um trabalho diferente para o segundo avanço do trabalho uma vez que você faça a sua decisão aqui?");
        } 
        else if (status == 2)
        {
            if (cm.haveItem(4031012))
                cm.gainItem(4031012, -1);
                
            cm.sendNext("Tudo bem, você é o " + (job == 110 ? "#bFighter#k" : job == 120 ? "#bPage#k" : "#bSpearman#k") + " daqui em diante.");
            if (cm.getJobId() != job)
                cm.changeJobById(job);
			cm.dispose();
        } 
        else if (status == 4)
            cm.sendNextPrev("Eu apenas dei-lhe um livro que lhe dá a lista de habilidades que você pode adquirir como um " + (job == 110 ? "fighter" : job == 120 ? "page" : "spearman") + ". Também o seu inventário ETC expandiu, adicionando outra linha para ele. O seu HP e MP máximo também aumentado. Veja por você mesmo.");
        else if (status == 5)
            cm.sendNextPrev("Eu também ter-lhe dado um pouco de #bSP#k. Abra o #bSkill Menu#k localizado no canto inferior esquerdo da tela. Você vai ser capaz de impulsionar as habilidades do segundo nível adquiridas recentemente. Uma palavra de aviso embora. Você não pode aumentar-los todos de uma vez. Algumas das habilidades só estão disponíveis depois de ter aprendido outras habilidades. Certifique-se de lembrar disso.");
        else if (status == 6)
            cm.sendNextPrev((job == 110 ? "Fighter" : job == 120 ? "Page" : "Spearman") + " precisa ser forte. Mas lembre-se que você não pode abusar desse poder e usá-lo em um fraco. Por favor, use o seu enorme poder da maneira certa, porque ... as vezes seguir o caminho correto é mais difícil do que apenas ficar mais forte. Por favor, me encontre depois de ter avançado muito mais longe. Eu estarei esperando por você.");
    } 
    else if (actionx["3thJobI"])
    {
        if (status == 0)
        {
            if (cm.getPlayer().gotPartQuestItem("JB3"))
            {
                cm.getPlayer().removePartQuestItem("JB3");
                cm.getPlayer().setPartQuestItemObtained("JBP");
            }
            cm.sendNextPrev("Meu outro eu é bem forte. Ele usa muitas habilidades especiais e você deve lutar com ele 1 a 1. No entanto, as pessoas não podem ficar muito tempo na passagem secreta, por isso é importante para vencê-lo o mais rápido possível. Bem ... Boa sorte e traga #b#t4031059##k para mim.");
        }
    } 
    else if (actionx["3thJobC"])
    {
        cm.getPlayer().removePartQuestItem("JBP");
        cm.gainItem(4031059, -1);
        cm.gainItem(4031057, 1);
        cm.dispose();
    }
}

/* 3th Job Part
	PORTAL 20 MINUTES.
 */