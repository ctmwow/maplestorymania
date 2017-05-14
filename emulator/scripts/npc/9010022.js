function start() {
    cm.askMapSelection("#1# Mu Lung Training Center#2# Monster Carnival 1#3# Monster Carnival 2#4# Dual Raid#5# Nett's Pyramid#6# Kerning Subway#7# Happyville#8# Golden Temple#9# Crimsonwood Party Quest");
}

function action(mode, type, selection) {
    if (mode == 1) {
		switch (selection) {
			case 1:
				if (cm.getPlayer().getLevel() >= 25) {
					cm.getPlayer().saveLocation("MIRROR");
					cm.warp(925020000, 0);
				}
				break;
			case 2:
				if (cm.getPlayer().getLevel() >= 30) {
					cm.getPlayer().saveLocation("MIRROR"); // h4x
					cm.warp(980000000, 4);
				}
				break;
			case 3:
				if (cm.getPlayer().getLevel() >= 51) {
					cm.getPlayer().saveLocation("MIRROR"); // h4x
					cm.warp(980030000, 4);
				}
				break;
			case 4:
				if (cm.getPlayer().getLevel() >= 60) {
					cm.getPlayer().saveLocation("MIRROR"); // h4x
					cm.warp(923020000, 0);
				}
				break;
			case 5:
				if (cm.getPlayer().getLevel() >= 40) {
					cm.getPlayer().saveLocation("MIRROR"); // h4x
					cm.warp(926010000, 4);
				}
				break;
			case 6:
				cm.getPlayer().saveLocation("MIRROR"); // h4x
				cm.warp(209000000, 0);
				break;
			case 7:
				cm.getPlayer().saveLocation("MIRROR"); // h4x
				cm.warp(950100000, 9);
				break;
			case 8:
				cm.getPlayer().saveLocation("MIRROR"); // h4x
				cm.warp(610010000, 0);
			break;
		}
    }
    cm.dispose();
}