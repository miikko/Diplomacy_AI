package pieces;

import java.util.ArrayList;
import java.util.List;

import board.Area;
import board.Land;
import board.Map;
import board.Ocean;
import controllers.IntentController;
import intents.Hold;
import intents.Move;
import intents.Support;
import players.Empire;

public class Piece {

	private String positionName;
	private Empire empire;

	public Piece(String positionName, Empire empire) {
		this.positionName = positionName;
		this.empire = empire;
	}

	public boolean movePiece(String destination, IntentController ic) {
		Map map = ic.getMap();
		if (destination.equals(positionName)) {
			ic.addIntent(new Hold(this));
			return true;
		}
		if (map.movePossible(this, destination)) {
			ic.addIntent(new Move(this, positionName, destination));
			return true;
		}
		return false;
	}

	public boolean supportPiece(String startingPositionName, String destinationName, IntentController ic) {
		Map map = ic.getMap();
		Area position = map.getAreaByName(positionName);
		Area startingPosition = map.getAreaByName(startingPositionName);
		Area destination = map.getAreaByName(destinationName);
		if (startingPosition.getOccupant() != null) {
			// Prevents supporting an attack against itself and its own attack
			if (position == destination || position == startingPosition) {
				return false;
			}
			// Handles problems that might appear with coast areas
			if (!startingPosition.getOccupant().getPositionName().equals(startingPositionName)) {
				return false;
			}
			List<String> adjacentAreaNames = new ArrayList<String>();
			if (toString().equals("Fleet")) {
				adjacentAreaNames = Map.fleetRoutes.get(positionName);
			} else {
				adjacentAreaNames = Map.neighboringAreas.get(positionName);
			}
			for (String adjacentAreaName : adjacentAreaNames) {
				Area adjacentArea = map.getAreaByName(adjacentAreaName);
				if (adjacentArea == destination) {
					if (startingPosition == destination) {
						ic.addIntent(new Support(this, new Hold(startingPosition.getOccupant())));
						return true;
					} else if (map.movePossible(startingPosition.getOccupant(), destinationName)) {
						ic.addIntent(new Support(this,
								new Move(startingPosition.getOccupant(), startingPositionName, destinationName)));
						return true;
					}
				}
			}
		}
		return false;
	}

	public void setPositionName(String position) {
		this.positionName = position;
	}

	public String getPositionName() {
		return positionName;
	}

	public Empire getEmpire() {
		return empire;
	}

}
