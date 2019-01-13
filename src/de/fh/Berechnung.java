package de.fh;

import de.fh.wumpus.enums.HunterAction;

import java.util.HashSet;

import static de.fh.Feld.Zustand.*;
//TODO verstecken in einer Ecke verhindern
//TODO Vom Wumpus getötet werden wenn beide gleichzeitig aufs gleiche Feld gehen
//TODO Fallen genauer bestimmen (modus Falltesten)

public class Berechnung {
    private boolean debug = true;

    private Welt welt;
    private String modus;

    private Feld zielfeld;
    private boolean zielfeldErreicht; //muss auf False gesetzt werden, wenn ein neues Ziel bestimmt wurde
    private Feld zwischenfeld;
    private boolean zwischenfeldErreicht; //muss auf False gesetzt werden, wenn ein neues Zwischen-Ziel bestimmt wurde
    private HashSet<Feld> nichtErreichbareFelder;

    private boolean besuchtSetzen;

    //Config:
    private final int anzahlEinkesselndeFelder = 2;
    private final int zielFaelle = 5;
    private final int zwischenzielFaelle = 3;

    private int umrandeModus = 1;

    public Berechnung(Welt welt) {
        this.welt = welt;
        setZielfeldErreicht();
        setZielfeldErreicht();
        modus = "umrande";
        besuchtSetzen = false;
    }

    //--------------------------------------------------------------------------------------------------------------------
    //Hauptmethode die aufgerufen wird
    //--------------------------------------------------------------------------------------------------------------------
    public void berechne() {
        HunterAction nextAction = HunterAction.SIT;
        nichtErreichbareFelder = new HashSet<Feld>();

        // ---------------------- Start Ausgabe:
        {
            System.out.println(" ----------------- Berechnungsausgabe: ----------------- ");
        }

        // ---------------------- Abfragen vor Berechnungsbeginn:
        {
            if (modus.equalsIgnoreCase("goldAufnehmen")) {
                modus = "goldAufnehmen";
            } else {

                if (welt.isUmrandet()) {
                    besuchtSetzen = true;
                    if (welt.isGoldAufgesammelt()) {
                        if (welt.isWumpusLebendig()) modus = "wumpusToeten";
                        else modus = "goldAufgenommenUndWumpusTot";
                    } else {
                        modus = "standard";
                    }
                    if (welt.getPunkte() == 0) modus = "aufgeben";

                } else { //Welt ist nicht umrandet
                    modus = "umrande";

                    //Wenn Hunter nach einer Runde wieder auf Feld 1,1 kommt: Map umranden
                    //Wenn er erst 10 Felder besucht hat, ist er wahrscheinlich zufällig auf 1,1 gekommen und sollte weiter die Map umranden
                    if (zielfeld == welt.getFeld(1, 1, besuchtSetzen) && welt.getFeld(1, 1, besuchtSetzen) == welt.getHunterFeld() && welt.anzahlBesuchterFelder() > 10) {
                        welt.umrande();
                        //Wenn das umranden der Map nicht geklappt hat, aufgeben
                        if (!welt.isUmrandet() && welt.getHunterFeld().isBesucht()) modus = "aufgeben";
                            //Wenn das umranden geklappt hat: standard Modus
                        else modus = "standard";
                    }
                }

                //Wenn jedes Feld besucht ist und das aktuelle Ziel nicht der Start ist: aufgeben
                if (zielfeld != welt.getFeld(1, 1, besuchtSetzen) && welt.anzahlBesuchterFelder() == welt.anzahlFelder())
                    modus = "aufgeben";
            }


            //Wenn Hunter sich am Ziel befindet oder eingekesselt ist
            if (welt.getHunterFeld() == zielfeld || welt.isEingekesselt(welt.getHunterFeld(), 45, besuchtSetzen, anzahlEinkesselndeFelder)) {
                if (debug) System.out.println("Hunter befindet sich auf aktuellem Zielfeld!");
                setZielfeldErreicht();
            }


            //Wenn der Hunter von Gestank eingekesselt ist und Wumpus lebendig ist, soll versucht werden den Wumpus zu töten
            if (welt.isWumpusLebendig() && welt.isVonGestankEingekesselt(besuchtSetzen) && welt.getAnzahlPfeile() > 0) {
                if (debug)
                    System.out.println("Berechnung setzt Modus auf Wumpus töten, da von Gestank umrandet und Wumpus lebendig!");
                modus = "wumpusToeten";
            }

            if ((!welt.isUmrandet() && modus.equalsIgnoreCase("wumpusToeten") || (modus.equalsIgnoreCase("umrande") && zielfeld == welt.getFeld(1, 1, besuchtSetzen))))
                besuchtSetzen = false;
            else besuchtSetzen = true;
            if (debug) System.out.println("besucht setzen: " + besuchtSetzen);


            //Wenn er im Umrande Modus 15 Felder gelaufen ist, die Felder um 1,1 wieder auf unbesucht setzen, damit 1,1 auf jeden Fall erreicht werden kann
            if (modus.equalsIgnoreCase("umrande") && welt.anzahlBesuchterFelder() == 15) {
                welt.getFeld(1, 1, besuchtSetzen).setNichtBesucht();
                welt.getFeld(2, 1, besuchtSetzen).setNichtBesucht();
                welt.getFeld(3, 1, besuchtSetzen).setNichtBesucht();
                welt.getFeld(1, 2, besuchtSetzen).setNichtBesucht();
                welt.getFeld(2, 2, besuchtSetzen).setNichtBesucht();
                welt.getFeld(3, 2, besuchtSetzen).setNichtBesucht();
            }

            //Wenn das aktuelle Feld von EVTFALLE eingekesselt wurde weil besuchtSetzen auf false war, (da Modus auf WumpusTöten war) : Feld hinter mir wieder löschen
            if (welt.isEingekesselt(welt.getHunterFeld(), 90, besuchtSetzen, 4))
                welt.getFeldHinterMir().removeZustand(EVTFALLE);

            if (!welt.isWumpusLebendig() && welt.isGoldAufgesammelt() && welt.getHunterFeld() == welt.getFeld(1,1,besuchtSetzen)) modus = "goldAufgenommenUndWumpusTotUndStart";
        }

        // ---------------------- Switch Modus:
        {
            //Zusätzliche Aktionen die in bestimmten Fällen ausgeführt werden sollen
            System.out.println("aktueller Modus: " + modus);
            switch (modus) {
                case "umrande": //bestimmt nächstes Ziel
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
                    zielfeld = welt.getFeld(1, 1, besuchtSetzen);
                    if (welt.getHunterFeld() != zielfeld) setZielfeldNichtErreicht();
                    break;

                case "goldAufgenommenUndWumpusTotUndStart":
                    welt.addPunkte(100);
                    nextAction = HunterAction.QUIT_GAME;
                    break;

                case "wumpusToeten":
                    nextAction = wumpusToeten();
                    break;

                case "aufgeben":
                    nextAction = HunterAction.QUIT_GAME;
                    break;

            }
        }

        // ---------------------- Standardfall:
        {
            //Wenn nextAction im Switch noch nicht überschrieben wurde
            if (nextAction == HunterAction.SIT) {
                if (debug) System.out.println("Standardfall:");
                nextAction = bestimmeZielUndZwischenziel();
            }
        }

        // ---------------------- Ende Ausgabe:
        {
            System.out.println("Nächste berechnete Aktion: " + nextAction);
            welt.addNextAction(nextAction);
            System.out.println(" ----------------- ENDE BERECHNUNGSAUSGABE ----------------- ");
        }

    }
    //--------------------------------------------------------------------------------------------------------------------


    //--------------------------------------------------------------------------------------------------------------------
    //Hilfsmethoden der Klasse
    //--------------------------------------------------------------------------------------------------------------------
    private void umrande(int maxRisiko) {
        //Ziel der Methode: die Welt soll vollständig umrandet werden
        //Vorgehensweise: eine Runde komplett am Rand entlang laufen
        Feld f;

        //if (debug) System.out.println("<<<<<<<<UMRANDE>>>>>>>>");
        switch (umrandeModus) {
            case 1: //nach UL
                if (debug) System.out.println("Modus UL");

                //1. nach links gehen
                if (!((f = welt.getFeld(welt.getHunterPos()[0] - 1, welt.getHunterPos()[1], besuchtSetzen)).isBesucht()) && f.getRisiko() < maxRisiko) {
                    setZielfeld(f);
                    if (debug) System.out.println("Case1 links");
                }

                //2. nach unten gehen
                else if (!((f = welt.getFeld(welt.getHunterPos()[0], welt.getHunterPos()[1] + 1, besuchtSetzen)).isBesucht()) && f.getRisiko() < maxRisiko) {
                    setZielfeld(f);
                    if (debug) System.out.println("Case1 unten");
                }


                //3. nach rechts gehen
                else if (!((f = welt.getFeld(welt.getHunterPos()[0] + 1, welt.getHunterPos()[1], besuchtSetzen)).isBesucht()) && f.getRisiko() < maxRisiko) {
                    setZielfeld(f);
                    umrandeModus = 2;
                    if (debug) System.out.println("Case1 rechts");
                }


                //4. nach oben gehen
                else if ((f = welt.getFeld(welt.getHunterPos()[0], welt.getHunterPos()[1] - 1, besuchtSetzen)).getRisiko() < maxRisiko) {
                    setZielfeld(f);
                    if (debug) System.out.println("Case1 oben");
                    umrandeModus = 3;
                    //ToDo mit Dokument abgleichen
                }

                break;

            case 2: //nach UR
                if (debug) System.out.println("Modus UR");
                //1. nach unten gehen
                if (!((f = welt.getFeld(welt.getHunterPos()[0], welt.getHunterPos()[1] + 1, besuchtSetzen)).isBesucht()) && f.getRisiko() < maxRisiko) {
                    setZielfeld(f);
                    if (debug) System.out.println("Case2 unten");
                     umrandeModus = 1;
                }

                //2. nach rechts gehen
                else if (!((f = welt.getFeld(welt.getHunterPos()[0] + 1, welt.getHunterPos()[1], besuchtSetzen)).isBesucht()) && f.getRisiko() < maxRisiko) {
                    setZielfeld(f);
                    if (debug) System.out.println("Case2 rechts");
                }


                //3. nach oben gehen
                else if (!((f = welt.getFeld(welt.getHunterPos()[0], welt.getHunterPos()[1] - 1, besuchtSetzen)).isBesucht()) && f.getRisiko() < maxRisiko) {
                    setZielfeld(f);
                    umrandeModus = 3;
                    if (debug) System.out.println("Case2 oben");

                }

                //4.. nach links gehen
                else if ((f = welt.getFeld(welt.getHunterPos()[0] - 1, welt.getHunterPos()[1], besuchtSetzen)).getRisiko() < maxRisiko) {
                    setZielfeld(f);
                    umrandeModus = 3;
                    if (debug) System.out.println("Case2 links");

                }

                break;

            case 3: //nach OR
                if (debug) System.out.println("Modus OR");

                //1. nach rechts gehen
                if (!((f = welt.getFeld(welt.getHunterPos()[0] + 1, welt.getHunterPos()[1], besuchtSetzen)).isBesucht()) && f.getRisiko() < maxRisiko) {
                    umrandeModus = 2;
                    setZielfeld(f);
                    if (debug) System.out.println("Case3 rechts");
                }

                //2. nach oben gehen
                else if (!((f = welt.getFeld(welt.getHunterPos()[0], welt.getHunterPos()[1] - 1, besuchtSetzen)).isBesucht()) && f.getRisiko() < maxRisiko) {
                    if (debug) System.out.println("Case3 oben");
                    setZielfeld(f);
                }

                //3.. nach links gehen
                else if (!((f = welt.getFeld(welt.getHunterPos()[0] - 1, welt.getHunterPos()[1], besuchtSetzen)).isBesucht()) && f.getRisiko() < maxRisiko) {
                    umrandeModus = 4;
                    setZielfeld(f);
                    if (debug) System.out.println("Case3 links");
                }

                //4. nach unten gehen
                else if ((f = welt.getFeld(welt.getHunterPos()[0], welt.getHunterPos()[1] + 1, besuchtSetzen)).getRisiko() < maxRisiko && !f.isBesucht()) {
                    if (debug) System.out.println("Case3 unten");
                    setZielfeld(f);
                }

                break;


            case 4: //nach OL
                if (debug) System.out.println("Modus OL");

                //1. nach oben gehen
                if ((f = welt.getFeld(welt.getHunterPos()[0], welt.getHunterPos()[1] - 1, besuchtSetzen)).getRisiko() < maxRisiko && !f.isBesucht()) {
                    setZielfeld(f);

                    umrandeModus = 3;
                    if (debug) System.out.println("Case4 oben");

                }

                //2. nach links gehen
                else if ((f = welt.getFeld(welt.getHunterPos()[0] - 1, welt.getHunterPos()[1], besuchtSetzen)).getRisiko() < maxRisiko && !f.isBesucht()) {
                    if (debug) System.out.println("Case4 links");
                    setZielfeld(f);
                }

                //3. nach unten gehen
                else if ((f = welt.getFeld(welt.getHunterPos()[0], welt.getHunterPos()[1] + 1, besuchtSetzen)).getRisiko() < maxRisiko && !f.isBesucht()) {
                    if (debug) System.out.println("Case4 unten");
                    setZielfeld(f);
                }

                //4.. nach rechts gehen
                else if ((f = welt.getFeld(welt.getHunterPos()[0] + 1, welt.getHunterPos()[1], besuchtSetzen)).getRisiko() < maxRisiko && !f.isBesucht()) {
                    if (debug) System.out.println("Case4 rechts");
                    setZielfeld(f);
                }

                break;
        }

        //if (debug) System.out.println("<<<<<<<<ENDE>>>>>>>>");
    }
    //--------------------------------------------------------------------------------------------------------------------


    //--------------------------------------------------------------------------------------------------------------------
    //BZUZW
    private HunterAction bestimmeZielUndZwischenziel() {
        //Methode bestimmt sofern möglich ein neues Ziel und ein neues Zwischenziel
        if (debug) System.out.println("BZUZW:: starte die Suche nach neuem Haupt- und Zwischenziel");
        int fall = 0;

        //Wenn bereits ein Zielfeld gegeben ist: Springen zu Zwischenfeld berechnen
        if (!welt.isInMap(zielfeld)) {
            //Sofern möglich ein neues Hauptziel bestimmen
            fall = aufrufBestimmeNaechstesZielFeld(0);

            //In dem Fall, dass kein neues HauptZielfeld mehr gefunden werden konnte: Spiel verlassen
            if (!welt.isInMap(zielfeld)) {
                if (debug) System.out.println("BZUZW:: 1. QuitGame da kein neues HauptZiel gefunden werden konnte");
                return HunterAction.QUIT_GAME;
            }

        }

        //!
        //Wenn dieser Punkt erreicht wurde, haben wir ein gültiges Zielfeld. Nun wird versucht ein entsprechendes Zwischenfeld zu ermitteln
        //!


        aufrufBestimmeNaechstesZwischenFeld(0);
        //Wenn das Zwischenfeld nicht erreicht werden konnte: versuchen andere Hauptziele zu bestimmmen, welche dann erreichbar sind!
        if (!welt.isInMap(zwischenfeld)) {

            for (int i = fall; i <= zielFaelle; i++) {
                //versuchen neues Hauptziel zu bestimmen

                aufrufBestimmeNaechstesZielFeld(i);
                if (welt.isInMap(zielfeld)) {

                    aufrufBestimmeNaechstesZwischenFeld(0);
                    if (welt.isInMap(zwischenfeld)) {
                        return bestimmeWegZumZwischenFeld();
                    }
                }
            }
        }
        //Fall, dass das 1. Hauptziel und 1. Zwischenziel bereits erfolgreich waren.
        else {
            return bestimmeWegZumZwischenFeld();
        }

        //Wenn keine Lösung mehr gefunden werden konnte, bisher also nie return bestimmeWegZumZwischenFeld(); in Kraft getreten ist: Spiel verlassen
        if (debug) System.out.println("BZUZW:: 2. QuitGame da kein Zwischenziel gefunden");
        return HunterAction.QUIT_GAME;
    }
    //--------------------------------------------------------------------------------------------------------------------


    //--------------------------------------------------------------------------------------------------------------------
    //BNZFA
    private int aufrufBestimmeNaechstesZielFeld(int start) {
        //Versucht ein neues Hauptziel zu bestimmen.
        //Zusätzliche Bedingungen zu den jeweiligen Einzelbedingungen:
        //1. das Feld muss innerhalb der Map liegen
        //2. das Feld darf nicht in der Liste der zuletzt besuchten Felder vorkommen
        //wenn kein neues Hauptziel bestimmt werden kann, wird das Hauptziel auf -1,-1 gesetzt


        if (debug) System.out.println("BNZFA:: starte Suche nach neuem Hauptziel");
        int i;
        for (i = start; i <= zielFaelle; i++) {
            bestimmeNaechstesZielFeld(i);
            //Schleife hört auf wenn alle Testfälle durch gelaufen sind, oder ein Feld innerhalb der Map gefunden wurde, oder
            if (welt.isInMap(zielfeld) && !welt.isInLetzteBesuchteFelder(zielfeld, 3) && !nichtErreichbareFelder.contains(zielfeld)) break;
        }

        if (debug && !welt.isInMap(zielfeld))
            System.out.println("BNZFA:: es konnte kein neues Hauptziel bestimmt werden! " + zielfeld);
        else if (debug) System.out.println("BNZFA:: neues Hauptziel in Case " + (i) + " gefunden: " + zielfeld);
        return (i - 1);
    }

    //BNZF
    private void bestimmeNaechstesZielFeld(int punkt) { //BNZF
        if (debug) System.out.println("BNZF:: Case " + punkt);
        Feld f = welt.getFeld(-1, -1, besuchtSetzen);
        switch (punkt) {
            case 0:
                f = sucheNaechstesUnbekanntes(welt.getHunterPos()[0], welt.getHunterPos()[1], 45, true);
                break;
            case 1:
                f = sucheNaechstesUnbekanntes(welt.getHunterPos()[0], welt.getHunterPos()[1], 45, false);
                break;
            case 2:
                f = sucheNaechstesUnbekanntes(welt.getHunterPos()[0], welt.getHunterPos()[1], 69, false);
                break;
            case 3:
                f = sucheNaechstesMitRisiko(welt.getHunterPos()[0], welt.getHunterPos()[1], 45, true);
                break;
            case 4:
                f = sucheNaechstesMitRisiko(welt.getHunterPos()[0], welt.getHunterPos()[1], 45, false);
                break;
            case 5:
                f = sucheNaechstesMitRisiko(welt.getHunterPos()[0], welt.getHunterPos()[1], 69, false);
                break;
        }

        zielfeld = f;
        if (welt.isInMap(zielfeld)) setZielfeldNichtErreicht();
    }

    private Feld sucheNaechstesMitRisiko(int x, int y, int maxRisiko, boolean eingekesselt) {
        //Sucht das nächste Feld von Position x,y ausgehend mit maximalem Risiko von maxRisiko
        for (int m = 1; m < Math.max(welt.getMapSize()[0], welt.getMapSize()[1]); m++) {
            if (eingekesselt) {
                if (welt.isInMap(welt.getFeldInBlickrichtung(besuchtSetzen)) && welt.getFeldInBlickrichtung(besuchtSetzen).getRisiko() < maxRisiko && !welt.isEingekesselt(welt.getFeldInBlickrichtung(besuchtSetzen), maxRisiko, besuchtSetzen, anzahlEinkesselndeFelder)) {
                    if (debug)
                        System.out.println("suche eingekesselt = Feld in Blickrichtung: " + welt.getFeldInBlickrichtung(besuchtSetzen));
                    return welt.getFeldInBlickrichtung(besuchtSetzen);
                }
                // 4 direkt erreichbare Felder prüfen
                if (welt.isInMap(x + m, y) && welt.getFeld(x + m, y, besuchtSetzen).getRisiko() < maxRisiko && !welt.isEingekesselt(x + m, y, maxRisiko, besuchtSetzen, anzahlEinkesselndeFelder)) {
                    if (debug) System.out.println("suche eingekessel: " + (x + m) + "," + y);
                    return welt.getFeld(x + m, y, besuchtSetzen);
                }
                if (welt.isInMap(x - m, y) && welt.getFeld(x - m, y, besuchtSetzen).getRisiko() < maxRisiko && !welt.isEingekesselt(x - m, y, maxRisiko, besuchtSetzen, anzahlEinkesselndeFelder)) {
                    if (debug) System.out.println("suche eingekessel: " + (x - m) + "," + y);
                    return welt.getFeld(x - m, y, besuchtSetzen);
                }
                if (welt.isInMap(x, y + m) && welt.getFeld(x, y + m, besuchtSetzen).getRisiko() < maxRisiko && !welt.isEingekesselt(x, y + m, maxRisiko, besuchtSetzen, anzahlEinkesselndeFelder)) {
                    if (debug) System.out.println("suche eingekessel: " + x + "," + (y + m));
                    return welt.getFeld(x, y + m, besuchtSetzen);
                }
                if (welt.isInMap(x, y - m) && welt.getFeld(x, y - m, besuchtSetzen).getRisiko() < maxRisiko && !welt.isEingekesselt(x, y - m, maxRisiko, besuchtSetzen, anzahlEinkesselndeFelder)) {
                    if (debug) System.out.println("suche eingekessel: " + x + m + "," + (y - m));
                    return welt.getFeld(x, y - m, besuchtSetzen);
                }


                //weitere Felder prüfen
                for (int i = 1; i <= m; i++) {
                    if (welt.isInMap(x + m, y + i) && welt.getFeld(x + m, y + i, besuchtSetzen).getRisiko() < maxRisiko && !welt.isEingekesselt(x + m, y + i, maxRisiko, besuchtSetzen, anzahlEinkesselndeFelder)) {
                        return welt.getFeld(x + m, y + i, besuchtSetzen);
                    }
                    if (welt.isInMap(x + m, y - i) && welt.getFeld(x + m, y - i, besuchtSetzen).getRisiko() < maxRisiko && !welt.isEingekesselt(x + m, y - i, maxRisiko, besuchtSetzen, anzahlEinkesselndeFelder)) {
                        return welt.getFeld(x + m, y - i, besuchtSetzen);
                    }
                    if (welt.isInMap(x - m, y + i) && welt.getFeld(x - m, y + i, besuchtSetzen).getRisiko() < maxRisiko && !welt.isEingekesselt(x - m, y + i, maxRisiko, besuchtSetzen, anzahlEinkesselndeFelder)) {
                        return welt.getFeld(x - m, y + i, besuchtSetzen);
                    }
                    if (welt.isInMap(x - m, y - i) && welt.getFeld(x - m, y - i, besuchtSetzen).getRisiko() < maxRisiko && !welt.isEingekesselt(x - m, y - i, maxRisiko, besuchtSetzen, anzahlEinkesselndeFelder)) {
                        return welt.getFeld(x - m, y - i, besuchtSetzen);
                    }
                }
                for (int i = 1; i <= m; i++) {
                    if (welt.isInMap(x + i, y + m) && welt.getFeld(x + i, y + m, besuchtSetzen).getRisiko() < maxRisiko && !welt.isEingekesselt(x + i, y + m, maxRisiko, besuchtSetzen, anzahlEinkesselndeFelder)) {
                        return welt.getFeld(x + i, y + m, besuchtSetzen);
                    }
                    if (welt.isInMap(x + i, y - m) && welt.getFeld(x + i, y - m, besuchtSetzen).getRisiko() < maxRisiko && !welt.isEingekesselt(x + i, y - m, maxRisiko, besuchtSetzen, anzahlEinkesselndeFelder)) {
                        return welt.getFeld(x + m, y - i, besuchtSetzen);
                    }
                    if (welt.isInMap(x - i, y + m) && welt.getFeld(x - i, y + m, besuchtSetzen).getRisiko() < maxRisiko && !welt.isEingekesselt(x - i, y + m, maxRisiko, besuchtSetzen, anzahlEinkesselndeFelder)) {
                        return welt.getFeld(x - m, y + i, besuchtSetzen);
                    }
                    if (welt.isInMap(x - i, y - m) && welt.getFeld(x - i, y - m, besuchtSetzen).getRisiko() < maxRisiko && !welt.isEingekesselt(x - i, y - m, maxRisiko, besuchtSetzen, anzahlEinkesselndeFelder)) {
                        return welt.getFeld(x - i, y - m, besuchtSetzen);
                    }
                }

            } else {
                //Schleife für Schrittweite

                if (welt.isInMap(welt.getFeldInBlickrichtung(besuchtSetzen)) && welt.getFeldInBlickrichtung(besuchtSetzen).getRisiko() < maxRisiko) {
                    if (debug)
                        System.out.println("suche eingekesselt = Feld in Blickrichtung: " + welt.getFeldInBlickrichtung(besuchtSetzen));
                    return welt.getFeldInBlickrichtung(besuchtSetzen);
                }
                // 4 direkt erreichbare Felder prüfen
                if (welt.isInMap(x + m, y) && welt.getFeld(x + m, y, besuchtSetzen).getRisiko() < maxRisiko) {
                    return welt.getFeld(x + m, y, besuchtSetzen);
                }
                if (welt.isInMap(x - m, y) && welt.getFeld(x - m, y, besuchtSetzen).getRisiko() < maxRisiko) {
                    return welt.getFeld(x - m, y, besuchtSetzen);
                }
                if (welt.isInMap(x, y + m) && welt.getFeld(x, y + m, besuchtSetzen).getRisiko() < maxRisiko) {
                    return welt.getFeld(x, y + m, besuchtSetzen);
                }
                if (welt.isInMap(x, y - m) && welt.getFeld(x, y - m, besuchtSetzen).getRisiko() < maxRisiko) {
                    return welt.getFeld(x, y - m, besuchtSetzen);
                }


                //weitere Felder prüfen
                for (int i = 1; i <= m; i++) {
                    if (welt.isInMap(x + m, y + i) && welt.getFeld(x + m, y + i, besuchtSetzen).getRisiko() < maxRisiko) {
                        return welt.getFeld(x + m, y + i, besuchtSetzen);
                    }
                    if (welt.isInMap(x + m, y - i) && welt.getFeld(x + m, y - i, besuchtSetzen).getRisiko() < maxRisiko) {
                        return welt.getFeld(x + m, y - i, besuchtSetzen);
                    }
                    if (welt.isInMap(x - m, y + i) && welt.getFeld(x - m, y + i, besuchtSetzen).getRisiko() < maxRisiko) {
                        return welt.getFeld(x - m, y + i, besuchtSetzen);
                    }
                    if (welt.isInMap(x - m, y - i) && welt.getFeld(x - m, y - i, besuchtSetzen).getRisiko() < maxRisiko) {
                        return welt.getFeld(x - m, y - i, besuchtSetzen);
                    }
                }
                for (int i = 1; i <= m; i++) {
                    if (welt.isInMap(x + i, y + m) && welt.getFeld(x + i, y + m, besuchtSetzen).getRisiko() < maxRisiko) {
                        return welt.getFeld(x + i, y + m, besuchtSetzen);
                    }
                    if (welt.isInMap(x + i, y - m) && welt.getFeld(x + i, y - m, besuchtSetzen).getRisiko() < maxRisiko) {
                        return welt.getFeld(x + m, y - i, besuchtSetzen);
                    }
                    if (welt.isInMap(x - i, y + m) && welt.getFeld(x - i, y + m, besuchtSetzen).getRisiko() < maxRisiko) {
                        return welt.getFeld(x - m, y + i, besuchtSetzen);
                    }
                    if (welt.isInMap(x - i, y - m) && welt.getFeld(x - i, y - m, besuchtSetzen).getRisiko() < maxRisiko) {
                        return welt.getFeld(x - i, y - m, besuchtSetzen);
                    }
                }
            }

        }


        System.err.println("Kein Feld mehr vorhanden (auch bereits besuchtes) oder Risiko zu groß!");
        return new Feld(Feld.Zustand.UNBEKANNT, -1, -1, besuchtSetzen);
    }

    private Feld sucheNaechstesUnbekanntes(int x, int y, int maxRisiko, boolean eingekesselt) {
        //Sucht das nächste nicht besuchte Feld von Position x,y ausgehend mit maximalem Risiko von maxRisiko

        //Schleife für Schrittweite
        for (int m = 1; m < Math.max(welt.getMapSize()[0], welt.getMapSize()[1]); m++) {

            if (eingekesselt) {
                if (welt.isInMap(welt.getFeldInBlickrichtung(besuchtSetzen)) && !welt.getFeldInBlickrichtung(besuchtSetzen).isBesucht() && welt.getFeldInBlickrichtung(besuchtSetzen).getRisiko() < maxRisiko && !welt.isEingekesselt(welt.getFeldInBlickrichtung(besuchtSetzen), maxRisiko, besuchtSetzen, anzahlEinkesselndeFelder)) {
                    if (debug)
                        System.out.println("suche Unbekanntes eingekesselt = Feld in Blickrichtung: " + welt.getFeldInBlickrichtung(besuchtSetzen));
                    return welt.getFeldInBlickrichtung(besuchtSetzen);
                }
                // 4 direkt erreichbare Felder prüfen
                if (welt.isInMap(x + m, y) && !welt.getFeld(x + m, y, besuchtSetzen).isBesucht() && welt.getFeld(x + m, y, besuchtSetzen).getRisiko() < maxRisiko && !welt.isEingekesselt(x + m, y, maxRisiko, besuchtSetzen, anzahlEinkesselndeFelder)) {
                    if (debug) System.out.println("suche Unbekanntes eingekessel: " + (x + m) + "," + y);
                    return welt.getFeld(x + m, y, besuchtSetzen);
                }
                if (welt.isInMap(x - m, y) && !welt.getFeld(x - m, y, besuchtSetzen).isBesucht() && welt.getFeld(x - m, y, besuchtSetzen).getRisiko() < maxRisiko && !welt.isEingekesselt(x - m, y, maxRisiko, besuchtSetzen, anzahlEinkesselndeFelder)) {
                    if (debug) System.out.println("suche Unbekanntes eingekessel: " + (x - m) + "," + y);
                    return welt.getFeld(x - m, y, besuchtSetzen);
                }
                if (welt.isInMap(x, y + m) && !welt.getFeld(x, y + m, besuchtSetzen).isBesucht() && welt.getFeld(x, y + m, besuchtSetzen).getRisiko() < maxRisiko && !welt.isEingekesselt(x, y + m, maxRisiko, besuchtSetzen, anzahlEinkesselndeFelder)) {
                    if (debug) System.out.println("suche Unbekanntes eingekessel: " + x + "," + (y + m));
                    return welt.getFeld(x, y + m, besuchtSetzen);
                }
                if (welt.isInMap(x, y - m) && !welt.getFeld(x, y - m, besuchtSetzen).isBesucht() && welt.getFeld(x, y - m, besuchtSetzen).getRisiko() < maxRisiko && !welt.isEingekesselt(x, y - m, maxRisiko, besuchtSetzen, anzahlEinkesselndeFelder)) {
                    if (debug) System.out.println("suche Unbekanntes eingekessel: " + x + m + "," + (y - m));
                    return welt.getFeld(x, y - m, besuchtSetzen);
                }

                //if (debug) System.out.println("suche unbekanntes eingekesselt 1. Schleife");
                //weitere Felder prüfen
                for (int i = 1; i <= m; i++) {
                    if (welt.isInMap(x + m, y + i) && !welt.getFeld(x + m, y + i, besuchtSetzen).isBesucht() && welt.getFeld(x + m, y + i, besuchtSetzen).getRisiko() < maxRisiko && !welt.isEingekesselt(x + m, y + i, maxRisiko, besuchtSetzen, anzahlEinkesselndeFelder)) {
                        return welt.getFeld(x + m, y + i, besuchtSetzen);
                    }
                    if (welt.isInMap(x + m, y - i) && !welt.getFeld(x + m, y - i, besuchtSetzen).isBesucht() && welt.getFeld(x + m, y - i, besuchtSetzen).getRisiko() < maxRisiko && !welt.isEingekesselt(x + m, y - i, maxRisiko, besuchtSetzen, anzahlEinkesselndeFelder)) {
                        return welt.getFeld(x + m, y - i, besuchtSetzen);
                    }
                    if (welt.isInMap(x - m, y + i) && !welt.getFeld(x - m, y + i, besuchtSetzen).isBesucht() && welt.getFeld(x - m, y + i, besuchtSetzen).getRisiko() < maxRisiko && !welt.isEingekesselt(x - m, y + i, maxRisiko, besuchtSetzen, anzahlEinkesselndeFelder)) {
                        return welt.getFeld(x - m, y + i, besuchtSetzen);
                    }
                    if (welt.isInMap(x - m, y - i) && !welt.getFeld(x - m, y - i, besuchtSetzen).isBesucht() && welt.getFeld(x - m, y - i, besuchtSetzen).getRisiko() < maxRisiko && !welt.isEingekesselt(x - m, y - i, maxRisiko, besuchtSetzen, anzahlEinkesselndeFelder)) {
                        return welt.getFeld(x - m, y - i, besuchtSetzen);
                    }
                }
                //if (debug) System.out.println("suche unbekanntes eingekesselt 2. Schleife");

                for (int i = 1; i <= m; i++) {
                    if (welt.isInMap(x + i, y + m) && !welt.getFeld(x + i, y + m, besuchtSetzen).isBesucht() && welt.getFeld(x + i, y + m, besuchtSetzen).getRisiko() < maxRisiko && !welt.isEingekesselt(x + i, y + m, maxRisiko, besuchtSetzen, anzahlEinkesselndeFelder)) {
                        return welt.getFeld(x + i, y + m, besuchtSetzen);
                    }
                    if (welt.isInMap(x + i, y - m) && !welt.getFeld(x + i, y - m, besuchtSetzen).isBesucht() && welt.getFeld(x + i, y - m, besuchtSetzen).getRisiko() < maxRisiko && !welt.isEingekesselt(x + i, y - m, maxRisiko, besuchtSetzen, anzahlEinkesselndeFelder)) {
                        return welt.getFeld(x + m, y - i, besuchtSetzen);
                    }
                    if (welt.isInMap(x - i, y + m) && !welt.getFeld(x - i, y + m, besuchtSetzen).isBesucht() && welt.getFeld(x - i, y + m, besuchtSetzen).getRisiko() < maxRisiko && !welt.isEingekesselt(x - i, y + m, maxRisiko, besuchtSetzen, anzahlEinkesselndeFelder)) {
                        return welt.getFeld(x - m, y + i, besuchtSetzen);
                    }
                    if (welt.isInMap(x - i, y - m) && !welt.getFeld(x - i, y - m, besuchtSetzen).isBesucht() && welt.getFeld(x - i, y - m, besuchtSetzen).getRisiko() < maxRisiko && !welt.isEingekesselt(x - i, y - m, maxRisiko, besuchtSetzen, anzahlEinkesselndeFelder)) {
                        return welt.getFeld(x - i, y - m, besuchtSetzen);
                    }
                }
            } else {
                if (welt.isInMap(welt.getFeldInBlickrichtung(besuchtSetzen)) && !welt.getFeldInBlickrichtung(besuchtSetzen).isBesucht() && welt.getFeldInBlickrichtung(besuchtSetzen).getRisiko() < maxRisiko) {
                    if (debug)
                        System.out.println("suche Unbekanntes = Feld in Blickrichtung: " + welt.getFeldInBlickrichtung(besuchtSetzen));
                    return welt.getFeldInBlickrichtung(besuchtSetzen);
                }
                // 4 direkt erreichbare Felder prüfen
                if (welt.isInMap(x + m, y) && !welt.getFeld(x + m, y, besuchtSetzen).isBesucht() && welt.getFeld(x + m, y, besuchtSetzen).getRisiko() < maxRisiko) {
                    if (debug) System.out.println("suche Unbekanntes: " + (x + m) + "," + y);
                    return welt.getFeld(x + m, y, besuchtSetzen);
                }
                if (welt.isInMap(x - m, y) && !welt.getFeld(x - m, y, besuchtSetzen).isBesucht() && welt.getFeld(x - m, y, besuchtSetzen).getRisiko() < maxRisiko) {
                    if (debug) System.out.println("suche Unbekanntes: " + (x - m) + "," + y);
                    return welt.getFeld(x - m, y, besuchtSetzen);
                }
                if (welt.isInMap(x, y + m) && !welt.getFeld(x, y + m, besuchtSetzen).isBesucht() && welt.getFeld(x, y + m, besuchtSetzen).getRisiko() < maxRisiko) {
                    if (debug) System.out.println("suche Unbekanntes: " + x + "," + (y + m));
                    return welt.getFeld(x, y + m, besuchtSetzen);
                }
                if (welt.isInMap(x, y - m) && !welt.getFeld(x, y - m, besuchtSetzen).isBesucht() && welt.getFeld(x, y - m, besuchtSetzen).getRisiko() < maxRisiko) {
                    if (debug) System.out.println("suche Unbekanntes: " + x + m + "," + (y - m));
                    return welt.getFeld(x, y - m, besuchtSetzen);
                }


                //weitere Felder prüfen
                for (int i = 1; i <= m; i++) {
                    if (welt.isInMap(x + m, y + i) && !welt.getFeld(x + m, y + i, besuchtSetzen).isBesucht() && welt.getFeld(x + m, y + i, besuchtSetzen).getRisiko() < maxRisiko) {
                        return welt.getFeld(x + m, y + i, besuchtSetzen);
                    }
                    if (welt.isInMap(x + m, y - i) && !welt.getFeld(x + m, y - i, besuchtSetzen).isBesucht() && welt.getFeld(x + m, y - i, besuchtSetzen).getRisiko() < maxRisiko) {
                        return welt.getFeld(x + m, y - i, besuchtSetzen);
                    }
                    if (welt.isInMap(x - m, y + i) && !welt.getFeld(x - m, y + i, besuchtSetzen).isBesucht() && welt.getFeld(x - m, y + i, besuchtSetzen).getRisiko() < maxRisiko) {
                        return welt.getFeld(x - m, y + i, besuchtSetzen);
                    }
                    if (welt.isInMap(x - m, y - i) && !welt.getFeld(x - m, y - i, besuchtSetzen).isBesucht() && welt.getFeld(x - m, y - i, besuchtSetzen).getRisiko() < maxRisiko) {
                        return welt.getFeld(x - m, y - i, besuchtSetzen);
                    }
                }
                for (int i = 1; i <= m; i++) {
                    if (welt.isInMap(x + i, y + m) && !welt.getFeld(x + i, y + m, besuchtSetzen).isBesucht() && welt.getFeld(x + i, y + m, besuchtSetzen).getRisiko() < maxRisiko) {
                        return welt.getFeld(x + i, y + m, besuchtSetzen);
                    }
                    if (welt.isInMap(x + i, y - m) && !welt.getFeld(x + i, y - m, besuchtSetzen).isBesucht() && welt.getFeld(x + i, y - m, besuchtSetzen).getRisiko() < maxRisiko) {
                        return welt.getFeld(x + m, y - i, besuchtSetzen);
                    }
                    if (welt.isInMap(x - i, y + m) && !welt.getFeld(x - i, y + m, besuchtSetzen).isBesucht() && welt.getFeld(x - i, y + m, besuchtSetzen).getRisiko() < maxRisiko) {
                        return welt.getFeld(x - m, y + i, besuchtSetzen);
                    }
                    if (welt.isInMap(x - i, y - m) && !welt.getFeld(x - i, y - m, besuchtSetzen).isBesucht() && welt.getFeld(x - i, y - m, besuchtSetzen).getRisiko() < maxRisiko) {
                        return welt.getFeld(x - i, y - m, besuchtSetzen);
                    }
                }
            }

        }

        if (debug)
            System.out.println("Kein unbekanntes Feld mehr vorhanden oder Risiko zu groß! Risiko: " + maxRisiko);
        return new Feld(Feld.Zustand.UNBEKANNT, -1, -1, besuchtSetzen);
    }
    //--------------------------------------------------------------------------------------------------------------------


    //--------------------------------------------------------------------------------------------------------------------
    //BNZWFA
    private void aufrufBestimmeNaechstesZwischenFeld(int start) {
        if (debug)
            System.out.println("BNZWFA:: starte Suche nach neuem Zwischenziel zu aktuellem Hauptziel: " + zielfeld);

        int i;
        for (i = 0; i <= zwischenzielFaelle; i++) {
            bestimmeNaechstesZwischenFeld(i);
            //Bedingungen für Zwischenfelder:
            if (!welt.isInLetzteBesuchteFelder(zwischenfeld, 3) && welt.isInMap(zwischenfeld)) {
                if (!modus.equalsIgnoreCase("umranden")) {
                    if (welt.isEingekesselt(zwischenfeld, 45, besuchtSetzen, anzahlEinkesselndeFelder)) break;
                } else break;
            }


        }

        if (debug && !welt.isInMap(zwischenfeld)){
            System.out.println("BNZWFA:: es konnte kein neues Zwischenziel zum aktuellen Hauptziel bestimmt werden! ");
            nichtErreichbareFelder.add(zielfeld);
        }

        else if (debug)
            System.out.println("BNZWFA:: neues Zwischenziel in Case " + (i - 1) + " gefunden: " + zwischenfeld);
    }

    //BNZWF
    private Feld bestimmeNaechstesZwischenFeld(int punkt) {
        //bestimmt mit Hilfe von A* das nächste Zwischenfeld zum aktuellen Zielfeld

        if (debug) System.out.println("BNZWF:: Case " + punkt);

        //Wenn das Zwischenfeld noch nicht erreicht ist, da er sich z.B. letztes mal nur gedreht hat, verändert sich das Zwischenfeld nicht
        if (!zwischenfeldErreicht) {
            if (debug)
                System.out.println("BNZWF:: altes Zwischenziel bleibt erhalten (Case " + punkt + "): " + zwischenfeld);
            return zwischenfeld;
        }


        //Ansonsten neues Zwischenfeld auf dem Weg zum Zielfeld bestimmen:

        Feld aktHunterPos = welt.getFeld(welt.getHunterPos()[0], welt.getHunterPos()[1], besuchtSetzen);
        ASternSuche aStern = new ASternSuche(aktHunterPos, zielfeld, welt);

        zwischenfeld = welt.getFeld(-1, -1, besuchtSetzen);

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

        if (welt.isInMap(zwischenfeld)) setZwischenfeldNichtErreicht();
        return zwischenfeld;
    }

    private HunterAction bestimmeWegZumZwischenFeld() {
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
    //--------------------------------------------------------------------------------------------------------------------


    //--------------------------------------------------------------------------------------------------------------------
    private HunterAction wumpusToeten() {
        //bereits bekannt: ich bin an mindestens 2 Stellen von Gestank oder ähnlichem umkesselt und der Wumpus lebt noch

        if (debug) System.out.println("!!!!!!!!!!!!!!!!!!! WUMPUS TOETEN !!!!!!!!!!!!!!!!!!!");
        Feld feldInBlickrichtung = welt.getFeldInBlickrichtung(besuchtSetzen);

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
    //--------------------------------------------------------------------------------------------------------------------


    //--------------------------------------------------------------------------------------------------------------------
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
        zielfeld = welt.getFeld(-1, -1, besuchtSetzen);
        setZwischenfeldErreicht();
    }

    public void setZielfeldNichtErreicht() {
        zielfeldErreicht = false;
        setZwischenfeldErreicht();
    }

    public void setZwischenfeldErreicht() {
        zwischenfeldErreicht = true;
        zwischenfeld = welt.getFeld(-1, -1, besuchtSetzen);
    }

    public void setZwischenfeldNichtErreicht() {
        zwischenfeldErreicht = false;
    }

    public boolean isBesuchtSetzen() {
        return besuchtSetzen;
    }
    //--------------------------------------------------------------------------------------------------------------------

}
