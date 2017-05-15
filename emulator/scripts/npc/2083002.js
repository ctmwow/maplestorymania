 importPackage(Packages.org.ascnet.leaftown.server.expeditions);
 
function start() {
    cm.sendYesNo("Do you wish to leave?");
}

function action(mode, type, selection) {
    var horn = MapleExpeditionType.HORNTAIL;
    var expeditionz = cm.getExpedition(horn);
    if (mode < 1)
        cm.dispose();
    else {
        if (cm.getPlayer().getMap().getCharacters().size() < 2){
            cm.getPlayer().getMap().killAllMonsters(false);
            cm.getPlayer().getMap().resetReactors();
            if (expeditionz != null) {
                cm.endExpedition(expeditionz);
                
            }
        }
        
        
        if (cm.getPlayer().getEventInstance() != null)
            cm.getPlayer().getEventInstance().removePlayer(cm.getPlayer());
        else
        cm.warp(240040700);
        cm.dispose();
    }
}