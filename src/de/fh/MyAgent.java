package de.fh;

import de.fh.agent.WumpusHunterAgent;
import de.fh.wumpus.HunterPercept;
import de.fh.wumpus.enums.HunterAction;
import de.fh.wumpus.enums.HunterActionEffect;

import java.util.Hashtable;
import java.util.Map;

import static de.fh.Feld.Zustand.*;

/*
 * DIESE KLASSE VERÄNDERN SIE BITTE NUR AN DEN GEKENNZEICHNETEN STELLEN
 * wenn die Bonusaufgabe bewertet werden soll.
 */
public class MyAgent extends WumpusHunterAgent {

    HunterPercept percept;
    HunterActionEffect actionEffect;
    Hashtable<Integer, Integer> stenchRadar;

    //eigene Anpassungen
    Welt welt;
    Berechnung berechnung;

    public static void main(String[] args) {

        MyAgent agent = new MyAgent("");
        MyAgent.start(agent, "127.0.0.1", 5000);
    }

    public MyAgent(String name) {
        super(name);

        //eigene Anpassung
        welt = new Welt();
        berechnung = new Berechnung(welt);
    }

    /**
     * In dieser Methode kann das Wissen über die Welt (der State, der Zustand)
     * entsprechend der aktuellen Wahrnehmungen anpasst, und die "interne Welt",
     * die Wissensbasis, des Agenten kontinuierlich ausgebaut werden.
     * <p>
     * Wichtig: Diese Methode wird aufgerufen, bevor der Agent handelt, d.h.
     * bevor die action()-Methode aufgerufen wird...
     *
     * @param percept      Aktuelle Wahrnehmung
     * @param actionEffect Reaktion des Servers auf vorhergewählte Aktion
     */
    @Override
    public void updateState(HunterPercept percept, HunterActionEffect actionEffect) {
        System.out.println("________________________________________________________________________________________________________________________________________________________________________________");
        this.percept = percept;
        this.actionEffect = actionEffect;
        stenchRadar = this.percept.getWumpusStenchRadar();


        // 1. ----------- WELT AKTUALISIEREN -----------

        // ---- Hunter Stats setzen ----
        this.updateHunterStats();


        // ---- Wind auswerten ----
        if (percept.isBreeze()) {
            welt.addZustand(welt.getHunterPos()[0], welt.getHunterPos()[1], WIND);
            welt.addZustand(welt.getHunterPos()[0]+1, welt.getHunterPos()[1], EVTFALLE);
            welt.addZustand(welt.getHunterPos()[0]-1, welt.getHunterPos()[1], EVTFALLE);
            welt.addZustand(welt.getHunterPos()[0], welt.getHunterPos()[1]+1, EVTFALLE);
            welt.addZustand(welt.getHunterPos()[0], welt.getHunterPos()[1]-1, EVTFALLE);

            //ToDo evt prüfen ob Falle genauer eingegrenzt werden kann!
        }

        // ---- Gold setzen ----
        if (percept.isGlitter()) {
            welt.addZustand(welt.getHunterPos()[0], welt.getHunterPos()[1], GOLD);
            berechnung.setModus("goldAufnehmen");
        }

        // ---- Gestank zurücksetzen wenn Wumpus sich bewegt hat ----
        if (percept.isRumble()) {
            this.gestankLoeschen();
        }

        //Wenn Wumpus sich bewegt hat, das aktuelle Zwischenfeld aber noch nicht erreicht ist, sollte eventuell ein neues Zwischenfeld berrechnet werden
        if (percept.isRumble() && !stenchRadar.isEmpty())  berechnung.setZwischenfeldErreicht();


        if (!stenchRadar.isEmpty()) this.gestankAuswerten();


        // -------- AKTUELLEN STAND AUSGEBEN --------

        System.out.println(".........................................................................");

        System.out.println("STATS VOR BERECHNUNG\nRisiko-Tabelle");
        welt.displayRisiko();
        System.out.println();
        System.out.println("Action Effekt: " + actionEffect);

        //Gib alle riechbaren Wumpis aus
        if (!stenchRadar.isEmpty()) {
            System.out.println("WumpusID: Intensitaet");
            for (Map.Entry<Integer, Integer> g : stenchRadar.entrySet()) {
                System.out.println(g.getKey() + ":\t\t\t" + g.getValue());
            }
            System.out.println();
        }

        //Wind gespürt
        if (percept.isBreeze()) {
            System.out.println("Wind bemerkt");
        }

        //Gibt aus ob ein Nachbarfeld eine Wand ist
        if (percept.isBump()) {
            //System.out.println("Ein Nachbarfeld ist Wand");
        }

        //Gib aus wenn aktuelle Position = Gold
        if (percept.isGlitter()) {
            System.out.println("Stehe auf Gold");
        }

        //Info wenn Wumpus sich bewegt hat
        if (percept.isRumble()) {
            System.out.println("Wumpus hat sich bewegt");
        }

        //Gib aus wenn Wumpus getroffen
        if (percept.isScream()) {
            System.out.println("Scream bemerkt");
        }

        System.out.println(".........................................................................\n");







        /*
         Aktuelle Reaktion des Server auf die letzte übermittelte Action.

         // Alle möglichen Serverrückmeldungen:
         if(actionEffect == HunterActionEffect.GAME_INITIALIZED) {
         //Erster Aufruf
         }

         if(actionEffect == HunterActionEffect.GAME_OVER) {
         //Das Spiel ist verloren
         }

         if(actionEffect == HunterActionEffect.BUMPED_INTO_WALL) {
         //Letzte Bewegungsaktion führte in eine Wand
         }

         if(actionEffect == HunterActionEffect.MOVEMENT_SUCCESSFUL) {
         //Letzte Bewegungsaktion war gültig, oder Pfeil geschossen
         }

         if(actionEffect == HunterActionEffect.GOLD_FOUND) {
         //Gold wurde aufgesammelt
         }

         if(actionEffect == HunterActionEffect.WUMPUS_KILLED) {
         //Ein Wumpus wurde getroffen
         }

         if(actionEffect == HunterActionEffect.NO_MORE_SHOOTS) {
         //Schuss ungültig, keine Pfeile mehr
         }
         */


    }

    /**
     * Diesen Part erweitern Sie so, dass die nächste(n) sinnvolle(n) Aktion(en),
     * auf Basis der vorhandenen Zustandsinformationen und gegebenen Zielen, ausgeführt wird/werden.
     * Der action-Part soll den Agenten so intelligent wie möglich handeln lassen
     *
     * Beispiel: Wenn die letzte Wahrnehmung
     * "percept.isGlitter() == true" enthielt, ist "HunterAction.GRAB" eine
     * geeignete Tätigkeit. Wenn Sie wissen, dass ein Quadrat "unsicher"
     * ist, können Sie wegziehen
     *
     * @return Die nächste HunterAction die vom Server ausgeführt werden soll
     */
    @Override
    public HunterAction action() {
        // 2. -----NÄCHSTEN SCHRITT PLANEN-----
        berechnung.berechne();
        HunterAction nextHunterAction = welt.getNextAction();
        welt.aktuelleAktionAusgefuehrt();
        System.out.println("\nRisiko Tabelle nach Berechnung");
        welt.displayRisiko();
        System.out.println("________________________________________________________________________________________________________________________________________________________________________________");
        return nextHunterAction;
    }


    //Update Methoden
    private void updateHunterStats() {
        switch (actionEffect) {
            case MOVEMENT_SUCCESSFUL:
                welt.removePunkte(1);
                switch (welt.getLastAction()) {
                    case SHOOT:
                        switch (welt.getBlickrichtung()) {
                            case EAST:
                                //Löscht 3 Wumpus Gefahren in der Map von Position des Hunters in Blick/Pfeilrichtung
                                welt.removeWumpusGefahr();
                                break;
                            case WEST:
                                welt.removeWumpusGefahr();
                                break;
                            case NORTH:
                                welt.removeWumpusGefahr();
                                break;
                            case SOUTH:
                                welt.removeWumpusGefahr();
                                break;
                            }
                        break;

                    case GO_FORWARD:
                        switch (welt.getBlickrichtung()) {
                            case EAST:
                                welt.updateHunterPos(welt.getHunterPos()[0] + 1, welt.getHunterPos()[1]);
                                break;
                            case WEST:
                                welt.updateHunterPos(welt.getHunterPos()[0] - 1, welt.getHunterPos()[1]);
                                break;
                            case NORTH:
                                welt.updateHunterPos(welt.getHunterPos()[0], welt.getHunterPos()[1] - 1);
                                break;
                            case SOUTH:
                                welt.updateHunterPos(welt.getHunterPos()[0], welt.getHunterPos()[1] + 1);
                                break;
                        }
                        break;

                    case TURN_RIGHT:
                        switch (welt.getBlickrichtung()) {
                            case SOUTH:
                                welt.setBlickrichtung(Welt.Himmelsrichtung.WEST);
                                break;
                            case NORTH:
                                welt.setBlickrichtung(Welt.Himmelsrichtung.EAST);
                                break;
                            case WEST:
                                welt.setBlickrichtung(Welt.Himmelsrichtung.NORTH);
                                break;
                            case EAST:
                                welt.setBlickrichtung(Welt.Himmelsrichtung.SOUTH);
                                break;
                        }
                        break;

                    case TURN_LEFT:
                        switch (welt.getBlickrichtung()) {
                            case SOUTH:
                                welt.setBlickrichtung(Welt.Himmelsrichtung.EAST);
                                break;
                            case NORTH:
                                welt.setBlickrichtung(Welt.Himmelsrichtung.WEST);
                                break;
                            case WEST:
                                welt.setBlickrichtung(Welt.Himmelsrichtung.SOUTH);
                                break;
                            case EAST:
                                welt.setBlickrichtung(Welt.Himmelsrichtung.NORTH);
                                break;
                        }
                        break;
                }
                welt.removeLastAction();
                break;

            case BUMPED_INTO_WALL:
                welt.wandErgaenzen();
                berechnung.setModus("checkEcke");
                berechnung.setZielfeldErreicht();
                welt.getLastAction();
                switch (welt.getBlickrichtung()) {
                    case EAST:
                        welt.addZustand(welt.getHunterPos()[0] + 1, welt.getHunterPos()[1], WALL);
                        break;
                    case WEST:
                        welt.addZustand(welt.getHunterPos()[0] - 1, welt.getHunterPos()[1], WALL);
                        break;
                    case NORTH:
                        welt.addZustand(welt.getHunterPos()[0], welt.getHunterPos()[1] - 1, WALL);
                        break;
                    case SOUTH:
                        welt.addZustand(welt.getHunterPos()[0], welt.getHunterPos()[1] + 1, WALL);
                        break;
                }
                welt.removeLastAction();
                break;

            case GOLD_FOUND:
                welt.setGoldGesammelt();
                berechnung.setModus("goldAufgenommen");
                break;

            case WUMPUS_KILLED:
                welt.setWumpusGetoetet();
                break;

            case NO_MORE_SHOOTS:
                System.err.println("Keine Pfeile mehr! Fehler in Berechnung!");
                break;
        }
    }

    private void gestankAuswerten() {
            for (Map.Entry<Integer, Integer> g : stenchRadar.entrySet()) {
                switch (g.getValue()) {
                    case 1:
                        welt.addZustand(welt.getHunterPos()[0] + 1, welt.getHunterPos()[1], GESTANK1);
                        welt.addZustand(welt.getHunterPos()[0] - 1, welt.getHunterPos()[1], GESTANK1);
                        welt.addZustand(welt.getHunterPos()[0], welt.getHunterPos()[1] + 1, GESTANK1);
                        welt.addZustand(welt.getHunterPos()[0], welt.getHunterPos()[1] - 1, GESTANK1);
                        //berechnung.setModus("wumpusToeten");
                        break;
                    case 2:
                        welt.addZustand(welt.getHunterPos()[0] + 2, welt.getHunterPos()[1], GESTANK2);
                        welt.addZustand(welt.getHunterPos()[0] - 2, welt.getHunterPos()[1], GESTANK2);
                        welt.addZustand(welt.getHunterPos()[0], welt.getHunterPos()[1] + 2, GESTANK2);
                        welt.addZustand(welt.getHunterPos()[0], welt.getHunterPos()[1] - 2, GESTANK2);
                        welt.addZustand(welt.getHunterPos()[0] + 1, welt.getHunterPos()[1] + 1, GESTANK2);
                        welt.addZustand(welt.getHunterPos()[0] + 1, welt.getHunterPos()[1] - 1, GESTANK2);
                        welt.addZustand(welt.getHunterPos()[0] - 1, welt.getHunterPos()[1] - 1, GESTANK2);
                        welt.addZustand(welt.getHunterPos()[0] - 1, welt.getHunterPos()[1] + 1, GESTANK2);
                        break;
                    case 3:
                        welt.addZustand(welt.getHunterPos()[0] + 3, welt.getHunterPos()[1], GESTANK3);
                        welt.addZustand(welt.getHunterPos()[0] - 3, welt.getHunterPos()[1], GESTANK3);
                        welt.addZustand(welt.getHunterPos()[0], welt.getHunterPos()[1] + 3, GESTANK3);
                        welt.addZustand(welt.getHunterPos()[0], welt.getHunterPos()[1] - 3, GESTANK3);
                        welt.addZustand(welt.getHunterPos()[0] + 2, welt.getHunterPos()[1] + 1, GESTANK3);
                        welt.addZustand(welt.getHunterPos()[0] + 2, welt.getHunterPos()[1] - 1, GESTANK3);
                        welt.addZustand(welt.getHunterPos()[0] - 2, welt.getHunterPos()[1] + 1, GESTANK3);
                        welt.addZustand(welt.getHunterPos()[0] - 2, welt.getHunterPos()[1] - 1, GESTANK3);
                        welt.addZustand(welt.getHunterPos()[0] + 1, welt.getHunterPos()[1] + 2, GESTANK3);
                        welt.addZustand(welt.getHunterPos()[0] + 1, welt.getHunterPos()[1] - 2, GESTANK3);
                        welt.addZustand(welt.getHunterPos()[0] - 1, welt.getHunterPos()[1] + 2, GESTANK3);
                        welt.addZustand(welt.getHunterPos()[0] - 1, welt.getHunterPos()[1] - 2, GESTANK3);
                        break;
                }
            }

    }

    private void gestankLoeschen(){
        for (int i = 0; i<welt.getMapSize()[0];i++){
            for (int j = 0; j < welt.getMapSize()[1]; j++) {
                welt.removeZustand(i,j, GESTANK1);
                welt.removeZustand(i,j, GESTANK2);
                welt.removeZustand(i,j, GESTANK3);
                welt.removeZustand(i,j, EVTWUMPUS);
            }
        }
        /*
        welt.removeZustand(welt.getHunterPos()[0] + 1, welt.getHunterPos()[1], GESTANK1);
        welt.removeZustand(welt.getHunterPos()[0] - 1, welt.getHunterPos()[1], GESTANK1);
        welt.removeZustand(welt.getHunterPos()[0], welt.getHunterPos()[1] + 1, GESTANK1);
        welt.removeZustand(welt.getHunterPos()[0], welt.getHunterPos()[1] - 1, GESTANK1);

        welt.removeZustand(welt.getHunterPos()[0] + 2, welt.getHunterPos()[1], GESTANK2);
        welt.removeZustand(welt.getHunterPos()[0] - 2, welt.getHunterPos()[1], GESTANK2);
        welt.removeZustand(welt.getHunterPos()[0], welt.getHunterPos()[1] + 2, GESTANK2);
        welt.removeZustand(welt.getHunterPos()[0], welt.getHunterPos()[1] - 2, GESTANK2);
        welt.removeZustand(welt.getHunterPos()[0] + 1, welt.getHunterPos()[1] + 1, GESTANK2);
        welt.removeZustand(welt.getHunterPos()[0] + 1, welt.getHunterPos()[1] - 1, GESTANK2);
        welt.removeZustand(welt.getHunterPos()[0] - 1, welt.getHunterPos()[1] - 1, GESTANK2);
        welt.removeZustand(welt.getHunterPos()[0] - 1, welt.getHunterPos()[1] + 1, GESTANK2);

        welt.removeZustand(welt.getHunterPos()[0] + 3, welt.getHunterPos()[1], GESTANK3);
        welt.removeZustand(welt.getHunterPos()[0] - 3, welt.getHunterPos()[1], GESTANK3);
        welt.removeZustand(welt.getHunterPos()[0], welt.getHunterPos()[1] + 3, GESTANK3);
        welt.removeZustand(welt.getHunterPos()[0], welt.getHunterPos()[1] - 3, GESTANK3);
        welt.removeZustand(welt.getHunterPos()[0] + 2, welt.getHunterPos()[1] + 1, GESTANK3);
        welt.removeZustand(welt.getHunterPos()[0] + 2, welt.getHunterPos()[1] - 1, GESTANK3);
        welt.removeZustand(welt.getHunterPos()[0] - 2, welt.getHunterPos()[1] + 1, GESTANK3);
        welt.removeZustand(welt.getHunterPos()[0] - 2, welt.getHunterPos()[1] - 1, GESTANK3);
        welt.removeZustand(welt.getHunterPos()[0] + 1, welt.getHunterPos()[1] + 2, GESTANK3);
        welt.removeZustand(welt.getHunterPos()[0] + 1, welt.getHunterPos()[1] - 2, GESTANK3);
        welt.removeZustand(welt.getHunterPos()[0] - 1, welt.getHunterPos()[1] + 2, GESTANK3);
        welt.removeZustand(welt.getHunterPos()[0] - 1, welt.getHunterPos()[1] - 2, GESTANK3);
        for(int i = 0; i<welt.wandGrenzeX();i++){
            for (int j = 0; j < welt.wandGrenzeY() ; j++) {
                welt.removeZustand(i,j,EVTWUMPUS);

            }
        }
        */
    }
}