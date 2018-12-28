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
	
	public static void main(String[] args) {

		MyAgent agent = new MyAgent("");
		MyAgent.start(agent,"127.0.0.1", 5000);
	}

	public MyAgent(String name) {
		super(name);

		//eigene Anpassung
		welt = new Welt();
	}

	/**
	 * In dieser Methode kann das Wissen über die Welt (der State, der Zustand)
	 * entsprechend der aktuellen Wahrnehmungen anpasst, und die "interne Welt",
	 * die Wissensbasis, des Agenten kontinuierlich ausgebaut werden.
	 *
	 * Wichtig: Diese Methode wird aufgerufen, bevor der Agent handelt, d.h.
	 * bevor die action()-Methode aufgerufen wird...
	 *
	 * @param percept Aktuelle Wahrnehmung
	 * @param actionEffect Reaktion des Servers auf vorhergewählte Aktion
	 */
	@Override
	public void updateState(HunterPercept percept, HunterActionEffect actionEffect) {
		this.percept = percept;
		this.actionEffect = actionEffect;
		stenchRadar = this.percept.getWumpusStenchRadar();



		// -----WELT AKTUALISIEREN-----
		this.updateHunterStats();


		// ---- Gestank auswerten
		if(!stenchRadar.isEmpty())
		{
			for(Map.Entry<Integer, Integer> g : stenchRadar.entrySet()){
				switch (g.getValue()){
					case 1:
						welt.addZustand(welt.getHunterPos()[0]+1,welt.getHunterPos()[1],GESTANK1);
						welt.addZustand(welt.getHunterPos()[0]-1,welt.getHunterPos()[1],GESTANK1);
						welt.addZustand(welt.getHunterPos()[0],welt.getHunterPos()[1]+1,GESTANK1);
						welt.addZustand(welt.getHunterPos()[0],welt.getHunterPos()[1]-1,GESTANK1);
						break;
					case 2:
						welt.addZustand(welt.getHunterPos()[0]+2,welt.getHunterPos()[1],GESTANK2);
						welt.addZustand(welt.getHunterPos()[0]-2,welt.getHunterPos()[1],GESTANK2);
						welt.addZustand(welt.getHunterPos()[0],welt.getHunterPos()[1]+2,GESTANK2);
						welt.addZustand(welt.getHunterPos()[0],welt.getHunterPos()[1]-2,GESTANK2);
						welt.addZustand(welt.getHunterPos()[0]+1,welt.getHunterPos()[1]+1,GESTANK2);
						welt.addZustand(welt.getHunterPos()[0]+1,welt.getHunterPos()[1]-1,GESTANK2);
						welt.addZustand(welt.getHunterPos()[0]-1,welt.getHunterPos()[1]-1,GESTANK2);
						welt.addZustand(welt.getHunterPos()[0]-1,welt.getHunterPos()[1]+1,GESTANK2);
						break;
					case 3:
						welt.addZustand(welt.getHunterPos()[0]+3,welt.getHunterPos()[1],GESTANK3);
						welt.addZustand(welt.getHunterPos()[0]-3,welt.getHunterPos()[1],GESTANK3);
						welt.addZustand(welt.getHunterPos()[0],welt.getHunterPos()[1]+3,GESTANK3);
						welt.addZustand(welt.getHunterPos()[0],welt.getHunterPos()[1]-3,GESTANK3);
						welt.addZustand(welt.getHunterPos()[0]+2,welt.getHunterPos()[1]+1,GESTANK3);
						welt.addZustand(welt.getHunterPos()[0]+2,welt.getHunterPos()[1]-1,GESTANK3);
						welt.addZustand(welt.getHunterPos()[0]-2,welt.getHunterPos()[1]+1,GESTANK3);
						welt.addZustand(welt.getHunterPos()[0]-2,welt.getHunterPos()[1]-1,GESTANK3);
						welt.addZustand(welt.getHunterPos()[0]+1,welt.getHunterPos()[1]+2,GESTANK3);
						welt.addZustand(welt.getHunterPos()[0]+1,welt.getHunterPos()[1]-2,GESTANK3);
						welt.addZustand(welt.getHunterPos()[0]-1,welt.getHunterPos()[1]+2,GESTANK3);
						welt.addZustand(welt.getHunterPos()[0]-1,welt.getHunterPos()[1]-2,GESTANK3);
						break;
				}
			}
		}


		// ---- AKTUELLEN STAND AUSGEBEN ----

		System.out.println("\n.........................................................................");

		System.out.println("Risiko-Tabelle");
		welt.displayRisiko();
		System.out.println();

		//Gib alle riechbaren Wumpis aus
		if(stenchRadar.isEmpty())
		{
			System.out.println("Kein Wumpi zu riechen");
		}
		else
		{
			System.out.println("WumpusID: Intensitaet");
			for(Map.Entry<Integer, Integer> g : stenchRadar.entrySet()){
				System.out.println(g.getKey() + ":\t\t\t" + g.getValue() );
			}
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
         //Letzte Bewegungsaktion war gültig
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


		/*
		Mögliche Percepts über die Welt erhält der Wumpushunter:

		percept.isBreeze();
        percept.isBump();
        percept.isGlitter();
        percept.isRumble();
        percept.isScream();
        percept.isStench();
        percept.getWumpusStenchRadar()
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

		// -----NÄCHSTEN SCHRITT PLANEN-----

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



		if (actionEffect == HunterActionEffect.BUMPED_INTO_WALL)
		{
			nextAction=HunterAction.TURN_RIGHT;
		}
		else {
			nextAction = HunterAction.GO_FORWARD;
		}



		welt.addNextAction(nextAction);
		HunterAction hunterAction = welt.getNextAction();

		welt.aktuelleAktionAusgefuehrt();
		return hunterAction;
	}




	private void updateHunterStats()
	{

		if (actionEffect == HunterActionEffect.MOVEMENT_SUCCESSFUL)
		{
			welt.removePunkte(1);
			switch (welt.getLastAction())
			{
				case GO_FORWARD:
					switch (welt.getBlickrichtung())
					{
						case EAST:
							welt.updateHunterPos(welt.getHunterPos()[0]+1,welt.getHunterPos()[1]);
							break;
						case WEST:
							welt.updateHunterPos(welt.getHunterPos()[0]-1,welt.getHunterPos()[1]);
							break;
						case NORTH:
							welt.updateHunterPos(welt.getHunterPos()[0],welt.getHunterPos()[1]-1);
							break;
						case SOUTH:
							welt.updateHunterPos(welt.getHunterPos()[0],welt.getHunterPos()[1]+1);
							break;
					}
					break;

				case TURN_RIGHT:
					switch (welt.getBlickrichtung())
					{
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
					switch (welt.getBlickrichtung())
					{
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
		}
		else if(actionEffect == HunterActionEffect.BUMPED_INTO_WALL){
			welt.removeLastAction();
			switch (welt.getBlickrichtung())
			{
				case EAST:
					welt.addZustand(welt.getHunterPos()[0]+1,welt.getHunterPos()[1], WALL);
					break;
				case WEST:
					welt.addZustand(welt.getHunterPos()[0]-1,welt.getHunterPos()[1],WALL);
					break;
				case NORTH:
					welt.addZustand(welt.getHunterPos()[0],welt.getHunterPos()[1]-1,WALL);
					break;
				case SOUTH:
					welt.addZustand(welt.getHunterPos()[0],welt.getHunterPos()[1]+1,WALL);
					break;
			}

		}
		else if(actionEffect == HunterActionEffect.GOLD_FOUND){
			welt.setGoldGefunden();
			welt.addZustand(welt.getHunterPos()[0], welt.getHunterPos()[1],GOLD);
		}
		else if(actionEffect == HunterActionEffect.WUMPUS_KILLED){
			welt.setWumpusGetoetet();
		}
	}
}