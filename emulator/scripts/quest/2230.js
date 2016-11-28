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
/* 
    Author :        Generic
    NPC Name:       Mar the Fairy
    Map(s):         Everywhere
    Description:    Quest - A Mysterious Small Egg
    Quest ID:       2230
*/
 
var status = -1;
 
function start(mode, type, selection)
{
    if (mode == -1)
    {
        qm.dispose();
    }
    else
    {
        if (mode == 1)
            status++;
        else
            status--;
        if (status == 0)
            qm.sendNext("Coloquei esta pequena e preciosa vida em suas mãos... Guarde-a com a sua vida...");
        else if (status == 1)
            qm.sendYesNo("Cuidar de outra vida... Essa é a inevitável missão dada a você... Siga a força que leva você até mim.");
        else if (status == 2) {
            qm.sendOk("Coloque a mão no bolso. Acho que o seu amigo já encontrou você.\r\nA campânula roxa que embebe no sol entre as árvores arranha-céus... Siga o caminho para o desconhecido que leva você até a campânula. Vou esperar por você aqui.");
            qm.forceStartQuest();
            qm.gainItem(4032086, 1); // Mysterious Egg * 1
        }
    }
}
 
function end(mode, type, selection)
{
    if (mode == -1)
    {
        qm.dispose();
    }
     else
     {
        if (mode == 1)
            status++;
        else
            status--;
        if (status == 0)
            qm.sendSimple("Olá, viajante... Finalmente veio me ver. Você já cumpriu seus deveres? \r\n #b#L0#Que deveres? Quem é você?#l#k");
        else if (selection == 0 && status == 1)
        {
            qm.sendNext("Encontrou um pequeno ovo no bolso? Esse ovo é seu dever, sua responsabilidade. A vida é difícil quando você está sozinho. Em momentos como este, não há nada como ter um amigo que estará lá para você em todos os momentos. Você já ouviu falar de um #bbicho de estimação#k?\r\nAs pessoas criam bichos de estimação para aliviar a carga, tristeza e solidão, porque saber que você tem alguém, ou algo parecido, do seu lado vai realmente trazer uma paz de espírito. Mas tudo tem consequências, e com isso vem a responsabilidade ...");
        }
        else if (status == 2)
        {
            qm.sendNextPrev("Criar um bicho de estimação requer uma quantidade enorme de responsabilidade. Lembre-se de um bicho de estimação é uma forma de vida, também, então você vai precisar para alimentá-lo, dar nome a ele, compartilhar seus pensamentos com ele, e, finalmente, formar um vínculo. É assim que os donos se apegam a estes bichos de estimação.");
        }
        else if (status == 3)
        {
            qm.sendNextPrev("Eu queria introduzir isso em você, e é por isso que eu lhe mandei um bebê que eu aprecio. O ovo que você trouxe é #bCaracol de Runa#k, uma criatura que nasce através do poder da mana. Desde que você tomou grande cuidado trazendo ele até aqui, o ovo irá chocar em breve.");
        }
         else if (status == 4)
         {
            qm.sendNextPrev("Caracol de Runa é um bicho de estimação com muitas habilidades. Vai pegar itens, alimentá-lo com poções, e fazer outras coisas que irá surpreendê-lo. A desvantagem é que, desde que o Caracol de Runa nasceu fora do poder de Mana, sua vida útil é muito curta. Uma vez que se transforma em uma boneca, nunca será capaz de ser revivido.");
        }
         else if (status == 5)
         {
            qm.sendYesNo("Agora você entende? Cada ação vem com conseqüências, e bichos de estimação não são exceção. O ovo do caracol deve chocar em breve.");
        }
         else if (status == 6)
         {
            qm.gainItem(5000054, 1); // rune snail * 1
            qm.gainItem(4032086, -1); // Mysterious Egg * -1
            qm.forceCompleteQuest();
            qm.sendNext("Este caracol só estará vivo por #b5 horas#k. Inunde-o com amor. Seu amor será retribuído no final.");
            qm.dispose();
        }
    }
}