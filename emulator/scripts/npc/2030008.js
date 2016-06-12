/* Adobis
 * 
 * El Nath: The Door to Zakum (211042300)
 * 
 * Custom Zakum Quest NPC 
 * MaplePhoenix, Zero
 * Eye of Fire & Dead Mine Scroll
 */

var status;

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
            if (cm.getPlayer().getLevel() >= 50) {
                cm.sendSimple("Beware, for the power of olde has not been forgotten... #b\r\n#L2#Forging the Eyes of Fire#l \r\n#L3#Forging the Dead Mine Scroll#l");
            } else if (cm.getPlayer().getLevel() < 50) { 
                cm.sendOk("You want to be permitted to do the Zakum Dungeon Quest?  Well, I, #bAdobis#k... judge you to be suitable.  You should be safe roaming around the dungeon.  Just be careful...");
                cm.dispose();
            }
        }
        if (selection == 2) {
            if (cm.haveItem(4000082, 40)) {
                cm.gainItem(4000082,-40);
                cm.gainItem(4001017, 5);
                cm.sendOk("Thank you for the teeth!  Next time you see me, I'll be blinging harder than #rJaws#k! Goodbye and good luck!");
                cm.dispose();
            } else if (!cm.haveItem(4000082, 40)) {
                cm.sendOk("Please collect 40 #i4000082# for me..");
                cm.dispose();
            } else if (!cm.canHoldSlots(1)) {
                cm.sendOk("Please ensure your inventory is not full.");
                cm.dispose();
            } else {
                cm.sendOk("You shtill didn't get me my teef! Howsh a man shupposhed to conshentrate wifout teef?");
                cm.dispose();
            }  
        }
        
        if (selection == 3) {
            if (cm.haveItem(4000082, 10)) {
                cm.gainItem(4000082, -10);
                cm.gainItem(2030007, 1);
                cm.sendOk("Thank you for the teeth!  Next time you see me, I'll be blinging harder than #rJaws#k! Goodbye and good luck!");
                cm.dispose();
            } else if (!cm.haveItem(4000082, 10)) {
                cm.sendOk("Please collect 10 #i4000082# for me..");
                cm.dispose();
            } else if (!cm.canHoldSlots(1)) {
                cm.sendOk("Please ensure your inventory is not full.");
                cm.dispose();
            } else {
                cm.sendOk("You shtill didn't get me my teef! Howsh a man shupposhed to conshentrate wifout teef?");
                cm.dispose();
            }  
        }
    }
}