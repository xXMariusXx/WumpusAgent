package de.fh;

import java.util.HashSet;
import java.util.LinkedList;

import de.fh.Feld.Zustand;
import de.fh.wumpus.enums.HunterAction;


import static de.fh.Feld.Zustand.*;

public class Welt {

    private Feld[][] map; //1. Wert = Y (Zeile), 2. Wert = X (Spalte)


    //ToDo - Punktestand
    //ToDo: Map nicht 2 unnötige Spalten breit erweitern


    // ---- Hunter Stats ----
    private int[] hunterPos = new int[2]; //[0] = X-Wert, [1] = Y-Wert
    private LinkedList<Wumpus> wumpiList;
    private Himmelsrichtung blickrichtung;
    private int anzahlPfeile = 100;
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
        wumpiList = new LinkedList<>();

        map[0][0] = new Feld(WALL, 0, 0);
        map[0][1] = new Feld(WALL, 1, 0);
        map[1][0] = new Feld(WALL, 0, 1);
        map[1][1] = new Feld(HUNTER, 1, 1);
        hunterPos[0] = 1;
        hunterPos[1] = 1;
        blickrichtung = Himmelsrichtung.EAST;
    }


    // ----Welt Zeugs ----

    public void addWumpus(int id){
        boolean vorhanden = false;
        for (Wumpus w: wumpiList) if (w.getId() == id) vorhanden = true;
        if(!vorhanden) wumpiList.add(new Wumpus(id));
    }

    public void addZustand(int x, int y, Zustand z) {
        if (x < 0 || y < 0 || ((x >= map[0].length || y >= map.length) && isUmrandet())) return;

        //Map automatisch vergrößern, wenn Feld noch nicht vorhanden
        if (x >= map[0].length || y >= map.length) {
            umrandet = false;
            Feld tmp[][];

            // Spalte + Zeile hinzufügen
            if (x >= map[0].length && y >= map.length) {
                System.out.println("Feld um X und Y erweitern:" + x +","+y );
                tmp = new Feld[y + 1][x + 1];
            }
            // Spalte hinzufügen
            else if (x >= map[0].length) {
                System.out.println("Feld um X erweitern:" + x +","+y );
                tmp = new Feld[map.length][x + 1];
            }
            //Zeile hinzufügen
            else {
                System.out.println("Feld um Y erweitern:" + x +","+y );
                tmp = new Feld[y + 1][map[0].length];
            }

            //automatisch mit-angelegte Felder erzeugen
            for (int a = 0; a < tmp.length; a++) {
                for (int b = 0; b < tmp[0].length; b++) {
                    tmp[a][b] = new Feld(UNBEKANNT, b, a);
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

        //Wenn Hunter sich bewegt hat nach Möglichkeit weitere Wände ergänzen
        if (z == HUNTER) {
            this.wandErgaenzen();
        }
        checkWumpusWandGefahr(x, y, z);


    }

    public void removeZustand(int x, int y, Zustand z) {
        if (x < map[0].length && y < map.length && x >= 0 && y >= 0) {
            map[y][x].removeZustand(z);
        }

    }

    public int wandGrenzeX() {
        int maxX = 0;
        //X Wert bis wohin die Map aus Wand besteht

        for (int i = 0; i < map[0].length; i++) {
            if (getFeld(i, 0).getZustaende().contains(WALL)) {
                maxX++;
            }
        }
        //-1 damit es mit den Index Werten übereinstimmt
        return maxX - 1;
    }

    public int wandGrenzeY() {
        int maxY = 0;
        //Y Wert bis wohin die Map aus Wand besteht
        for (int i = 0; i < map.length; i++) {
            if (getFeld(0, i).getZustaende().contains(WALL)) {
                maxY++;
            }
        }
        //-1 damit es mit den Index Werten übereinstimmt
        return maxY - 1;
    }

    public void wandErgaenzen() {
        System.out.println("Wand ergänzen aufgerufen!");
        if (hunterPos[0] == 1 && getFeld(hunterPos[0],hunterPos[1]+1).getZustaende().contains(WALL)) {
            System.out.println("Eckenwand UL wird hinzugefügt");
            addZustand(hunterPos[0]-1,hunterPos[1]+1,WALL);
        }

        if (hunterPos[1] == 1 && getFeld(hunterPos[0]+1,hunterPos[1]).getZustaende().contains(WALL)) {
            System.out.println("Eckenwand OR wird hinzugefügt");
            addZustand(hunterPos[0]+1,hunterPos[1]-1,WALL);
        }

        if (!getFeld(hunterPos[0],0).getZustaende().contains(WALL)) addZustand(hunterPos[0],0,WALL);

        if (!getFeld(0,hunterPos[1]).getZustaende().contains(WALL)) addZustand(0,hunterPos[1],WALL);

        if (getFeld(hunterPos[0]+1,getHunterPos()[1]).getZustaende().contains(WALL)) addZustand(hunterPos[0]+1,0,WALL);

    }

    public void umrande(){
        int maxX = wandGrenzeX();
        int maxY = wandGrenzeY();

        //Wenn die Ecke unten rechts aus einer Wand besteht, vollstädigen Wand Rahmen um die Map erzeugen
        //if (maxX > 5 && maxX > 5 && (maxY > 5 && map[maxY - 1][maxX].getZustaende().contains(WALL) || map[maxY - 2][maxX].getZustaende().contains(WALL))
             //   && (map[maxY][maxX - 1].getZustaende().contains(WALL) || map[maxY][maxX - 2].getZustaende().contains(WALL))) {
            //Zeile auf Höhe maxY vervollständigen
            for (int i = 0; i <= maxX; i++) {
                addZustand(i, maxY, WALL);
            }
            //Spalte maxX vervollständigen
            for (int i = 0; i <= maxY; i++) {
                addZustand(maxX, i, WALL);
            }
        //}

        //map reduzieren
        Feld tmp[][] = new Feld[maxX+1][maxY+1];

        for (int i = 0; i < tmp.length; i++) {
            for (int j = 0; j < tmp[0].length; j++) {
                tmp[i][j] = map[i][j];
            }
        }
        map = tmp;

    }

    public boolean isUmrandet() {

        int maxX = wandGrenzeX();
        int maxY = wandGrenzeY();

        if (maxX < 3 || maxY < 3) {
            umrandet = false;
            return umrandet;
        }

        for (int i = 0; i <= maxX; i++) {
            if (!getFeld(i, maxY).getZustaende().contains(WALL)) {
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

    public Feld getFeld(int[] pos) {
        return getFeld(pos[0], pos[1]);
    }

    public Feld getFeld(int x, int y) {
        if (x >= 0 && y >= 0 && x < map[0].length && y < map.length) return map[y][x];
        //map vergrößern solange sie noch nicht umrandet ist, falls das geforderte Feld nicht innerhalb der Map liegt
        if ((x >= map[0].length || y >= map.length) && x >= 0 && y >= 0 && !isUmrandet()) {
            addZustand(x, y, UNBEKANNT);
            return getFeld(x, y);
        }
        return new Feld(UNBEKANNT, -55, -55);

    }

    public boolean isInMap(Feld f) {
        return isInMap(f.getPosition()[0], f.getPosition()[1]);
    }

    public boolean isInMap(int x, int y) {
        //System.out.println("isInMap von :" + x +","+y + "="+ (x < map[0].length && y < map.length && x >= 0 && y >= 0));
        return (x < map[0].length && y < map.length && x >= 0 && y >= 0);
    }

    public int[] getMapSize() {
        int[] size = new int[2];
        size[0] = map[0].length;
        size[1] = map.length;
        return size;
    }

    public void addPunkte(int i) {
        punkte += i;
    }

    public void removePunkte(int i) {
        punkte -= i;
    }

    public boolean isEingekesselt(int x, int y, int maxRisiko){
        return isEingekesselt(getFeld(x,y),maxRisiko);
    }

    public boolean isEingekesselt(Feld f, int maxRisiko){
        int i = 0;
        int x = f.getPosition()[0];
        int y = f.getPosition()[1];

        if (isInMap(x+1, y) && getFeld(x+1, y).getRisiko() > maxRisiko){
            i++;
            //System.out.println("Eingekessel von: " + (hunterPos[0]+1) + "," + hunterPos[1]);
        }

        if (isInMap(x-1, y) && getFeld(x-1, y).getRisiko() > maxRisiko) {
            i++;
            //System.out.println("Eingekessel von: " + (hunterPos[0]-1) + "," + hunterPos[1]);
        }

        if (isInMap(x, y+1) && getFeld(x, y+1).getRisiko() > maxRisiko) {
            i++;
           // System.out.println("Eingekessel von: " + (hunterPos[0]) + "," + (hunterPos[1]+1));
        }

        if (isInMap(x, y-1) && getFeld(x, y-1).getRisiko() > maxRisiko){
            i++;
            //System.out.println("Eingekessel von: " + (hunterPos[0]) + "," + (hunterPos[1]-1));
        }

        if (i > 2) return true;

        return false;
    }

    public boolean isVonGestankEingekesselt(){
        int i = 0;
        if (isInMap(hunterPos[0]+1, hunterPos[1]) && (getFeld(hunterPos[0]+1, hunterPos[1]).getZustaende().contains(GESTANK1) || getFeld(hunterPos[0]+1, hunterPos[1]).getZustaende().contains(EVTWUMPUS))) i++;
        if (isInMap(hunterPos[0]-1, hunterPos[1]) && (getFeld(hunterPos[0]-1, hunterPos[1]).getZustaende().contains(GESTANK1) || getFeld(hunterPos[0]-1, hunterPos[1]).getZustaende().contains(EVTWUMPUS))) i++;
        if (isInMap(hunterPos[0], hunterPos[1]+1) && (getFeld(hunterPos[0], hunterPos[1]+1).getZustaende().contains(GESTANK1) || getFeld(hunterPos[0], hunterPos[1]+1).getZustaende().contains(EVTWUMPUS))) i++;
        if (isInMap(hunterPos[0], hunterPos[1]-1) && (getFeld(hunterPos[0], hunterPos[1]-1).getZustaende().contains(GESTANK1) || getFeld(hunterPos[0], hunterPos[1]-1).getZustaende().contains(EVTWUMPUS))) i++;
        if (i > 1) return true;
        return false;
    }

    public boolean isInBlickrichtung(Feld f){
        switch (blickrichtung) {
            case EAST:
                if (getFeld(hunterPos[0] + 1, hunterPos[1]) == f) return true;
                return false;
            case SOUTH:
                if (getFeld(hunterPos[0], hunterPos[1] + 1) == f) return true;
                return false;
            case WEST:
                if (getFeld(hunterPos[0] - 1, hunterPos[1]) == f) return true;
                return false;
            case NORTH:
                if (getFeld(hunterPos[0], hunterPos[1] - 1) == f) return true;
                return false;
            default:
                return false;
        }
    }

    public Feld getFeldInBlickrichtung(){
        switch (blickrichtung) {
            case EAST:
                return getFeld(hunterPos[0] + 1, hunterPos[1]);
            case SOUTH:
                return getFeld(hunterPos[0], hunterPos[1] + 1);
            case WEST:
                return getFeld(hunterPos[0] - 1, hunterPos[1]);
            case NORTH:
                return getFeld(hunterPos[0], hunterPos[1] - 1);
        }
        System.err.println("Fehler beim Feldzugriff!");
        return null;
    }

    public void setAnzahlPfeile(int anzahlPfeile) {
        this.anzahlPfeile = anzahlPfeile;
    }

    public int getPunkte() {
        return punkte;
    }

    // ---- Aktion Zeugs ----
    public HunterAction getLastAction() {
        //System.out.println(lastActionList);
        return lastActionList.getLast();
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


    // ---- Hunter/Stats Zeugs ----
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

    public Feld getHunterFeld() {
        return getFeld(hunterPos[0], hunterPos[1]);
    }

    public Himmelsrichtung getBlickrichtung() {
        return blickrichtung;
    }

    public void setBlickrichtung(Himmelsrichtung b) {
        blickrichtung = b;
    }

    public void setGoldGesammelt() {
        goldAufgesammelt = true;
        removeZustand(getHunterPos()[0], getHunterPos()[1], GOLD);
    }

    public void setWumpusGetoetet() {
        wumpusLebendig = false;
        for (int i = 0; i < map[0].length; i++) {
            for (int j = 0; j < map.length; j++) {
                map[j][i].removeZustand(GESTANK3);
                map[j][i].removeZustand(GESTANK2);
                map[j][i].removeZustand(GESTANK1);
                map[j][i].removeZustand(EVTWUMPUS);
            }
        }

    }

    public boolean isWumpusLebendig() {
        return wumpusLebendig;
    }

    public int getAnzahlPfeile() {
        return anzahlPfeile;
    }

    public void removeWumpusGefahr(){
        Feld f = getFeld(hunterPos[0],hunterPos[1]);
        int i = 0;
        while(i < 3){
            switch (blickrichtung) {
                case EAST:
                    f = getFeld(hunterPos[0] + i, hunterPos[1]);
                    break;
                case SOUTH:
                    f= getFeld(hunterPos[0], hunterPos[1] + i);
                    break;
                case WEST:
                    f= getFeld(hunterPos[0] - i, hunterPos[1]);
                    break;
                case NORTH:
                    f= getFeld(hunterPos[0], hunterPos[1] - i);
                    break;
            }

            f.setBeschossen();
            f.removeZustand(GESTANK3);
            f.removeZustand(GESTANK2);
            f.removeZustand(GESTANK1);

            i++;
        }
    }

    public void pfeilGeschossen() {
        removePunkte(10);
        anzahlPfeile--;
    }

    public boolean isGoldAufgesammelt() {
        return goldAufgesammelt;
    }

    public void checkWumpusWandGefahr(int x, int y, Zustand z) {
        //Wenn Gestank 1 Feld entfernt von einer Wand gesetzt werden soll, riskante Felder markieren
        if (z == GESTANK3 || z == GESTANK2 || z == GESTANK1) {
            if (getFeld(x, y - 2).getZustaende().contains(WALL)) {
                addZustand(x - 1, y - 1, EVTWUMPUS);
                addZustand(x + 1, y - 1, EVTWUMPUS);
            }
            if (getFeld(x, y + 2).getZustaende().contains(WALL)) {
                addZustand(x - 1, y + 1, EVTWUMPUS);
                addZustand(x + 1, y + 1, EVTWUMPUS);
            }
            if (getFeld(x + 2, y).getZustaende().contains(WALL)) {
                addZustand(x + 1, y - 1, EVTWUMPUS);
                addZustand(x + 1, y + 1, EVTWUMPUS);
            }
            if (getFeld(x - 2, y - 2).getZustaende().contains(WALL)) {
                addZustand(x - 1, y - 1, EVTWUMPUS);
                addZustand(x - 1, y + 1, EVTWUMPUS);
            }
        }
    }

    public void berechneWumpusPosition(){

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
                        if (map[i][z].getZustaende().contains(HUNTER)) System.out.print("[-H-] ");
                        else System.out.print("[0" + risiko + "] ");
                        break;

                    default:
                        System.out.print("[" + risiko + "] ");
                }
            }
            System.out.println();
        }
        System.out.println();
    }

    public void displayBesucht() {
        for (int i = 0; i < map.length; i++) {
            for (int z = 0; z < map[0].length; z++) {
                boolean besucht = map[i][z].isBesucht();
                if (besucht) System.out.print("[T] ");
                else System.out.print("[F] ");
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
