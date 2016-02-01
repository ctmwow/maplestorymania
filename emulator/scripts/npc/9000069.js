/*
 * MaplePhoenix
 * Zero
 * Aran & Cygnus Mount 
 */
var status;

var cost1 = 1000000;
var cost2 = 10000000;
var cost3 = 15000000;
 
function start() {
    status = -1;
    action(1, 0, 0);
}

function action(mode, type, selection) {
   if (mode == -1) {
       cm.dispose();
   } else {
       if (status >= 0 && mode == 0) {
           cm.dispose();
           return;
       }
       if (mode == 1) {
           status++;
       } else {
           status--;
       }
       if (status == 0) {  // aran
           if (cm.getLevel() < 50) {
               cm.sendOk("You must be at least level 50 to use this NPC.");
               cm.dispose(); 
		return;
            } 
            if (cm.getLevel() >= 50 && cm.getJobId() == 1100 || cm.getJobId() == 1110 || cm.getJobId() == 1111 || 
                cm.getJobId() == 1200 || cm.getJobId() == 1210 || cm.getJobId() == 1211 || 
                cm.getJobId() == 1300 || cm.getJobId() == 1310 || cm.getJobId() == 1311 || 
                cm.getJobId() == 1400 || cm.getJobId() == 1410 || cm.getJobId() == 1411 ||
                cm.getJobId() == 1500 || cm.getJobId() == 1510 || cm.getJobId() == 1511) {
                
                cm.sendSimple("Hello, I'm Temporary Cygnus Of Knight Mount NPC. \r\n #L1000# #bLearn Monster Rider Skill#l\r\n #L1001# #bLevel 50 Mimiana Mount (1,000,000 Mesos)#l\r\n #L1002# Level 100 Mimio Mount (10,000,000 Mesos)#l\r\n #L1003# Level 120 Shinjo Mount (15,000,000 Mesos)#l");
            } else if (cm.getLevel() >= 50 && cm.getJobId() == 2110 || cm.getJobId() == 2111 || cm.getJobId() == 2112) {
               cm.sendSimple("Hello, I'm Temporary Aran Mount NPC. \r\n #L10# #bLearn Aran Monster Rider Skill#l\r\n #L1# #bLevel 50 Aran Mount (1,000,000 Mesos)#l\r\n #L2# Level 100 Aran Mount (10,000,000 Mesos)#l");
            } else {
	       cm.sendOk("You are not an Aran or Knight Of Cygnus..");
               cm.dispose(); 
	    }
       }
       if (selection == 1000 ) {
           if (cm.getLevel() >= 50 && cm.getJobId() == 1100 || cm.getJobId() == 1110 || cm.getJobId() == 1111 || 
                cm.getJobId() == 1200 || cm.getJobId() == 1210 || cm.getJobId() == 1211 || 
                cm.getJobId() == 1300 || cm.getJobId() == 1310 || cm.getJobId() == 1311 || 
                cm.getJobId() == 1400 || cm.getJobId() == 1410 || cm.getJobId() == 1411 ||
                cm.getJobId() == 1500 || cm.getJobId() == 1510 || cm.getJobId() == 1511) {
                
                if (cm.getPlayer().getSkillLevel(10001004) == 0) {
                   cm.teachSkill(10001004 , 1, 1, -1);
                   cm.sendOk("You have learned Monster Rider skill..");
                   cm.dispose(); 
               } else {
                   cm.sendOk("You have already learned Monster Rider skill. ");
                   cm.dispose(); 
               }
           }
       }
       
       if (selection == 1001) { 
           if (cm.getLevel() >= 50) {
               if (cm.haveItem(1902005)) {
                   cm.sendOk("You have already received Mimiana");
                   cm.dispose(); 
               } else if (cm.canHoldSlots(2)) {
                   cm.gainItem(1912005);
                   cm.gainItem(1902005);
                   cm.gainMeso(-1000000);
                   cm.sendOk("Enjoy");
                   cm.dispose();
               } else if (cm.getMeso() < cost1) {
                   cm.sendOk("It looks like you don't have enough mesos.");
                   cm.dispose();
               } else {
                   cm.sendOk("Please check your inventory slot..");
                   cm.dispose();
               }
           }
       }
       
       if (selection == 1002) {
           if (cm.getLevel() < 100) {
               cm.sendOk("You must be level 100 in order to purcahse this..");
               cm.dispose(); 
           }
           if (cm.getLevel() >= 100) {
               if (cm.haveItem(1902006)) {
                   cm.sendOk("You have already received level 100 Mimio Mount");
                   cm.dispose(); 
               } else if (!cm.haveItem(1902005)) {
                   cm.sendOk("You need to purchase Level 50 Mimiana Mount before getting Level 100 Mimio Mount.");
                   cm.dispose(); 
               } else if (cm.getMeso() < cost2) {
                   cm.sendOk("It looks like you don't have enough mesos.");
                   cm.dispose();
               } else if (cm.canHoldSlots(2)) {
                   cm.gainItem(1902006);
                   cm.gainMeso(-10000000);
                   cm.sendOk("Enjoy");
                   cm.dispose();
               } else {
                   cm.sendOk("Please check your inventory slot..");
                   cm.dispose();
               }
           }
       }
       
       if (selection == 1003) {
           if (cm.getLevel() < 120) {
               cm.sendOk("You must be level 120 in order to purcahse this..");
               cm.dispose(); 
           }
           if (cm.getLevel() >= 100) {
               if (cm.haveItem(1902007)) {
                   cm.sendOk("You have already received level 120 Shinjo Mount");
                   cm.dispose(); 
               } else if (!cm.haveItem(1902006)) {
                   cm.sendOk("You need to purchase Level 100 Mimio before getting Level 120 Shinjo Mount");
                   cm.dispose(); 
               } else if (cm.getMeso() < cost3) {
                   cm.sendOk("It looks like you don't have enough mesos.");
                   cm.dispose();
               } else if (cm.canHoldSlots(2)) {
                   cm.gainItem(1902007);
                   cm.gainMeso(-15000000);
                   cm.sendOk("Enjoy");
                   cm.dispose();
               } else {
                   cm.sendOk("Please check your inventory slot..");
                   cm.dispose();
               }
           }
       }
       
       if (selection == 10) {
           if (cm.getLevel() >= 50 && cm.getJobId() == 2110 || cm.getJobId() == 2111 || cm.getJobId() == 2112) {
               if (cm.getPlayer().getSkillLevel(20001004) == 0) {
                   cm.teachSkill(20001004 , 1, 1, -1);
                   cm.sendOk("You have learned Monster Rider skill..");
                   cm.dispose(); 
               } else {
                   cm.sendOk("You have already learned Monster Rider skill. ");
                   cm.dispose(); 
               }
           }
       }
       if (selection == 1) { 
           if (cm.getLevel() >= 50) {
               if (cm.haveItem(1902015)) {
                   cm.sendOk("You have already received level 50 Aran Mount");
                   cm.dispose(); 
               } else if (cm.canHoldSlots(2)) {
                   cm.gainItem(1902015);
                   cm.gainItem(1912011);
                   cm.gainMeso(-1000000);
                   cm.sendOk("Enjoy");
                   cm.dispose();
               } else if (cm.getMeso() < cost1) {
                   cm.sendOk("It looks like you don't have enough mesos.");
                   cm.dispose();
               } else {
                   cm.sendOk("Please check your inventory slot..");
                   cm.dispose();
               }
           }
       }
       if (selection == 2) {
           if (cm.getLevel() < 100) {
               cm.sendOk("You must be level 100 in order to purcahse this..");
               cm.dispose(); 
           }
           if (cm.getLevel() >= 100) {
               if (cm.haveItem(1902016)) {
                   cm.sendOk("You have already received level 100 Aran Mount");
                   cm.dispose(); 
               } else if (!cm.haveItem(1902015)) {
                   cm.sendOk("You need to purchase Level 50 Aran Mount before getting Level 100 Aran Mount.");
                   cm.dispose(); 
               } else if (cm.getMeso() < cost2) {
                   cm.sendOk("It looks like you don't have enough mesos.");
                   cm.dispose();
               } else if (cm.canHoldSlots(2)) {
                   cm.gainItem(1902016);
                   cm.gainMeso(-10000000);
                   cm.sendOk("Enjoy");
                   cm.dispose();
               } else {
                   cm.sendOk("Please check your inventory slot..");
                   cm.dispose();
               }
           }
       }
   }
}
         