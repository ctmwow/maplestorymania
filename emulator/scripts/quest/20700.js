var status = -1;

function start(mode, type, selection) 
{
    if (mode == -1) 
    {
        qm.dispose();
    }
    else 
    {
        qm.gainExp(224);
        qm.forceStartQuest();
        qm.forceCompleteQuest();
        qm.dispose();
    }
}