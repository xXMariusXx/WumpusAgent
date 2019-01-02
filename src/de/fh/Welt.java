package de.fh;

import java.util.HashSet;
import java.util.LinkedList;

import de.fh.Feld.Zustand;
import de.fh.wumpus.enums.HunterAction;


import static de.fh.Feld.Zustand.*;

public class Welt {

    private Feld[][] map; //1. Wert = Y (Zeile), 2. Wert = X (Spalte)


    //TODO -Außengrenzen festlegen für möglichen Wumpus
    //TODO -automatische Randmauern

    // ---- Hunter Stats ----
    private int[] hunterPos = new int[2]; //[0] = X-Wert, [1] = Y-Wert
    private Himmelsrichtung blickrichtung;
    private int anzahlPfeile = 5;
    private boolean goldAufgesammelt = false;
    private boolean wumpusLebendig = true;
    private int punkte = 1000;
    private boolean umrandet = false;


    enum Himmelsrichtung {
        NORTH, SOUTH, EAST, WEST;
    }


    // ---- Aktionen ----
    private LinkedList<HunterAction> lastActionList;
    private LinkedList<HunterAction> nextActionList;


    public Welt() {
        map = new Feld[2][2]; //map[0].length = Anzahl Spalten (X), map.length = Anzahl Zeilen (Y)

        nextActionList = new LinkedList<HunterAction>();
        lastActionList = new LinkedList<HunterAction>();

        map[0][0] = new Feld(WALL, 0, 0);
        map[0][1] = new Feld(WALL, 1, 0);
        map[1][0] = new Feld(WALL, 0, 1);
        map[1][1] = new Feld(HUNTER, 1, 1);
        hunterPos[0] = 1;
        hunterPos[1] = 1;
        blickrichtung = Himmelsrichtung.EAST;
    }


    // ----Zustands Zeugs ----
    public HashSet<Feld.Zustand> getZustaende(int x, int y) {
        return map[y][x].getZustaende();
    }

    public void addZustand(int x, int y, Zustand z) {
        if (x <= 0 || y <= 0) return;
        //Map automatisch vergrößern, wenn Feld noch nicht vorhanden
        if (x >= map[0].length || y >= map.length) {
            umrandet = false;
            Feld tmp[][];
            // Spalte hinzufügen
            if (x >= map[0].length) {
                tmp = new Feld[map.length][x + 1];
            }
            //Zeile hinzufügen
            else {
                tmp = new Feld[y + 1][map[0].length];
            }

            //automatisch mit-angelegte Felder erzeugen
            for (int a = 0; a < tmp.length; a++) {
                for (int b = 0; b < tmp[0].length; b++) {
                    tmp[a][b] = new Feld(new HashSet<Zustand>(), b, a);
                }
            }
            //neuen Zustand speichern
            tmp[y][x] = new Feld(z, x, y);
            //Map kopieren
            for (int i = 0; i < map.length; i++) {
                for (int j = 0; j < map[0].length; j++) {
                    tmp[i][j] = map[i][j];
                }
            }

            map = tmp;

        } else {
            map[y][x].addZustand(z);
        }

        if (getFeld(x,y).isBesucht()){
            this.wandErgaenzen();
        }


    }

    public void removeZustand(int x, int y, Zustand z) {
        if (x < map[0].length && y < map.length && x >= 0 && y >= 0) {
            map[y][x].removeZustand(z);
        }

    }

    private int wandGrenzeX(){
        int maxX = 0;
        //X Wert bis wohin die Map aus Wand besteht

        for (int i = 0; i < map[0].length; i++) {
            if (getFeld(i, 0).getZustaende().contains(WALL)){
                maxX++;
                return maxX;
            }
        }
        return maxX;
    }

    private int wandGrenzeY(){
        int maxY = 0;
        //Y Wert bis wohin die Map aus Wand besteht
        for (int i = 0; i < map.length; i++) {
            if (getFeld(0, i).getZustaende().contains(WALL)){
                maxY++;
                return maxY;
            }
        }
        return maxY;
    }

    public void wandErgaenzen() {
            System.out.println("Wand wird ergänzt!");
             int maxX = wandGrenzeX();
             int maxY = wandGrenzeY();
            //fehlende Umrandung auf X und Y Achse ergänzen, wenn Map vergrößert wird.
            map[0][maxX+1].addZustand(WALL);
            map[maxY+1][0].addZustand(WALL);


            //Wenn die Ecke unten rechts aus einer Wand besteht, vollstädigen Wand Rahmen um die Map erzeugen
            if ((map[maxY - 1][maxX].getZustaende().contains(WALL) || map[maxY - 2][maxX].getZustaende().contains(WALL))
                    && (map[maxY][maxX - 1].getZustaende().contains(WALL) || map[maxY][maxX - 2].getZustaende().contains(WALL))) {
                //Zeile auf Höhe maxY vervollständigen
                for (int i = 0; i <= maxX; i++) {
                    addZustand(i, maxY, WALL);
                }
                //Spalte maxX vervollständigen
                for (int i = 0; i <= maxY; i++) {
                    addZustand(maxX, i, WALL);
                }
            }
    }

    public boolean isUmrandet() {
        int maxX = wandGrenzeX();
        int maxY = wandGrenzeY();
        System.out.println("maxX und maxY aus isUmrandet: " + maxX + "," + maxY);

        for (int i = 0; i <= maxX; i++) {
            if (!getFeld(i, maxY).getZustaende().contains(WALL)){
                umrandet = false;
                return umrandet;
            }
        }

        for (int i = 0; i <= maxY; i++) {
            if (!getFeld(maxX, i).getZustaende().contains(WALL)) {
                umrandet = false;
                return umrandet;
            }
        }

        umrandet = true;
        return umrandet;
    }

    public Feld getFeld(int x, int y) {
        if (x >= 0 && y >= 0 && x < map[0].length && y < map.length) return map[y][x];
        //ToDo: map nicht vergrößern wenn x,y außerhalb der Außenwände
        if ((x >= map[0].length || y >= map.length) && x >= 0 && y >= 0) {
            addZustand(x, y, UNBEKANNT);
            return map[y][x];
        }
        return new Feld(UNBEKANNT, -55, -55);

    }

    public boolean isInMap(int x, int y) {
        return (x < map[0].length && y < map.length && x >= 0 && y >= 0);
    }

    public int[] mapSize() {
        int[] size = new int[2];
        size[0] = map[0].length;
        size[1] = map.length;
        return size;
    }


    // ---- Aktion Zeugs ----
    public HunterAction getLastAction() {
        //System.out.println(lastActionList);
        return lastActionList.removeLast();
    }

    public void addNextAction(HunterAction a) {
        nextActionList.add(a);
    }

    public HunterAction getNextAction() {
        HunterAction action = nextActionList.getFirst();
        if (action == HunterAction.SHOOT) this.pfeilGeschossen();
        return action;
    }

    public void aktuelleAktionAusgefuehrt() {
        lastActionList.add(nextActionList.removeFirst());
    }

    public void removeLastAction() {
        lastActionList.removeLast();
    }


    // ---- Hunter Zeugs ----
    public void updateHunterPos(int x, int y) {
        //Hunter an alter Position aus Map löschen
        removeZustand(hunterPos[0], hunterPos[1], HUNTER);
        //Hunter an neuer Position in Map setzen
        addZustand(x, y, HUNTER);
        hunterPos[0] = x;
        hunterPos[1] = y;
    }

    public int[] getHunterPos() {
        return hunterPos;
    }

    public Himmelsrichtung getBlickrichtung() {
        return blickrichtung;
    }

    public void setBlickrichtung(Himmelsrichtung b) {
        blickrichtung = b;
    }

    public void setGoldGesammelt() {
        goldAufgesammelt = true;
        addPunkte(100);
    }

    public void setWumpusGetoetet() {
        wumpusLebendig = false;
        addPunkte(100);
    }

    public void addPunkte(int i) {
        punkte += i;
    }

    public void removePunkte(int i) {
        punkte -= i;
    }

    public void pfeilGeschossen() {
        removePunkte(10);
        anzahlPfeile--;
    }

    public boolean isGoldAufgesammelt() {
        return goldAufgesammelt;
    }

    // ---- Debug Zeugs ----

    public void displayRisiko() {
        for (int i = 0; i < map.length; i++) {
            for (int z = 0; z < map[0].length; z++) {
                int risiko = map[i][z].getRisiko();
                switch (Integer.toString(risiko).length()) {
                    case 1:
                        System.out.print("[00" + risiko + "] ");
                        break;
                    case 2:
                        System.out.print("[0" + risiko + "] ");
                        break;

                    default:
                        System.out.print("[" + risiko + "] ");
                }
            }
            System.out.println();
        }
    }

    public void displayWelt() {
        for (int i = 0; i < map.length; i++) {
            for (int z = 0; z < map[i].length; z++) {
                System.out.print("[");
                HashSet<Zustand> feld = map[i][z].getZustaende();
                for (Zustand zustand : feld) {
                    System.out.print(zustand + " ");
                }
                System.out.print("]");
            }
            System.out.println();
        }
    }
}
