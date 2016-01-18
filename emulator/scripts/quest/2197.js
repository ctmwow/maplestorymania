/*
 * Tienk the Monster Book Salesman Quest.
 */

function start(mode, type, selection) {

	qm.forceStartQuest(2197);
}

function end(mode, type, selection) {
qm.forceCompleteQuest(2197);
qm.gainItem(2030001, 1);
}