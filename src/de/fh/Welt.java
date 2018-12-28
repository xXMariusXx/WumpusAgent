package de.fh;

import java.util.HashSet;
import java.util.LinkedList;

import de.fh.Feld.Zustand;
import de.fh.wumpus.enums.HunterAction;


import static de.fh.Feld.Zustand.*;

public class Welt {

    private Feld[][] map;


    // ---- Hunter Stats ----
    private int[] hunterPos = new int[2]; //[0] = X-Wert, [1] = Y-Wert
    private Himmelsrichtung blickrichtung;
    private int anzahlPfeile = 5;
    private boolean goldGefunden = false;
    private boolean wumpusLebendig = true;
    private int punkte = 1000;



    enum Himmelsrichtung{
        NORTH, SOUTH, EAST, WEST;
    }


    // ---- Aktionen ----
    private LinkedList<HunterAction> lastActionList;
    private LinkedList<HunterAction> nextActionList;



    public Welt()
    {
        map = new Feld[2][2]; //map[0].length = Anzahl Spalten (X), map.length = Anzahl Zeilen (Y)

        nextActionList = new LinkedList<HunterAction>();
        lastActionList = new LinkedList<HunterAction>();

        map[0][0] = new Feld(WALL);
        map[0][1] = new Feld(WALL);
        map[1][0] = new Feld(WALL);
        map[1][1] = new Feld(HUNTER);
        hunterPos[0]=1; hunterPos[1]=1;
        blickrichtung = Himmelsrichtung.EAST;
    }




    // ----Zustands Zeugs ----
    public HashSet<Feld.Zustand> getZustaende(int x, int y) {
        return map[y][x].getZustaende();
    }

    public void addZustand(int x, int y, Zustand z){
        if (x <= 0 || y <= 0) return;
        //Map automatisch vergrößern, wenn Feld noch nicht vorhanden
        if (x >= map[0].length || y >= map.length){

            Feld tmp[][];
            // Spalte hinzufügen
            if (x >= map[0].length){
                tmp = new Feld[map.length][x+1];
            }
            //Zeile hinzufügen
            else{
                tmp = new Feld[y+1][map[0].length];
            }

            //automatisch mit-angelegte Felder erzeugen
            for (int a = 0; a < tmp.length; a++ ) {
                for (int b = 0; b < tmp[0].length; b++) {
                    tmp[a][b] = new Feld(new HashSet<Zustand>());
                }
            }
            //neuen Zustand speichern
            tmp[y][x] = new Feld(z);
            //Map kopieren
            for(int i = 0; i<map.length;i++){
                for (int j = 0; j<map[0].length;j++){
                    tmp[i][j] = map[i][j];
                }
            }

            map = tmp;
        }
        else{
            map[y][x].addZustand(z);
        }
    }

    public void removeZustand(int x, int y, Zustand z){
        map[y][x].removeZustand(z);
    }



    // ---- Aktion Zeugs ----
    public HunterAction getLastAction(){
        //System.out.println(lastActionList);
        return lastActionList.removeLast();
    }

    public void addNextAction(HunterAction a) {
        nextActionList.add(a);
    }

    public HunterAction getNextAction(){
        HunterAction action = nextActionList.getFirst();
        if (action == HunterAction.SHOOT) this.pfeilGeschossen();
        return action;
    }

    public void aktuelleAktionAusgefuehrt() {
        lastActionList.add(nextActionList.removeFirst());
    }

    public void removeLastAction(){
        lastActionList.removeLast();
    }




    // ---- Hunter Zeugs ----

    public void updateHunterPos(int x, int y) {
        //Hunter an alter Position aus Map löschen
        removeZustand(hunterPos[0],hunterPos[1], HUNTER);
        //Hunter an neuer Position in Map setzen
        addZustand(x,y,HUNTER);
        hunterPos[0]=x; hunterPos[1]=y;
    }

    public int[] getHunterPos(){
        return hunterPos;
    }

    public Himmelsrichtung getBlickrichtung(){
        return blickrichtung;
    }

    public void setBlickrichtung(Himmelsrichtung b) {
        blickrichtung = b;
    }

    public void setGoldGefunden(){
        goldGefunden = true;
        addPunkte(100);
    }

    public void setWumpusGetoetet(){
        wumpusLebendig = false;
        addPunkte(100);
    }

    public void addPunkte(int i){
        punkte += i;
    }

    public void removePunkte(int i){
        punkte -= i;
    }

    public void pfeilGeschossen() {
        removePunkte(10);
        anzahlPfeile--;
    }





    // ---- Debug Zeugs ----

    public void displayRisiko()
    {
        for (int i = 0; i < map.length; i++)
        {
            for(int z = 0; z<map[0].length;z++)
            {
                int risiko = map[i][z].getRisiko();
                switch (Integer.toString(risiko).length())
                {
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

    public void displayWelt()
    {
        for (int i = 0; i < map.length; i++)
        {
            for(int z = 0; z<map[i].length;z++)
            {
                System.out.print("[");
                HashSet<Zustand> feld = map[i][z].getZustaende();
                for(Zustand zustand: feld)
                {
                    System.out.print(zustand + " ");
                }
                System.out.print("]");
            }
            System.out.println();
        }
    }
}