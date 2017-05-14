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
/* Author: Xterminator
	NPC Name: 			Shanks
	Map(s): 			Maple Road : Southperry (60000)
	Description: 		Brings you to Victoria Island
*/
var status = 0;

function start() 
{
    cm.sendYesNo("Pegue esse navil e você irá para um continente maior! Por #e150 mesos#n, eu irei te levar para #bIlha Victoria#k. Porém, uma vez que você deixar este lugar, você não nunca poderá voltar. O que você acha? Você quer ir para Ilha Victoria?");
}

function action(mode, type, selection) 
{
    status++;
    if (mode != 1)
    {
        if(mode == 0 && type != 1)
            status -= 2;
        else if(type == 1 || (mode == -1 && type != 1))
        {
            if(mode == 0)
                cm.sendOk("Hmm ... Eu acho que você ainda tem coisas para fazer aqui?");
                
            cm.dispose();
            return;
        }
    }
    if (status == 1) 
    {
        if (cm.haveItem(4031801))
            cm.sendNext("Ok, agora me dê 150 mesos ... Ei, o que é isso? É uma carta de recomendação do Lucas, o chefe de Amherst? Ei, você deveria ter me falado que tinha isso. Eu, Shanks, reconheço a grandeza quando a vejo, e desde que você tenha sido recomendado por Lucas, eu vejo que você tem um grande, grande potencial como um aventureiro. De jeito nenhum eu iria cobrá-lo por esta viagem!");
        else
            cm.sendNext("Entediado deste lugar? Aqui... Me dê #e150 mesos#n primeiro...");
    }
     else if (status == 2) 
     {
        if (cm.haveItem(4031801))
            cm.sendNextPrev("Desde que você tenha a carta de recomendação, não lhe cobrarei por isso. Tudo bem, apertem os cintos, porque vamos para Ilha Victoria, e pode ser um pouco turbulento!!");
        else
        if (cm.getLevel() > 6) 
        {
            if (cm.getMeso() < 150) 
            {
                cm.sendOk("O que? Você está me dizendo que queria ir sem nenhum dinheiro? Você é uma pessoa estranha...");
                cm.dispose();
            }
             else
                cm.sendNext("Perfeito! #e150#n mesos aceitos! Tudo bem, vamos para Ilha Victoria!");
        } 
        else 
        {
            cm.sendOk("Vamos ver ... Eu não acho que você é forte o suficiente. Você vai ter que ser, pelo menos, Level 7 para ir à Ilha Victoria.");
            cm.dispose();
        }
    }
     else if (status == 3) 
     {
        if (cm.haveItem(4031801))
            cm.gainItem(4031801, -1);
            
        cm.warp(2010000);
        cm.dispose();
    }
}