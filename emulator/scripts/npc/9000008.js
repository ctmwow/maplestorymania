/*
 * MaplePhoenix
 * Zero
 * Learn maker skills 
 * Proper way of learning Maker skills by job and level 
 */

var status;
var cost = 5000000;
 
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
       if (status == 0) {  
           if (cm.getLevel() < 45) {
               cm.sendOk("You need to level 45 in order to learn Maker Skill..");
               cm.dispose(); 
               return;
           }
           if (cm.getLevel() >= 45 && cm.getPlayer().getSkillLevel(10001007) == 0 && cm.getJobId() >= 1000 && cm.getJobId() <= 1512 || cm.getPlayer().getSkillLevel(20001007) == 0 && cm.getJobId() >= 2000 || cm.getPlayer().getSkillLevel(1007) == 0 && cm.getJobId() >= 100 && cm.getJobId() <= 1000)  {
               cm.sendSimple("Hello, Are you interested on Maker skill? \r\n #L1# #bLearn Maker Skill#l#l");
           } else if (cm.getLevel() >= 75 && cm.getPlayer().getSkillLevel(10001007) == 1 && cm.getJobId() >= 1000 && cm.getJobId() <= 1512 || cm.getPlayer().getSkillLevel(20001007) == 1 && cm.getJobId() >= 2000 || cm.getPlayer().getSkillLevel(1007) == 1 && cm.getJobId() >= 100 && cm.getJobId() <= 1000)  {
               cm.sendSimple("Hello, Are you interested upgrading Maker skill? \r\n #L2# Upgrade Level 2 Maker Skill (5,000,000 Mesos)#l");
           } else if (cm.getLevel() >= 105 && cm.getPlayer().getSkillLevel(10001007) == 2 && cm.getJobId() >= 1000 && cm.getJobId() <= 1512 || cm.getPlayer().getSkillLevel(20001007) == 2 && cm.getJobId() >= 2000 || cm.getPlayer().getSkillLevel(1007) == 2 && cm.getJobId() >= 100 && cm.getJobId() <= 1000)  {
               cm.sendSimple("Hello, Are you interested upgrading Maker skill? \r\n #L3# Upgrade Level 3 Maker Skill (5,000,000 Mesos)#l");
           }
       }
       if (selection == 1) {
           if (cm.getLevel() >= 45) { 
               if (cm.getPlayer().getSkillLevel(10001007) > 0 || cm.getPlayer().getSkillLevel(20001007) > 0 || cm.getPlayer().getSkillLevel(1007) > 0) {
                   cm.sendOk("You have learned Maker skill..");
                   cm.dispose(); 
                   return;
               }
               if (cm.getJobId() >= 2000) { 
                   cm.teachSkill(20001007 , 1, 3, -1);
               } else if (cm.getJobId() >= 1000) {
                   cm.teachSkill(10001007 , 1, 3, -1);
               } else {
                   cm.teachSkill(1007 , 1, 3, -1);
               }
               cm.sendOk("I have taught you Maker skill. Please come back again to upgrade your Maker Skill at level 75.");
               cm.dispose();
 
           }
       }
       if (selection == 2) { 
           if (cm.getLevel() < 75) {
               cm.sendOk("You need to reach level 75 to upgrade Maker Skill..");
               cm.dispose(); 
               return;
           } else if (cm.getMeso() < cost) {
               cm.sendOk("It looks like you don't have enough mesos.");
               cm.dispose();
               return;
           }
           if (cm.getLevel() >= 75 && cm.getPlayer().getSkillLevel(10001007) == 1 && cm.getJobId() >= 1000 && cm.getJobId() <= 1512 || cm.getPlayer().getSkillLevel(20001007) == 1 && cm.getJobId() >= 2000 || cm.getPlayer().getSkillLevel(1007) == 1 && cm.getJobId() >= 100 && cm.getJobId() <= 1000) {
               if (cm.getJobId() >= 2000) { 
                   cm.teachSkill(20001007, 2, 3, -1);
               } else if (cm.getJobId() >= 1000) {
                   cm.teachSkill(10001007, 2, 3, -1);
               } else {
                   cm.teachSkill(1007, 2, 3, -1);
               }
               cm.gainMeso(-5000000);
               cm.sendOk("Upgraded to level 2 Maker skills.. Please come back when you're level 105");
               cm.dispose();
           } else {
               cm.sendOk("You have already learned level 2 Maker skills, please come back when you're level 105..");
               cm.dispose();
           }
       }
       if (selection == 3) {
           if (cm.getLevel() < 105) {
               cm.sendOk("You need to reach level 105 in order to upgrade Maker Skill..");
               cm.dispose(); 
               return;
           } else if (cm.getMeso() < cost) {
               cm.sendOk("It looks like you don't have enough mesos.");
               cm.dispose();
               return;
           }
           if (cm.getLevel() >= 105) {
               if (cm.getPlayer().getSkillLevel(10001007) == 2 && cm.getJobId() >= 1000 && cm.getJobId() <= 1512 || cm.getPlayer().getSkillLevel(20001007) == 2 && cm.getJobId() >= 2000 || cm.getPlayer().getSkillLevel(1007) == 2 && cm.getJobId() >= 100 && cm.getJobId() <= 1000) {
                   if (cm.getJobId() >= 2000) { 
                       cm.teachSkill(20001007, 3, 3, -1);
                   } else if (cm.getJobId() >= 1000) {
                       cm.teachSkill(10001007, 3, 3, -1);
                   } else {
                       cm.teachSkill(1007, 3, 3, -1);
                   }
                   cm.gainMeso(-5000000);
                   cm.sendOk("Congratulations for upgrading Maker skill to Level 3");
                   cm.dispose();
               } else {
                   cm.sendOk("You have already learned Maker skill...");
                   cm.dispose();   
               }
           }
       }
   }
}
         