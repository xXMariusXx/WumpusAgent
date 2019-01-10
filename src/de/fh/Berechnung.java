package de.fh;

import de.fh.wumpus.enums.HunterAction;

import java.util.Comparator;
import java.util.LinkedList;

import static de.fh.Feld.Zustand.*;

public class Berechnung {
    private Welt welt;
    private String modus;
    private Feld zielfeld;
    private boolean zielfeldErreicht = true; //muss auf False gesetzt werden, wenn ein neues Ziel bestimmt wurde
    private Feld zwischenfeld;
    private boolean zwischenfeldErreicht = true; //muss auf False gesetzt werden, wenn ein neues Zwischen-Ziel bestimmt wurde
    private boolean debug = true;

    public Berechnung(Welt welt) {
        this.welt = welt;
        zielfeld = new Feld(Feld.Zustand.UNBEKANNT, -99, -99);
        zwischenfeld = new Feld(Feld.Zustand.UNBEKANNT, -88, -88);
        modus = "standard";
    }

    //Hauptmethode die aufgerufen wird
    public void berechne() {

        System.out.println(" ----------------- Berechnungsausgabe: ----------------- ");
        HunterAction nextAction = HunterAction.SIT;

        //Wenn Hunter sich am Ziel befindet: boolean Zielerreicht auf true setzen
        if (welt.getHunterFeld() == zielfeld) zielfeldErreicht = true;

        if (welt.isGoldAufgesammelt()) zielfeld = welt.getFeld(1, 1);

        if (welt.isEingekesselt(welt.getHunterFeld(),69)) {
            zielfeldErreicht = true;
            zwischenfeldErreicht = true;
        }

        //Wenn der Hunter von Gestank eingekesselt ist und Wumpus lebendig ist, soll versucht werden
        //den Wumpus zu töten
        if (welt.isWumpusLebendig() && welt.isVonGestankEingekesselt() && welt.getAnzahlPfeile() > 0)
            modus = "wumpusToeten";


        //Zusätzliche Aktionen die in bestimmten Fällen ausgeführt werden sollen
        //break wird nur dann gesetzt, wenn default nicht mehr durchlaufen werden soll
        //z.B. wenn im Case bereits die nächste Aktion gesetzt werden konnte
        System.out.println("aktueller Modus: " + modus);
        switch (modus) {
            case "checkEcke":
                if (!welt.isUmrandet() && zielfeldErreicht) {
                    if (welt.getHunterPos()[1] == 1 && (welt.getBlickrichtung() == Welt.Himmelsrichtung.EAST || welt.getBlickrichtung() == Welt.Himmelsrichtung.WEST)
                            && !welt.getFeld(welt.getHunterPos()[0], welt.getHunterPos()[1] - 1).isBesucht()) {
                        zielfeld = welt.getFeld(welt.getHunterPos()[0], welt.getHunterPos()[1] - 1);
                        if (debug) System.out.println("Zielfeld durch Ecke 1: " + zielfeld);
                        zielfeldErreicht = false;
                    } else if ((welt.getBlickrichtung() == Welt.Himmelsrichtung.EAST || welt.getBlickrichtung() == Welt.Himmelsrichtung.WEST)
                            && !welt.getFeld(welt.getHunterPos()[0], welt.getHunterPos()[1] + 1).isBesucht()) {
                        zielfeld = welt.getFeld(welt.getHunterPos()[0], welt.getHunterPos()[1] + 1);
                        if (debug) System.out.println("Zielfeld durch Ecke 2: " + zielfeld);
                        zielfeldErreicht = false;
                    } else if ((welt.getHunterPos()[0] == 1 && (welt.getBlickrichtung() == Welt.Himmelsrichtung.SOUTH || welt.getBlickrichtung() == Welt.Himmelsrichtung.NORTH))
                            && !welt.getFeld(welt.getHunterPos()[0] - 1, welt.getHunterPos()[1]).isBesucht()) {
                        zielfeld = welt.getFeld(welt.getHunterPos()[0] - 1, welt.getHunterPos()[1]);
                        if (debug) System.out.println("Zielfeld durch Ecke 3: " + zielfeld);
                        zielfeldErreicht = false;
                    } else if ((welt.getBlickrichtung() == Welt.Himmelsrichtung.SOUTH || welt.getBlickrichtung() == Welt.Himmelsrichtung.NORTH)
                            && !welt.getFeld(welt.getHunterPos()[0] + 1, welt.getHunterPos()[1]).isBesucht()) {
                        zielfeld = welt.getFeld(welt.getHunterPos()[0] + 1, welt.getHunterPos()[1]);
                        if (debug) System.out.println("Zielfeld durch Ecke 4: " + zielfeld);
                        zielfeldErreicht = false;
                    }
                    welt.wandErgaenzen();
                }
                modus = "standard";
                break;

            case "goldAufnehmen":
                nextAction = HunterAction.GRAB;
                System.out.println("Gold gefunden!");
                modus = "goldAufgenommen";
                break;

            case "goldAufgenommen":
                //Hunter soll zum Start laufen, wenn er das Gold aufgenommen hat
                setZielfeld(welt.getFeld(1, 1));
                break;

            case "wumpusToeten":
                nextAction = wumpusToeten();
                modus = "standard";
                break;

        }
        //Wenn Zielfeld noch Wand, da noch nicht erreicht: ??????
        //if (aktuellesZielfeld.getRisiko() > 69) {
        //    aktuellesZielfeld = new Feld(Feld.Zustand.UNBEKANNT, -99, -99);
        //}

        //Wenn nextAction im Switch nicht überschrieben wurde
        if (nextAction == HunterAction.SIT) {
            //Standardfall 1
            //wenn das aktuelle Ziel erreicht wurde (bedeutet auch -> in anderen Cases kein neues Ziel gesetzt)
            // und der Hunter das Gold noch nicht aufgesammelt hat: neues Ziel berechnen und hinlaufen
            if (zielfeldErreicht && !welt.isGoldAufgesammelt()) {
                if (debug) System.out.println("Standardfall 1: Ziel erreicht");
                bestimmeNaechstesZielFeld();

                //wenn das Zwischenfeld nicht erreicht werden kann
                if (!welt.isInMap(bestimmeNaechstesZwischenFeld())) {
                    if (debug)
                        System.out.println("(3) Es wird keine Möglichkeit gefunden das Zwischenfeld zu erreichen: " + zwischenfeld);
                    nextAction = HunterAction.QUIT_GAME;
                }
                //Zwischenfeld kann erreicht werden: bestimme Weg dahin
                else {
                    nextAction = bestimmeWegZumFeld();
                }
            }
            //wenn das aktuelle Ziel erreicht und das Gold bereits aufgesammelt wurde: Spiel beenden
            else if (zielfeldErreicht && welt.getHunterFeld() == welt.getFeld(1, 1)) {
                if (debug) System.out.println("Sonderfall Gold");
                nextAction = HunterAction.QUIT_GAME;
            }
            //Standardfall 2
            //wenn das aktuelle Ziel noch nicht erreicht wurde, versuchen Ziel über Zwischenziele zu erreichen
            else {
                if (debug) System.out.println("Standardfall 2: Ziel NICHT erreicht");
                //wenn das aktuelle Ziel nicht in der Map liegt (z.B. wenn das Ziel schon länger besteht, die Welt aber mittlerweile umrandet ist)
                //versuchen ein neues Ziel zu bestimmen
                if (!welt.isInMap(zielfeld)) {
                    bestimmeNaechstesZielFeld();
                    //wenn die Bestimmung eines alternativen Zielfelds nicht geklappt hat, gibt es kein Zielfeld mehr und das Spiel wird beendet
                    if (!welt.isInMap(zielfeld)) {
                        System.out.println("Es wird kein neues Zielfeld mehr in der Map gefunden: " + zielfeld);
                        nextAction = HunterAction.QUIT_GAME;
                    }
                    //Alternatives Zielfeld gefunden
                    else {
                        //wenn das Zwischenfeld nicht erreicht werden kann
                        if (!welt.isInMap(bestimmeNaechstesZwischenFeld())) {
                            System.out.println("(1) Es wird keine Möglichkeit gefunden, das Zwischenfeld zu erreichen: " + zwischenfeld);
                            nextAction = HunterAction.QUIT_GAME;
                        }
                        //Zwischenfeld kann erreicht werden: bestimme Weg dahin
                        else {
                            nextAction = bestimmeWegZumFeld();
                        }
                    }
                }
                //aktuelles Ziel liegt in der Map
                else {
                    //wenn das Zwischenfeld nicht erreicht werden kann
                    if (!welt.isInMap(bestimmeNaechstesZwischenFeld())) {
                        System.out.println("(2) Es wird keine Möglichkeit gefunden, das Zwischenfeld zu erreichen: " + zwischenfeld);
                        nextAction = HunterAction.QUIT_GAME;
                    }
                    //Zwischenfeld kann erreicht werden: bestimme Weg dahin
                    else {
                        nextAction = bestimmeWegZumFeld();
                    }
                }

            }
        }


        System.out.println("Nächste berechnete Aktion: " + nextAction);
        welt.addNextAction(nextAction);

        System.out.println(" ----------------- ENDE BERECHNUNGSAUSGABE ----------------- ");
    }

    //Hilfsmethoden der Klasse

    private HunterAction bestimmeWegZumFeld() {
        //bestimmt nextAction um von aktueller Position zum nächsten Zwischenfeld zu gelangen

        if (debug) System.out.println("Bestimme Weg von " + welt.getHunterFeld() + " nach " + zwischenfeld);
        int[] feldPos = zwischenfeld.getPosition();

        if (welt.getHunterPos()[0] == feldPos[0] && welt.getHunterPos()[1] + 1 == feldPos[1]) {
            switch (welt.getBlickrichtung()) {
                case EAST:
                    return HunterAction.TURN_RIGHT;
                case WEST:
                    return HunterAction.TURN_LEFT;
                case NORTH:
                    return HunterAction.TURN_RIGHT;
                case SOUTH:
                    zwischenfeldErreicht = true;
                    return HunterAction.GO_FORWARD;
            }
        } else if (welt.getHunterPos()[0] == feldPos[0] && welt.getHunterPos()[1] - 1 == feldPos[1]) {
            switch (welt.getBlickrichtung()) {
                case EAST:
                    return HunterAction.TURN_LEFT;
                case WEST:
                    return HunterAction.TURN_RIGHT;
                case NORTH:
                    zwischenfeldErreicht = true;
                    return HunterAction.GO_FORWARD;
                case SOUTH:
                    return HunterAction.TURN_RIGHT;
            }
        } else if (welt.getHunterPos()[0] + 1 == feldPos[0] && welt.getHunterPos()[1] == feldPos[1]) {
            switch (welt.getBlickrichtung()) {
                case EAST:
                    zwischenfeldErreicht = true;
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
                    zwischenfeldErreicht = true;
                    return HunterAction.GO_FORWARD;
                case NORTH:
                    return HunterAction.TURN_LEFT;
                case SOUTH:
                    return HunterAction.TURN_RIGHT;
            }
        }

        //Hier ist return NULL (Programmabsturz) ok, da dies bedeuten würde dass es eine fehlerhafte Bestimmung des Zwischenfeldes gab
        System.err.println("Fehler bei Wegbestimmung, Zwischenziel liegt nicht in der Nähe des Hunters");
        return null;
    }

    private Feld bestimmeNaechstesZwischenFeld() {
        //bestimmt mit Hilfe von A* das nächste Zwischenfeld zum aktuellen Zielfeld

        //Wenn das Zwischenfeld noch nicht erreicht ist, da er sich z.B. letztes mal nur gedreht hat, verändert sich das Zwischenfeld nicht
        if (!zwischenfeldErreicht) {
            if (debug) System.out.println("altes Zwischenziel bleibt erhalten: " + zwischenfeld);
            return zwischenfeld;
        }

        //Ansonsten neues Zwischenfeld auf dem Weg zum Zielfeld bestimmen:

        Feld aktHunterPos = welt.getFeld(welt.getHunterPos()[0], welt.getHunterPos()[1]);
        ASternSuche aStern = new ASternSuche(aktHunterPos, zielfeld, welt);


        //Versuchen möglichst schnell und sicher zum Ziel zu kommen
        zwischenfeld = aStern.suche(20, 10);
        if (zwischenfeld.getPosition()[0] != -1 && zwischenfeld.getPosition()[1] != -1 && !welt.isEingekesselt(zwischenfeld,45)) {
            System.out.println("Zwischenfeld1: " + zwischenfeld);
            return zwischenfeld;
        }

        //Versuchen relativ schnell und sicher zum Ziel zu kommen
        zwischenfeld = aStern.suche(45, 5);
        if (zwischenfeld.getPosition()[0] != -1 && zwischenfeld.getPosition()[1] != -1 && !welt.isEingekesselt(zwischenfeld,45)) {
            System.out.println("Zwischenfeld2: " + zwischenfeld);
            return zwischenfeld;
        }

        //Versuchen ohne zu sterben zum Ziel zu kommen
        zwischenfeld = aStern.suche(69, 1);
        if (zwischenfeld.getPosition()[0] != -1 && zwischenfeld.getPosition()[1] != -1 && !welt.isEingekesselt(zwischenfeld,45)) {
            System.out.println("Zwischenfeld3: " + zwischenfeld);
            return zwischenfeld;
        }

        //Versuchen überhaupt mit maximalem Risiko zum Ziel zu kommen, Länge der Strecke irrelevant
        zwischenfeld = aStern.suche(90, 0);
        if (zwischenfeld.getPosition()[0] != -1 && zwischenfeld.getPosition()[1] != -1) {
            System.out.println("Zwischenfeld4: " + zwischenfeld);
            return zwischenfeld;
        }

        //Wenn es keine Möglichkeit gibt das Ziel zu erreichen:
        return zwischenfeld; //hat die Werte -1,-1 -> liegt nicht in der Map


    }

    private void bestimmeNaechstesZielFeld() {

        //ToDo verhindern dass der Hunter die komplette rechte Seite einzeln abklappert
        Feld f;
        if (debug) System.out.println("........ZIELSUCHE........");

        //Als erstes wird versucht ein noch nie besuchtes Feld mit möglichst geringem Risiko zu finden, das zudem nicht von maxRisiko eingekesselt ist
        //Wenn die Welt noch nicht umrandet ist, wird sie erweitert.
        f = sucheNaechstesUnbekanntes(welt.getHunterPos()[0], welt.getHunterPos()[1], 45,true);

        if (f.getPosition()[0] < 0) {
            if (debug) System.out.println("1. kein Unbekanntes mit Risiko 45 das nicht eingekesselt ist.");
            f = sucheNaechstesUnbekanntes(welt.getHunterPos()[0], welt.getHunterPos()[1], 45,false);
        }

        if (f.getPosition()[0] < 0) {
            if (debug) System.out.println("2. kein Unbekanntes mit Risiko 45");
            //Wenn es kein unbesuchtes Feld mit maxRisiko mehr gibt, wird versucht die Map zu erweitern
            f = sucheNaechstesErweitert();
        }

        if (f.getPosition()[0] < 0) {
            if (debug) System.out.println("3. kein Erweitertes");
            //klappt dies nicht, wird das Risiko für ein unbesuchtes Feld erhöht
            f = sucheNaechstesUnbekanntes(welt.getHunterPos()[0], welt.getHunterPos()[1], 69,false);
        }

        if (f.getPosition()[0] < 0) {
            if (debug) System.out.println("4. kein Unbekanntes mit Risiko 69");
            //wenn er von 3 Feldern mit zu hohem Risiko umgeben ist: zurück gehen
            f = sucheNaechstesMitRisiko(welt.getHunterPos()[0], welt.getHunterPos()[1], 45);
        }

        if (f.getPosition()[0] < 0) {
            if (debug) System.out.println("5. kein Bekanntes mit Risiko 45");
            f = sucheNaechstesMitRisiko(welt.getHunterPos()[0], welt.getHunterPos()[1], 69);
        }

        if (f.getPosition()[0] < 0) {
            if (debug) System.out.println("6. kein Bekanntes mit Risiko 69");

        }
        //Fall: Es gibt kein unbesuchtes Feld mit passablem Risiko und die Map kann nicht mehr erweitert werden
        // -> Aufgeben (Feld mit -1, -1) wird zurück gegeben

        zielfeld = f;

        zielfeldErreicht = false;
        if (debug) System.out.println("........ENDE ZIELSUCHE........");
        if (debug) System.out.println("neu bestimmtes Ziel: " + zielfeld);

    }

    private Feld sucheNaechstesMitRisiko(int x, int y, int maxRisiko) {
        //Sucht das nächste Feld von Position x,y ausgehend mit maximalem Risiko von maxRisiko

        //Schleife für Schrittweite
        for (int m = 1; m < Math.max(welt.getMapSize()[0], welt.getMapSize()[1]); m++) {

            // 4 direkt erreichbare Felder prüfen
            if (welt.isInMap(x + m, y) && welt.getFeld(x + m, y).getRisiko() < maxRisiko) {
                return welt.getFeld(x + m, y);
            }
            if (welt.isInMap(x - m, y) && welt.getFeld(x - m, y).getRisiko() < maxRisiko) {
                return welt.getFeld(x - m, y);
            }
            if (welt.isInMap(x, y + m) && welt.getFeld(x, y + m).getRisiko() < maxRisiko) {
                return welt.getFeld(x, y + m);
            }
            if (welt.isInMap(x, y - m) && welt.getFeld(x, y - m).getRisiko() < maxRisiko) {
                return welt.getFeld(x, y - m);
            }


            //weitere Felder prüfen
            for (int i = 1; i <= m; i++) {
                if (welt.isInMap(x + m, y + i) && welt.getFeld(x + m, y + i).getRisiko() < maxRisiko) {
                    return welt.getFeld(x + m, y + i);
                }
                if (welt.isInMap(x + m, y - i) && welt.getFeld(x + m, y - i).getRisiko() < maxRisiko) {
                    return welt.getFeld(x + m, y - i);
                }
                if (welt.isInMap(x - m, y + i) && welt.getFeld(x - m, y + i).getRisiko() < maxRisiko) {
                    return welt.getFeld(x - m, y + i);
                }
                if (welt.isInMap(x - m, y - i) && welt.getFeld(x - m, y - i).getRisiko() < maxRisiko) {
                    return welt.getFeld(x - m, y - i);
                }
            }
            for (int i = 1; i <= m; i++) {
                if (welt.isInMap(x + i, y + m) && welt.getFeld(x + i, y + m).getRisiko() < maxRisiko) {
                    return welt.getFeld(x + i, y + m);
                }
                if (welt.isInMap(x + i, y - m) && welt.getFeld(x + i, y - m).getRisiko() < maxRisiko) {
                    return welt.getFeld(x + m, y - i);
                }
                if (welt.isInMap(x - i, y + m) && welt.getFeld(x - i, y + m).getRisiko() < maxRisiko) {
                    return welt.getFeld(x - m, y + i);
                }
                if (welt.isInMap(x - i, y - m) && welt.getFeld(x - i, y - m).getRisiko() < maxRisiko) {
                    return welt.getFeld(x - i, y - m);
                }
            }
        }


        System.err.println("Kein Feld mehr vorhanden (auch bereits besuchtes) oder Risiko zu groß!");
        return new Feld(Feld.Zustand.UNBEKANNT, -1, -1);
    }

    private Feld sucheNaechstesUnbekanntes(int x, int y, int maxRisiko, boolean eingekesselt) {
        //Sucht das nächste nicht besuchte Feld von Position x,y ausgehend mit maximalem Risiko von maxRisiko

        //Schleife für Schrittweite
        for (int m = 1; m < Math.max(welt.getMapSize()[0], welt.getMapSize()[1]); m++) {

            if (eingekesselt) {
                // 4 direkt erreichbare Felder prüfen
                if (welt.isInMap(x + m, y) && !welt.getFeld(x + m, y).isBesucht() && welt.getFeld(x + m, y).getRisiko() < maxRisiko && !welt.isEingekesselt(x + m, y, maxRisiko)) {
                    if (debug) System.out.println("suche Unbekanntes eingekessel: " + (x + m) + "," + y);
                    return welt.getFeld(x + m, y);
                }
                if (welt.isInMap(x - m, y) && !welt.getFeld(x - m, y).isBesucht() && welt.getFeld(x - m, y).getRisiko() < maxRisiko && !welt.isEingekesselt(x - m, y, maxRisiko)) {
                    if (debug) System.out.println("suche Unbekanntes eingekessel: " + (x - m) + "," + y);
                    return welt.getFeld(x - m, y);
                }
                if (welt.isInMap(x, y + m) && !welt.getFeld(x, y + m).isBesucht() && welt.getFeld(x, y + m).getRisiko() < maxRisiko && !welt.isEingekesselt(x, y + m, maxRisiko)) {
                    if (debug) System.out.println("suche Unbekanntes eingekessel: " + x + "," + (y + m));
                    return welt.getFeld(x, y + m);
                }
                if (welt.isInMap(x, y - m) && !welt.getFeld(x, y - m).isBesucht() && welt.getFeld(x, y - m).getRisiko() < maxRisiko && !welt.isEingekesselt(x, y - m, maxRisiko)) {
                    if (debug) System.out.println("suche Unbekanntes eingekessel: " + x + m + "," + (y - m));
                    return welt.getFeld(x, y - m);
                }


                //weitere Felder prüfen
                for (int i = 1; i <= m; i++) {
                    if (welt.isInMap(x + m, y + i) && !welt.getFeld(x + m, y + i).isBesucht() && welt.getFeld(x + m, y + i).getRisiko() < maxRisiko && !welt.isEingekesselt(x + m, y + i, maxRisiko)) {
                        return welt.getFeld(x + m, y + i);
                    }
                    if (welt.isInMap(x + m, y - i) && !welt.getFeld(x + m, y - i).isBesucht() && welt.getFeld(x + m, y - i).getRisiko() < maxRisiko && !welt.isEingekesselt(x + m, y - i, maxRisiko)) {
                        return welt.getFeld(x + m, y - i);
                    }
                    if (welt.isInMap(x - m, y + i) && !welt.getFeld(x - m, y + i).isBesucht() && welt.getFeld(x - m, y + i).getRisiko() < maxRisiko && !welt.isEingekesselt(x - m, y + i, maxRisiko)) {
                        return welt.getFeld(x - m, y + i);
                    }
                    if (welt.isInMap(x - m, y - i) && !welt.getFeld(x - m, y - i).isBesucht() && welt.getFeld(x - m, y - i).getRisiko() < maxRisiko && !welt.isEingekesselt(x - m, y - i, maxRisiko)) {
                        return welt.getFeld(x - m, y - i);
                    }
                }
                for (int i = 1; i <= m; i++) {
                    if (welt.isInMap(x + i, y + m) && !welt.getFeld(x + i, y + m).isBesucht() && welt.getFeld(x + i, y + m).getRisiko() < maxRisiko && !welt.isEingekesselt(x + i, y + m, maxRisiko)) {
                        return welt.getFeld(x + i, y + m);
                    }
                    if (welt.isInMap(x + i, y - m) && !welt.getFeld(x + i, y - m).isBesucht() && welt.getFeld(x + i, y - m).getRisiko() < maxRisiko && !welt.isEingekesselt(x + i, y - m, maxRisiko)) {
                        return welt.getFeld(x + m, y - i);
                    }
                    if (welt.isInMap(x - i, y + m) && !welt.getFeld(x - i, y + m).isBesucht() && welt.getFeld(x - i, y + m).getRisiko() < maxRisiko && !welt.isEingekesselt(x - i, y + m, maxRisiko)) {
                        return welt.getFeld(x - m, y + i);
                    }
                    if (welt.isInMap(x - i, y - m) && !welt.getFeld(x - i, y - m).isBesucht() && welt.getFeld(x - i, y - m).getRisiko() < maxRisiko && !welt.isEingekesselt(x - i, y - m, maxRisiko)) {
                        return welt.getFeld(x - i, y - m);
                    }
                }
            } else {
                // 4 direkt erreichbare Felder prüfen
                if (welt.isInMap(x + m, y) && !welt.getFeld(x + m, y).isBesucht() && welt.getFeld(x + m, y).getRisiko() < maxRisiko) {
                    if (debug) System.out.println("suche Unbekanntes: " + (x + m) + "," + y);
                    return welt.getFeld(x + m, y);
                }
                if (welt.isInMap(x - m, y) && !welt.getFeld(x - m, y).isBesucht() && welt.getFeld(x - m, y).getRisiko() < maxRisiko) {
                    if (debug) System.out.println("suche Unbekanntes: " + (x - m) + "," + y);
                    return welt.getFeld(x - m, y);
                }
                if (welt.isInMap(x, y + m) && !welt.getFeld(x, y + m).isBesucht() && welt.getFeld(x, y + m).getRisiko() < maxRisiko) {
                    if (debug) System.out.println("suche Unbekanntes: " + x + "," + (y + m));
                    return welt.getFeld(x, y + m);
                }
                if (welt.isInMap(x, y - m) && !welt.getFeld(x, y - m).isBesucht() && welt.getFeld(x, y - m).getRisiko() < maxRisiko) {
                    if (debug) System.out.println("suche Unbekanntes: " + x + m + "," + (y - m));
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

        }


        if (debug) System.out.println("Kein unbekanntes Feld mehr vorhanden oder Risiko zu groß! Risiko: " + maxRisiko);
        return new Feld(Feld.Zustand.UNBEKANNT, -1, -1);
    }

    private Feld sucheNaechstesErweitert() {
        //Wenn kein unbesuchtes Feld mehr in der Nähe, die Weltgröße aber noch nicht begrenzt ist: Map nach unten-rechts erweitern
        if (!welt.isUmrandet()) {
            return welt.getFeld(welt.getMapSize()[0], welt.getMapSize()[1]);
        }

        if (debug) System.out.println("Map konnte nicht erweitert werden, vermutlich umrandet? " + welt.isUmrandet());
        return new Feld(Feld.Zustand.UNBEKANNT, -1, -1);
    }

    private HunterAction wumpusToeten() {
        //bereits bekannt: ich bin an mindestens 2 Stellen von Gestank oder ähnlichem umkesselt und der Wumpus lebt noch

        Feld feldInBlickrichtung = welt.getFeldInBlickrichtung();

        if ((feldInBlickrichtung.getZustaende().contains(GESTANK1) || feldInBlickrichtung.getZustaende().contains(GESTANK2)) && !feldInBlickrichtung.isBeschossen()) {
            if (debug) System.out.println("Schießen in Blickrichtung");
            welt.pfeilGeschossen();
            return HunterAction.SHOOT;
        }

        if (feldInBlickrichtung.getRisiko() < 69) {
            if (debug) System.out.println("Zielfeld auf das Feld in Blickrichtung gesetzt");
            setZielfeld(feldInBlickrichtung);
        }

        return HunterAction.SIT;
    }


    //Getter-Setter
    public void setModus(String modus) {
        this.modus = modus;
    }

    public void setZielfeld(Feld f) {
        zielfeldErreicht = false;
        zielfeld = f;
    }

    public void setZielfeldErreicht() {
        //Wenn BumpedIntoWall, Ziel auf erreicht setzen, da es keinen Sinn macht weiter die Wand versuchen zu erreichen
        zielfeldErreicht = true;
    }

    public void setZwischenfeldErreicht() {
        zwischenfeldErreicht = true;
    }
}
