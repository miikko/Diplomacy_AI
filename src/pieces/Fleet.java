package pieces;

import board.Map;
import controllers.IntentController;
import intents.Convoy;
import intents.Move;
import players.Empire;

public class Fleet extends Piece {

	public Fleet(String positionName, Empire empire) {
		super(positionName, empire);
	}

	public boolean convoyInfantry(String startingPositionName, String destinationName, IntentController ic) {
		Map map = ic.getMap();
		if (map.getAreaByName(getPositionName()).toString().equals("Ocean")
				&& map.getAreaByName(startingPositionName).getOccupant() != null
				&& map.getAreaByName(startingPositionName).getOccupant().toString().equals("Infantry")
				&& map.movePossible(map.getAreaByName(startingPositionName).getOccupant(), destinationName)) {
			ic.addIntent(new Convoy(this,
					new Move(map.getAreaByName(startingPositionName).getOccupant(), startingPositionName, destinationName)));
			return true;
		}
		return false;
	}

	@Override
	public String toString() {
		return "Fleet";
	}
}
