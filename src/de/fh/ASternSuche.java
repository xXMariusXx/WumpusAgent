package de.fh;

import java.util.ArrayList;
import java.util.HashSet;

public class ASternSuche {
    private Feld start;
    private Feld ziel;
    private Welt welt;
    private int risiko;
    private int faktorEntfernung;
    private boolean debug = false;

    private ArrayList<Knoten> openList;
    private HashSet<Integer> closeList;

    public ASternSuche(Feld start, Feld ziel, Welt welt) {
        this.start = start;
        this.ziel = ziel;
        this.welt = welt;
        if (true) System.out.println("A*Startfeld:    " + start);
        if (true) System.out.println("A*Zielfeld:     " + ziel);

    }

    //Wenn erfolgreich, wird der Nachfolger vom Start zurück gegeben
    public Feld suche(int risiko, int faktorEntfernung) {
        this.risiko = risiko;
        this.faktorEntfernung = faktorEntfernung;

        openList = new ArrayList<>();
        closeList = new HashSet<>();

        if (debug) System.out.println("A* mit: " + risiko + ", " + faktorEntfernung);
        //Such-Algorithmus
        if (start != ziel) {
            this.fuegeKnotenEin(new Knoten(start));

            //Solange noch Expansionskandidaten vorhanden (Mindestens die Wurzel)
            while (!openList.isEmpty()) {
                if (debug) System.out.println("---openList---");
                if(debug) {
                    for(int i = 0; i<openList.size() && i<5;i++){
                        System.out.println(openList.get(i).getFeld() + " Bewertung: " + openList.get(i).getBewertung());
                    }

                }
                if (debug) System.out.println("---ENDE openList---");
                //Es wird *immer* der erste Knoten aus der Openlist entnommen
                //Die Sortierung der Openlist bestimmt die Suche
                Knoten expansionsKandidat = this.openList.remove(0);
                //System.out.println("herausgenommenes Feld:" + expansionsKandidat.getFeld());
                //Wird ein Knoten aus der Openlist entfernt landet dieser sofort in der Closelist, damit dieser nicht noch einmal expandiert wird
                this.closeList.add(expansionsKandidat.hashCode());

                //Schaue ob Knoten Ziel ist

                if (expansionsKandidat.isZiel(ziel)) {
                    //Kandidat entspricht dem geünschten Zielzustand
                    Knoten loesungsKnoten = expansionsKandidat;
                    while (loesungsKnoten.getVorgaenger().getFeld() != start) {
                        loesungsKnoten = loesungsKnoten.getVorgaenger();
                    }
                    return loesungsKnoten.getFeld();

                } else {
                    //Ist nicht gleich dem Zielzustand, also expandiere nächsten Knoten
                    expandiereKnoten(expansionsKandidat);

                }
            }
        }

        //Keine Lösung gefunden
        return new Feld(Feld.Zustand.UNBEKANNT, -1, -1,true);
    }

    public void bewerteKnoten(Knoten expansionsKandidat) {

        int schaetzwert, pfadkosten = 15; //je höher die Pfadkosten gesetzt werden, desto unwichtiger der Schätzwert

        //möglich geringe Entfernung zum Ziel und geringes Risiko
        schaetzwert = expansionsKandidat.getFeld().getRisiko() + faktorEntfernung * berechneEntfernung(expansionsKandidat); //je höher der Faktor desto unwichtiger das Risiko
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
        openList.add(pos, expansionsKandidat);

    }

    private void expandiereKnoten(Knoten f) {
            if (welt.isInMap(f.getFeld().getPosition()[0] + 1, f.getFeld().getPosition()[1]))
                berechneNachfolger(welt.getFeld(f.getFeld().getPosition()[0] + 1, f.getFeld().getPosition()[1],true), f);
            if (welt.isInMap(f.getFeld().getPosition()[0] - 1, f.getFeld().getPosition()[1]))
                berechneNachfolger(welt.getFeld(f.getFeld().getPosition()[0] - 1, f.getFeld().getPosition()[1],true), f);
            if (welt.isInMap(f.getFeld().getPosition()[0], f.getFeld().getPosition()[1] + 1))
                berechneNachfolger(welt.getFeld(f.getFeld().getPosition()[0], f.getFeld().getPosition()[1] + 1,true), f);
            if (welt.isInMap(f.getFeld().getPosition()[0], f.getFeld().getPosition()[1] - 1))
                berechneNachfolger(welt.getFeld(f.getFeld().getPosition()[0], f.getFeld().getPosition()[1] - 1,true), f);
    }

    private void berechneNachfolger(Feld neuesFeld, Knoten vorgaenger) {
        //Wenn Risiko des Feldes zu hoch: abbrechen
        if (neuesFeld.getRisiko() > risiko || !welt.isInMap(neuesFeld.getPosition()[0], neuesFeld.getPosition()[1]))
            return;

        if(berechneEntfernung(new Knoten(neuesFeld)) > berechneEntfernung(new Knoten(start))+8) return;


        //Erzeuge Nachfolgerknoten
        Knoten nachfolger = new Knoten(neuesFeld, vorgaenger);

        //Wenn Feld bereits in closeList, also schon getestet: nicht hinzufügen
        if (closeList.contains(nachfolger.hashCode())){
            //System.out.println("Knoten wurde nicht hinzugefügt da bereits in Closelist");
            return;
        }

        for (Knoten k: openList) {
            if(k.hashCode() == nachfolger.hashCode()) return;
        }


        //Knoten bewerten
        this.bewerteKnoten(nachfolger);
        //Es ist ein gültiger Nachfolgezustand, also in die Openlist
        this.fuegeKnotenEin(nachfolger);
    }

    private int berechneEntfernung(Knoten aktKnoten) {
        return Math.abs(aktKnoten.getFeld().getPosition()[0] - ziel.getPosition()[0]) + Math.abs(aktKnoten.getFeld().getPosition()[1] - ziel.getPosition()[1]);
    }
}