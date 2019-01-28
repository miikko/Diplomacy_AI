package controllers;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import board.Area;
import board.Land;
import board.Map;
import gui.VisualMap;
import intents.Convoy;
import intents.Disband;
import intents.Hold;
import intents.Intent;
import intents.Move;
import intents.Support;
import pieces.Fleet;
import pieces.Infantry;
import pieces.Piece;
import players.Empire;

//BUGS:
//Pieces that moved to their counterparts starting position caused a stack overflow on moveSuccessful()
//Stack overflow from support cancelled()-getmoveIntentStrength(), rows 287, 317 FIXED??

public class IntentController {

	private List<Intent> playerIntents;
	private int pieceCount;
	private LinkedHashMap<Move, List<Convoy>> movesAndConvoys;
	public LinkedHashMap<Piece, String> dislodgedPiecesAndForbiddenAreas;
	public LinkedHashMap<String, Piece> successfulMoves;
	private VisualMap vm;
	private Map map;

	public IntentController(Map map) {
		playerIntents = new ArrayList<>();
		movesAndConvoys = new LinkedHashMap<>();
		dislodgedPiecesAndForbiddenAreas = new LinkedHashMap<Piece, String>();
		successfulMoves = new LinkedHashMap<String, Piece>();
		this.map = map;
	}

	public void addIntent(Intent intent) {
		playerIntents.add(intent);
		Piece piece = intent.getOwner();
		String message = piece.getEmpire().getName() + ": " + piece.toString() + " in " + piece.getPositionName() + " "
				+ intent.toString() + " ";
		if (intent.toString().equals("Move")) {
			Move moveIntent = (Move) intent;
			message += moveIntent.getStartingPositionName() + "-" + moveIntent.getDestinationName();
			vm.drawMove(moveIntent.getStartingPositionName(), moveIntent.getDestinationName(), true);
		} else if (intent.toString().equals("Support")) {
			Support supportIntent = (Support) intent;
			if (supportIntent.getSupportedIntent().toString().equals("Move")) {
				Move supportedMoveIntent = (Move) supportIntent.getSupportedIntent();
				message += "Move " + supportedMoveIntent.getStartingPositionName() + "-"
						+ supportedMoveIntent.getDestinationName();
				vm.drawSupport(piece.getPositionName(), supportedMoveIntent.getStartingPositionName(),
						supportedMoveIntent.getDestinationName(), true);
			} else {
				Hold supportedHoldIntent = (Hold) supportIntent.getSupportedIntent();
				message += "Hold " + supportedHoldIntent.getOwner().getPositionName();
				vm.drawSupport(piece.getPositionName(), supportedHoldIntent.getOwner().getPositionName(), true);
			}
		} else if (intent.toString().equals("Convoy")) {
			Convoy convoyIntent = (Convoy) intent;
			Move convoyedMove = convoyIntent.getMove();
			message += convoyedMove.getStartingPositionName() + "-" + convoyedMove.getDestinationName();
			vm.drawConvoy(intent.getOwner().getPositionName(), true);
		} else {
			message += intent.getOwner().getPositionName();
			vm.drawHold(intent.getOwner().getPositionName(), true);
		}
		System.out.println(message);
	}

	public void resolveIntents() {
		if (pieceCount == playerIntents.size()) {
			// Remove/transform invalid intents
			for (int i = 0; i < playerIntents.size(); i++) {
				if (playerIntents.get(i).toString().equals("Move") && !validateMoveOrSupportIntent(playerIntents.get(i))) {
					Piece intentOwner = playerIntents.get(i).getOwner();
					playerIntents.set(i, new Hold(intentOwner));
				}
			}
			for (int i = 0; i < playerIntents.size(); i++) {
				if (playerIntents.get(i).toString().equals("Support") && !validateMoveOrSupportIntent(playerIntents.get(i))) {
					Piece intentOwner = playerIntents.get(i).getOwner();
					playerIntents.set(i, new Hold(intentOwner));
				}
			}
			List<List<Convoy>> convoys = new ArrayList<>(movesAndConvoys.values());
			for (int i = 0; i < playerIntents.size(); i++) {
				boolean convoyObsolete = true;
				if (playerIntents.get(i).toString().equals("Convoy")) {
					for (int j = 0; j < convoys.size(); j++) {
						if (convoys.get(j).contains(playerIntents.get(i))) {
							convoyObsolete = false;
						}
					}
					if (convoyObsolete) {
						Piece intentOwner = playerIntents.get(i).getOwner();
						playerIntents.set(i, new Hold(intentOwner));
					}
				}
			}
			// Change failed convoy moves to hold intents
			for (int i = 0; i < playerIntents.size(); i++) {
				if (playerIntents.get(i).toString().equals("Move")) {
					Move moveIntent = (Move) playerIntents.get(i);
					if (getMoveIntentStrength(moveIntent) == 0) {
						if (movesAndConvoys.containsKey(moveIntent)) {
							movesAndConvoys.remove(moveIntent);
						}
						playerIntents.set(i, new Hold(moveIntent.getOwner()));
					}
				}
			}
			// resolve remaining moveIntents using moveSuccessful
			successfulMoves = new LinkedHashMap<>();
			dislodgedPiecesAndForbiddenAreas = new LinkedHashMap<>();
			for (int i = 0; i < playerIntents.size(); i++) {
				if (playerIntents.get(i).toString().equals("Move")) {
					Move moveIntent = (Move) playerIntents.get(i);
					if (moveSuccessful(moveIntent, new ArrayList<Move>())) {
						successfulMoves.put(moveIntent.getDestinationName(), moveIntent.getOwner());
						if (map.getAreaByName(moveIntent.getDestinationName()).getOccupant() != null) {
							for (int j = 0; j < playerIntents.size(); j++) {
								if (playerIntents.get(j).getOwner() == map.getAreaByName(moveIntent.getDestinationName())
										.getOccupant()) {
									if (playerIntents.get(j).toString().equals("Move")) {
										Move escapingMoveIntent = (Move) playerIntents.get(j);
										if (!moveSuccessful(escapingMoveIntent, new ArrayList<Move>())) {
											dislodgedPiecesAndForbiddenAreas.put(
													map.getAreaByName(moveIntent.getDestinationName()).getOccupant(),
													moveIntent.getStartingPositionName());
										}
									} else {
										dislodgedPiecesAndForbiddenAreas.put(
												map.getAreaByName(moveIntent.getDestinationName()).getOccupant(),
												moveIntent.getStartingPositionName());
									}
									break;
								}
							}
						}
					}
				}
			}
			playerIntents = new ArrayList<>();
			movesAndConvoys = new LinkedHashMap<>();
		} else if (pieceCount < playerIntents.size()) {
			System.out.println("There are more intents than there are active pieces. Pieces: " + pieceCount
					+ ", intents: " + playerIntents.size());
			System.exit(0);
		}
	}

	public boolean resolveRetreatIntent(Move retreatIntent) {
		String destination = retreatIntent.getDestinationName();
		if (validateMoveOrSupportIntent(retreatIntent) && map.getAreaByName(destination).getOccupant() == null) {
			Piece retreatingPiece = retreatIntent.getOwner();
			map.removePieceFromArea(retreatingPiece);
			map.getAreaByName(destination).setOccupant(retreatingPiece);
			retreatingPiece.setPositionName(destination);
			System.out.println(retreatingPiece.getEmpire().getName() + ": " + retreatingPiece.toString() + " fled from "
					+ retreatIntent.getStartingPositionName() + " to " + destination);
			return true;
		}
		return false;
	}

	public void resolveDisbandIntent(Disband disbandIntent) {
		Piece disbandedPiece = disbandIntent.getOwner();
		String article = "an";
		if (disbandedPiece.toString().equals("Fleet")) {
			article = "a";
		}
		Empire empire = disbandedPiece.getEmpire();
		System.out.println(empire.getName() + " destroyed " + article + " " + disbandedPiece.toString() + " in " + disbandedPiece.getPositionName() + ".");
		map.removePieceFromArea(disbandedPiece);
		List<Piece> army = empire.getArmy();
		army.remove(disbandedPiece);
		empire.setArmy(army);
		vm.reset();
	}
	
	public void resolveBuildIntent(Empire empire, String pieceType, String positionName) throws IllegalArgumentException {
		Land position = (Land) map.getAreaByName(positionName);
		if (position.getOccupant() != null || (!pieceType.equals("Infantry") && !pieceType.equals("Fleet")) || (pieceType.equals("Fleet") && !Map.fleetRoutes.containsKey(positionName))) {
			throw new IllegalArgumentException();
		}
		List<Piece> army = empire.getArmy();
		String article = "an";
		if (pieceType.equals("Infantry")) {
			Infantry inf = new Infantry(positionName, empire);
			map.addPieceToArea(inf, positionName);
			army.add(inf);
			empire.setArmy(army);
		} else if (pieceType.equals("Fleet")) {
			Fleet fleet = new Fleet(positionName, empire);
			map.addPieceToArea(fleet, positionName);
			army.add(fleet);
			empire.setArmy(army);
			article = "a";
		}
		System.out.println(empire.getName() + " built " + article + " " + pieceType + " in " + positionName + ".");
		vm.reset();
	}

	private boolean moveSuccessful(Move moveIntent, List<Move> checkedMoveIntents) {
		if (checkedMoveIntents.contains(moveIntent)) {
			return false;
		}
		checkedMoveIntents.add(moveIntent);
		int moveIntentStrength = getMoveIntentStrength(moveIntent);
		String destinationName = moveIntent.getDestinationName();
		Area destination = map.getAreaByName(destinationName);
		if (moveIntentStrength == 0) {
			return false;
		} else if (destination.getOccupant() != null) {
			for (int i = 0; i < playerIntents.size(); i++) {
				Intent anotherIntent = playerIntents.get(i);
				if (map.getAreaByName(anotherIntent.getOwner().getPositionName()) == destination) {
					if (anotherIntent.toString().equals("Move")) {
						Move anotherMoveIntent = (Move) anotherIntent;
						if (map.getAreaByName(anotherMoveIntent.getDestinationName()) == map.getAreaByName(moveIntent.getStartingPositionName()) && !movesAndConvoys.containsKey(moveIntent) && !movesAndConvoys.containsKey(anotherMoveIntent) && moveIntentStrength <= getMoveIntentStrength(anotherMoveIntent)) {
							return false;
						}
						if (!moveSuccessful(anotherMoveIntent, checkedMoveIntents) && (moveIntentStrength < 2 || moveIntent.getOwner().getEmpire() == anotherIntent.getOwner().getEmpire())) {
							return false;
						}
					} else if (moveIntentStrength <= getHoldingPieceStrength(anotherIntent.getOwner()) || moveIntent.getOwner().getEmpire() == anotherIntent.getOwner().getEmpire()) {
						return false;
					}
					break;
				}
			}
		}
		List<Move> competingMoves = new ArrayList<>();
		for (int i = 0; i < playerIntents.size(); i++) {
			if (playerIntents.get(i).toString().equals("Move") && playerIntents.get(i) != moveIntent) {
				Move competingMoveIntent = (Move) playerIntents.get(i);
				if (map.getAreaByName(competingMoveIntent.getDestinationName()) == destination) {
					competingMoves.add(competingMoveIntent);
				}
			}
		}
		for (Move competingMove : competingMoves) {
			if (getMoveIntentStrength(competingMove) >= moveIntentStrength) {
				return false;
			}
		}
		return true;
	}

	private boolean convoyChainBroke(Move moveIntent) throws IllegalArgumentException {
		String empireName = moveIntent.getOwner().getEmpire().getName();
		if (movesAndConvoys.containsKey(moveIntent)) {
			// if convoy breaks, return 0;
			List<Convoy> convoys = movesAndConvoys.get(moveIntent);
			for (int i = 0; i < convoys.size(); i++) {
				int fleetStrength = getHoldingPieceStrength(convoys.get(i).getOwner());
				for (int j = 0; j < playerIntents.size(); j++) {
					if (playerIntents.get(j).toString().equals("Move")
							&& !playerIntents.get(j).getOwner().getEmpire().getName().equals(empireName)) {
						Move hostileMoveIntent = (Move) playerIntents.get(j);
						if (hostileMoveIntent.getDestinationName().equals(convoys.get(i).getOwner().getPositionName())
								&& getMoveIntentStrength(hostileMoveIntent) > fleetStrength) {
							return true;
						}
					}
				}
			}
			return false;
		} else {
			System.out.println("This move is not listed as convoy-dependent");
			throw new IllegalArgumentException();
		}

	}

	private boolean supportCancelled(Support supportIntent) {
		String position = supportIntent.getOwner().getPositionName();
		String empireName = supportIntent.getOwner().getEmpire().getName();
		for (int i = 0; i < playerIntents.size(); i++) {
			if (!playerIntents.get(i).getOwner().getEmpire().getName().equals(empireName)
					&& playerIntents.get(i).toString().equals("Move")) {
				Move hostileMoveIntent = (Move) playerIntents.get(i);
				if (hostileMoveIntent.getDestinationName().equals(position)) {
					int hostileMoveStrength = getMoveIntentStrength(hostileMoveIntent);
					if (movesAndConvoys.containsKey(hostileMoveIntent) && convoyChainBroke(hostileMoveIntent)) {
						continue;
					}
					if (supportIntent.getSupportedIntent().toString().equals("Move")) {
						Move supportedMoveIntent = (Move) supportIntent.getSupportedIntent();
						if (!supportedMoveIntent.getDestinationName().equals(hostileMoveIntent.getStartingPositionName())
								|| (movesAndConvoys.containsKey(supportedMoveIntent) && convoyChainBroke(supportedMoveIntent))
								|| getHoldingPieceStrength(supportIntent.getOwner()) < hostileMoveStrength) {
							// return true if supportIntent owner gets dislodged
							return true;
						}
					} else {
						return true;
					}
				}
			}
		}
		return false;
	}

	private int getMoveIntentStrength(Move moveIntent) {
		int strength = 1;
		if (movesAndConvoys.containsKey(moveIntent) && convoyChainBroke(moveIntent)) {
			// if convoy breaks, return 0;
			return 0;
		}
		for (int i = 0; i < playerIntents.size(); i++) {
			if (playerIntents.get(i).toString().equals("Support")) {
				Support supportIntent = (Support) playerIntents.get(i);
				if (supportIntent.getSupportedIntent().toString().equals("Move")) {
					Move supportedMove = (Move) supportIntent.getSupportedIntent();
					if (moveIntent.isIdenticalWith(supportedMove) && !supportCancelled(supportIntent)) {
						strength++;
					}
				}
			}
		}
		return strength;
	}

	private int getHoldingPieceStrength(Piece piece) {
		if (piece == null) {
			return 0;
		}
		int strength = 1;
		for (int i = 0; i < playerIntents.size(); i++) {
			if (playerIntents.get(i).toString().equals("Support")) {
				Support supportIntent = (Support) playerIntents.get(i);
				if (supportIntent.getSupportedIntent().toString().equals("Hold") && supportIntent.getSupportedIntent().getOwner() == piece && !supportCancelled(supportIntent)) {
					strength++;
				}
			}
		}
		return strength;
	}

	private boolean validateMoveOrSupportIntent(Intent intent) {
		if (intent.toString().equals("Move") && intent.getOwner().toString().equals("Infantry")) {
			Move moveIntent = (Move) intent;
			String destination = moveIntent.getDestinationName();
			if (!Map.neighboringAreas.get(moveIntent.getStartingPositionName()).contains(destination)) {
				List<Convoy> convoys = new ArrayList<>();
				for (Intent item : playerIntents) {
					if (item.toString().equals("Convoy")) {
						Convoy convoy = (Convoy) item;
						if (convoy.getMove().isIdenticalWith(moveIntent)) {
							convoys.add(convoy);
						}
					}
				}
				if (!verifyConvoy(convoys, moveIntent.getOwner().getPositionName(), destination, moveIntent, new ArrayList<Convoy>())) {
					return false;
				}
			}
		} else if (intent.toString().equals("Support")) {
			Support supportIntent = (Support) intent;
			if (supportIntent.getSupportedIntent().toString().equals("Move")) {
				Move supportedMove = (Move) supportIntent.getSupportedIntent();
				for (Intent item : playerIntents) {
					if (item.toString().equals("Move")) {
						Move anotherMove = (Move) item;
						if (supportedMove.isIdenticalWith(anotherMove)) {
							return true;
						}
					}
				}
				return false;
			}
		}
		return true;
	}

	private boolean verifyConvoy(List<Convoy> usableConvoys, String currentPosName, String destinationName,
			Move moveIntent, List<Convoy> usedConvoys) {
		Area currentPos = map.getAreaByName(currentPosName);
		if (currentPos.toString().equals("Land")) {
			Land landArea = (Land) currentPos;
			if (landArea.hasCoasts()) {
				String[] coastNames = landArea.getCoasts();
				for (String coastName : coastNames) {
					for (Convoy convoy : usableConvoys) {
						String convoyPosName = convoy.getOwner().getPositionName();
						if (Map.fleetRoutes.get(coastName).contains(convoyPosName)) {
							usedConvoys.add(convoy);
							if (verifyConvoy(usableConvoys, convoyPosName, destinationName, moveIntent, usedConvoys)) {
								return true;
							}
							usedConvoys.remove(convoy);
						}
					}
				}
			} else {
				for (Convoy convoy : usableConvoys) {
					String convoyPosName = convoy.getOwner().getPositionName();
					if (Map.fleetRoutes.get(currentPosName).contains(convoyPosName)) {
						usedConvoys.add(convoy);
						if (verifyConvoy(usableConvoys, convoyPosName, destinationName, moveIntent, usedConvoys)) {
							return true;
						}
						usedConvoys.remove(convoy);
					}
				}
			}
		} else {
			Land destination = (Land) map.getAreaByName(destinationName);
			if (destination.hasCoasts()) {
				String[] coastNames = destination.getCoasts();
				for (String coastName : coastNames) {
					if (Map.fleetRoutes.get(currentPosName).contains(coastName)) {
						movesAndConvoys.put(moveIntent, usedConvoys);
						return true;
					}
				}
			} else {
				if (Map.fleetRoutes.get(currentPosName).contains(destinationName)) {
					movesAndConvoys.put(moveIntent, usedConvoys);
					return true;
				}
			}
			for (Convoy convoy : usableConvoys) {
				String convoyPosName = convoy.getOwner().getPositionName();
				if (Map.fleetRoutes.get(currentPosName).contains(convoyPosName) && !usedConvoys.contains(convoy)) {
					usedConvoys.add(convoy);
					if (verifyConvoy(usableConvoys, convoyPosName, destinationName, moveIntent, usedConvoys)) {
						return true;
					}
					usedConvoys.remove(convoy);
				}
			}
		}
		return false;
	}

	public void updatePieceCount(List<Empire> empires) {
		pieceCount = 0;
		for (Empire empire : empires) {
			pieceCount += empire.getArmy().size();
		}
	}

	public VisualMap getVM() {
		return vm;
	}
	
	public void setVM(VisualMap vm) {
		this.vm = vm;
	}
	
	public Map getMap() {
		return map;
	}

}
