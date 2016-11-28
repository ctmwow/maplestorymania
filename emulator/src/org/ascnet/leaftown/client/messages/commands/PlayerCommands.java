/*
 * This file is part of AscNet Leaftown.
 * Copyright (C) 2014 Ascension Network
 *
 * AscNet Leaftown is a fork of the OdinMS MapleStory Server.
 * The following is the original copyright notice:
 *
 *     This file is part of the OdinMS Maple Story Server
 *     Copyright (C) 2008 Patrick Huy <patrick.huy@frz.cc>
 *                        Matthias Butz <matze@odinms.de>
 *                        Jan Christian Meyer <vimes@odinms.de>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License version 3
 * as published by the Free Software Foundation. You may not use, modify
 * or distribute this program under any other version of the
 * GNU Affero General Public License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.ascnet.leaftown.client.messages.commands;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.ascnet.leaftown.client.MapleCharacter;
import org.ascnet.leaftown.client.MapleClient;
import org.ascnet.leaftown.client.MapleStat;
import org.ascnet.leaftown.client.messages.Command;
import org.ascnet.leaftown.client.messages.CommandDefinition;
import org.ascnet.leaftown.client.messages.MessageCallback;
import org.ascnet.leaftown.scripting.npc.NPCScriptManager;

public class PlayerCommands implements Command 
{
	@Override
	public void execute(MapleClient c, MessageCallback mc, String[] splitted) throws Exception 
	{
		final MapleCharacter player = c.getPlayer();
		
		if (splitted[0].equalsIgnoreCase("@str")) 
		{
			int up = 0;
			
			try 
			{
				up = Integer.parseInt(splitted[1]);
			}
			catch (Exception e) 
			{
				mc.dropMessage("Por favor, informe uma quantia válida de AP para adicionar em STR. Você ainda tem " + player.getRemainingAp() + " AP para utilizar.");
			}
			
			if (player.getRemainingAp() <= 0) 
				mc.dropMessage("Você não tem mais nenhum AP para usar.");
			else if (up == 0 || up < 1 || player.getRemainingAp() < up)
				mc.dropMessage("Por favor, informe uma quantia válida de AP para adicionar em STR. Você ainda tem " + player.getRemainingAp() + " AP para utilizar.");
			else if (player.getStr() == 999)
				mc.dropMessage("Você chegou ao máximo de pontos no status.");
			else if (player.getStr() + up > 999)
				mc.dropMessage("O máximo de pontos em STR é 999. Você ainda pode adicionar " + (999 - player.getStr()) + " pontos.");
			else 
			{
				player.setStr(player.getStr() + up);
				player.setRemainingAp(player.getRemainingAp() - up);
				player.updateSingleStat(MapleStat.AVAILABLEAP, player.getRemainingAp());
				player.updateSingleStat(MapleStat.STR, player.getStr());
			}
		} 
		else if (splitted[0].equalsIgnoreCase("@dex")) 
		{
			int up = 0;
			try 
			{
				up = Integer.parseInt(splitted[1]);
			}
			catch (Exception e) 
			{
				mc.dropMessage("Por favor, informe uma quantia válida de AP para adicionar em DEX. Você ainda tem " + player.getRemainingAp() + " AP para utilizar.");
			}
			
			if (player.getRemainingAp() <= 0)
				mc.dropMessage("Você não tem mais nenhum AP para usar.");
			else if (up == 0 || up < 1 || player.getRemainingAp() < up)
				mc.dropMessage("Por favor, informe uma quantia válida de AP para adicionar em DEX. Você ainda tem " + player.getRemainingAp() + " AP para utilizar.");
			else if (player.getDex() == 999)
				mc.dropMessage("Você chegou ao máximo de pontos no status.");
			else if (player.getDex() + up > 999)
				mc.dropMessage("O máximo de pontos em DEX é 999. Você ainda pode adicionar " + (999 - player.getDex()) + " pontos.");
			else
			{
				player.setDex(player.getDex() + up);
				player.setRemainingAp(player.getRemainingAp() - up);
				player.updateSingleStat(MapleStat.AVAILABLEAP, player.getRemainingAp());
				player.updateSingleStat(MapleStat.DEX, player.getDex());
			}
		}
		else if (splitted[0].equalsIgnoreCase("@int")) 
		{
			int up = 0;
			try 
			{
				up = Integer.parseInt(splitted[1]);
			} 
			catch (Exception e) 
			{
				mc.dropMessage("Por favor, informe uma quantia válida de AP para adicionar em INT. Você ainda tem " + player.getRemainingAp() + " AP para utilizar.");
			}
			
			if (player.getRemainingAp() <= 0)
				mc.dropMessage("Você não tem mais nenhum AP para usar.");
			else if (up == 0 || up < 1 || player.getRemainingAp() < up)
				mc.dropMessage("Por favor, informe uma quantia válida de AP para adicionar em INT. Você ainda tem " + player.getRemainingAp() + " AP para utilizar.");
			else if (player.getInt() == 999)
				mc.dropMessage("Você chegou ao máximo de pontos no status.");
			else if (player.getInt() + up > 999)
				mc.dropMessage("O máximo de pontos em INT é 999. Você ainda pode adicionar " + (999 - player.getInt()) + " pontos.");
			else 
			{
				player.setInt(player.getInt() + up);
				player.setRemainingAp(player.getRemainingAp() - up);
				player.updateSingleStat(MapleStat.AVAILABLEAP, player.getRemainingAp());
				player.updateSingleStat(MapleStat.INT, player.getInt());
			}
		} 
		else if (splitted[0].equalsIgnoreCase("@luk")) 
		{
			int up = 0;
			try 
			{
				up = Integer.parseInt(splitted[1]);
			} 
			catch (Exception e) 
			{
				mc.dropMessage("Por favor, informe uma quantia válida de AP para adicionar em LUK. Você ainda tem " + player.getRemainingAp() + " AP para utilizar.");
			}
			
			if (player.getRemainingAp() <= 0)
				mc.dropMessage("Você não tem mais nenhum AP para usar.");
			else if (up == 0 || up < 1 || player.getRemainingAp() < up)
				mc.dropMessage("Por favor, informe uma quantia válida de AP para adicionar em LUK. Você ainda tem " + player.getRemainingAp() + " AP para utilizar.");
			else if (player.getLuk() == 999)
				mc.dropMessage("Você chegou ao máximo de pontos no status.");
			else if (player.getLuk() + up > 999)
				mc.dropMessage("O máximo de pontos em LUK é 999. Você ainda pode adicionar " + (999 - player.getLuk()) + " pontos.");
			else 
			{
				player.setLuk(player.getLuk() + up);
				player.setRemainingAp(player.getRemainingAp() - up);
				player.updateSingleStat(MapleStat.AVAILABLEAP, player.getRemainingAp());
				player.updateSingleStat(MapleStat.LUK, player.getLuk());
			}
		} 
		else if (splitted[0].equalsIgnoreCase("@ajuda")) 
		{
			if (splitted.length < 2) 
				mc.dropMessage("Por enquanto, temos só uma página de ajuda. Por favor, utilize @ajuda 1 para exibi-lá!");
			else 
			{
                switch (splitted[1]) 
                {
                    case "1":
                        mc.dropMessage("--- PÁGINA [1] ---Os comandos do player estão listados e explicados abaixo:");
                        mc.dropMessage("@ajuda <no.> mostra a lista de comandos, sendo <no.> o número da página");
                        mc.dropMessage("@str, @int, @dex & @luk ajudam à adicionar seus pontos mais rápidamente");
                        mc.dropMessage("@horarioAtual mostra o horário atual do servidor.");
                        mc.dropMessage("@listaGM mostra todos os GameMasters oficiais do servidor.");
                        mc.dropMessage("@cancelar cancela todas as ações atuais. Se alguma ação está com problema, deixará você apto a falar com npcs novamente");
                        break;
                    default:
                        mc.dropMessage("Número da página inválido. Use @ajuda <numero da página> para utilizar essa função.");
                        break;
                }
			}
		} 
		else if (splitted[0].equalsIgnoreCase("@listaGM")) 
		{
			mc.dropMessage("MapleStory Mania GMs: " + c.getChannelServer().getGMList());
		}
		else if (splitted[0].equals("@cancelar")) 
		{
			NPCScriptManager.getInstance().dispose(c);
			if (c.getCM() != null)
				c.getCM().dispose();
			if (c.getQM() != null) 
				c.getQM().dispose();
			mc.dropMessage("Todas as ações foram canceladas. Você está apto à falar com os NPC's novamente!");
		}
		else if (splitted[0].equals("@horarioAtual")) 
		{
			mc.dropMessage(new SimpleDateFormat("dd/MM/yyyy hh:mm:ss").format(new Date()));
		} 
	}

	@Override
	public CommandDefinition[] getDefinition() 
	{
		return new CommandDefinition[]
		{
				new CommandDefinition("str", "", "Adiciona seus pontos em STR.", 0x00000000),
				new CommandDefinition("dex", "", "Adiciona seus pontos em DEX.", 0x00000000),
				new CommandDefinition("int", "", "Adiciona seus pontos em INT.", 0x00000000),
				new CommandDefinition("luk", "", "Adiciona seus pontos em LUK.", 0x00000000),
				new CommandDefinition("listaGM", "", "Exibe a lista de GameMasters.", 0x00000000),
				new CommandDefinition("ajuda", "", "Exibe todos os comandos disponíveis", 0x00000000),
				new CommandDefinition("cancelar", "", "Você será capaz de falar novamente com os NPC's", 0x00000000),
				new CommandDefinition("horarioAtual", "", "Exibe o horário atual do servidor.", 0x00000000),
		};
	}
}