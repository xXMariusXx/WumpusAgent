package de.fh;

import java.util.HashSet;

public class Feld {

    //Attribute
    private HashSet<Zustand> set;
    private int risiko;
    private boolean besucht;
    private int[] position;
    private boolean beschossen = false;
    private int anzahlBesucht = 0;


    //Zustand Enum
    enum Zustand {
        HUNTER(39),

        WALL(99),

        UNBEKANNT(7),
        FREI(10),
        GOLD(1),

        GESTANK1(86),
        GESTANK2(46),
        GESTANK3(26),
        EVTWUMPUS(66),

        WIND(31),
        EVTFALLE(91),
        FALLE(99);


        private final int bewertung;

        Zustand(int i) {
            bewertung = i;
        }

        public int getBewertung() {
            return bewertung;
        }
    }



    public Feld(Zustand z, int x, int y,boolean besuchtSetzen) {
        set = new HashSet<>();
        addZustand(z,besuchtSetzen);
        position = new int[2];
        position[0] = x;
        position[1] = y;
    }


    //Getter und Setter
    public void addZustand(Zustand z, boolean besuchtSetzen) {
        switch (z) {
            case GESTANK1:
                //wenn schon stärkerer Gestank vorhanden ist, muss der schwächere nicht hinzugefügt werden
                set.remove(Zustand.GESTANK2);
                set.remove(Zustand.GESTANK3);
                if (set.contains(Zustand.WALL)) break;
                if (isBesucht()){
                    set.add(Zustand.GESTANK2);
                    return;
                }
                set.add(z);
                break;

            case GESTANK2:
                if (set.contains(Zustand.GESTANK1)) break;
                set.remove(Zustand.GESTANK3);
                if (set.contains(Zustand.WALL)) break;
                if (isBesucht()){
                    set.add(Zustand.GESTANK1);
                    return;
                }
                set.add(z);
                break;

            case GESTANK3:
                if (set.contains(Zustand.GESTANK3) || set.contains(Zustand.GESTANK2)) break;
                if (set.contains(Zustand.WALL)) break;
                if (isBesucht()) return;
                set.add(z);
                break;

            case WALL:
                set.add(z);
                //Wand muss nicht vom Hunter betreten werden, daher auf "besucht" gesetzt
                setBesucht();
                break;

            case HUNTER:
                set.remove(Zustand.GESTANK3);
                set.remove(Zustand.GESTANK2);
                set.remove(Zustand.GESTANK1);
                set.remove(Zustand.EVTFALLE);
                set.remove(Zustand.EVTWUMPUS);
                anzahlBesucht++;
                set.add(z);
                if(besuchtSetzen) setBesucht();
                break;

            case EVTFALLE:
                //wenn Feld schon besucht, kann es keine Falle mehr sein
                if (besucht) break;
                set.add(z);
                setBesucht();
                break;

            case FALLE:
                set.remove(Zustand.EVTFALLE);
                if (besucht) break;
                set.add(z);
                setBesucht();
                break;

            case EVTWUMPUS:
                if (set.contains(Zustand.WALL)) break;
                set.add(z);
                break;

            default:

        }
        set.remove(Zustand.UNBEKANNT);

    }

    public void removeZustand(Zustand z) {
        set.remove(z);
    }

    public int getRisiko() {
        berechneRisiko();
        return risiko;
    }

    public HashSet<Zustand> getZustaende() {
        return set;
    }

    public void setZustaende(HashSet<Zustand> zustaende) {
        set = zustaende;
    }

    public void setBesucht() {
        besucht = true;
    }

    public void setNichtBesucht() {
        besucht = false;
    }

    public boolean isBesucht() {
        return besucht;
    }

    public int[] getPosition() {
        return position;
    }

    public void setPosition(int[] position) {
        this.position = position;
    }

    public void setBeschossen() {
        this.beschossen = true;
    }

    public boolean isBeschossen() {
        return beschossen;
    }

    public boolean isGueltig(){
        return position[0] > -1 && position[1] > -1;
    }

    public int getAnzahlBesucht() {
        return anzahlBesucht;
    }

    public void setAnzahlBesucht(int anzahlBesucht) {
        this.anzahlBesucht = anzahlBesucht;
    }

    public void addAnzahlBesucht(){
        anzahlBesucht++;
    }

    @Override
    public String toString() {
        return getPosition()[0] + "," + getPosition()[1] + " " + getZustaende() + " " + getRisiko() + ", beschossen:" + isBeschossen() + ", besucht:" + isBesucht();
    }

    //Interne Methoden
    private void berechneRisiko() {
        //TODO verfeinern, z.B. Felder rund um das Feld herum angucken
        int max = 5; //unbekanntes Feld ist besser als bekanntes freies Feld aber schlechter als Gold
        for (Zustand z : set) {
            if (z.getBewertung() > max) max = z.getBewertung();
        }
        risiko = max;


    }
}
