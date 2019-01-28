package intents;

import pieces.Piece;

public class Hold extends Intent {
	
	public Hold(Piece owner) {
		super(owner);
	}

	@Override
	public String toString() {
		return "Hold";
	}
}
