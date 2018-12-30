package de.fh;

import java.util.ArrayList;
import java.util.HashSet;

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

    //Wenn erfolgreich, wird der Nachfolger vom Start zurück gegeben
    public Feld suche() {
        System.out.println("start Feld: " + start);
        System.out.println("ziel Feld: " + ziel);
        if (start != ziel) {
            this.fuegeKnotenEin(new Knoten(start));

            //Solange noch Expansionskandidaten vorhanden (Mindestens die Wurzel)
            while (!openList.isEmpty()) {

                //Es wird *immer* der erste Knoten aus der Openlist entnommen
                //Die Sortierung der Openlist bestimmt die Suche
                Knoten expansionsKandidat = this.openList.remove(0);
                System.out.println("herausgenommenes Feld:" + expansionsKandidat.getFeld());
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
        return new Feld(Feld.Zustand.UNBEKANNT, -1, -1);
    }

    public void bewerteKnoten(Knoten expansionsKandidat) {

        int schaetzwert, pfadkosten = 10; //je höher die Pfadkosten gesetzt werden, desto unwichtiger der Schätzwert

        //möglich geringe Entfernung zum Ziel und geringes Risiko
        schaetzwert = expansionsKandidat.getFeld().getRisiko() + 4 * berechneEntfernung(expansionsKandidat); //je höher der Faktor des unwichtiger das Risiko
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
        berechneNachfolger(welt.getFeld(f.getFeld().getPosition()[0] + 1, f.getFeld().getPosition()[1]), f);
        berechneNachfolger(welt.getFeld(f.getFeld().getPosition()[0] - 1, f.getFeld().getPosition()[1]), f);
        berechneNachfolger(welt.getFeld(f.getFeld().getPosition()[0], f.getFeld().getPosition()[1] + 1), f);
        berechneNachfolger(welt.getFeld(f.getFeld().getPosition()[0], f.getFeld().getPosition()[1] - 1), f);
    }

    private void berechneNachfolger(Feld neuesFeld, Knoten vorgaenger) {
        //Wenn Risiko des Feldes zu hoch oder nicht in Map: abbrechen
        if (neuesFeld.getRisiko() > 69)// || !welt.isInMap(neuesFeld.getPosition()[0],neuesFeld.getPosition()[1]))
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

    private int berechneEntfernung(Knoten aktKnoten) {
        return Math.abs(aktKnoten.getFeld().getPosition()[0] - ziel.getPosition()[0]) + Math.abs(aktKnoten.getFeld().getPosition()[1] - ziel.getPosition()[1]);
    }
}