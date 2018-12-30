package de.fh.suche;

import de.fh.Feld;
import de.fh.Welt;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;

public class ASternSuche {
    private Feld start;
    private Feld ziel;
    private Welt welt;

    private ArrayList<Knoten> openList;
    private HashSet<Integer> closeList;

    public ASternSuche(Feld start, Feld ziel, Welt welt) {
        this.start = start;
        this.ziel = ziel;
        this.welt = welt;

        openList = new ArrayList<>();
        closeList = new HashSet<>();
    }

    //Wenn erfolgreich, wird
    public Feld suche() {
        return null;
    }

    public void bewerteKnoten(Knoten expansionsKandidat) {

        int schaetzwert, pfadkosten = 15;

        //möglich geringe Entfernung zum Ziel und geringes Risiko
        schaetzwert = expansionsKandidat.getFeld().getRisiko() + berechneEntfernung(expansionsKandidat);
        expansionsKandidat.setSchaetzwert(schaetzwert);

        //setzt die bisherigen Pfadkosten zu dem Knoten
        pfadkosten = pfadkosten + expansionsKandidat.getVorgaenger().getPfadkosten();
        expansionsKandidat.setPfadkosten(pfadkosten);
    }

    private void fuegeKnotenEin(Knoten expansionsKandidat) {
        int pos = 0;
        for (Knoten k : openList) {
            if (k.getBewertung() >= expansionsKandidat.getBewertung()) {
                break;
            }
            pos++;
        }

        //Implementiert openList.add(Index,exp) mit dem richtigen Index gemäß Suchstrategie
        openList.add(pos, expansionsKandidat);

    }

    private void expandiereKnoten(Knoten f) {
        berechneNachfolger(welt.getFeld(f.getFeld().getPosition()[0] + 1, f.getFeld().getPosition()[1]), f);
        berechneNachfolger(welt.getFeld(f.getFeld().getPosition()[0] - 1, f.getFeld().getPosition()[1]), f);
        berechneNachfolger(welt.getFeld(f.getFeld().getPosition()[0], f.getFeld().getPosition()[1] + 1), f);
        berechneNachfolger(welt.getFeld(f.getFeld().getPosition()[0], f.getFeld().getPosition()[1] - 1), f);
    }

    private void berechneNachfolger(Feld neuesFeld, Knoten vorgaenger) {
        //Wenn Risiko des Feldes zu hoch: abbrechen
        if (neuesFeld.getRisiko() > 69)
            return;

        //Erzeuge Nachfolgerknoten
        Knoten nachfolger = new Knoten(neuesFeld, vorgaenger);

        //Wenn Feld bereits in closeList, also schon getestet: nicht hinzufügen
        if (closeList.contains(nachfolger.hashCode()))
            return;

        //Knoten bewerten
        this.bewerteKnoten(nachfolger);
        //Es ist ein gültiger Nachfolgezustand, also in die Openlist
        this.fuegeKnotenEin(nachfolger);
    }

    private int berechneEntfernung(Knoten aktKnoten){
        return Math.abs(aktKnoten.getFeld().getPosition()[0]-ziel.getPosition()[0]) + Math.abs(aktKnoten.getFeld().getPosition()[1]-ziel.getPosition()[1]);
    }
}