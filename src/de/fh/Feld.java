package de.fh;

import java.util.HashSet;

public class Feld {

    //Attribute
    private HashSet<Zustand> set;
    private int risiko;
    private boolean besucht = false;


    //Zustand Enum
    enum Zustand {
        HUNTER(39),
        WALL(99),
        UNBEKANNT(7),
        FREI(10),
        WIND(30),
        GESTANK1(90),
        GESTANK2(42),
        GESTANK3(25),
        EVTFALLE(70),
        FALLE(100),
        GOLD(1);

        private final int bewertung;

        Zustand(int i) {
            bewertung = i;
        }

        public int getBewertung() {
            return bewertung;
        }
    }


    //Konstruktoren
    public Feld(HashSet<Zustand> tmp) {
        set = tmp;
    }

    public Feld(Zustand z) {
        set = new HashSet<Zustand>();
        set.add(z);
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

            case EVTFALLE:
                if (besucht) break;
                set.add(z);

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
