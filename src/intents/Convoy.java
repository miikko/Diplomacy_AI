package intents;

import pieces.Piece;

public class Convoy extends Intent {
	
	private Move move;
	
	public Convoy(Piece owner, Move move) throws IllegalArgumentException {
		super(owner);
		if (owner.toString().equals("Infantry")) {
			System.out.println("Tried to give a convoy-order to an Infantry-piece.");
			throw new IllegalArgumentException();
		}
		this.move = move;
	}

	public Move getMove() {
		return move;
	}

	@Override
	public String toString() {
		return "Convoy";
	}
}
