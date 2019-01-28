package board;

import pieces.Piece;

public class Area {

	private String name;
	private Piece occupant;
	
	public Area(String name, Piece occupant) {
		this.name = name;
		this.setOccupant(occupant);
	}

	public String getName() {
		return name;
	}

	public Piece getOccupant() {
		return occupant;
	}

	public void setOccupant(Piece occupant) {
		this.occupant = occupant;
	}

}
