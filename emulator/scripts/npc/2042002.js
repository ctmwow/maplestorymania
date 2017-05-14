/* 
 * Spiegelmann - Monster Carnival
 */
importPackage(java.lang);

var status = -1;
var rank = "D";
var exp = 0;

function start() 
{
    if (cm.getPlayer().getMonsterCarnival() != null)
		status = 99;
		
    action(1, 0, 0);
}
 
function action(mode, type, selection) 
{
    (mode == 1 ? status++ : mode == 0 ? status-- : cm.dispose());
    
	if (status == 999)
	{
		cm.warp(980000000, "st00");
		cm.dispose();
	}
    else if (status == 0) 
    {
		switch(cm.getPlayer().getMapId()) 
		{
			case 980000000:
			case 103000000:
				cm.sendSimple("O que você gostaria de fazer? Se você nunca participou do Festival de Monstros, você precisará saber uma coisa ou duas sobre antes de entrar.\r\n#b#L0# Vá para o Monster Carnival Field #l");
				break;
			default:
				status = 998;
				break;
		}
    } 
    else if (status == 1) 
    {
        switch (selection) 
        {
            case 0: 
            {
                var level = cm.getPlayer().getLevel();                
                if (level < 30 || level > 50)
                    cm.sendOk("Desculpe-me, mas somente jogadores de entre os leveis 30 e 50 podem participar do Festival de Monstros.");
                else
                    cm.warp(980000000, "st00");
                cm.dispose();
            }
            default: 
            {
                cm.dispose();
                break;
            }
            break;
        }
    } 
    else if (status == 100) 
    {
        var carnivalparty = cm.getPlayer().getMonsterCarnival();
        
        if (carnivalparty.getTotalCP(cm.getPlayer().getTeam()) >= 501) 
        {
            rank = "A";
            exp = 30000;
        } 
        else if (carnivalparty.getTotalCP(cm.getPlayer().getTeam()) >= 251) 
        {
            rank = "B";
            exp = 25000;
        } 
        else if (carnivalparty.getTotalCP(cm.getPlayer().getTeam()) >= 101) 
        {
            rank = "C";
            exp = 15000;
        } 
        else if (carnivalparty.getTotalCP(cm.getPlayer().getTeam()) >= 0) 
        {
            rank = "D";
            exp = 7500;
        }
        
        if (carnivalparty.isWinner(cm.getPlayer().getTeam()))
            cm.sendOk("Você ganhou a batalha, incrível performance jogador! A vitória é sua. \r\n#bRanque do Festival de Monstros : " + rank);
        else
            cm.sendOk("Infelizmente você perdeu a batalha, apesar da sua incrível performace. A vitória poderá ser sua numa próxima vez. \r\n#bRanque do Festival de Monstros : " + rank);
    } 
    else if (status == 101) 
    {
        var carnivalparty = cm.getPlayer().getMonsterCarnival();
       
        if (carnivalparty.isWinner(cm.getPlayer().getTeam()))
            cm.gainExp(exp);
        else 
		{
			switch (rank)
            {
				case "A": // 10000
				case "B": // 8500
					cm.gainExp(exp / 3);
					break;
				case "C": // 7500
					cm.gainExp(exp / 2);
				case "D": // 1000
					cm.gainExp(1000);
			}
		}
		cm.saveLocation("MIRROR"); //salvar o local que o jogador estava antes de teleportar
        cm.warp(980000000, "st00");
		cm.dispose();
    }
	else if (status == 998)
	{
		cm.sendOk("Ops! Parece que o Festival de Monstros se encerrou.");
		cm.dispose();
	}
}