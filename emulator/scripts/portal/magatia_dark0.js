function enter(pi) {
    if (pi.getQuestStatusId(3309)) {
		pi.forceCompleteQuest(3309);
		pi.playerMessage("Quest complete.");
    }
    pi.warp(261020700,0);
    pi.playPortalSE();
}