package intents;

import pieces.Piece;

public class Disband extends Intent{

	public Disband(Piece owner) {
		super(owner);
	}
	
	@Override
	public String toString() {
		return "Disband";
	}
	
}
