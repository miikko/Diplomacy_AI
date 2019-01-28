package players;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Random;

import board.Area;
import board.Land;
import board.Map;
import controllers.IntentController;
import controllers.TurnController;
import intents.Hold;
import intents.Intent;
import intents.Move;
import intents.Support;
import pieces.Fleet;
import pieces.Infantry;
import pieces.Piece;

public class Brain {

	private final String[] allEmpireNames = { "Austria", "England", "France", "Germany", "Italy", "Russia", "Turkey" };
	private String filePath = "weights/empire_";
	//private LinkedHashMap<String, Double> areaWeights;
	private List<Double> moveWeights = new ArrayList<>();
	private List<Double> holdWeights = new ArrayList<>();
	private List<Double> supportWeights = new ArrayList<>();
	private Empire empire;
	private LinkedHashMap<String, Intent[]> seasonalGivenIntents = new LinkedHashMap<>();
	private final double LEARN_RATE = 0.03;

	public Brain(Empire empire) {
		this.empire = empire;
		filePath += empire.getName() + ".txt";
		if (createInitialWeights()) {
			System.out.println("Weights created");
		}
		readWeights();
	}

	private boolean createInitialWeights() {
		File f = new File(filePath);
		PrintWriter pw = null;
		if (f.isFile()) {
			return false;
		}
		try {
			pw = new PrintWriter(f);
			pw.println("Move");
			pw.println("c:0.32");
			pw.println("p:0.17");
			pw.println("d:0.20");
			pw.println("g:0.78");
			pw.println("t:0.16");
			pw.println("Hold");
			pw.println("c:0.34");
			pw.println("e:0.17");
			pw.println("o:0.97");
			pw.println("g:0.65");
			pw.println("t:0.18");
			pw.println("Support");
			pw.println("g:0.58");
			pw.println("t:0.18");
			pw.println("c:0.86");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return false;
		} finally {
			if (pw != null) {
				pw.close();
			}
		}
		return true;
	}

	private boolean readWeights() {
		FileReader fr = null;
		BufferedReader br = null;
		try {
			fr = new FileReader(filePath);
			br = new BufferedReader(fr);
			String currentLine;
			String currIntentType = "Move";
			while ((currentLine = br.readLine()) != null) {
				if (currentLine.contains(":")) {
					if (currIntentType.equals("Move")) {
						moveWeights.add(Double.parseDouble(currentLine.split(":")[1]));
					} else if (currIntentType.equals("Hold")) {
						holdWeights.add(Double.parseDouble(currentLine.split(":")[1]));
					} else if (currIntentType.equals("Support")) {
						supportWeights.add(Double.parseDouble(currentLine.split(":")[1]));
					}
				} else {
					currIntentType = currentLine;
				}
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return false;
		} catch (IOException ioE) {
			ioE.printStackTrace();
			return false;
		} finally {
			try {
				if (fr != null) {
					fr.close();
				}
				if (br != null) {
					br.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
				return false;
			}
		}
		return true;
	}
	
	private boolean modifyWeights(List<Double> weights, double howMuch) {
		File f = new File(filePath);
		PrintWriter pw = null;
		Random rand = new Random();
		try {
			pw = new PrintWriter(f);
			for (int i = 0; i < weights.size(); i++) {
				boolean increment = rand.nextBoolean();
				if (increment) {
					weights.set(i, weights.get(i) + howMuch);
				} else {
					weights.set(i, weights.get(i) - howMuch);
				}
			}
			pw.println("Move");
			pw.println("c:" + moveWeights.get(0));
			pw.println("p:" + moveWeights.get(1));
			pw.println("d:" + moveWeights.get(2));
			pw.println("g:" + moveWeights.get(3));
			pw.println("t:" + moveWeights.get(4));
			pw.println("Hold");
			pw.println("c:" + holdWeights.get(0));
			pw.println("e:" + holdWeights.get(1));
			pw.println("o:" + holdWeights.get(2));
			pw.println("g:" + holdWeights.get(3));
			pw.println("t:" + holdWeights.get(4));
			pw.println("Support");
			pw.println("g:" + supportWeights.get(0));
			pw.println("t:" + supportWeights.get(1));
			pw.println("c:" + supportWeights.get(2));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return false;
		} finally {
			if (pw != null) {
				pw.close();
			}
		}
		return true;
	}

	// Outputs:
	// List of nodes with Intents and areas
	// Does not use convoys yet
	public Intent[] calculateOptimalIntents(Map map) {
		List<Area> mapState = map.areas;
		List<Piece> army = empire.getArmy();
		Intent[] optimalIntents = new Intent[army.size()];
		List<Intent> givenIntents = new ArrayList<>();
		List<String> empireNamesWithoutThis = new ArrayList<>(Arrays.asList(allEmpireNames));
		empireNamesWithoutThis.remove(empire.getName());
		List<String> justThisEmpireName = new ArrayList<>();
		justThisEmpireName.add(empire.getName());
		int t = 0;
		switch (TurnController.season) {
		case "Winter":
			t = 0;
			break;
		case "Spring":
			t = 2;
			break;
		case "Fall":
			t = 1;
			break;
		default:
			break;
		}
		for (int i = 0; i < army.size(); i++) {
			List<String> movableAreaNames = new ArrayList<>();
			Piece piece = army.get(i);
			if (piece.toString().equals("Infantry")) {
				Infantry inf = (Infantry) army.get(i);
				movableAreaNames = Map.neighboringAreas.get(piece.getPositionName());
			} else {
				movableAreaNames = Map.fleetRoutes.get(piece.getPositionName());
				Fleet fleet = (Fleet) army.get(i);
			}
			// Hold value
			int cHold = 0;
			if (map.getAreaByName(piece.getPositionName()).toString().equals("Land")) {
				Land landArea = (Land) map.getAreaByName(piece.getPositionName());
				if (landArea.containsCenter()) {
					cHold = 1;
				}
			}
			int eHold = map.getPieceCountInAreaVicinity(piece.getPositionName(), empireNamesWithoutThis);
			int oHold = map.getPieceCountInAreaVicinity(piece.getPositionName(), justThisEmpireName);
			int gHold = 0;
			for (Intent givenIntent : givenIntents) {
				if (givenIntent.toString().equals("Support")) {
					Support givenSupportIntent = (Support) givenIntent;
					if (givenSupportIntent.getSupportedIntent().toString().equals("Hold")) {
						Hold supportedIntent = (Hold) givenSupportIntent.getSupportedIntent();
						if (supportedIntent.getOwner() == piece) {
							gHold++;
						}
					}
				}
			}
			String intentType = "Hold";
			String startingPosName = piece.getPositionName();
			String destinationName = piece.getPositionName();
			double value = hNeuron(cHold, eHold, oHold, gHold, t);
			for (String areaName : movableAreaNames) {
				// mNeuron
				int c = 0;
				if (map.getAreaByName(areaName).toString().equals("Land")) {
					Land land = (Land) map.getAreaByName(areaName);
					if (land.containsCenter()) {
						String actualAreaName = areaName;
						if (areaName.contains("-")) {
							actualAreaName = areaName.split("-")[0];
						}
						if (empire.getOwnedCenterNames().contains(actualAreaName)) {
							c = 1;
						} else {
							c = 2;
						}
					}
				}
				int p = 0;
				if (map.getAreaByName(areaName).getOccupant() != null && map.getAreaByName(areaName).getOccupant().getEmpire() == empire) {
					p = 1;
				}
				int d = map.getPieceCountInAreaVicinity(areaName, justThisEmpireName) - map.getPieceCountInAreaVicinity(areaName, empireNamesWithoutThis);
				int g = 0;
				for (Intent givenIntent : givenIntents) {
					if (givenIntent.toString().equals("Support")) {
						Support givenSupportIntent = (Support) givenIntent;
						if (givenSupportIntent.getSupportedIntent().toString().equals("Move")) {
							Move supportedMoveIntent = (Move) givenSupportIntent.getSupportedIntent();
							if (supportedMoveIntent.getOwner() == piece
									&& supportedMoveIntent.getStartingPositionName().equals(piece.getPositionName())
									&& supportedMoveIntent.getDestinationName().equals(areaName)) {
								g++;
							}
						}
					}
				}
				if (mNeuron(c, p, d, g, t) > value) {
					value = mNeuron(c, p, d, g, t);
					intentType = "Move";
					startingPosName = piece.getPositionName();
					destinationName = areaName;
				}
				// sNeuron
				g = 0;
				String potentialStartingPosName = "";
				for (Intent givenIntent : givenIntents) {
					if (givenIntent.toString().equals("Move")) {
						Move moveIntent = (Move) givenIntent;
						if (moveIntent.getDestinationName().equals(areaName)) {
							potentialStartingPosName = givenIntent.getOwner().getPositionName();
							g++;
							break;
						}
					} else if (givenIntent.toString().equals("Hold")
							&& givenIntent.getOwner().getPositionName().equals(areaName)) {
						potentialStartingPosName = areaName;
						g++;
						break;
					}
				}
				c = 1;
				if (map.getPieceCountInAreaVicinity(piece.getPositionName(), empireNamesWithoutThis) > 0) {
					c = 0;
				}
				if (sNeuron(g, t, c) > value) {
					if (potentialStartingPosName.equals("")) {
						System.out.println(
								"Brain chose a support intent even though there were no potential supportable pieces.");
					} else {
						value = sNeuron(g, t, c);
						intentType = "Support";
						startingPosName = potentialStartingPosName;
						destinationName = areaName;
					}
				}
				// cNeuron
				if (piece.toString().equals("Fleet")) {

				}
			}
			switch (intentType) {
			case "Hold":
				Hold holdIntent = new Hold(piece);
				givenIntents.add(holdIntent);
				optimalIntents[i] = holdIntent;
				break;
			case "Move":
				Move moveIntent = new Move(piece, startingPosName, destinationName);
				givenIntents.add(moveIntent);
				optimalIntents[i] = moveIntent;
				break;
			case "Support":
				Support supportIntent;
				if (startingPosName.equals(destinationName)) {
					supportIntent = new Support(piece, new Hold(map.getAreaByName(startingPosName).getOccupant()));
				} else {
					supportIntent = new Support(piece, new Move(map.getAreaByName(startingPosName).getOccupant(),
							startingPosName, destinationName));
				}
				givenIntents.add(supportIntent);
				optimalIntents[i] = supportIntent;
				break;
			default:
				break;
			}
		}
		
		seasonalGivenIntents.put(TurnController.season, optimalIntents);
		return optimalIntents;
	}
	
	public void train() {
		modifyWeights(moveWeights, LEARN_RATE);
		modifyWeights(holdWeights, LEARN_RATE);
		modifyWeights(supportWeights, LEARN_RATE);
	}
	
	//Variables:
	//-season
	//-year
	//-army size/number of controlled centers
	//-army positioning
	//-army composition
	//-number of enemy players alive
	//-enemy army sizes/number of enemy controlled centers
	//-enemies army positioning
	//-enemies army composition
	//-names of controlled centers
	//-names of centers under enemy control
	//-names of unclaimed centers


	// Inputs:
	// c: Area doesn't contain a center: 0, Area contains owned center: 1, not owned center: 2
	// p: Area is occupied by a friendly piece: 1 yes, 0 no
	// d: difference in potential strengths
	// g: Guaranteed own strength in area
	// t: Turns until winter comes

	private double mNeuron(int c, int p, int d, int g, int t) {
		double cWeight = moveWeights.get(0);
		double pWeight = moveWeights.get(1);
		double dWeight = moveWeights.get(2);
		double gWeight = moveWeights.get(3);
		double tWeight = moveWeights.get(4);
		double value = cWeight * c + p * pWeight + dWeight * d + gWeight * g + tWeight * t;
		/*
		if (value < 0) {
			value = 0;
		}*/
		return sigmoid(value);
	}

	// Inputs:
	// c: Area contains center: 1 yes, 0 no
	// e: Potential max enemy strength in area
	// o: Potential max own strength in area
	// g: Guaranteed own strength in area
	// t: Turns until winter comes
	private double hNeuron(int c, int e, int o, int g, int t) {
		double cWeight = holdWeights.get(0);
		double eWeight = holdWeights.get(1);
		double oWeight = holdWeights.get(2);
		double gWeight = holdWeights.get(3);
		double tWeight = holdWeights.get(4);
		double value = cWeight * c + eWeight * e + oWeight * o + gWeight * g + tWeight * t;
		/*
		if (value < 0) {
			value = 0;
		}*/
		return sigmoid(value);
	}

	// Inputs:
	// g: Guaranteed own strength in area
	// t: Turns until winter comes
	// c: support can be cancelled: 0 yes, 1 no
	private double sNeuron(int g, int t, int c) {
		double gWeight = supportWeights.get(0);
		double tWeight = supportWeights.get(1);
		double cWeight = supportWeights.get(2);
		double value = cWeight * c + gWeight * g + tWeight * t;
		/*
		if (value < 0) {
			value = 0;
		}*/
		return sigmoid(value);
	}

	// Inputs:
	// c: Can convoy infantry: 1 yes, -1 no
	// m: Convoyable move given: 1 yes, -1 no
	private double cNeuron(int c, int m) {
		return 0;
	}
	
	private double sigmoid(double x) {
		return 1 / (1 + Math.exp(-x));
	}

/*
	private boolean createInitialAreaWeights() {
		File f = new File(filePath);
		PrintWriter pw = null;
		if (f.isFile()) {
			return false;
		}
		try {
			pw = new PrintWriter(f);
			for (Area area : Map.areas) {
				double weight = 1;
				if (area.toString().equals("Land")) {
					Land land = (Land) area;
					if (land.containsCenter()) {
						weight = 2;
					}
				}
				pw.println(area.getName() + ":" + weight);
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return false;
		} finally {
			if (pw != null) {
				pw.close();
			}
		}
		return true;
	}

	private LinkedHashMap<String, Double> readAreaWeights() {
		LinkedHashMap<String, Double> areaWeights = new LinkedHashMap<>();
		FileReader fr = null;
		BufferedReader br = null;
		try {
			fr = new FileReader(filePath);
			br = new BufferedReader(fr);
			String currentLine;
			while ((currentLine = br.readLine()) != null) {
				String areaName = currentLine.split(":")[0];
				Double weight = Double.parseDouble(currentLine.split(":")[1]);
				areaWeights.put(areaName, weight);
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException ioE) {
			ioE.printStackTrace();
		} finally {
			try {
				if (fr != null) {
					fr.close();
				}
				if (br != null) {
					br.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return areaWeights;
	}

	// Give a value to each movable area including the pieces current position
	// Each area has a default value.
	// Subtract from the value if there are enemies in the area's vicinity
	// Add to the value if there are friendly pieces in the area's vicinity
	// The amount added is subtracted from if the friendly piece's optimal intent is
	// a Move intent. This is referred to as the cost
	// The cost is the value of the supporting pieces best Move intent's value
	// Choose the area with the highest value and give intents accordingly
	
	public Intent[] areaBasedAlgorithm() {
		List<Piece> army = empire.getArmy();
		List<Piece> usablePieces = new ArrayList<>(army);
		List<String> empireNamesWoThis = Arrays.asList(allEmpireNames);
		empireNamesWoThis.remove(empire.getName());
		Intent[] optimalIntents = new Intent[army.size()];
		// Find possible intent plans for each piece
		LinkedHashMap<Piece, List<IntentPlan>> intentPlans = new LinkedHashMap<>();
		for (Piece piece : army) {
			List<String> movableAreaNames = new ArrayList<>();
			if (piece.toString().equals("Infantry")) {
				movableAreaNames = Map.neighboringAreas.get(piece.getPositionName());
			} else {
				movableAreaNames = Map.fleetRoutes.get(piece.getPositionName());
			}
			// Remove areas that are occupied by a friendly piece
			for (int i = 0; i < movableAreaNames.size(); i++) {
				String areaName = movableAreaNames.get(i);
				Area area = Map.getAreaByName(areaName);
				if (area.getOccupant() != null && area.getOccupant().getEmpire() == empire) {
					movableAreaNames.remove(i);
					i--;
				}
			}
			// Add the currently occupied area to the list
			movableAreaNames.add(piece.getPositionName());
			// Choose one area for each piece
			for (String areaName : movableAreaNames) {
				// Remove coast endings from area names
				String actualAreaName = areaName;
				if (areaName.contains("-")) {
					actualAreaName = areaName.split("-")[0];
				}
				// Get enemy power
				int ePresenceInVicinity = Map.getPieceCountInAreaVicinity(areaName, empireNamesWoThis);
				// Add intent plan to hash map
				List<Piece> otherOwnPiecesInVicinity = Map.getPiecesInAreaVicinity(areaName, empire.getName());
				otherOwnPiecesInVicinity.remove(piece);
				if (intentPlans.get(piece) == null) {
					intentPlans.put(piece, new ArrayList<IntentPlan>());
				}
				intentPlans.get(piece)
						.add(new IntentPlan(piece, areaName, areaWeights.get(actualAreaName) - ePresenceInVicinity,
								otherOwnPiecesInVicinity, ePresenceInVicinity));

			}
		}
		// Choose the optimal intent plan for each piece
		for (Piece piece : army) {
			List<IntentPlan> plans = intentPlans.get(piece);
			IntentPlan bestIntentPlan = null;
			for (IntentPlan plan : plans) {
				int piecesNeeded = plan.getReqSupportStr();
				while (piecesNeeded > 0) {
					
				}
			}
		}
		return optimalIntents;
	}*/
	
}
