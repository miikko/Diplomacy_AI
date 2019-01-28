package players;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Random;

import board.Area;
import board.Land;
import board.Map;
import controllers.IntentController;
import intents.Disband;
import intents.Intent;
import intents.Move;
import pieces.Fleet;
import pieces.Piece;

//Bugs:


//Inputs given to the perceptron:
//
public class PlayerAI {
	private Empire empire;
	private List<Piece> army;
	private Random random;
	private IntentController ic;
	private Brain brain;
	private Map map;

	public PlayerAI(Empire empire, IntentController ic) {
		this.empire = empire;
		this.ic = ic;
		army = empire.getArmy();
		brain = new Brain(empire);
		random = new Random();
		map = ic.getMap();
	}

	public void assignNormalIntents() {
		Intent[] optimalIntents = brain.calculateOptimalIntents(map);
		for (Intent intent : optimalIntents) {
			ic.addIntent(intent);
		}
		/*
		for (Piece piece : army) {
			List<Integer> forbiddenIntentTypes = new ArrayList<>();
			int intentType = random.nextInt(3);
			outerloop: while (true) {
				while (forbiddenIntentTypes.contains(intentType)) {
					intentType = random.nextInt(3);
				}
				switch (intentType) {
				case 0:
					// Move
					int index = random.nextInt(Map.allAreaNames.size());
					List<Integer> forbiddenIndexes = new ArrayList<>();
					while (!piece.movePiece(Map.allAreaNames.get(index), ic)) {
						forbiddenIndexes.add(index);
						while (forbiddenIndexes.contains(index)) {
							index = random.nextInt(Map.allAreaNames.size());
						}
					}
					break outerloop;
				case 1:
					// Support
					int startingPosIndex = random.nextInt(Map.allAreaNames.size());
					int destinationIndex = random.nextInt(Map.allAreaNames.size());
					List<Integer> forbiddenStartingIndexes = new ArrayList<>();
					while (!piece.supportPiece(Map.allAreaNames.get(startingPosIndex),
							Map.allAreaNames.get(destinationIndex), ic)) {
						List<Integer> forbiddenDestinationIndexes = new ArrayList<>();
						boolean viableParameters = true;
						while (!piece.supportPiece(Map.allAreaNames.get(startingPosIndex),
								Map.allAreaNames.get(destinationIndex), ic)) {
							forbiddenDestinationIndexes.add(destinationIndex);
							if (forbiddenDestinationIndexes.size() == Map.allAreaNames.size()) {
								viableParameters = false;
								break;
							}
							while (forbiddenDestinationIndexes.contains(destinationIndex)) {
								destinationIndex = random.nextInt(Map.allAreaNames.size());
							}
						}
						if (viableParameters) {
							break outerloop;
						}
						forbiddenStartingIndexes.add(startingPosIndex);
						// Cant support
						if (forbiddenStartingIndexes.size() == Map.allAreaNames.size()) {
							forbiddenIntentTypes.add(intentType);
							continue outerloop;
						}
						while (forbiddenStartingIndexes.contains(startingPosIndex)) {
							startingPosIndex = random.nextInt(Map.allAreaNames.size());
						}
					}
					break outerloop;
				// convoy
				case 2:
					if (piece.toString().equals("Infantry")) {
						forbiddenIntentTypes.add(intentType);
						continue outerloop;
					}
					Fleet fleet = (Fleet) piece;
					int convoyStartIndex = random.nextInt(Map.allAreaNames.size());
					int convoyEndIndex = random.nextInt(Map.allAreaNames.size());
					List<Integer> forbiddenStartIndexes = new ArrayList<>();
					while (!fleet.convoyInfantry(Map.allAreaNames.get(convoyStartIndex),
							Map.allAreaNames.get(convoyEndIndex), ic)) {
						List<Integer> forbiddenDestinationIndexes = new ArrayList<>();
						boolean viableParameters = true;
						while (!fleet.convoyInfantry(Map.allAreaNames.get(convoyStartIndex),
								Map.allAreaNames.get(convoyEndIndex), ic)) {
							forbiddenDestinationIndexes.add(convoyEndIndex);
							if (forbiddenDestinationIndexes.size() == Map.allAreaNames.size()) {
								viableParameters = false;
								break;
							}
							while (forbiddenDestinationIndexes.contains(convoyEndIndex)) {
								convoyEndIndex = random.nextInt(Map.allAreaNames.size());
							}
						}
						if (viableParameters) {
							break outerloop;
						}
						forbiddenStartIndexes.add(convoyStartIndex);
						// Cant convoy
						if (forbiddenStartIndexes.size() == Map.allAreaNames.size()) {
							forbiddenIntentTypes.add(intentType);
							continue outerloop;
						}
						while (forbiddenStartIndexes.contains(convoyStartIndex)) {
							convoyStartIndex = random.nextInt(Map.allAreaNames.size());
						}
					}
					break outerloop;
				default:
					break;
				}
			}
		}
*/
	}

	// either move or destroy
	public void assignRetreatIntents(Piece dislodgedPiece, String forbiddenAreaName) throws IllegalArgumentException {
		String position = dislodgedPiece.getPositionName();
		List<String> possibleEscapeAreaNames;
		if (dislodgedPiece.toString().equals("Fleet")) {
			if (!Map.fleetRoutes.containsKey(dislodgedPiece.getPositionName())) {
				throw new IllegalArgumentException();
			}
			possibleEscapeAreaNames = Map.fleetRoutes.get(position);
		} else {
			if (!Map.neighboringAreas.containsKey(dislodgedPiece.getPositionName())) {
				throw new IllegalArgumentException();
			}
			possibleEscapeAreaNames = Map.neighboringAreas.get(position);
		}
		possibleEscapeAreaNames.remove(forbiddenAreaName);
		if (possibleEscapeAreaNames.size() == 0) {
			ic.resolveDisbandIntent(new Disband(dislodgedPiece));
		} else {
			int index = random.nextInt(possibleEscapeAreaNames.size());
			Move retreatIntent = new Move(dislodgedPiece, position, possibleEscapeAreaNames.get(index));
			List<Integer> forbiddenIndexes = new ArrayList<>();
			while (!ic.resolveRetreatIntent(retreatIntent)) {
				forbiddenIndexes.add(index);
				if (forbiddenIndexes.size() == possibleEscapeAreaNames.size()) {
					ic.resolveDisbandIntent(new Disband(dislodgedPiece));
					break;
				}
				while (forbiddenIndexes.contains(index)) {
					index = random.nextInt(possibleEscapeAreaNames.size());
				}
				retreatIntent = new Move(dislodgedPiece, position, possibleEscapeAreaNames.get(index));
			}
		}
	}
	
	public void assignWinterIntents() {
		List<String> ownedCenterNames = empire.getOwnedCenterNames();
		List<String> homeCenterNames = empire.getHomeCenterNames();
		List<Piece> army = empire.getArmy();
		while (army.size() > ownedCenterNames.size()) {
			int disbandPieceIndex = random.nextInt(army.size());
			ic.resolveDisbandIntent(new Disband(army.get(disbandPieceIndex)));
			army = empire.getArmy();
		}
		while (army.size() < ownedCenterNames.size()) {
			List<String> viableHomeCenterNames = new ArrayList<>();
			for (int i = 0; i < homeCenterNames.size(); i++) {
				String homeCenterName = homeCenterNames.get(i);
				Area homeCenterArea = map.getAreaByName(homeCenterName);
				if (ownedCenterNames.contains(homeCenterName) && homeCenterArea.getOccupant() == null) {
					viableHomeCenterNames.add(homeCenterName);
				}
			}
			if (viableHomeCenterNames.size() == 0) {
				break;
			} else {
				int homeCenterNameIndex = random.nextInt(viableHomeCenterNames.size());
				int pieceType = random.nextInt(2);
				//Infantry
				if (pieceType == 0) {
					ic.resolveBuildIntent(empire, "Infantry", viableHomeCenterNames.get(homeCenterNameIndex));
				} else {
					//Fleet
					Land landArea = (Land) map.getAreaByName(viableHomeCenterNames.get(homeCenterNameIndex));
					String areaName = viableHomeCenterNames.get(homeCenterNameIndex);
					if (landArea.hasCoasts()) {
						int coastIndex = random.nextInt(landArea.getCoasts().length);
						areaName = landArea.getCoasts()[coastIndex];
					}
					if (Map.fleetRoutes.containsKey(areaName)) {
						ic.resolveBuildIntent(empire, "Fleet", areaName);
					}
				}
			}
			army = empire.getArmy();
		}
	}

	public Empire getEmpire() {
		return empire;
	}
	
	public Brain getBrain() {
		return brain;
	}
}
