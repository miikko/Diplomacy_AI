package board;

import pieces.Piece;

public class Ocean extends Area {

	public Ocean(String name, Piece occupant) {
		super(name, occupant);
	}

	@Override
	public String toString() {
		return "Ocean";
	}
}
