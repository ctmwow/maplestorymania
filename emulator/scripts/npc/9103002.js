/*
@author Jvlaple
*Prize List by Shedder
*Rolly gives you a random item :D
*/
var status = 0;
var itemArray = Array(2040601, 2040001, 2040504, 2040901, 2040401, 2040605, 2041027, 2041029, 2041029, 2040511, 2040905, 2040405, 2040602, 2041017, 2041023, 2041020, 2040002, 2040902, 2040402, 2000006, 2001000, 2022000, 2030009, 2030008, 2030009, 2001002, 2001001, 2000012, 2000005, 2020007, 2022018, 2020006, 2020008, 2020010, 1302016, 1032013, 1442017, 1322025);      

var itemQuan = Array(1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 100, 50, 50, 20, 20, 20, 5, 5, 5, 1, 100, 50, 100, 20, 20, 1, 1, 1, 1);                                          

function start() {
    status = -1;
    action(1, 0, 0);
}

function action(mode, type, selection) {
		if (mode == -1) {
		cm.dispose();
		} else { 
		  if (status >= 2 && mode == 0) { 
		   cm.dispose(); 
		   return; 
		} 
        if (mode == 1)
            status++;
        else
            status--;
        if (status == 0) {
			cm.sendYesNo("Seu grupo fez um esforço imenso e coletou o maior número possível de cupons. Por causa disto, eu tenho um presente para cada um de vocês. Após receber o presente, você será levado de volta para Ludibrium. Bem, deseja receber o presente agora mesmo?");
        } else if (status == 1) {
			var randmm = Math.floor(Math.random() * itemArray.length);
			if (cm.canHold(itemArray[randmm])) {
				cm.gainItem(itemArray[randmm], itemQuan[randmm]);
				cm.removeAll(4001106);
				cm.warp(220000000);
			} else {
				cm.sendNext("Seu inventário está muito cheio para receber sua recompensa.");
			}
			cm.dispose();
		}
    }
}