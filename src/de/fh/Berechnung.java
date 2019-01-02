package de.fh;

import de.fh.wumpus.enums.HunterAction;

public class Berechnung {
    private Welt welt;
    private Feld aktuellesZielfeld;
    private String modus;

    public Berechnung(Welt welt) {
        this.welt = welt;
        aktuellesZielfeld = new Feld(Feld.Zustand.UNBEKANNT, -99, -99);
        modus = "standard";
    }

    //Hauptmethode die aufgerufen wird
    public void berechne() {

        System.out.println(" -------- Berechnungsausgabe: -------- ");

        switch (modus) {
            case "checkEcke":
                System.out.println(welt.isUmrandet());
                if (!welt.isUmrandet()) {
                    if (welt.getHunterPos()[1] == 1 && (welt.getBlickrichtung() == Welt.Himmelsrichtung.EAST || welt.getBlickrichtung() == Welt.Himmelsrichtung.WEST)) {
                        System.out.println("ecke1");
                        aktuellesZielfeld = welt.getFeld(welt.getHunterPos()[0], welt.getHunterPos()[1] - 1);
                    } else if ((welt.getBlickrichtung() == Welt.Himmelsrichtung.EAST || welt.getBlickrichtung() == Welt.Himmelsrichtung.WEST)) {
                        aktuellesZielfeld = welt.getFeld(welt.getHunterPos()[0], welt.getHunterPos()[1] + 1);
                        System.out.println("ecke2");
                    } else if (welt.getHunterPos()[0] == 1 && (welt.getBlickrichtung() == Welt.Himmelsrichtung.SOUTH || welt.getBlickrichtung() == Welt.Himmelsrichtung.NORTH)) {
                        aktuellesZielfeld = welt.getFeld(welt.getHunterPos()[0] - 1, welt.getHunterPos()[1]);
                        System.out.println("ecke3");
                    } else if (welt.getBlickrichtung() == Welt.Himmelsrichtung.SOUTH || welt.getBlickrichtung() == Welt.Himmelsrichtung.NORTH) {
                        aktuellesZielfeld = welt.getFeld(welt.getHunterPos()[0] + 1, welt.getHunterPos()[1]);
                        System.out.println("ecke4");
                    }
                }
                setModus("standard");
                //kein break, damit default noch ausgeführt wird

            default:
                HunterAction nextAction;
                //Wenn Zielfeld noch Wand, da noch nicht erreicht: ??????
                if (aktuellesZielfeld.getRisiko() > 69) {
                    aktuellesZielfeld = new Feld(Feld.Zustand.UNBEKANNT, -99, -99);
                }

                //wenn noch kein Zielfeld gesetzt oder Zielfeld erreicht: neues Ziel berrechnen
                if (aktuellesZielfeld.getPosition()[0] == -99 && aktuellesZielfeld.getPosition()[1] == -99 || welt.getFeld(welt.getHunterPos()[0], welt.getHunterPos()[1]) == aktuellesZielfeld) {
                    bestimmeNaechstesZielFeld();
                }

                //wenn Gold aufgesammelt und auf Startposition
                if (welt.getFeld(welt.getHunterPos()[0], welt.getHunterPos()[1]) == aktuellesZielfeld && welt.isGoldAufgesammelt()) {
                    nextAction = HunterAction.QUIT_GAME;
                }
                //wenn aktuell ein Zielfeld gegeben ist: hin laufen
                else if (aktuellesZielfeld.getPosition()[0] > -1 && aktuellesZielfeld.getPosition()[1] > -1) {
                    nextAction = bestimmeWegZumFeld();
                }
                //wenn aktuell kein Ziel gegeben (d.h jedes Feld mit geringem Risiko besucht und Gold nicht gefunden) Werte: (-1, -1)
                else {
                    System.err.println("Spiel verlassen!");
                    nextAction = HunterAction.QUIT_GAME;
                }

                welt.addNextAction(nextAction);

                System.out.println(" -------- ENDE -------- ");
        }
    }

    //Hilfsmethoden der Klasse

    private HunterAction bestimmeWegZumFeld() {
        //bestimmt den nächsten Schritt um von aktueller Position zum nächsten Zwischenfeld zu gelangen
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
        } else if (welt.getHunterPos()[0] == feldPos[0] && welt.getHunterPos()[1] == feldPos[1]) {
            return HunterAction.SIT;
        }


        System.err.println("Fehler bei Wegbestimmung, Zwischenziel liegt nicht in der Nähe des Hunters");
        return null;
    }

    private Feld bestimmeNaechstesZwischenFeld() {
        //bestimmt mit Hilfe von A* das nächste Zwischenfeld zum aktuellen Zielfeld


        Feld aktHunterPos = welt.getFeld(welt.getHunterPos()[0], welt.getHunterPos()[1]);

        ASternSuche aStern = new ASternSuche(aktHunterPos, aktuellesZielfeld, welt);
        Feld res = aStern.suche();
        System.out.println("Zwischenfeld: " + res);

        if (res.getPosition()[0] != -1 && res.getPosition()[1] != -1) {
            return res;
        }

        System.err.println("Keinen Weg zum Ziel gefunden");
        return null;
    }

    private void bestimmeNaechstesZielFeld() {

        //wenn Gold bereits aufgesammelt ist, zurück zum Start
        if (welt.isGoldAufgesammelt()) {
            aktuellesZielfeld = welt.getFeld(1, 1);
            System.err.println("Gold gefunden!");
        } else {
            //nächstes unbekanntes Feld suchen
            Feld f = sucheNaechstesUnbekanntes(welt.getHunterPos()[0], welt.getHunterPos()[1], 69);
            aktuellesZielfeld = f;
        }


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

        if (!welt.isUmrandet()) {
            if (welt.getFeld(welt.getHunterPos()[0] + 1, welt.getHunterPos()[1]).getRisiko() < maxRisiko) {
                return welt.getFeld(welt.getHunterPos()[0] + 1, welt.getHunterPos()[1]);
            }
            if (welt.getFeld(welt.getHunterPos()[0] - 1, welt.getHunterPos()[1]).getRisiko() < maxRisiko) {
                return welt.getFeld(welt.getHunterPos()[0] - 1, welt.getHunterPos()[1]);
            }
            if (welt.getFeld(welt.getHunterPos()[0], welt.getHunterPos()[1] + 1).getRisiko() < maxRisiko) {
                return welt.getFeld(welt.getHunterPos()[0], welt.getHunterPos()[1] + 1);
            }
            if (welt.getFeld(welt.getHunterPos()[0], welt.getHunterPos()[1] - 1).getRisiko() < maxRisiko) {
                return welt.getFeld(welt.getHunterPos()[0], welt.getHunterPos()[1] - 1);
            }

        }


        System.err.println("Kein unbekanntes Feld mehr vorhanden oder Risiko zu groß!");
        return new Feld(Feld.Zustand.UNBEKANNT, -1, -1);
    }


    //Getter-Setter
    public void setModus(String modus) {
        this.modus = modus;
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
