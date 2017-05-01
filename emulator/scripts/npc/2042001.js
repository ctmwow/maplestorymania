var status = 0;
var request;

function start() 
{
    status = -1;
    action(1, 0, 0);
}

function action(mode, type, selection) 
{
    if (mode == 1)
        status++;
    else
        status = 0;
        
    if (status == 0) 
    {
        request = cm.getNextCarnivalRequest();
        
        if (request != null) 
            cm.sendAcceptDecline(request.getChallengeInfo());
        else 
            cm.dispose();
    } 
    else if (status == 1) 
    {
        try 
        {
        	cm.startCPQ(request.getChallenger(), 1);
            cm.getPlayer().getEventInstance().registerParty(request.getChallenger().getParty(), request.getChallenger().getMap());
            cm.getPlayer().getEventInstance().schedule("start", 11000);
            cm.dispose();
        }
        catch (e)
        {
            cm.sendOk("O desafio expirou!");
        }
        status = -1;
    }
}