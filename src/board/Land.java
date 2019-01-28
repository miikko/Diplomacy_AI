package board;

import pieces.Piece;

public class Land extends Area {

	private boolean hasCenter;
	private String[] coasts;
	
	public Land(String name, Piece occupant, boolean hasCenter) {
		super(name, occupant);
		this.hasCenter = hasCenter;
	}
	
	public Land(String name, Piece occupant, boolean hasCenter, String[] coasts) {
		super(name, occupant);
		this.hasCenter = hasCenter;
		this.coasts = coasts;
	}

	public boolean containsCenter() {
		return hasCenter;
	}
	
	public boolean hasCoasts() {
		if (coasts == null) {
			return false;
		} else {
			return true;
		}
	}
	
	public String[] getCoasts() {
		return coasts;
	}

	@Override
	public String toString() {
		return "Land";
	}
}
