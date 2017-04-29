importPackage(Packages.org.ascnet.leaftown.tools);
importPackage(Packages.org.ascnet.leaftown.server);
importPackage(Packages.org.ascnet.leaftown.server.life);
importPackage(java.awt);

var status;
var curMap;
var playerStatus;
var chatState;
var preamble;
var mySelection;


function start() {
    status = -1;
    mapId = cm.getMapId();
    
    if (cm.getParty() != null) //Check for Party
        playerStatus = cm.isPartyLeader();
        
    preamble = null;
    action(1, 0, 0);
}

function action(mode, type, selection) {
    if (mode == -1) {
        cm.dispose();
    } else {
        if (mode == 0 && status == 0) {
            cm.dispose();
            return;
        }
        if (mode == 1)
            status++;
        else
            status--;
            
        if (playerStatus) 
        {
            var eim = cm.getPlayer().getEventInstance();
            
            if(eim == null)
            {
                cm.warp(910010300, 0);
                cm.dispose();
            }
            
            var party = cm.getPlayer().getEventInstance().getPlayers();
            if (status == 0) {
                cm.sendSimple("Olá, Sou Growlie e eu quero alguns #bBolinhos de Arroz#k...#b\r\n#L0#Eu trouxe alguns Bolinhos de Arroz para você!#l\r\n#L1#O que eu tenho de fazer aqui?#l\r\n#L2#Eu quero sair!#l#k");
            } else if (status == 1) {
                mySelection = selection;
                switch (mySelection) {
                    case 0 :
                        if (cm.haveItem(4001101, 10)) {
                            clear(1, eim, cm);
                            cm.sendNext("Obrigado por me dar #bBolinhos de Arroz#k!");
                        } else {
                            cm.sendNext("Você não possui 10 #bBolinhos de Arroz#k! Rawr!");
                            cm.dispose();
                        }
                        break;
                    case 1 :
                        cm.sendNext("Esta é a Colina das Prímulas onde o Coelhinho de Lua faz #bBolinhos de Arroz#k quando há uma lua cheia. Para ter uma lua cheia, plante as semente obtidas das prímulas e quando todas as 6 sementes forem plantadas, a lua cheia aparecerá. O #rCoelhinho da Lua será invocado, e você deve proteger-lo de outros monstros que irão tentar atacar-lo#k. Num caso do #bCoelhinho da Lua#k morrer, você irá falhar a missão e eu ficarei faminto e zangado...");
                        cm.dispose();
                        break;
                    case 2 :
                        cm.sendNext("Certo, mas volte logo e me traga alguns #bBolinhos de Arroz#k!");
                        break;
                }
            } else if (status == 2) {
                switch (mySelection) {
                    case 0 :
                        var mf = eim.getMapFactory();
                        cm.removeAll(4001101);
                        map = mf.getMap(910010100);
                        cm.givePartyExp(16000, party);
                        //cm.givePartyNX(100, party);
                        for (var i = 0; i < party.size(); i++) {
                            party.get(i).changeMap(map, map.getPortal(0));
                            eim.unregisterPlayer(party.get(i));
                        }
                        eim.disbandParty();
                        cm.dispose();
                        break;
                    case 1 :
                        break;
                    case 2 :
                        eim.disbandParty();
                        cm.dispose();
                }
            }
        } else {
            var eim = cm.getPlayer().getEventInstance();
            var party = cm.getPlayer().getEventInstance().getPlayers();
            if (status == 0) {
                cm.sendYesNo("Você gostaria de sair da Missão de Grupo?");
            } else if (status == 1) {
                eim.unregisterPlayer(cm.getPlayer());
                cm.warp(910010300, 0);
                cm.dispose();
            }
        }
    }
}

function clear(stage, eim, cm) {
    eim.setProperty("1stageclear", "true");
    var packetef = MaplePacketCreator.showEffect("quest/party/clear");
    var packetsnd = MaplePacketCreator.playSound("Party1/Clear");
    var packetglow = MaplePacketCreator.environmentChange("gate", 2);
    var map = eim.getMapInstance(cm.getPlayer().getMapId());
    var party = cm.getPlayer().getEventInstance().getPlayers();
    map.broadcastMessage(packetef);
    map.broadcastMessage(packetsnd);
    var mf = eim.getMapFactory();
}