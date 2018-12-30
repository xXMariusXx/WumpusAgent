package de.fh;

import de.fh.suche.ASternSuche;
import de.fh.wumpus.enums.HunterAction;
import de.fh.wumpus.enums.HunterActionEffect;

import java.util.*;

public class Berechnung {
    private Welt welt;
    private Feld aktuellesZielfeld;

    public Berechnung(Welt welt) {
        this.welt = welt;
        aktuellesZielfeld = new Feld(Feld.Zustand.UNBEKANNT, -99, -99);
    }

    //Hauptmethode die aufgerufen wird
    public void berechne() {
        HunterAction nextAction = HunterAction.GO_FORWARD;

        //wenn noch kein Zielfeld gesetzt oder Zielfeld erreicht: neues Ziel berrechnen
        if (aktuellesZielfeld.getPosition()[0] == -99 && aktuellesZielfeld.getPosition()[1] == -99 || welt.getFeld(welt.getHunterPos()[0], welt.getHunterPos()[1]) == aktuellesZielfeld) {
            bestimmeNaechstesZielFeld();
        }

        //wenn aktuell ein Zielfeld gegeben ist: hin laufen
        if (aktuellesZielfeld.getPosition()[0] > -1 && aktuellesZielfeld.getPosition()[1] > -1 && welt.isInMap(aktuellesZielfeld.getPosition()[0], aktuellesZielfeld.getPosition()[1])) {
            nextAction = bestimmeWegZumFeld();
        }
        //wenn aktuell kein Ziel gegeben (d.h jedes Feld mit geringem Risiko besucht und Gold nicht gefunden) Werte: (-1, -1)
        else {
            System.err.println("Spiel verlassen!");
            nextAction = HunterAction.QUIT_GAME;
        }

        welt.addNextAction(nextAction);
    }

    //Hilfsmethoden der Klasse
    private HunterAction bestimmeWegZumFeld() {
        int[] feldPos = bestimmeNaechstesZwischenFeld().getPosition();

        if (welt.getHunterPos()[0] == feldPos[0] && welt.getHunterPos()[1] + 1 == feldPos[1]) {
            switch (welt.getBlickrichtung()) {
                case EAST:
                    return HunterAction.TURN_RIGHT;
                case WEST:
                    return HunterAction.TURN_LEFT;
                case NORTH:
                    return HunterAction.TURN_RIGHT;
                case SOUTH:
                    return HunterAction.GO_FORWARD;
            }
        } else if (welt.getHunterPos()[0] == feldPos[0] && welt.getHunterPos()[1] - 1 == feldPos[1]) {
            switch (welt.getBlickrichtung()) {
                case EAST:
                    return HunterAction.TURN_LEFT;
                case WEST:
                    return HunterAction.TURN_RIGHT;
                case NORTH:
                    return HunterAction.GO_FORWARD;
                case SOUTH:
                    return HunterAction.TURN_RIGHT;
            }
        } else if (welt.getHunterPos()[0] + 1 == feldPos[0] && welt.getHunterPos()[1] == feldPos[1]) {
            switch (welt.getBlickrichtung()) {
                case EAST:
                    return HunterAction.GO_FORWARD;
                case WEST:
                    return HunterAction.TURN_RIGHT;
                case NORTH:
                    return HunterAction.TURN_RIGHT;
                case SOUTH:
                    return HunterAction.TURN_LEFT;
            }
        } else if (welt.getHunterPos()[0] - 1 == feldPos[0] && welt.getHunterPos()[1] == feldPos[1]) {
            switch (welt.getBlickrichtung()) {
                case EAST:
                    return HunterAction.TURN_LEFT;
                case WEST:
                    return HunterAction.GO_FORWARD;
                case NORTH:
                    return HunterAction.TURN_LEFT;
                case SOUTH:
                    return HunterAction.TURN_RIGHT;
            }
        }


        System.err.println("Fehler bei Wegbestimmung, Zwischenziel liegt in in der Nähe des Hunters");
        return null;
    }

    private Feld bestimmeNaechstesZwischenFeld()

    {
        Feld aktHunterPos = welt.getFeld(welt.getHunterPos()[0], welt.getHunterPos()[1]);
        ASternSuche aStern = new ASternSuche(aktHunterPos,aktuellesZielfeld, welt);
        Feld zwischenZiel = aStern.suche();


        System.err.println("Keinen Weg zum Ziel gefunden");
        return zwischenZiel;
    }

    private void bestimmeNaechstesZielFeld() {

        //wenn Gold bereits aufgesammelt ist, zurück zum Start
        if (welt.isGoldAufgesammelt()) {
            aktuellesZielfeld = welt.getFeld(1, 1);
        }

        //nächstes unbekanntes Feld suchen
        Feld f = sucheNaechstesUnbekanntes(welt.getHunterPos()[0], welt.getHunterPos()[1], 69);
        aktuellesZielfeld = f;


    }

    private Feld sucheNaechstesUnbekanntes(int x, int y, int maxRisiko) {
        //Schleife für Schrittweite
        for (int m = 1; m < Math.max(welt.mapSize()[0], welt.mapSize()[1]); m++) {

            // 4 direkt erreichbare Felder prüfen
            if (welt.isInMap(x + m, y) && !welt.getFeld(x + m, y).isBesucht() && welt.getFeld(x + m, y).getRisiko() < maxRisiko) {
                return welt.getFeld(x + m, y);
            }
            if (welt.isInMap(x - m, y) && !welt.getFeld(x - m, y).isBesucht() && welt.getFeld(x - m, y).getRisiko() < maxRisiko) {
                return welt.getFeld(x - m, y);
            }
            if (welt.isInMap(x, y + m) && !welt.getFeld(x, y + m).isBesucht() && welt.getFeld(x, y + m).getRisiko() < maxRisiko) {
                return welt.getFeld(x, y + m);
            }
            if (welt.isInMap(x, y - m) && !welt.getFeld(x, y - m).isBesucht() && welt.getFeld(x, y - m).getRisiko() < maxRisiko) {
                return welt.getFeld(x, y - m);
            }


            //weitere Felder prüfen
            for (int i = 1; i <= m; i++) {
                if (welt.isInMap(x + m, y + i) && !welt.getFeld(x + m, y + i).isBesucht() && welt.getFeld(x + m, y + i).getRisiko() < maxRisiko) {
                    return welt.getFeld(x + m, y + i);
                }
                if (welt.isInMap(x + m, y - i) && !welt.getFeld(x + m, y - i).isBesucht() && welt.getFeld(x + m, y - i).getRisiko() < maxRisiko) {
                    return welt.getFeld(x + m, y - i);
                }
                if (welt.isInMap(x - m, y + i) && !welt.getFeld(x - m, y + i).isBesucht() && welt.getFeld(x - m, y + i).getRisiko() < maxRisiko) {
                    return welt.getFeld(x - m, y + i);
                }
                if (welt.isInMap(x - m, y - i) && !welt.getFeld(x - m, y - i).isBesucht() && welt.getFeld(x - m, y - i).getRisiko() < maxRisiko) {
                    return welt.getFeld(x - m, y - i);
                }
            }
            for (int i = 1; i <= m; i++) {
                if (welt.isInMap(x + i, y + m) && !welt.getFeld(x + i, y + m).isBesucht() && welt.getFeld(x + i, y + m).getRisiko() < maxRisiko) {
                    return welt.getFeld(x + i, y + m);
                }
                if (welt.isInMap(x + i, y - m) && !welt.getFeld(x + i, y - m).isBesucht() && welt.getFeld(x + i, y - m).getRisiko() < maxRisiko) {
                    return welt.getFeld(x + m, y - i);
                }
                if (welt.isInMap(x - i, y + m) && !welt.getFeld(x - i, y + m).isBesucht() && welt.getFeld(x - i, y + m).getRisiko() < maxRisiko) {
                    return welt.getFeld(x - m, y + i);
                }
                if (welt.isInMap(x - i, y - m) && !welt.getFeld(x - i, y - m).isBesucht() && welt.getFeld(x - i, y - m).getRisiko() < maxRisiko) {
                    return welt.getFeld(x - i, y - m);
                }
            }
        }
        System.err.println("Kein unbekanntes Feld mehr vorhanden oder Risiko zu groß!");
        return new Feld(Feld.Zustand.UNBEKANNT, -1, -1);
    }


}


		/*HunterAction
        Mögliche HunterActions sind möglich:

       	HunterAction.GO_FORWARD
       	HunterAction.TURN_LEFT
		HunterAction.TURN_RIGHT
		HunterAction.SHOOT
		HunterAction.SIT
		HunterAction.GRAB
		HunterAction.QUIT_GAME
		*/
