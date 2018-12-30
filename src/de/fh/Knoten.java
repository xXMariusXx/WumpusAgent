package de.fh;

public class Knoten {

    private Feld feld;
    private Knoten vorgaenger;
    private int pfadkosten = 0; //zurückgelegte Strecke bis zu diesem Feld
    private int schaetzwert = 0; //restliche Entfernung zum Ziel + Risiko


    //Konstruktor für Nachfolgeknoten
    public Knoten(Feld feld, Knoten vorgaenger) {
        this.vorgaenger = vorgaenger;
        this.feld = feld;
    }

    //Konstruktor für Startfeld
    public Knoten(Feld feld) {
        this.vorgaenger = null;
        this.feld = feld;
    }

    //Getter-Setter
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

    public void setSchaetzwert(int schaetzwert) {
        this.schaetzwert = schaetzwert;
    }

    public int getSchaetzwert() {
        return this.schaetzwert;
    }

    public int getBewertung() {
        return pfadkosten + schaetzwert;
    }

    public boolean isZiel(Feld feld) {
        return this.feld == feld ;
    }

    @Override
    public int hashCode(){
        String s = feld.getPosition()[0]+","+feld.getPosition()[1];
        return s.hashCode();
    }

}
