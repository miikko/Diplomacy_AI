package board;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;

import pieces.Piece;

public class Map {

	public List<Area> areas = new ArrayList<Area>();
	public static List<String> coasts = new ArrayList<String>();
	public static List<String> allAreaNames = new ArrayList<String>();
	public static LinkedHashMap<String, List<String>> neighboringAreas = new LinkedHashMap<>();
	public static LinkedHashMap<String, List<String>> oceanCoastConnections = new LinkedHashMap<>();
	public static LinkedHashMap<String, List<String>> fleetRoutes = new LinkedHashMap<>();

	public Map() {
		createMap();
		createCoasts();
		fillNeighboringAreas();
		for (Area area : areas) {
			allAreaNames.add(area.getName());
		}
		allAreaNames.addAll(coasts);
	}

	public Area getAreaByName(String areaName) throws IllegalArgumentException {
		String copy = areaName;
		if (copy.contains("-")) {
			copy = areaName.split("-")[0];
		}
		for (int i = 0; i < areas.size(); i++) {
			if (areas.get(i).getName().equals(copy)) {
				return areas.get(i);
			}
		}
		System.out.println("No area with the given name exist in the map. Name: " + areaName);
		throw new IllegalArgumentException();
	}

	public boolean addPieceToArea(Piece piece, String areaName) throws IllegalArgumentException {
		Area area = getAreaByName(areaName);
		if (piece.toString().equals("Infantry") && area.toString().equals("Ocean")) {
			System.out.println("Tried to add an Infantry to an Ocean area. Area: " + areaName);
			throw new IllegalArgumentException();
		} else if (piece.toString().equals("Fleet") && !fleetRoutes.containsKey(areaName)) {
			System.out.println("Tried to add a fleet to a mainland area. Area: " + areaName);
			throw new IllegalArgumentException();
		}

		if (area.getOccupant() != null) {
			return false;
		} else {
			area.setOccupant(piece);
			return true;
		}
	}

	public void removePieceFromArea(Piece piece) {
		Area area = getAreaByName(piece.getPositionName());
		if (area.getOccupant() == piece) {
			area.setOccupant(null);
		}
	}
/*
 * Stupid function for this specific map, since in any area the distance to a center is either 0 or 1
	public static List<String> getPathToClosestCenter(Piece piece, List<String> path, String currPosName) {
		Area currArea = getAreaByName(currPosName);
		if (currArea.toString().equals("Land")) {
			Land landArea = (Land) currArea;
			if (landArea.containsCenter()) {
				List<String> thisPath = new ArrayList<>(path);
				return thisPath;
			}
		}
		List<String> movableAreaNames = new ArrayList<>();
		if (piece.toString().equals("Infantry")) {
			movableAreaNames = new ArrayList<>(neighboringAreas.keySet());
			movableAreaNames.remove(currPosName);
		} else {
			movableAreaNames = fleetRoutes.get(currPosName);
		}
		List<List<String>> paths = new ArrayList<>();
		paths.add(path);
		for (String movableAreaName : movableAreaNames) {
			if (movePossible(piece, movableAreaName) && !path.contains(movableAreaName)) {
				path.add(movableAreaName);
				List<String> alternatePath = getPathToClosestCenter(piece, path, movableAreaName);
				paths.add(alternatePath);
				path.remove(movableAreaName);
			}
		}
		path = paths.get(0);
		for (List<String> list : paths) {
			if (list.size() < path.size()) {
				path = list;
			}
		}
		return path;
	}*/

	public int getPieceCountInAreaVicinity(String areaName, List<String> empireNames) {
		int pieceCount = 0;
		List<Area> checkedAreas = new ArrayList<>();
		if (neighboringAreas.containsKey(areaName)) {
			List<String> adjInfAreaNames = neighboringAreas.get(areaName);
			for (String adjInfAreaName : adjInfAreaNames) {
				Area adjInfArea = getAreaByName(adjInfAreaName);
				if (!checkedAreas.contains(adjInfArea)) {
					checkedAreas.add(adjInfArea);
					if (adjInfArea.getOccupant() != null
							&& empireNames.contains(adjInfArea.getOccupant().getEmpire().getName())) {
						pieceCount++;
					}
				}
			}
		}
		if (fleetRoutes.containsKey(areaName)) {
			List<String> adjFleetAreaNames = fleetRoutes.get(areaName);
			for (String adjFleetAreaName : adjFleetAreaNames) {
				Area adjFleetArea = getAreaByName(adjFleetAreaName);
				if (!checkedAreas.contains(adjFleetArea)) {
					checkedAreas.add(adjFleetArea);
					if (adjFleetArea.getOccupant() != null
							&& empireNames.contains(adjFleetArea.getOccupant().getEmpire().getName())) {
						pieceCount++;
					}
				}
			}
		}
		return pieceCount;
	}

	public int getPieceCountInAreaVicinity(String areaName, String empireName) {
		int pieceCount = 0;
		List<Area> checkedAreas = new ArrayList<>();
		if (neighboringAreas.containsKey(areaName)) {
			List<String> adjInfAreaNames = neighboringAreas.get(areaName);
			for (String adjInfAreaName : adjInfAreaNames) {
				Area adjInfArea = getAreaByName(adjInfAreaName);
				if (!checkedAreas.contains(adjInfArea)) {
					checkedAreas.add(adjInfArea);
					if (adjInfArea.getOccupant() != null
							&& empireName.equals(adjInfArea.getOccupant().getEmpire().getName())) {
						pieceCount++;
					}
				}
			}
		}
		if (fleetRoutes.containsKey(areaName)) {
			List<String> adjFleetAreaNames = fleetRoutes.get(areaName);
			for (String adjFleetAreaName : adjFleetAreaNames) {
				Area adjFleetArea = getAreaByName(adjFleetAreaName);
				if (!checkedAreas.contains(adjFleetArea)) {
					checkedAreas.add(adjFleetArea);
					if (adjFleetArea.getOccupant() != null
							&& empireName.equals(adjFleetArea.getOccupant().getEmpire().getName())) {
						pieceCount++;
					}
				}
			}
		}
		return pieceCount;
	}

	public List<Piece> getPiecesInAreaVicinity(String areaName, String empireName) {
		List<Piece> pieces = new ArrayList<>();
		if (neighboringAreas.containsKey(areaName)) {
			List<String> adjInfAreaNames = neighboringAreas.get(areaName);
			for (String adjInfAreaName : adjInfAreaNames) {
				Area adjInfArea = getAreaByName(adjInfAreaName);
				Piece adjPiece = adjInfArea.getOccupant();
				if (adjPiece != null && !pieces.contains(adjPiece)
						&& empireName.equals(adjPiece.getEmpire().getName())) {
					pieces.add(adjPiece);
				}
			}
		}
		if (fleetRoutes.containsKey(areaName)) {
			List<String> adjFleetAreaNames = fleetRoutes.get(areaName);
			for (String adjFleetAreaName : adjFleetAreaNames) {
				Area adjFleetArea = getAreaByName(adjFleetAreaName);
				Piece adjPiece = adjFleetArea.getOccupant();
				if (adjFleetArea.getOccupant() != null && !pieces.contains(adjPiece)
						&& empireName.equals(adjPiece.getEmpire().getName())) {
					pieces.add(adjPiece);
				}
			}
		}
		return pieces;
	}

	// Does not check if the starting position is at ocean
	// Starting position meaning the position parameter on the first function call
	private boolean convoyMovePossible(Area position, Land destination, List<Area> chartedAreas) {
		if (chartedAreas.contains(destination)) {
			return true;
		}
		if (!destination.hasCoasts() && !fleetRoutes.containsKey(destination.getName())) {
			return false;
		}
		if (position.toString().equals("Land")) {
			Land landArea = (Land) position;
			if (landArea.hasCoasts()) {
				String[] coastNames = landArea.getCoasts();
				for (String coastName : coastNames) {
					List<String> adjacentAreaNames = fleetRoutes.get(coastName);
					for (int i = 0; i < adjacentAreaNames.size(); i++) {
						Area thisArea = getAreaByName(adjacentAreaNames.get(i));
						if (thisArea == destination || (thisArea.toString().equals("Ocean")
								&& thisArea.getOccupant() != null && !chartedAreas.contains(thisArea))) {
							chartedAreas.add(thisArea);
							if (convoyMovePossible(thisArea, destination, chartedAreas)) {
								return true;
							}
						}
					}
				}
			} else {
				if (!fleetRoutes.containsKey(position.getName())) {
					return false;
				}
				List<String> adjacentAreaNames = fleetRoutes.get(position.getName());
				for (int i = 0; i < adjacentAreaNames.size(); i++) {
					Area thisArea = getAreaByName(adjacentAreaNames.get(i));
					if (thisArea == destination || (thisArea.toString().equals("Ocean")
							&& thisArea.getOccupant() != null && !chartedAreas.contains(thisArea))) {
						chartedAreas.add(thisArea);
						if (convoyMovePossible(thisArea, destination, chartedAreas)) {
							return true;
						}
					}
				}
			}
		} else {
			List<String> adjacentAreaNames = fleetRoutes.get(position.getName());
			for (int i = 0; i < adjacentAreaNames.size(); i++) {
				Area thisArea = getAreaByName(adjacentAreaNames.get(i));
				if (thisArea == destination || (thisArea.toString().equals("Ocean") && thisArea.getOccupant() != null
						&& !chartedAreas.contains(thisArea))) {
					chartedAreas.add(thisArea);
					if (convoyMovePossible(thisArea, destination, chartedAreas)) {
						return true;
					}
				}
			}
		}
		return false;
	}

	public boolean movePossible(Piece piece, String destinationName) throws IllegalArgumentException {
		if (!neighboringAreas.containsKey(destinationName) && !fleetRoutes.containsKey(destinationName)) {
			throw new IllegalArgumentException();
		}
		if (piece.getPositionName().equals(destinationName)) {
			return true;
		}
		if (piece.toString().equals("Infantry") && getAreaByName(destinationName).toString().equals("Land")) {
			// Prevents Infantry pieces from moving to coasts
			if (destinationName.contains("-")) {
				return false;
			}
			List<String> neighboringAreaNames = Map.neighboringAreas.get(piece.getPositionName());
			Land destinationArea = (Land) getAreaByName(destinationName);
			if (neighboringAreaNames.contains(destinationName) || convoyMovePossible(
					getAreaByName(piece.getPositionName()), destinationArea, new ArrayList<Area>())) {
				return true;
			}
		} else if (piece.toString().equals("Fleet")
				&& fleetRoutes.get(piece.getPositionName()).contains(destinationName)) {
			return true;
		}
		return false;
	}

	// Oceans 19
	// Lands 56
	private void createMap() {
		// ADD sea areas to map
		areas.add(new Ocean("NAO", null));
		areas.add(new Ocean("NWG", null));
		areas.add(new Ocean("BAR", null));
		areas.add(new Ocean("NTH", null));
		areas.add(new Ocean("HEL", null));
		areas.add(new Ocean("SKA", null));
		areas.add(new Ocean("BAL", null));
		areas.add(new Ocean("BOT", null));
		areas.add(new Ocean("ENG", null));
		areas.add(new Ocean("IRI", null));
		areas.add(new Ocean("MAO", null));
		areas.add(new Ocean("WES", null));
		areas.add(new Ocean("LYO", null));
		areas.add(new Ocean("TYS", null));
		areas.add(new Ocean("ION", null));
		areas.add(new Ocean("ADR", null));
		areas.add(new Ocean("AEG", null));
		areas.add(new Ocean("EAS", null));
		areas.add(new Ocean("BLA", null));
		// ADD land areas to map
		// First line
		areas.add(new Land("Stp", null, true, new String[] { "Stp-nc", "Stp-sc" }));
		areas.add(new Land("Fin", null, false));
		areas.add(new Land("Swe", null, true));
		areas.add(new Land("Nwy", null, true));
		areas.add(new Land("Den", null, true));
		areas.add(new Land("Cly", null, false));
		areas.add(new Land("Edi", null, true));
		areas.add(new Land("Lvp", null, true));
		areas.add(new Land("Yor", null, false));
		areas.add(new Land("Wal", null, false));
		areas.add(new Land("Lon", null, true));
		// Second line
		areas.add(new Land("Lvn", null, false));
		areas.add(new Land("Pru", null, false));
		areas.add(new Land("Ber", null, true));
		areas.add(new Land("Kie", null, true));
		areas.add(new Land("Hol", null, true));
		areas.add(new Land("Bel", null, true));
		areas.add(new Land("Pic", null, false));
		areas.add(new Land("Bre", null, true));
		// Third line
		areas.add(new Land("Mos", null, true));
		areas.add(new Land("War", null, true));
		areas.add(new Land("Sil", null, false));
		areas.add(new Land("Mun", null, true));
		areas.add(new Land("Ruh", null, false));
		areas.add(new Land("Bur", null, false));
		areas.add(new Land("Par", null, true));
		// Fourth line
		areas.add(new Land("Sev", null, true));
		areas.add(new Land("Ukr", null, false));
		areas.add(new Land("Gal", null, false));
		areas.add(new Land("Boh", null, false));
		areas.add(new Land("Tyr", null, false));
		areas.add(new Land("Pie", null, false));
		areas.add(new Land("Mar", null, true));
		areas.add(new Land("Gas", null, false));
		areas.add(new Land("Spa", null, true, new String[] { "Spa-nc", "Spa-sc" }));
		areas.add(new Land("Por", null, true));
		// Fifth line
		areas.add(new Land("Rum", null, true));
		areas.add(new Land("Bud", null, true));
		areas.add(new Land("Vie", null, true));
		areas.add(new Land("Tri", null, true));
		areas.add(new Land("Ven", null, true));
		areas.add(new Land("Tus", null, false));
		areas.add(new Land("Rom", null, true));
		areas.add(new Land("Nap", null, true));
		areas.add(new Land("Apu", null, false));
		// Africa
		areas.add(new Land("Naf", null, false));
		areas.add(new Land("Tun", null, true));
		// Bottom right corner
		areas.add(new Land("Arm", null, false));
		areas.add(new Land("Syr", null, false));
		areas.add(new Land("Smy", null, true));
		areas.add(new Land("Ank", null, true));
		areas.add(new Land("Con", null, true));
		areas.add(new Land("Bul", null, true, new String[] { "Bul-ec", "Bul-sc" }));
		areas.add(new Land("Ser", null, true));
		areas.add(new Land("Alb", null, false));
		areas.add(new Land("Gre", null, true));
	}

	private void createCoasts() {
		coasts.add("Stp-nc");
		coasts.add("Stp-sc");
		coasts.add("Spa-nc");
		coasts.add("Spa-sc");
		coasts.add("Bul-sc");
		coasts.add("Bul-ec");
	}

	private void fillNeighboringAreas() {
		// FleetRoute oceans
		fleetRoutes.put("NAO", new ArrayList<String>());
		fleetRoutes.get("NAO").add("NWG");
		fleetRoutes.get("NAO").add("Cly");
		fleetRoutes.get("NAO").add("Lvp");
		fleetRoutes.get("NAO").add("MAO");
		fleetRoutes.get("NAO").add("IRI");
		fleetRoutes.put("NWG", new ArrayList<String>());
		fleetRoutes.get("NWG").add("BAR");
		fleetRoutes.get("NWG").add("NAO");
		fleetRoutes.get("NWG").add("NTH");
		fleetRoutes.get("NWG").add("Nwy");
		fleetRoutes.get("NWG").add("Edi");
		fleetRoutes.get("NWG").add("Cly");
		fleetRoutes.put("BAR", new ArrayList<String>());
		fleetRoutes.get("BAR").add("NWG");
		fleetRoutes.get("BAR").add("Nwy");
		fleetRoutes.get("BAR").add("Stp-nc");
		fleetRoutes.put("NTH", new ArrayList<String>());
		fleetRoutes.get("NTH").add("NWG");
		fleetRoutes.get("NTH").add("HEL");
		fleetRoutes.get("NTH").add("SKA");
		fleetRoutes.get("NTH").add("ENG");
		fleetRoutes.get("NTH").add("Nwy");
		fleetRoutes.get("NTH").add("Edi");
		fleetRoutes.get("NTH").add("Yor");
		fleetRoutes.get("NTH").add("Lon");
		fleetRoutes.get("NTH").add("Den");
		fleetRoutes.get("NTH").add("Hol");
		fleetRoutes.get("NTH").add("Bel");
		fleetRoutes.put("HEL", new ArrayList<String>());
		fleetRoutes.get("HEL").add("NTH");
		fleetRoutes.get("HEL").add("Den");
		fleetRoutes.get("HEL").add("Kie");
		fleetRoutes.get("HEL").add("Hol");
		fleetRoutes.put("SKA", new ArrayList<String>());
		fleetRoutes.get("SKA").add("NTH");
		fleetRoutes.get("SKA").add("Swe");
		fleetRoutes.get("SKA").add("Nwy");
		fleetRoutes.get("SKA").add("Den");
		fleetRoutes.put("BAL", new ArrayList<String>());
		fleetRoutes.get("BAL").add("BOT");
		fleetRoutes.get("BAL").add("Swe");
		fleetRoutes.get("BAL").add("Den");
		fleetRoutes.get("BAL").add("Lvn");
		fleetRoutes.get("BAL").add("Pru");
		fleetRoutes.get("BAL").add("Ber");
		fleetRoutes.get("BAL").add("Kie");
		fleetRoutes.put("BOT", new ArrayList<String>());
		fleetRoutes.get("BOT").add("BAL");
		fleetRoutes.get("BOT").add("Stp-sc");
		fleetRoutes.get("BOT").add("Lvn");
		fleetRoutes.get("BOT").add("Fin");
		fleetRoutes.get("BOT").add("Swe");
		fleetRoutes.put("ENG", new ArrayList<String>());
		fleetRoutes.get("ENG").add("NTH");
		fleetRoutes.get("ENG").add("IRI");
		fleetRoutes.get("ENG").add("MAO");
		fleetRoutes.get("ENG").add("Lon");
		fleetRoutes.get("ENG").add("Wal");
		fleetRoutes.get("ENG").add("Bel");
		fleetRoutes.get("ENG").add("Pic");
		fleetRoutes.get("ENG").add("Bre");
		fleetRoutes.put("IRI", new ArrayList<String>());
		fleetRoutes.get("IRI").add("NAO");
		fleetRoutes.get("IRI").add("ENG");
		fleetRoutes.get("IRI").add("MAO");
		fleetRoutes.get("IRI").add("Lvp");
		fleetRoutes.get("IRI").add("Wal");
		fleetRoutes.put("MAO", new ArrayList<String>());
		fleetRoutes.get("MAO").add("NAO");
		fleetRoutes.get("MAO").add("IRI");
		fleetRoutes.get("MAO").add("ENG");
		fleetRoutes.get("MAO").add("WES");
		fleetRoutes.get("MAO").add("Bre");
		fleetRoutes.get("MAO").add("Gas");
		fleetRoutes.get("MAO").add("Spa-nc");
		fleetRoutes.get("MAO").add("Por");
		fleetRoutes.get("MAO").add("Naf");
		fleetRoutes.put("WES", new ArrayList<String>());
		fleetRoutes.get("WES").add("MAO");
		fleetRoutes.get("WES").add("LYO");
		fleetRoutes.get("WES").add("TYS");
		fleetRoutes.get("WES").add("Spa-sc");
		fleetRoutes.get("WES").add("Naf");
		fleetRoutes.get("WES").add("Tun");
		fleetRoutes.put("LYO", new ArrayList<String>());
		fleetRoutes.get("LYO").add("WES");
		fleetRoutes.get("LYO").add("TYS");
		fleetRoutes.get("LYO").add("Spa");
		fleetRoutes.get("LYO").add("Mar");
		fleetRoutes.get("LYO").add("Pie");
		fleetRoutes.get("LYO").add("Tus");
		fleetRoutes.put("TYS", new ArrayList<String>());
		fleetRoutes.get("TYS").add("LYO");
		fleetRoutes.get("TYS").add("WES");
		fleetRoutes.get("TYS").add("ION");
		fleetRoutes.get("TYS").add("Tun");
		fleetRoutes.get("TYS").add("Tus");
		fleetRoutes.get("TYS").add("Rom");
		fleetRoutes.get("TYS").add("Nap");
		fleetRoutes.put("ION", new ArrayList<String>());
		fleetRoutes.get("ION").add("TYS");
		fleetRoutes.get("ION").add("ADR");
		fleetRoutes.get("ION").add("AEG");
		fleetRoutes.get("ION").add("EAS");
		fleetRoutes.get("ION").add("Tun");
		fleetRoutes.get("ION").add("Nap");
		fleetRoutes.get("ION").add("Apu");
		fleetRoutes.get("ION").add("Alb");
		fleetRoutes.get("ION").add("Gre");
		fleetRoutes.put("ADR", new ArrayList<String>());
		fleetRoutes.get("ADR").add("ION");
		fleetRoutes.get("ADR").add("Apu");
		fleetRoutes.get("ADR").add("Ven");
		fleetRoutes.get("ADR").add("Tri");
		fleetRoutes.get("ADR").add("Alb");
		fleetRoutes.put("AEG", new ArrayList<String>());
		fleetRoutes.get("AEG").add("ION");
		fleetRoutes.get("AEG").add("EAS");
		fleetRoutes.get("AEG").add("Gre");
		fleetRoutes.get("AEG").add("Bul-sc");
		fleetRoutes.get("AEG").add("Con");
		fleetRoutes.get("AEG").add("Smy");
		fleetRoutes.put("EAS", new ArrayList<String>());
		fleetRoutes.get("EAS").add("AEG");
		fleetRoutes.get("EAS").add("ION");
		fleetRoutes.get("EAS").add("Smy");
		fleetRoutes.get("EAS").add("Syr");
		fleetRoutes.put("BLA", new ArrayList<String>());
		fleetRoutes.get("BLA").add("Sev");
		fleetRoutes.get("BLA").add("Arm");
		fleetRoutes.get("BLA").add("Ank");
		fleetRoutes.get("BLA").add("Con");
		fleetRoutes.get("BLA").add("Bul-ec");
		fleetRoutes.get("BLA").add("Rum");
		// FleetRoute lands
		fleetRoutes.put("Stp-nc", new ArrayList<String>());
		fleetRoutes.get("Stp-nc").add("BAR");
		fleetRoutes.get("Stp-nc").add("Nwy");
		fleetRoutes.put("Stp-sc", new ArrayList<String>());
		fleetRoutes.get("Stp-sc").add("BOT");
		fleetRoutes.get("Stp-sc").add("Fin");
		fleetRoutes.get("Stp-sc").add("Lvn");
		fleetRoutes.put("Fin", new ArrayList<String>());
		fleetRoutes.get("Fin").add("BOT");
		fleetRoutes.get("Fin").add("Stp-sc");
		fleetRoutes.get("Fin").add("Swe");
		fleetRoutes.put("Swe", new ArrayList<String>());
		fleetRoutes.get("Swe").add("BOT");
		fleetRoutes.get("Swe").add("BAL");
		fleetRoutes.get("Swe").add("SKA");
		fleetRoutes.get("Swe").add("Fin");
		fleetRoutes.get("Swe").add("Nwy");
		fleetRoutes.get("Swe").add("Den");
		fleetRoutes.put("Nwy", new ArrayList<String>());
		fleetRoutes.get("Nwy").add("NWG");
		fleetRoutes.get("Nwy").add("NTH");
		fleetRoutes.get("Nwy").add("SKA");
		fleetRoutes.get("Nwy").add("BAR");
		fleetRoutes.get("Nwy").add("Stp-nc");
		fleetRoutes.get("Nwy").add("Swe");
		fleetRoutes.put("Den", new ArrayList<String>());
		fleetRoutes.get("Den").add("BAL");
		fleetRoutes.get("Den").add("SKA");
		fleetRoutes.get("Den").add("HEL");
		fleetRoutes.get("Den").add("NTH");
		fleetRoutes.get("Den").add("Swe");
		fleetRoutes.get("Den").add("Kie");
		fleetRoutes.put("Cly", new ArrayList<String>());
		fleetRoutes.get("Cly").add("NWG");
		fleetRoutes.get("Cly").add("NAO");
		fleetRoutes.get("Cly").add("Edi");
		fleetRoutes.get("Cly").add("Lvp");
		fleetRoutes.put("Edi", new ArrayList<String>());
		fleetRoutes.get("Edi").add("NWG");
		fleetRoutes.get("Edi").add("NTH");
		fleetRoutes.get("Edi").add("Cly");
		fleetRoutes.get("Edi").add("Yor");
		fleetRoutes.put("Lvp", new ArrayList<String>());
		fleetRoutes.get("Lvp").add("NAO");
		fleetRoutes.get("Lvp").add("IRI");
		fleetRoutes.get("Lvp").add("Cly");
		fleetRoutes.get("Lvp").add("Wal");
		fleetRoutes.put("Yor", new ArrayList<String>());
		fleetRoutes.get("Yor").add("NTH");
		fleetRoutes.get("Yor").add("Edi");
		fleetRoutes.get("Yor").add("Lon");
		fleetRoutes.put("Wal", new ArrayList<String>());
		fleetRoutes.get("Wal").add("IRI");
		fleetRoutes.get("Wal").add("ENG");
		fleetRoutes.get("Wal").add("Lvp");
		fleetRoutes.get("Wal").add("Lon");
		fleetRoutes.put("Lon", new ArrayList<String>());
		fleetRoutes.get("Lon").add("NTH");
		fleetRoutes.get("Lon").add("ENG");
		fleetRoutes.get("Lon").add("Yor");
		fleetRoutes.get("Lon").add("Wal");
		fleetRoutes.put("Lvn", new ArrayList<String>());
		fleetRoutes.get("Lvn").add("BOT");
		fleetRoutes.get("Lvn").add("BAL");
		fleetRoutes.get("Lvn").add("Stp-sc");
		fleetRoutes.get("Lvn").add("Pru");
		fleetRoutes.put("Pru", new ArrayList<String>());
		fleetRoutes.get("Pru").add("BAL");
		fleetRoutes.get("Pru").add("Lvn");
		fleetRoutes.get("Pru").add("Ber");
		fleetRoutes.put("Ber", new ArrayList<String>());
		fleetRoutes.get("Ber").add("BAL");
		fleetRoutes.get("Ber").add("Pru");
		fleetRoutes.get("Ber").add("Kie");
		fleetRoutes.put("Kie", new ArrayList<String>());
		fleetRoutes.get("Kie").add("BAL");
		fleetRoutes.get("Kie").add("HEL");
		fleetRoutes.get("Kie").add("Ber");
		fleetRoutes.get("Kie").add("Hol");
		fleetRoutes.get("Kie").add("Den");
		fleetRoutes.put("Hol", new ArrayList<String>());
		fleetRoutes.get("Hol").add("HEL");
		fleetRoutes.get("Hol").add("NTH");
		fleetRoutes.get("Hol").add("Kie");
		fleetRoutes.get("Hol").add("Bel");
		fleetRoutes.put("Bel", new ArrayList<String>());
		fleetRoutes.get("Bel").add("NTH");
		fleetRoutes.get("Bel").add("ENG");
		fleetRoutes.get("Bel").add("Hol");
		fleetRoutes.get("Bel").add("Pic");
		fleetRoutes.put("Pic", new ArrayList<String>());
		fleetRoutes.get("Pic").add("ENG");
		fleetRoutes.get("Pic").add("Bel");
		fleetRoutes.get("Pic").add("Bre");
		fleetRoutes.put("Bre", new ArrayList<String>());
		fleetRoutes.get("Bre").add("ENG");
		fleetRoutes.get("Bre").add("MAO");
		fleetRoutes.get("Bre").add("Pic");
		fleetRoutes.get("Bre").add("Gas");
		fleetRoutes.put("Sev", new ArrayList<String>());
		fleetRoutes.get("Sev").add("BLA");
		fleetRoutes.get("Sev").add("Arm");
		fleetRoutes.get("Sev").add("Rum");
		fleetRoutes.put("Pie", new ArrayList<String>());
		fleetRoutes.get("Pie").add("LYO");
		fleetRoutes.get("Pie").add("Tus");
		fleetRoutes.get("Pie").add("Mar");
		fleetRoutes.put("Mar", new ArrayList<String>());
		fleetRoutes.get("Mar").add("LYO");
		fleetRoutes.get("Mar").add("Pie");
		fleetRoutes.get("Mar").add("Spa-sc");
		fleetRoutes.put("Gas", new ArrayList<String>());
		fleetRoutes.get("Gas").add("MAO");
		fleetRoutes.get("Gas").add("Bre");
		fleetRoutes.get("Gas").add("Spa-nc");
		fleetRoutes.put("Spa-nc", new ArrayList<String>());
		fleetRoutes.get("Spa-nc").add("MAO");
		fleetRoutes.get("Spa-nc").add("Gas");
		fleetRoutes.get("Spa-nc").add("Por");
		fleetRoutes.put("Spa-sc", new ArrayList<String>());
		fleetRoutes.get("Spa-sc").add("WES");
		fleetRoutes.get("Spa-sc").add("LYO");
		fleetRoutes.get("Spa-sc").add("MAO");
		fleetRoutes.get("Spa-sc").add("Mar");
		fleetRoutes.get("Spa-sc").add("Por");
		fleetRoutes.put("Por", new ArrayList<String>());
		fleetRoutes.get("Por").add("MAO");
		fleetRoutes.get("Por").add("Spa-nc");
		fleetRoutes.get("Por").add("Spa-sc");
		fleetRoutes.put("Rum", new ArrayList<String>());
		fleetRoutes.get("Rum").add("BLA");
		fleetRoutes.get("Rum").add("Sev");
		fleetRoutes.get("Rum").add("Bul-ec");
		fleetRoutes.put("Tri", new ArrayList<String>());
		fleetRoutes.get("Tri").add("ADR");
		fleetRoutes.get("Tri").add("Alb");
		fleetRoutes.get("Tri").add("Ven");
		fleetRoutes.put("Ven", new ArrayList<String>());
		fleetRoutes.get("Ven").add("ADR");
		fleetRoutes.get("Ven").add("Tri");
		fleetRoutes.get("Ven").add("Apu");
		fleetRoutes.put("Tus", new ArrayList<String>());
		fleetRoutes.get("Tus").add("LYO");
		fleetRoutes.get("Tus").add("TYS");
		fleetRoutes.get("Tus").add("Rom");
		fleetRoutes.get("Tus").add("Pie");
		fleetRoutes.put("Rom", new ArrayList<String>());
		fleetRoutes.get("Rom").add("TYS");
		fleetRoutes.get("Rom").add("Nap");
		fleetRoutes.get("Rom").add("Tus");
		fleetRoutes.put("Nap", new ArrayList<String>());
		fleetRoutes.get("Nap").add("ION");
		fleetRoutes.get("Nap").add("TYS");
		fleetRoutes.get("Nap").add("Apu");
		fleetRoutes.get("Nap").add("Rom");
		fleetRoutes.put("Apu", new ArrayList<String>());
		fleetRoutes.get("Apu").add("ADR");
		fleetRoutes.get("Apu").add("ION");
		fleetRoutes.get("Apu").add("Ven");
		fleetRoutes.get("Apu").add("Nap");
		fleetRoutes.put("Naf", new ArrayList<String>());
		fleetRoutes.get("Naf").add("MAO");
		fleetRoutes.get("Naf").add("WES");
		fleetRoutes.get("Naf").add("Tun");
		fleetRoutes.put("Tun", new ArrayList<String>());
		fleetRoutes.get("Tun").add("WES");
		fleetRoutes.get("Tun").add("TYS");
		fleetRoutes.get("Tun").add("ION");
		fleetRoutes.get("Tun").add("Naf");
		fleetRoutes.put("Arm", new ArrayList<String>());
		fleetRoutes.get("Arm").add("BLA");
		fleetRoutes.get("Arm").add("Sev");
		fleetRoutes.get("Arm").add("Ank");
		fleetRoutes.put("Syr", new ArrayList<String>());
		fleetRoutes.get("Syr").add("EAS");
		fleetRoutes.get("Syr").add("Smy");
		fleetRoutes.put("Smy", new ArrayList<String>());
		fleetRoutes.get("Smy").add("EAS");
		fleetRoutes.get("Smy").add("AEG");
		fleetRoutes.get("Smy").add("Syr");
		fleetRoutes.get("Smy").add("Con");
		fleetRoutes.put("Ank", new ArrayList<String>());
		fleetRoutes.get("Ank").add("BLA");
		fleetRoutes.get("Ank").add("Arm");
		fleetRoutes.get("Ank").add("Con");
		fleetRoutes.put("Con", new ArrayList<String>());
		fleetRoutes.get("Con").add("BLA");
		fleetRoutes.get("Con").add("AEG");
		fleetRoutes.get("Con").add("Ank");
		fleetRoutes.get("Con").add("Smy");
		fleetRoutes.get("Con").add("Bul-ec");
		fleetRoutes.get("Con").add("Bul-sc");
		fleetRoutes.put("Bul-ec", new ArrayList<String>());
		fleetRoutes.get("Bul-ec").add("BLA");
		fleetRoutes.get("Bul-ec").add("Rum");
		fleetRoutes.get("Bul-ec").add("Con");
		fleetRoutes.put("Bul-sc", new ArrayList<String>());
		fleetRoutes.get("Bul-sc").add("AEG");
		fleetRoutes.get("Bul-sc").add("Con");
		fleetRoutes.get("Bul-sc").add("Gre");
		fleetRoutes.put("Alb", new ArrayList<String>());
		fleetRoutes.get("Alb").add("ADR");
		fleetRoutes.get("Alb").add("ION");
		fleetRoutes.get("Alb").add("Gre");
		fleetRoutes.get("Alb").add("Tri");
		fleetRoutes.put("Gre", new ArrayList<String>());
		fleetRoutes.get("Gre").add("AEG");
		fleetRoutes.get("Gre").add("ION");
		fleetRoutes.get("Gre").add("Alb");
		fleetRoutes.get("Gre").add("Bul-sc");
		// Neighboring Lands
		neighboringAreas.put("Stp", new ArrayList<String>());
		neighboringAreas.get("Stp").add("Fin");
		neighboringAreas.get("Stp").add("Lvn");
		neighboringAreas.get("Stp").add("Mos");
		neighboringAreas.get("Stp").add("Nwy");
		neighboringAreas.put("Fin", new ArrayList<String>());
		neighboringAreas.get("Fin").add("Stp");
		neighboringAreas.get("Fin").add("Nwy");
		neighboringAreas.get("Fin").add("Swe");
		neighboringAreas.put("Swe", new ArrayList<String>());
		neighboringAreas.get("Swe").add("Fin");
		neighboringAreas.get("Swe").add("Nwy");
		neighboringAreas.get("Swe").add("Den");
		neighboringAreas.put("Nwy", new ArrayList<String>());
		neighboringAreas.get("Nwy").add("Stp");
		neighboringAreas.get("Nwy").add("Fin");
		neighboringAreas.get("Nwy").add("Swe");
		neighboringAreas.put("Den", new ArrayList<String>());
		neighboringAreas.get("Den").add("Swe");
		neighboringAreas.get("Den").add("Kie");
		neighboringAreas.put("Cly", new ArrayList<String>());
		neighboringAreas.get("Cly").add("Edi");
		neighboringAreas.get("Cly").add("Lvp");
		neighboringAreas.put("Edi", new ArrayList<String>());
		neighboringAreas.get("Edi").add("Cly");
		neighboringAreas.get("Edi").add("Lvp");
		neighboringAreas.get("Edi").add("Yor");
		neighboringAreas.put("Lvp", new ArrayList<String>());
		neighboringAreas.get("Lvp").add("Cly");
		neighboringAreas.get("Lvp").add("Edi");
		neighboringAreas.get("Lvp").add("Yor");
		neighboringAreas.get("Lvp").add("Wal");
		neighboringAreas.put("Yor", new ArrayList<String>());
		neighboringAreas.get("Yor").add("Edi");
		neighboringAreas.get("Yor").add("Lvp");
		neighboringAreas.get("Yor").add("Wal");
		neighboringAreas.get("Yor").add("Lon");
		neighboringAreas.put("Wal", new ArrayList<String>());
		neighboringAreas.get("Wal").add("Lvp");
		neighboringAreas.get("Wal").add("Yor");
		neighboringAreas.get("Wal").add("Lon");
		neighboringAreas.put("Lon", new ArrayList<String>());
		neighboringAreas.get("Lon").add("Yor");
		neighboringAreas.get("Lon").add("Wal");
		neighboringAreas.put("Lvn", new ArrayList<String>());
		neighboringAreas.get("Lvn").add("Stp");
		neighboringAreas.get("Lvn").add("Mos");
		neighboringAreas.get("Lvn").add("War");
		neighboringAreas.get("Lvn").add("Pru");
		neighboringAreas.put("Pru", new ArrayList<String>());
		neighboringAreas.get("Pru").add("Lvn");
		neighboringAreas.get("Pru").add("War");
		neighboringAreas.get("Pru").add("Sil");
		neighboringAreas.get("Pru").add("Ber");
		neighboringAreas.put("Ber", new ArrayList<String>());
		neighboringAreas.get("Ber").add("Pru");
		neighboringAreas.get("Ber").add("Sil");
		neighboringAreas.get("Ber").add("Mun");
		neighboringAreas.get("Ber").add("Kie");
		neighboringAreas.put("Kie", new ArrayList<String>());
		neighboringAreas.get("Kie").add("Ber");
		neighboringAreas.get("Kie").add("Mun");
		neighboringAreas.get("Kie").add("Ruh");
		neighboringAreas.get("Kie").add("Hol");
		neighboringAreas.get("Kie").add("Den");
		neighboringAreas.put("Hol", new ArrayList<String>());
		neighboringAreas.get("Hol").add("Kie");
		neighboringAreas.get("Hol").add("Ruh");
		neighboringAreas.get("Hol").add("Bel");
		neighboringAreas.put("Bel", new ArrayList<String>());
		neighboringAreas.get("Bel").add("Hol");
		neighboringAreas.get("Bel").add("Ruh");
		neighboringAreas.get("Bel").add("Bur");
		neighboringAreas.get("Bel").add("Pic");
		neighboringAreas.put("Pic", new ArrayList<String>());
		neighboringAreas.get("Pic").add("Bel");
		neighboringAreas.get("Pic").add("Bur");
		neighboringAreas.get("Pic").add("Par");
		neighboringAreas.get("Pic").add("Bre");
		neighboringAreas.put("Bre", new ArrayList<String>());
		neighboringAreas.get("Bre").add("Pic");
		neighboringAreas.get("Bre").add("Par");
		neighboringAreas.get("Bre").add("Gas");
		neighboringAreas.put("Mos", new ArrayList<String>());
		neighboringAreas.get("Mos").add("Sev");
		neighboringAreas.get("Mos").add("Ukr");
		neighboringAreas.get("Mos").add("War");
		neighboringAreas.get("Mos").add("Lvn");
		neighboringAreas.get("Mos").add("Stp");
		neighboringAreas.put("War", new ArrayList<String>());
		neighboringAreas.get("War").add("Mos");
		neighboringAreas.get("War").add("Ukr");
		neighboringAreas.get("War").add("Gal");
		neighboringAreas.get("War").add("Sil");
		neighboringAreas.get("War").add("Pru");
		neighboringAreas.get("War").add("Lvn");
		neighboringAreas.put("Sil", new ArrayList<String>());
		neighboringAreas.get("Sil").add("Pru");
		neighboringAreas.get("Sil").add("War");
		neighboringAreas.get("Sil").add("Gal");
		neighboringAreas.get("Sil").add("Boh");
		neighboringAreas.get("Sil").add("Mun");
		neighboringAreas.get("Sil").add("Ber");
		neighboringAreas.put("Mun", new ArrayList<String>());
		neighboringAreas.get("Mun").add("Ber");
		neighboringAreas.get("Mun").add("Sil");
		neighboringAreas.get("Mun").add("Boh");
		neighboringAreas.get("Mun").add("Tyr");
		neighboringAreas.get("Mun").add("Bur");
		neighboringAreas.get("Mun").add("Ruh");
		neighboringAreas.get("Mun").add("Kie");
		neighboringAreas.put("Ruh", new ArrayList<String>());
		neighboringAreas.get("Ruh").add("Kie");
		neighboringAreas.get("Ruh").add("Mun");
		neighboringAreas.get("Ruh").add("Bur");
		neighboringAreas.get("Ruh").add("Bel");
		neighboringAreas.get("Ruh").add("Hol");
		neighboringAreas.put("Bur", new ArrayList<String>());
		neighboringAreas.get("Bur").add("Bel");
		neighboringAreas.get("Bur").add("Ruh");
		neighboringAreas.get("Bur").add("Mun");
		neighboringAreas.get("Bur").add("Mar");
		neighboringAreas.get("Bur").add("Gas");
		neighboringAreas.get("Bur").add("Par");
		neighboringAreas.get("Bur").add("Pic");
		neighboringAreas.put("Par", new ArrayList<String>());
		neighboringAreas.get("Par").add("Pic");
		neighboringAreas.get("Par").add("Bur");
		neighboringAreas.get("Par").add("Gas");
		neighboringAreas.get("Par").add("Bre");
		neighboringAreas.put("Sev", new ArrayList<String>());
		neighboringAreas.get("Sev").add("Mos");
		neighboringAreas.get("Sev").add("Arm");
		neighboringAreas.get("Sev").add("Rum");
		neighboringAreas.get("Sev").add("Ukr");
		neighboringAreas.put("Ukr", new ArrayList<String>());
		neighboringAreas.get("Ukr").add("Mos");
		neighboringAreas.get("Ukr").add("Sev");
		neighboringAreas.get("Ukr").add("Rum");
		neighboringAreas.get("Ukr").add("Gal");
		neighboringAreas.get("Ukr").add("War");
		neighboringAreas.put("Gal", new ArrayList<String>());
		neighboringAreas.get("Gal").add("War");
		neighboringAreas.get("Gal").add("Ukr");
		neighboringAreas.get("Gal").add("Rum");
		neighboringAreas.get("Gal").add("Bud");
		neighboringAreas.get("Gal").add("Vie");
		neighboringAreas.get("Gal").add("Boh");
		neighboringAreas.get("Gal").add("Sil");
		neighboringAreas.put("Boh", new ArrayList<String>());
		neighboringAreas.get("Boh").add("Sil");
		neighboringAreas.get("Boh").add("Mun");
		neighboringAreas.get("Boh").add("Tyr");
		neighboringAreas.get("Boh").add("Vie");
		neighboringAreas.get("Boh").add("Gal");
		neighboringAreas.put("Tyr", new ArrayList<String>());
		neighboringAreas.get("Tyr").add("Boh");
		neighboringAreas.get("Tyr").add("Vie");
		neighboringAreas.get("Tyr").add("Tri");
		neighboringAreas.get("Tyr").add("Ven");
		neighboringAreas.get("Tyr").add("Ven");
		neighboringAreas.get("Tyr").add("Pie");
		neighboringAreas.get("Tyr").add("Mun");
		neighboringAreas.put("Pie", new ArrayList<String>());
		neighboringAreas.get("Pie").add("Tyr");
		neighboringAreas.get("Pie").add("Ven");
		neighboringAreas.get("Pie").add("Tus");
		neighboringAreas.get("Pie").add("Mar");
		neighboringAreas.put("Mar", new ArrayList<String>());
		neighboringAreas.get("Mar").add("Bur");
		neighboringAreas.get("Mar").add("Pie");
		neighboringAreas.get("Mar").add("Spa");
		neighboringAreas.get("Mar").add("Gas");
		neighboringAreas.put("Gas", new ArrayList<String>());
		neighboringAreas.get("Gas").add("Bre");
		neighboringAreas.get("Gas").add("Par");
		neighboringAreas.get("Gas").add("Bur");
		neighboringAreas.get("Gas").add("Mar");
		neighboringAreas.get("Gas").add("Spa");
		neighboringAreas.put("Spa", new ArrayList<String>());
		neighboringAreas.get("Spa").add("Gas");
		neighboringAreas.get("Spa").add("Mar");
		neighboringAreas.get("Spa").add("Por");
		neighboringAreas.put("Por", new ArrayList<String>());
		neighboringAreas.get("Por").add("Spa");
		neighboringAreas.put("Rum", new ArrayList<String>());
		neighboringAreas.get("Rum").add("Ukr");
		neighboringAreas.get("Rum").add("Sev");
		neighboringAreas.get("Rum").add("Bul");
		neighboringAreas.get("Rum").add("Ser");
		neighboringAreas.get("Rum").add("Bud");
		neighboringAreas.get("Rum").add("Gal");
		neighboringAreas.put("Bud", new ArrayList<String>());
		neighboringAreas.get("Bud").add("Gal");
		neighboringAreas.get("Bud").add("Rum");
		neighboringAreas.get("Bud").add("Ser");
		neighboringAreas.get("Bud").add("Tri");
		neighboringAreas.get("Bud").add("Vie");
		neighboringAreas.put("Vie", new ArrayList<String>());
		neighboringAreas.get("Vie").add("Gal");
		neighboringAreas.get("Vie").add("Bud");
		neighboringAreas.get("Vie").add("Tri");
		neighboringAreas.get("Vie").add("Tyr");
		neighboringAreas.get("Vie").add("Boh");
		neighboringAreas.put("Tri", new ArrayList<String>());
		neighboringAreas.get("Tri").add("Vie");
		neighboringAreas.get("Tri").add("Bud");
		neighboringAreas.get("Tri").add("Ser");
		neighboringAreas.get("Tri").add("Alb");
		neighboringAreas.get("Tri").add("Ven");
		neighboringAreas.get("Tri").add("Tyr");
		neighboringAreas.put("Ven", new ArrayList<String>());
		neighboringAreas.get("Ven").add("Tyr");
		neighboringAreas.get("Ven").add("Tri");
		neighboringAreas.get("Ven").add("Apu");
		neighboringAreas.get("Ven").add("Rom");
		neighboringAreas.get("Ven").add("Tus");
		neighboringAreas.get("Ven").add("Pie");
		neighboringAreas.put("Tus", new ArrayList<String>());
		neighboringAreas.get("Tus").add("Ven");
		neighboringAreas.get("Tus").add("Rom");
		neighboringAreas.get("Tus").add("Pie");
		neighboringAreas.put("Rom", new ArrayList<String>());
		neighboringAreas.get("Rom").add("Ven");
		neighboringAreas.get("Rom").add("Apu");
		neighboringAreas.get("Rom").add("Nap");
		neighboringAreas.get("Rom").add("Tus");
		neighboringAreas.put("Nap", new ArrayList<String>());
		neighboringAreas.get("Nap").add("Apu");
		neighboringAreas.get("Nap").add("Rom");
		neighboringAreas.put("Apu", new ArrayList<String>());
		neighboringAreas.get("Apu").add("Ven");
		neighboringAreas.get("Apu").add("Nap");
		neighboringAreas.get("Apu").add("Rom");
		neighboringAreas.put("Naf", new ArrayList<String>());
		neighboringAreas.get("Naf").add("Tun");
		neighboringAreas.put("Tun", new ArrayList<String>());
		neighboringAreas.get("Tun").add("Naf");
		neighboringAreas.put("Arm", new ArrayList<String>());
		neighboringAreas.get("Arm").add("Sev");
		neighboringAreas.get("Arm").add("Syr");
		neighboringAreas.get("Arm").add("Smy");
		neighboringAreas.get("Arm").add("Ank");
		neighboringAreas.put("Syr", new ArrayList<String>());
		neighboringAreas.get("Syr").add("Arm");
		neighboringAreas.get("Syr").add("Smy");
		neighboringAreas.put("Smy", new ArrayList<String>());
		neighboringAreas.get("Smy").add("Ank");
		neighboringAreas.get("Smy").add("Arm");
		neighboringAreas.get("Smy").add("Syr");
		neighboringAreas.get("Smy").add("Con");
		neighboringAreas.put("Ank", new ArrayList<String>());
		neighboringAreas.get("Ank").add("Arm");
		neighboringAreas.get("Ank").add("Smy");
		neighboringAreas.get("Ank").add("Con");
		neighboringAreas.put("Con", new ArrayList<String>());
		neighboringAreas.get("Con").add("Ank");
		neighboringAreas.get("Con").add("Smy");
		neighboringAreas.get("Con").add("Bul");
		neighboringAreas.put("Bul", new ArrayList<String>());
		neighboringAreas.get("Bul").add("Rum");
		neighboringAreas.get("Bul").add("Con");
		neighboringAreas.get("Bul").add("Gre");
		neighboringAreas.get("Bul").add("Ser");
		neighboringAreas.put("Ser", new ArrayList<String>());
		neighboringAreas.get("Ser").add("Bud");
		neighboringAreas.get("Ser").add("Rum");
		neighboringAreas.get("Ser").add("Bul");
		neighboringAreas.get("Ser").add("Gre");
		neighboringAreas.get("Ser").add("Alb");
		neighboringAreas.get("Ser").add("Tri");
		neighboringAreas.put("Alb", new ArrayList<String>());
		neighboringAreas.get("Alb").add("Ser");
		neighboringAreas.get("Alb").add("Gre");
		neighboringAreas.get("Alb").add("Tri");
		neighboringAreas.put("Gre", new ArrayList<String>());
		neighboringAreas.get("Gre").add("Alb");
		neighboringAreas.get("Gre").add("Ser");
		neighboringAreas.get("Gre").add("Bul");
		// Coasts
		oceanCoastConnections.put("BAR", new ArrayList<String>());
		oceanCoastConnections.get("BAR").add("Stp-nc");
		oceanCoastConnections.put("Nwy", new ArrayList<String>());
		oceanCoastConnections.get("Nwy").add("Stp-nc");
		oceanCoastConnections.put("Stp-nc", new ArrayList<String>());
		oceanCoastConnections.get("Stp-nc").add("BAR");
		oceanCoastConnections.get("Stp-nc").add("Nwy");
		oceanCoastConnections.put("BOT", new ArrayList<String>());
		oceanCoastConnections.get("BOT").add("Stp-sc");
		oceanCoastConnections.put("Fin", new ArrayList<String>());
		oceanCoastConnections.get("Fin").add("Stp-sc");
		oceanCoastConnections.put("Lvn", new ArrayList<String>());
		oceanCoastConnections.get("Lvn").add("Stp-sc");
		oceanCoastConnections.put("Stp-sc", new ArrayList<String>());
		oceanCoastConnections.get("Stp-sc").add("BOT");
		oceanCoastConnections.get("Stp-sc").add("Fin");
		oceanCoastConnections.get("Stp-sc").add("Lvn");
		oceanCoastConnections.put("MAO", new ArrayList<String>());
		oceanCoastConnections.get("MAO").add("Spa-nc");
		oceanCoastConnections.put("Gas", new ArrayList<String>());
		oceanCoastConnections.get("Gas").add("Spa-nc");
		oceanCoastConnections.put("Por", new ArrayList<String>());
		oceanCoastConnections.get("Por").add("Spa-nc");
		oceanCoastConnections.put("Spa-nc", new ArrayList<String>());
		oceanCoastConnections.get("Spa-nc").add("MAO");
		oceanCoastConnections.get("Spa-nc").add("Gas");
		oceanCoastConnections.get("Spa-nc").add("Por");
		oceanCoastConnections.put("WES", new ArrayList<String>());
		oceanCoastConnections.get("WES").add("Spa-sc");
		oceanCoastConnections.put("LYO", new ArrayList<String>());
		oceanCoastConnections.get("LYO").add("Spa-sc");
		oceanCoastConnections.put("Mar", new ArrayList<String>());
		oceanCoastConnections.get("Mar").add("Spa-sc");
		oceanCoastConnections.put("Spa-sc", new ArrayList<String>());
		oceanCoastConnections.get("Spa-sc").add("LYO");
		oceanCoastConnections.get("Spa-sc").add("WES");
		oceanCoastConnections.get("Spa-sc").add("Mar");
		oceanCoastConnections.put("BLA", new ArrayList<String>());
		oceanCoastConnections.get("BLA").add("Bul-ec");
		oceanCoastConnections.put("Rum", new ArrayList<String>());
		oceanCoastConnections.get("Rum").add("Bul-ec");
		oceanCoastConnections.put("Con", new ArrayList<String>());
		oceanCoastConnections.get("Con").add("Bul-ec");
		oceanCoastConnections.put("Bul-ec", new ArrayList<String>());
		oceanCoastConnections.get("Bul-ec").add("BLA");
		oceanCoastConnections.get("Bul-ec").add("Rum");
		oceanCoastConnections.get("Bul-ec").add("Con");
		oceanCoastConnections.put("AEG", new ArrayList<String>());
		oceanCoastConnections.get("AEG").add("Bul-sc");
		oceanCoastConnections.put("Con", new ArrayList<String>());
		oceanCoastConnections.get("Con").add("Bul-sc");
		oceanCoastConnections.put("Gre", new ArrayList<String>());
		oceanCoastConnections.get("Gre").add("Bul-sc");
		oceanCoastConnections.put("Bul-sc", new ArrayList<String>());
		oceanCoastConnections.get("Bul-sc").add("AEG");
		oceanCoastConnections.get("Bul-sc").add("Con");
		oceanCoastConnections.get("Bul-sc").add("Gre");
	}
}
