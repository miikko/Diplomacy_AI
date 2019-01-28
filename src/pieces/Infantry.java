package pieces;

import players.Empire;

public class Infantry extends Piece {

	public Infantry(String positionName, Empire empire) {
		super(positionName, empire);
	}

	@Override
	public String toString() {
		return "Infantry";
	}
}
