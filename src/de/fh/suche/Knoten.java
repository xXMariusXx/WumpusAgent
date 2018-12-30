package de.fh.suche;

import de.fh.Feld;

public class Knoten {

    private Feld feld;
    protected Knoten vorgaenger;
    protected int pfadkosten = 0; //zurückgelegte Strecke bis zu diesem Feld
    protected int schaetzwert = 0; //restliche Entfernung zum Ziel + Risiko
    private int[] startPos;



    public Knoten(Feld feld, Knoten vorgaenger){
        this.vorgaenger = vorgaenger;
        this.feld = feld;
    }

    //Konstruktor für Startfeld
    public Knoten(Feld feld){
        this.vorgaenger = null;
        this.feld = feld;
    }

    public Feld getFeld() {
        return feld;
    }

    public int getPfadkosten() {
        return pfadkosten;
    }

    public void setPfadkosten(int pfadkosten) {
        this.pfadkosten = pfadkosten;
    }

    public Knoten getVorgaenger() {
        return vorgaenger;
    }

    public void setSchaetzwert(int schaetzwert){
        this.schaetzwert = schaetzwert;
    }

    public int getSchaetzwert(){
        return this.schaetzwert;
    }

    public int getBewertung(){
        return pfadkosten + schaetzwert;
    }



    /**
     * Ist der Konten der Zielzustand
     *
     * @param knoten
     * @return
     */
    public boolean isZiel(Knoten knoten) {
       return this.feld == knoten.feld;
    }


}
