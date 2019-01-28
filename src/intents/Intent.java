package intents;

import pieces.Piece;

public class Intent {

	private Piece owner;
	
	public Intent(Piece owner) {
		this.owner = owner;
	}

	public Piece getOwner() {
		return owner;
	}

}
