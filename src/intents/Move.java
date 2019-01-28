package intents;

import pieces.Piece;

public class Move extends Intent {

	private String startingPositionName;
	private String destinationName;
	
	public Move (Piece owner, String startingPositionName, String destinationName) {
		super(owner);
		this.startingPositionName = startingPositionName;
		this.destinationName = destinationName;
	}

	public String getStartingPositionName() {
		return startingPositionName;
	}

	public String getDestinationName() {
		return destinationName;
	}
	
	@Override
	public String toString() {
		return "Move";
	}
	
	public final boolean isIdenticalWith(Move otherMove) {
		if (startingPositionName.equals(otherMove.getStartingPositionName()) && getOwner() == otherMove.getOwner() && destinationName.equals(otherMove.getDestinationName())) {
			return true;
		}
		return false;
	}

}
