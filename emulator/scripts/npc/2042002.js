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
				cm.sendSimple("O que você gostaria de fazer? Se você nunca participou do Monster Carnival, você precisará saber uma coisa ou duas sobre antes de entrar.\r\n#b#L0# Vá para o Monster Carnival Field #l");
				break;
			default:
				status = 998;
			    cm.sendOk("Ops! Parece que o Festival de Monstros se encerrou.");
				break;
		}
    } 
    else if (status == 1) 
    {
        switch (selection) 
        {
            case 0: 
            {
                var level = cm.getPlayerStat("LVL");
                
                if ( level < 30)
                    cm.sendOk("I'm sorry, but only the users Level 30+ may participate in Monster Carnival.");
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
            exp = 7500;
        } 
        else if (carnivalparty.getTotalCP(cm.getPlayer().getTeam()) >= 251) 
        {
            rank = "B";
            exp = 6000;
        } 
        else if (carnivalparty.getTotalCP(cm.getPlayer().getTeam()) >= 101) 
        {
            rank = "C";
            exp = 3000;
        } 
        else if (carnivalparty.getTotalCP(cm.getPlayer().getTeam()) >= 0) 
        {
            rank = "D";
            exp = 1000;
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
            cm.gainExp(exp / 2);
        
        cm.warp(980000000, "st00");
		cm.dispose();
    }
}