package de.fh;

import de.fh.wumpus.enums.HunterAction;

import static de.fh.Feld.Zustand.*;
//ToDo verhindern dass der Hunter die komplette rechte Seite einzeln abklappert

public class Berechnung {
    private Welt welt;
    private String modus;
    private Feld zielfeld;
    private boolean zielfeldErreicht = true; //muss auf False gesetzt werden, wenn ein neues Ziel bestimmt wurde
    private Feld zwischenfeld;
    private boolean zwischenfeldErreicht = true; //muss auf False gesetzt werden, wenn ein neues Zwischen-Ziel bestimmt wurde
    private boolean debug = false;
    private int umrandeModus = 1;

    private final int zielFaelle = 6;

    public Berechnung(Welt welt) {
        this.welt = welt;
        zielfeld = new Feld(Feld.Zustand.UNBEKANNT, -99, -99);
        zwischenfeld = new Feld(Feld.Zustand.UNBEKANNT, -88, -88);
        modus = "umrande";
    }

    //Hauptmethode die aufgerufen wird
    public void berechne() {
        HunterAction nextAction = HunterAction.SIT;

        // ---------------------- Start Ausgabe:
        {
            System.out.println(" ----------------- Berechnungsausgabe: ----------------- ");
        }

        // ---------------------- Abfragen vor Berechnungsbeginn:
        {
            if (modus != "goldAufnehmen"){
                if (welt.isUmrandet()) {
                    if (welt.isGoldAufgesammelt()) {
                        if (welt.isWumpusLebendig()) modus = "wumpusToeten";
                        else modus = "goldAufgenommenUndWumpusTot";
                    } else {
                        modus = "standard";
                    }
                    if(welt.getPunkte() == 0) modus = "aufgeben";
                } else {
                    if (zielfeldErreicht) modus = "umrande";
                    if (zielfeld == welt.getFeld(2,1) && modus.equalsIgnoreCase("umrande")){
                        welt.umrande();
                        if (!welt.isUmrandet()) modus="aufgeben";
                        else modus = "standard";
                    }
                }
            }

            //Attribute/Zustände wieder richtig setzen
            {
                //Wenn Hunter sich am Ziel befindet: boolean Zielerreicht auf true setzen
                if (welt.getHunterFeld() == zielfeld) {
                    if(debug) System.out.println("Hunter befindet sich auf Zielposition!");
                    setZielfeldErreicht();
                }

                //Wenn Hunter eingekesselt, Attribute auf True damit neue Ziele bestimmt werden
                if (welt.isEingekesselt(welt.getHunterFeld(), 69)) {
                    setZielfeldErreicht();
                    setZwischenfeldErreicht();
                }

                if (zielfeldErreicht) zielfeld = welt.getFeld(-1, -1);
                if (zwischenfeldErreicht) zwischenfeld = welt.getFeld(-1, -1);
            }


            //Wenn der Hunter von Gestank eingekesselt ist und Wumpus lebendig ist, soll versucht werden
            //den Wumpus zu töten
            if (welt.isWumpusLebendig() && welt.isVonGestankEingekesselt() && welt.getAnzahlPfeile() > 0) {
                if (debug) System.out.println("Berechnung setzt Modus auf Wumpus töten!");
                modus = "wumpusToeten";
            }
        }

        // ---------------------- Switch Modus:
        {
            //Zusätzliche Aktionen die in bestimmten Fällen ausgeführt werden sollen
            System.out.println("aktueller Modus: " + modus);
            switch (modus) {
                case "umrande":
                    this.umrande(69);
                    break;

                case "goldAufnehmen":
                    nextAction = HunterAction.GRAB;
                    System.out.println("Gold gefunden!");
                    if (welt.isWumpusLebendig()) modus = "standard";
                    else modus = "goldAufgenommenUndWumpusTot";

                    break;

                case "goldAufgenommenUndWumpusTot":
                    //Hunter soll zum Start laufen, wenn er das Gold aufgenommen hat
                    zielfeld = welt.getFeld(1, 1);
                    if (welt.getHunterFeld() != zielfeld) zielfeldErreicht = false;
                    break;

                case "wumpusToeten":
                    nextAction = wumpusToeten();
                    modus = "standard";
                    break;

                case "aufgeben":
                    nextAction = HunterAction.QUIT_GAME;
                    break;

            }
        }

        // ---------------------- Standardfälle:
        {
            //Wenn nextAction im Switch noch nicht überschrieben wurde
            while (nextAction == HunterAction.SIT) {
                //STANDARDFALL 1
                //Ziel erreicht und noch kein Gold aufgesammelt
                if (zielfeldErreicht && !welt.isGoldAufgesammelt()) {
                    if (debug) System.out.println("Standardfall 1: Ziel erreicht");
                    nextAction = bestimmeZielUndZwischenziel();
                    if (nextAction == HunterAction.GRAB) //wenn Ziel und Zwischenziel erfolgreich gesetzt wurden
                    {
                        nextAction = bestimmeWegZumFeld();
                    }
                }

                //SONDERFALL GOLD
                //wenn das aktuelle Ziel erreicht und das Gold bereits aufgesammelt wurde: Spiel beenden
                else if (zielfeldErreicht && welt.getHunterFeld() == welt.getFeld(1, 1) && welt.isGoldAufgesammelt()) {
                    if (debug) System.out.println("Sonderfall Gold");
                    welt.addPunkte(100);
                    nextAction = HunterAction.QUIT_GAME;
                }

                //STANDARDFALL 2
                //Zielfeld NOCH NICHT erreicht
                else if (!zielfeldErreicht) {
                    if (debug) System.out.println("Standardfall 2: Ziel NICHT erreicht\nZielfeld: " + zielfeld);
                    //wenn das aktuelle Ziel nicht in der Map liegt (z.B. wenn das Ziel schon länger besteht, die Welt aber mittlerweile umrandet ist)
                    //Neues Ziel bestimmen (wie in Standardfall 1)
                    if (!welt.isInMap(zielfeld)) {
                        zielfeldErreicht = false;
                        zielfeld = welt.getFeld(-1, -1);
                        nextAction = bestimmeZielUndZwischenziel();
                        if (nextAction == HunterAction.GRAB) //wenn Ziel und Zwischenziel erfolgreich gesetzt wurden
                        {
                            nextAction = bestimmeWegZumFeld();
                        }
                    }
                    //aktuelles Ziel liegt in der Map
                    else {
                        boolean gefunden = false;
                        for (int i = 0; i <= zielFaelle && !welt.isInMap(zwischenfeld); i++) {
                            //Schleife die probiert zu aktuell gesetztem Zielfeld ein zwischenFeld zu finden
                            for (int z = 0; z < 4; z++) {
                                bestimmeNaechstesZwischenFeld(z);
                                if (welt.isInMap(zwischenfeld)) {
                                    if (i < 3) {
                                        if (!welt.isEingekesselt(zwischenfeld, 45)) {
                                            gefunden = true;
                                            break;
                                        }
                                    } else {
                                        gefunden = true;
                                        break;
                                    }
                                }
                            }//Wenn Schleife komplett durchgelaufen ist, und für das aktuelle Zielfeld kein zwischenFeld gefunden hat: versuchen neues Zielfeld zu ermitteln
                            if (gefunden) break;
                            bestimmeNaechstesZielFeld(i);
                        }

                        //In dem Fall, dass kein Zielfeld mehr gefunden wurde zu dem es ein Zwischenfeld gibt: Spiel verlassen
                        if (!welt.isInMap(zwischenfeld)) {
                            if (debug) System.out.println("2. QuitGame da kein Zwischenziel gefunden");
                            nextAction = HunterAction.QUIT_GAME;
                        } else nextAction = bestimmeWegZumFeld();

                        //!
                        //Wenn dieser Punkt erreicht wurde, haben wir ein gültiges Zielfeld und ein gültiges Zwischenfeld.
                        //!

                    }
                }

            }

        }

        // ---------------------- Ende Ausgabe:
        {
            System.out.println("Nächste berechnete Aktion: " + nextAction);
            welt.addNextAction(nextAction);
            System.out.println(" ----------------- ENDE BERECHNUNGSAUSGABE ----------------- ");
        }

    }

    //Hilfsmethoden der Klasse

    private void umrande(int maxRisiko) {
        //Ziel der Methode: die Welt soll vollständig umrandet werden
        //Vorgehensweise: eine Runde komplett am Rand entlang laufen
        Feld f;

        if (debug) System.out.println("<<<<<<<<UMRANDE>>>>>>>>");
        switch (umrandeModus) {
            case 1: //nach UL
                if(debug) System.out.println("Modus UL");

                //1. nach links gehen
                if (!((f = welt.getFeld(welt.getHunterPos()[0] - 1, welt.getHunterPos()[1])).isBesucht()) && f.getRisiko() < maxRisiko) {
                    setZielfeld(f);
                    if (debug) System.out.println("Case1 links");
                }

                //2. nach unten gehen
                else if (!((f = welt.getFeld(welt.getHunterPos()[0], welt.getHunterPos()[1] + 1)).isBesucht()) && f.getRisiko() < maxRisiko) {
                    setZielfeld(f);
                    if (debug) System.out.println("Case1 unten");
                }


                //3. nach rechts gehen
                else if (!((f = welt.getFeld(welt.getHunterPos()[0] + 1, welt.getHunterPos()[1])).isBesucht()) && f.getRisiko() < maxRisiko) {
                    setZielfeld(f);
                    umrandeModus = 2;
                    if (debug) System.out.println("Case1 rechts");
                }


                //4. nach oben gehen
                else if (!((f = welt.getFeld(welt.getHunterPos()[0], welt.getHunterPos()[1] - 1)).isBesucht()) && f.getRisiko() < maxRisiko) {
                    setZielfeld(f);
                    if (debug) System.out.println("Case1 oben");
                }

                break;

            case 2: //nach UR
                if(debug) System.out.println("Modus UR");
                //1. nach unten gehen
                if (!((f = welt.getFeld(welt.getHunterPos()[0], welt.getHunterPos()[1] + 1)).isBesucht()) && f.getRisiko() < maxRisiko) {
                    setZielfeld(f);
                    umrandeModus = 1;
                }

                //2. nach rechts gehen
                else if (!((f = welt.getFeld(welt.getHunterPos()[0] + 1, welt.getHunterPos()[1])).isBesucht()) && f.getRisiko() < maxRisiko)
                    setZielfeld(f);

                    //3. nach oben gehen
                else if (!((f = welt.getFeld(welt.getHunterPos()[0], welt.getHunterPos()[1] - 1)).isBesucht()) && f.getRisiko() < maxRisiko) {
                    setZielfeld(f);
                    umrandeModus = 3;
                }

                //4.. nach links gehen
                else if (!((f = welt.getFeld(welt.getHunterPos()[0] - 1, welt.getHunterPos()[1])).isBesucht()) && f.getRisiko() < maxRisiko){
                    setZielfeld(f);
                    umrandeModus = 4;
                }

                break;

            case 3: //nach OR
                if(debug) System.out.println("Modus OR");

                //1. nach rechts gehen
                if (!((f = welt.getFeld(welt.getHunterPos()[0] + 1, welt.getHunterPos()[1])).isBesucht()) && f.getRisiko() < maxRisiko) {
                    umrandeModus = 2;
                    setZielfeld(f);
                }

                //2. nach oben gehen
                else if (!((f = welt.getFeld(welt.getHunterPos()[0], welt.getHunterPos()[1] - 1)).isBesucht()) && f.getRisiko() < maxRisiko)
                    setZielfeld(f);

                    //3.. nach links gehen
                else if (!((f = welt.getFeld(welt.getHunterPos()[0] - 1, welt.getHunterPos()[1])).isBesucht()) && f.getRisiko() < maxRisiko) {
                    umrandeModus = 4;
                    setZielfeld(f);
                }

                //4. nach unten gehen
                else if (!((f = welt.getFeld(welt.getHunterPos()[0], welt.getHunterPos()[1] + 1)).isBesucht()) && f.getRisiko() < maxRisiko)
                    setZielfeld(f);
                break;


            case 4: //nach OL
                if(debug) System.out.println("Modus OL");

                //1. nach oben gehen
                if (!((f = welt.getFeld(welt.getHunterPos()[0], welt.getHunterPos()[1] - 1)).isBesucht()) && f.getRisiko() < maxRisiko) {
                    setZielfeld(f);
                    umrandeModus = 3;
                }

                //2. nach links gehen
                else if (!((f = welt.getFeld(welt.getHunterPos()[0] - 1, welt.getHunterPos()[1])).isBesucht()) && f.getRisiko() < maxRisiko)
                    setZielfeld(f);

                    //3. nach unten gehen
                else if (!((f = welt.getFeld(welt.getHunterPos()[0], welt.getHunterPos()[1] + 1)).isBesucht()) && f.getRisiko() < maxRisiko)
                    setZielfeld(f);

                    //4.. nach rechts gehen
                else if (!((f = welt.getFeld(welt.getHunterPos()[0] + 1, welt.getHunterPos()[1])).isBesucht()) && f.getRisiko() < maxRisiko)
                    setZielfeld(f);

                break;
        }

        if (debug) System.out.println("<<<<<<<<ENDE>>>>>>>>");
    }

    private HunterAction bestimmeZielUndZwischenziel() {
        aufrufBestimmeNaechstesZielFeld(0);
        //In dem Fall, dass kein Zielfeld mehr gefunden werden konnte: Spiel verlassen
        if (!welt.isInMap(zielfeld)) {
            if (debug) System.out.println("1. QuitGame da kein Ziel gefunden");
            return HunterAction.QUIT_GAME;
        }

        //!
        //Wenn dieser Punkt erreicht wurde, haben wir ein gültiges Zielfeld. Nun wird versucht ein entsprechendes Zwischenfeld zu ermitteln
        //!


        boolean gefunden = false;
        for (int i = 1; i <= zielFaelle && !welt.isInMap(zwischenfeld); i++) {
            //Schleife die probiert zu aktuell gesetztem Zielfeld ein zwischenFeld zu finden
            for (int z = 0; z < 4; z++) {
                bestimmeNaechstesZwischenFeld(z);
                if (welt.isInMap(zwischenfeld)) {
                    if (z < 3) {
                        if (!welt.isEingekesselt(zwischenfeld, 45)) {
                            gefunden = true;
                            break;
                        }
                    } else {
                        gefunden = true;
                        break;
                    }
                }
            }
            //Wenn Schleife komplett durchgelaufen ist, und für das aktuelle Zielfeld kein zwischenFeld gefunden hat: versuchen neues Zielfeld zu ermitteln &
            //nicht erneut versuchen dieses Feld zu erreichen
            if (gefunden) break;
            //zielfeld.setBesucht();
            setZielfeldErreicht();
            bestimmeNaechstesZielFeld(i);
        }

        //In dem Fall, dass kein Zielfeld mehr gefunden wurde zu dem es ein Zwischenfeld gibt: Spiel verlassen
        if (!welt.isInMap(zwischenfeld)) {
            if (debug) System.out.println("2. QuitGame da kein Zwischenziel gefunden");
            return HunterAction.QUIT_GAME;
        }

        //!
        //Wenn dieser Punkt erreicht wurde, haben wir ein gültiges Zielfeld und ein gültiges Zwischenfeld.
        //!
        return HunterAction.GRAB; //Grab, um was zu haben auf das man prüfen kann ob erfolgreich
    }

    private Feld bestimmeNaechstesZwischenFeld(int punkt) {
        //bestimmt mit Hilfe von A* das nächste Zwischenfeld zum aktuellen Zielfeld

        //Wenn das Zwischenfeld noch nicht erreicht ist, da er sich z.B. letztes mal nur gedreht hat, verändert sich das Zwischenfeld nicht
        if (!zwischenfeldErreicht) {
            if (debug) System.out.println("altes Zwischenziel bleibt erhalten (Case" + punkt + "): " + zwischenfeld);
            return zwischenfeld;
        }


        //Ansonsten neues Zwischenfeld auf dem Weg zum Zielfeld bestimmen:

        Feld aktHunterPos = welt.getFeld(welt.getHunterPos()[0], welt.getHunterPos()[1]);
        ASternSuche aStern = new ASternSuche(aktHunterPos, zielfeld, welt);

        zwischenfeld = welt.getFeld(-1, -1);

        switch (punkt) {
            case 0:
                zwischenfeld = aStern.suche(20, 10);
                break;
            case 1:
                zwischenfeld = aStern.suche(45, 5);
                break;
            case 2:
                zwischenfeld = aStern.suche(69, 1);
                break;
            case 3:
                zwischenfeld = aStern.suche(90, 0);
                break;
        }
        if (debug) System.out.println("neu bestimmtes Zwischenziel durch Case" + punkt + ": " + zwischenfeld);
        return zwischenfeld;
    }

    private void aufrufBestimmeNaechstesZielFeld(int start) {
        //BestimmteNaechstesZielfeld der Reihe nach abklappern bis Zielfeld gefunden oder keine Möglichkeit mehr
        int i;
        for (i = start; i <= zielFaelle && !welt.isInMap(zielfeld); i++) {
            bestimmeNaechstesZielFeld(i);
        }
        if (debug) System.out.println("BNZFA hat Ziel in Case gefunden: " + (i - 1));
    }

    private void bestimmeNaechstesZielFeld(int punkt) {
        if (debug) System.out.println("BNZF Case: " + punkt);
        Feld f = welt.getFeld(-1, -1);
        switch (punkt) {
            case 0:
                f = sucheNaechstesUnbekanntes(welt.getHunterPos()[0], welt.getHunterPos()[1], 45, true);
                break;
            case 1:
                f = sucheNaechstesUnbekanntes(welt.getHunterPos()[0], welt.getHunterPos()[1], 45, false);
                break;
            case 2:
                f = sucheNaechstesUnbekanntes(welt.getHunterPos()[0], welt.getHunterPos()[1], 45, false);
                //f = sucheNaechstesErweitert(); damit Map im Moment nicht erweitert wird
                break;
            case 3:
                f = sucheNaechstesUnbekanntes(welt.getHunterPos()[0], welt.getHunterPos()[1], 69, false);
                break;
            case 4:
                f = sucheNaechstesMitRisiko(welt.getHunterPos()[0], welt.getHunterPos()[1], 45, true);
                break;
            case 5:
                f = sucheNaechstesMitRisiko(welt.getHunterPos()[0], welt.getHunterPos()[1], 45, false);
                break;
            case 6:
                f = sucheNaechstesMitRisiko(welt.getHunterPos()[0], welt.getHunterPos()[1], 69, false);
                break;
        }

        zielfeld = f;
        zielfeldErreicht = false;
        if (debug) System.out.println("neu bestimmtes Zielfeld: " + zielfeld);
    }

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
                    setZwischenfeldErreicht();
                    ;
                    return HunterAction.GO_FORWARD;
            }
        } else if (welt.getHunterPos()[0] == feldPos[0] && welt.getHunterPos()[1] - 1 == feldPos[1]) {
            switch (welt.getBlickrichtung()) {
                case EAST:
                    return HunterAction.TURN_LEFT;
                case WEST:
                    return HunterAction.TURN_RIGHT;
                case NORTH:
                    setZwischenfeldErreicht();
                    return HunterAction.GO_FORWARD;
                case SOUTH:
                    return HunterAction.TURN_RIGHT;
            }
        } else if (welt.getHunterPos()[0] + 1 == feldPos[0] && welt.getHunterPos()[1] == feldPos[1]) {
            switch (welt.getBlickrichtung()) {
                case EAST:
                    setZwischenfeldErreicht();
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
                    setZwischenfeldErreicht();
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

    private Feld sucheNaechstesMitRisiko(int x, int y, int maxRisiko, boolean eingekesselt) {
        //Sucht das nächste Feld von Position x,y ausgehend mit maximalem Risiko von maxRisiko
        for (int m = 1; m < Math.max(welt.getMapSize()[0], welt.getMapSize()[1]); m++) {
            if (eingekesselt) {
                if (welt.isInMap(welt.getFeldInBlickrichtung()) && welt.getFeldInBlickrichtung().getRisiko() < maxRisiko && !welt.isEingekesselt(welt.getFeldInBlickrichtung(), maxRisiko)) {
                    if (debug)
                        System.out.println("suche eingekesselt = Feld in Blickrichtung: " + welt.getFeldInBlickrichtung());
                    return welt.getFeldInBlickrichtung();
                }
                // 4 direkt erreichbare Felder prüfen
                if (welt.isInMap(x + m, y) && welt.getFeld(x + m, y).getRisiko() < maxRisiko && !welt.isEingekesselt(x + m, y, maxRisiko)) {
                    if (debug) System.out.println("suche eingekessel: " + (x + m) + "," + y);
                    return welt.getFeld(x + m, y);
                }
                if (welt.isInMap(x - m, y) && welt.getFeld(x - m, y).getRisiko() < maxRisiko && !welt.isEingekesselt(x - m, y, maxRisiko)) {
                    if (debug) System.out.println("suche eingekessel: " + (x - m) + "," + y);
                    return welt.getFeld(x - m, y);
                }
                if (welt.isInMap(x, y + m) && welt.getFeld(x, y + m).getRisiko() < maxRisiko && !welt.isEingekesselt(x, y + m, maxRisiko)) {
                    if (debug) System.out.println("suche eingekessel: " + x + "," + (y + m));
                    return welt.getFeld(x, y + m);
                }
                if (welt.isInMap(x, y - m) && welt.getFeld(x, y - m).getRisiko() < maxRisiko && !welt.isEingekesselt(x, y - m, maxRisiko)) {
                    if (debug) System.out.println("suche eingekessel: " + x + m + "," + (y - m));
                    return welt.getFeld(x, y - m);
                }


                //weitere Felder prüfen
                for (int i = 1; i <= m; i++) {
                    if (welt.isInMap(x + m, y + i) && welt.getFeld(x + m, y + i).getRisiko() < maxRisiko && !welt.isEingekesselt(x + m, y + i, maxRisiko)) {
                        return welt.getFeld(x + m, y + i);
                    }
                    if (welt.isInMap(x + m, y - i) && welt.getFeld(x + m, y - i).getRisiko() < maxRisiko && !welt.isEingekesselt(x + m, y - i, maxRisiko)) {
                        return welt.getFeld(x + m, y - i);
                    }
                    if (welt.isInMap(x - m, y + i) && welt.getFeld(x - m, y + i).getRisiko() < maxRisiko && !welt.isEingekesselt(x - m, y + i, maxRisiko)) {
                        return welt.getFeld(x - m, y + i);
                    }
                    if (welt.isInMap(x - m, y - i) && welt.getFeld(x - m, y - i).getRisiko() < maxRisiko && !welt.isEingekesselt(x - m, y - i, maxRisiko)) {
                        return welt.getFeld(x - m, y - i);
                    }
                }
                for (int i = 1; i <= m; i++) {
                    if (welt.isInMap(x + i, y + m) && welt.getFeld(x + i, y + m).getRisiko() < maxRisiko && !welt.isEingekesselt(x + i, y + m, maxRisiko)) {
                        return welt.getFeld(x + i, y + m);
                    }
                    if (welt.isInMap(x + i, y - m) && welt.getFeld(x + i, y - m).getRisiko() < maxRisiko && !welt.isEingekesselt(x + i, y - m, maxRisiko)) {
                        return welt.getFeld(x + m, y - i);
                    }
                    if (welt.isInMap(x - i, y + m) && welt.getFeld(x - i, y + m).getRisiko() < maxRisiko && !welt.isEingekesselt(x - i, y + m, maxRisiko)) {
                        return welt.getFeld(x - m, y + i);
                    }
                    if (welt.isInMap(x - i, y - m) && welt.getFeld(x - i, y - m).getRisiko() < maxRisiko && !welt.isEingekesselt(x - i, y - m, maxRisiko)) {
                        return welt.getFeld(x - i, y - m);
                    }
                }

            } else {
                //Schleife für Schrittweite

                if (welt.isInMap(welt.getFeldInBlickrichtung()) && welt.getFeldInBlickrichtung().getRisiko() < maxRisiko) {
                    if (debug)
                        System.out.println("suche eingekesselt = Feld in Blickrichtung: " + welt.getFeldInBlickrichtung());
                    return welt.getFeldInBlickrichtung();
                }
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

        }


        System.err.println("Kein Feld mehr vorhanden (auch bereits besuchtes) oder Risiko zu groß!");
        return new Feld(Feld.Zustand.UNBEKANNT, -1, -1);
    }

    private Feld sucheNaechstesUnbekanntes(int x, int y, int maxRisiko, boolean eingekesselt) {
        //Sucht das nächste nicht besuchte Feld von Position x,y ausgehend mit maximalem Risiko von maxRisiko

        //Schleife für Schrittweite
        for (int m = 1; m < Math.max(welt.getMapSize()[0], welt.getMapSize()[1]); m++) {

            if (eingekesselt) {
                if (welt.isInMap(welt.getFeldInBlickrichtung()) && !welt.getFeldInBlickrichtung().isBesucht() && welt.getFeldInBlickrichtung().getRisiko() < maxRisiko && !welt.isEingekesselt(welt.getFeldInBlickrichtung(), maxRisiko)) {
                    if (debug)
                        System.out.println("suche Unbekanntes eingekesselt = Feld in Blickrichtung: " + welt.getFeldInBlickrichtung());
                    return welt.getFeldInBlickrichtung();
                }
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

                //if (debug) System.out.println("suche unbekanntes eingekesselt 1. Schleife");
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
                //if (debug) System.out.println("suche unbekanntes eingekesselt 2. Schleife");

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
                if (welt.isInMap(welt.getFeldInBlickrichtung()) && !welt.getFeldInBlickrichtung().isBesucht() && welt.getFeldInBlickrichtung().getRisiko() < maxRisiko) {
                    if (debug)
                        System.out.println("suche Unbekanntes = Feld in Blickrichtung: " + welt.getFeldInBlickrichtung());
                    return welt.getFeldInBlickrichtung();
                }
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

        if (debug)
            System.out.println("Kein unbekanntes Feld mehr vorhanden oder Risiko zu groß! Risiko: " + maxRisiko);
        return new Feld(Feld.Zustand.UNBEKANNT, -1, -1);
    }

    private Feld sucheNaechstesErweitert() {
        //Wenn kein unbesuchtes Feld mehr in der Nähe, die Weltgröße aber noch nicht begrenzt ist: Map nach unten-rechts erweitern
        if (!welt.isUmrandet()) {
            return welt.getFeld(welt.getMapSize()[0], welt.getMapSize()[1]);
        }

        if (debug)
            System.out.println("Map konnte nicht erweitert werden, vermutlich umrandet? " + welt.isUmrandet());
        return new Feld(Feld.Zustand.UNBEKANNT, -1, -1);
    }

    private HunterAction wumpusToeten() {
        //bereits bekannt: ich bin an mindestens 2 Stellen von Gestank oder ähnlichem umkesselt und der Wumpus lebt noch

        if (debug) System.out.println("!!!!!!!!!!!!!!!!!!! WUMPUS TOETEN !!!!!!!!!!!!!!!!!!!");
        Feld feldInBlickrichtung = welt.getFeldInBlickrichtung();

        if (debug) System.out.println("Feld in Blickrichtung: " + feldInBlickrichtung);
        if ((feldInBlickrichtung.getZustaende().contains(GESTANK1) || feldInBlickrichtung.getZustaende().contains(GESTANK2)) && !feldInBlickrichtung.isBeschossen()) {
            if (debug) System.out.println("Schießen in Blickrichtung");
            return HunterAction.SHOOT;
        }

        if (feldInBlickrichtung.getRisiko() < 69) {
            if (debug) System.out.println("Zielfeld auf das Feld in Blickrichtung gesetzt");
            setZielfeld(feldInBlickrichtung);
        }

        if (debug) System.out.println("!!!!!!!!!!!!!!!!!!! ENDE WUMPUS TOETEN !!!!!!!!!!!!!!!!!!!");
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
        zielfeld = welt.getFeld(-1, -1);
    }

    public void setZwischenfeldErreicht() {
        zwischenfeldErreicht = true;
        zwischenfeld = welt.getFeld(-1, -1);
    }
}
