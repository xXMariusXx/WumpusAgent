package de.fh;

import java.util.HashSet;

public class Feld {

    //Attribute
    private HashSet<Zustand> set;
    private int risiko;
    private boolean besucht;
    private int[] position;


    //Zustand Enum
    enum Zustand {
        HUNTER(39),

        WALL(99),

        UNBEKANNT(9),
        FREI(10),
        GOLD(1),

        GESTANK1(86),
        GESTANK2(46),
        GESTANK3(26),

        WIND(31),
        EVTFALLE(71),
        FALLE(100);


        private final int bewertung;

        Zustand(int i) {
            bewertung = i;
        }

        public int getBewertung() {
            return bewertung;
        }
    }


    //Konstruktoren
    public Feld(HashSet<Zustand> tmp, int x, int y) {
        set = new HashSet<Zustand>();
        tmp.forEach(this::addZustand);
        position = new int[2];
        position[0] = x;
        position[1] = y;

    }

    public Feld(Zustand z, int x, int y) {
        set = new HashSet<Zustand>();
        addZustand(z);
        position = new int[2];
        position[0] = x;
        position[1] = y;
    }


    //Getter und Setter
    public void addZustand(Zustand z) {
        switch (z) {
            case GESTANK1:
                if (set.contains(Zustand.GESTANK2)) set.remove(Zustand.GESTANK2);
                if (set.contains(Zustand.GESTANK3)) set.remove(Zustand.GESTANK3);
                set.add(z);
                break;

            case GESTANK2:
                if (set.contains(Zustand.GESTANK1)) break;
                if (set.contains(Zustand.GESTANK3)) set.remove(Zustand.GESTANK3);
                set.add(z);
                break;

            case GESTANK3:
                if (set.contains(Zustand.GESTANK3) || set.contains(Zustand.GESTANK2)) break;
                set.add(z);
                break;

            case WALL:
                set.add(z);
                setBesucht();
                break;

            case HUNTER:
                set.add(z);
                setBesucht();
                break;

            case EVTFALLE:
                if (besucht) break;
                set.add(z);
                setBesucht();
                break;

            default:
                set.add(z);
        }

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

    public boolean isBesucht() {
        return besucht;
    }

    public int[] getPosition() {
        return position;
    }

    public void setPosition(int[] position) {
        this.position = position;
    }

    @Override
    public String toString() {
        return "(X,Y):" + getPosition()[0] + "," + getPosition()[1] + getZustaende() + getRisiko();
    }

    //Interne Methoden
    private void berechneRisiko() {
        //TODO verfeinern
        int max = 5; //unbekanntes Feld ist besser als bekanntes freies Feld aber schlechter als Gold
        for (Zustand z : set) {
            if (z.getBewertung() > max) max = z.getBewertung();
        }
        risiko = max;
    }
}
