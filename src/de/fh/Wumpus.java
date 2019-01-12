package de.fh;

public class Wumpus {
    private int id;
    private boolean lebendig;

    public Wumpus(int id) {
        this.id = id;
        this.lebendig = true;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public boolean isLebendig() {
        return lebendig;
    }

    public void setLebendig(boolean lebendig) {
        this.lebendig = lebendig;
    }
}
