package de.fh;

import de.fh.wumpus.enums.HunterAction;
import de.fh.wumpus.enums.HunterActionEffect;

import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

public class Berechnung {
    private Welt welt;

    public Berechnung(Welt welt) {
        this.welt = welt;
    }

    public void berechne() {
        HunterAction nextAction = HunterAction.GO_FORWARD;


        Feld naechstesFeld = this.bestimmeNaechstesFeld();

        int[] feldPos = welt.getFeldPos(naechstesFeld);

        if (welt.getHunterPos()[0] == feldPos[0] && welt.getHunterPos()[1]+1 == feldPos[1])
        {
            switch (welt.getBlickrichtung()){
                case EAST:
                    nextAction = HunterAction.TURN_RIGHT;
                    break;
                case WEST:
                    nextAction = HunterAction.TURN_LEFT;
                    break;
                case NORTH:
                    nextAction = HunterAction.TURN_RIGHT;
                    break;
                case SOUTH:
                    nextAction = HunterAction.GO_FORWARD;
                    break;
            }
        }
        else if (welt.getHunterPos()[0] == feldPos[0] && welt.getHunterPos()[1]-1 == feldPos[1])
        {
            switch (welt.getBlickrichtung()){
                case EAST:
                    nextAction = HunterAction.TURN_LEFT;
                    break;
                case WEST:
                    nextAction = HunterAction.TURN_RIGHT;
                    break;
                case NORTH:
                    nextAction = HunterAction.GO_FORWARD;
                    break;
                case SOUTH:
                    nextAction = HunterAction.TURN_RIGHT;
                    break;
            }
        }
        else if (welt.getHunterPos()[0]+1 == feldPos[0] && welt.getHunterPos()[1] == feldPos[1])
        {
            switch (welt.getBlickrichtung()){
                case EAST:
                    nextAction = HunterAction.GO_FORWARD;
                    break;
                case WEST:
                    nextAction = HunterAction.TURN_RIGHT;
                    break;
                case NORTH:
                    nextAction = HunterAction.TURN_RIGHT;
                    break;
                case SOUTH:
                    nextAction = HunterAction.TURN_LEFT;
                    break;
            }
        }
        else if (welt.getHunterPos()[0]-1 == feldPos[0] && welt.getHunterPos()[1] == feldPos[1])
        {
            switch (welt.getBlickrichtung()){
                case EAST:
                    nextAction = HunterAction.TURN_LEFT;
                    break;
                case WEST:
                    nextAction = HunterAction.GO_FORWARD;
                    break;
                case NORTH:
                    nextAction = HunterAction.TURN_LEFT;
                    break;
                case SOUTH:
                    nextAction = HunterAction.TURN_RIGHT;
                    break;
            }
        }


        welt.addNextAction(nextAction);
    }

    private Feld bestimmeNaechstesFeld(){

        LinkedList<Feld> moeglicheFelder = new LinkedList<>();
        moeglicheFelder.add(welt.getFeld(welt.getHunterPos()[0] + 1, welt.getHunterPos()[1]));
        moeglicheFelder.add(welt.getFeld(welt.getHunterPos()[0] - 1, welt.getHunterPos()[1]));
        moeglicheFelder.add(welt.getFeld(welt.getHunterPos()[0], welt.getHunterPos()[1] + 1));
        moeglicheFelder.add(welt.getFeld(welt.getHunterPos()[0], welt.getHunterPos()[1] - 1));
        moeglicheFelder.sort(Comparator.comparingInt(Feld::getRisiko));

        for (Feld f: moeglicheFelder) {
            if (!f.isBesucht()){
                return f;
            }
        }
        return moeglicheFelder.getFirst();
    }


}


		/*HunterAction
        Mögliche HunterActions sind möglich:

       	HunterAction.GO_FORWARD
       	HunterAction.TURN_LEFT
		HunterAction.TURN_RIGHT
		HunterAction.SHOOT
		HunterAction.SIT
		HunterAction.GRAB
		HunterAction.QUIT_GAME
		*/
