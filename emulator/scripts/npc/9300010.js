/*
 * AP reset 
 * MaplePhoenix
 * Zero
 */
var status;

var price = 5000000
 
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
           if (cm.getLevel() < 10) {
               cm.sendOk("You must be at least level 10 to use this NPC.");
               cm.dispose(); 
               return;
           } 
           if (cm.getLevel() >= 10 && cm.getJobId() >= 200 && cm.getJobId() < 233 || cm.getJobId() >= 1200 && cm.getJobId() < 1212) {
               cm.sendSimple("Hello, I'm here to reset your AP.\r\nEach reset will cost 5,000,000 mesos.\r\n#L1# #bReset all my stats#l");
           } else {
               cm.sendSimple("Hello, I'm here to reset your AP.\r\nEach reset will cost 5,000,000 mesos.\r\n#L1# #bReset my Str, Dex & Luk#l");
           }
       }
       if (selection == 1) {
           if (cm.getMeso() < price) {
               cm.sendOk("It looks like you don't have enough mesos.");
               cm.dispose(); 
               return;
           } 
           if (cm.getJobId() == 200 || cm.getJobId() == 210 || cm.getJobId() == 211 || cm.getJobId() == 212 || cm.getJobId() == 220 || cm.getJobId() == 221 || cm.getJobId() == 222 || cm.getJobId() == 230 || cm.getJobId() == 231 || cm.getJobId() == 232 || cm.getJobId() == 1200 || cm.getJobId() == 1210 || cm.getJobId() == 1211) {
               cm.resetStats(4,4,4,4);
               cm.gainMeso(-5000000);
               cm.sendOk("Done, please come back again if you need me.");
               cm.dispose();
           } else {
               cm.resetStats(4,4,4);
               cm.gainMeso(-5000000);
               cm.sendOk("Done, please come back again if you need me.");
               cm.dispose();
           }
       }
   }
}