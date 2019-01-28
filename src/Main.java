import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import javax.swing.SwingUtilities;

import board.Area;
import board.Land;
import board.Map;
import controllers.IntentController;
import controllers.TurnController;
import gui.VisualMap;
import intents.Intent;
import intents.Move;
import pieces.Fleet;
import pieces.Infantry;
import pieces.Piece;
import players.Empire;
import players.PlayerAI;

public class Main {
	
	private static final String[] EMPIRENAMES = new String[]{"Austria", "England", "France", "Germany", "Italy", "Russia", "Turkey"};
	private static int turnCounter = 0;
	
	public static void main(String[] args) {
		//while (turnCounter < 1000) {
		Map map = new Map();
		IntentController ic = new IntentController(map);
		List<Empire> empires = new ArrayList<>();
		List<PlayerAI> players = new ArrayList<>();
		for (int i = 0; i < EMPIRENAMES.length; i++) {
			Empire thisEmpire = new Empire(EMPIRENAMES[i]);
			for (Piece piece : thisEmpire.getArmy()) {
				map.addPieceToArea(piece, piece.getPositionName());
			}
			empires.add(thisEmpire);
			players.add(new PlayerAI(thisEmpire, ic));
		}
		System.out.println("Empires created");
		VisualMap vm = new VisualMap(empires, true);
		ic.updatePieceCount(empires);
		ic.setVM(vm);
		boolean winnerFound = false;
		Scanner scanner = new Scanner(System.in);
		TurnController tc = new TurnController(players, ic);
		tc.nextTurn(vm);
		while (!winnerFound /*&& !TurnController.endTraining*/) {
			while (!scanner.nextLine().equals("next")) {}
			tc.nextTurn(vm);
		}
		turnCounter++;
		//}
	}
}
