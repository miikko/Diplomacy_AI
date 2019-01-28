package controllers;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Scanner;

import board.Area;
import board.Land;
import board.Map;
import gui.VisualMap;
import pieces.Piece;
import players.Empire;
import players.PlayerAI;

public class TurnController {

	public static int year = 1899;
	public static String season = "Winter";
	private List<PlayerAI> players;
	private IntentController ic;
	private Map map;
	public static boolean endTraining = false;

	public TurnController(List<PlayerAI> players, IntentController ic) {
		year = 1899;
		season = "Winter";
		endTraining = false;
		this.ic = ic;
		this.map = ic.getMap();
		this.players = players;
	}
	
	public void nextTurn(VisualMap vm) {
		if (ic.successfulMoves.size() > 0) {
			List<String> destinations = new ArrayList<>(ic.successfulMoves.keySet());
			for (String destination : destinations) {
				Piece thisPiece = ic.successfulMoves.get(destination);
				String previousPosition = thisPiece.getPositionName();
				map.removePieceFromArea(thisPiece);
				map.getAreaByName(destination).setOccupant(thisPiece);
				thisPiece.setPositionName(destination);
				System.out.println(thisPiece.getEmpire().getName() + ": " + thisPiece.toString() + " moved from " + previousPosition + " to " + destination);
			}
		}
		if (ic.dislodgedPiecesAndForbiddenAreas.size() > 0) {
			List<Piece> dislodgedPieces = new ArrayList<>(ic.dislodgedPiecesAndForbiddenAreas.keySet());
			for (Piece dislodgedPiece : dislodgedPieces) {
				Empire thisEmpire = dislodgedPiece.getEmpire();
				for (PlayerAI player : players) {
					if (player.getEmpire() == thisEmpire) {
						player.assignRetreatIntents(dislodgedPiece, ic.dislodgedPiecesAndForbiddenAreas.get(dislodgedPiece));
					}
				}
			}
		}
		vm.reset();
		List<Empire> empires = new ArrayList<>();
		for (PlayerAI player : players) {
			empires.add(player.getEmpire());
		}
		ic.successfulMoves = new LinkedHashMap<>();
		ic.dislodgedPiecesAndForbiddenAreas = new LinkedHashMap<>();
		ic.updatePieceCount(empires);
		switch (season) {
		case "Spring":
			season = "Fall";
			break;
		case "Fall":
			season = "Winter";
			for (PlayerAI player : players) {
				updatePlayerCenterCount(player);
			}
			break;
		case "Winter":
			season = "Spring";
			year++;
		default:
			break;
		}
		System.out.println(season + ", Year: " + year);
		callPlayers();
	}
	
	public void callPlayers() {
		Scanner scanner = new Scanner(System.in);
		for (PlayerAI player : players) {
			if (season.equals("Winter")) {
				//build/disband pieces
				player.assignWinterIntents();
			} else {			
				player.assignNormalIntents();
			}
			System.out.println(player.getEmpire().getName() + " intents processed.");
			/*
			while (!scanner.nextLine().equals("emp")) {
				
			}*/
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
		while (!scanner.nextLine().equals("emp")) {
			
		}
		//scanner.close();
		System.out.println("Resolving intents.");
		ic.resolveIntents();
	}
	
	private void updatePlayerCenterCount(PlayerAI player) {
		Empire empire = player.getEmpire();
		boolean needsTraining = true;
		//Remove lost centers
		List<String> ownedCenterNames = empire.getOwnedCenterNames();
		for (int i = 0; i < ownedCenterNames.size(); i++) {
			String ownedCenterName = ownedCenterNames.get(i);
			Land centerArea = (Land) map.getAreaByName(ownedCenterName);
			if (centerArea.getOccupant() != null && centerArea.getOccupant().getEmpire() != empire) {
				ownedCenterNames.remove(i);
				i--;
			}
		}
		//Add captured centers
		List<Piece> army = empire.getArmy();
		for (Piece piece : army) {
			Area ownedArea = map.getAreaByName(piece.getPositionName());
			if (ownedArea.toString().equals("Land")) {
				Land ownedLandArea = (Land) ownedArea;
				if (ownedLandArea.containsCenter()) {
					String posNameCopy = piece.getPositionName();
					if (posNameCopy.contains("-")) {
						posNameCopy = piece.getPositionName().split("-")[0];
					}
					if (!ownedCenterNames.contains(posNameCopy)) {
						needsTraining = false;
						ownedCenterNames.add(posNameCopy);
						ic.getVM().drawCenter(posNameCopy, empire.getName());
					}
				}				
			}
		}
		empire.setOwnedCenterNames(ownedCenterNames);
		//Training period has ended
		if (year == 1900 && season.equals("Winter")) {
			if (empire.getName().equals("Russia")) {
				if (empire.getOwnedCenterNames().size() < 5) {
					player.getBrain().train();
					System.out.println(empire.getName() + "'s brain has been trained.");
				}
			} else if (empire.getOwnedCenterNames().size() < 4) {
				player.getBrain().train();
				System.out.println(empire.getName() + "'s brain has been trained.");
			}
			endTraining = true;
		}
		if (ownedCenterNames.size() >= 18) {
			//TODO: Add game end code
			System.out.println(empire.getName() + " has won the game!");
			System.exit(0);
		} else if (ownedCenterNames.size() == 0) {
			//TODO: Add player removal code
			players.remove(player);
		}
	}

}
