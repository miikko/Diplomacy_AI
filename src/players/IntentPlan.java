package players;

import java.util.List;

import pieces.Piece;

public class IntentPlan {
	private Piece owner;
	private String areaName;
	private double areaValue;
	private List<Piece> otherNearbyPieces;
	private int reqSupportStr;
	
	public IntentPlan(Piece owner, String areaName, double areaValue, List<Piece> otherNearbyPieces, int reqSupportStr) {
		this.setOwner(owner);
		this.setAreaName(areaName);
		this.setAreaValue(areaValue);
		this.setOtherNearbyPieces(otherNearbyPieces);
		this.setReqSupportStr(reqSupportStr);
	}

	public Piece getOwner() {
		return owner;
	}

	public void setOwner(Piece owner) {
		this.owner = owner;
	}
	
	public String getAreaName() {
		return areaName;
	}

	public void setAreaName(String areaName) {
		this.areaName = areaName;
	}

	public double getAreaValue() {
		return areaValue;
	}

	public void setAreaValue(double areaValue) {
		this.areaValue = areaValue;
	}

	public List<Piece> getOtherNearbyPieces() {
		return otherNearbyPieces;
	}

	public void setOtherNearbyPieces(List<Piece> otherNearbyPieces) {
		this.otherNearbyPieces = otherNearbyPieces;
	}

	public int getReqSupportStr() {
		return reqSupportStr;
	}

	public void setReqSupportStr(int reqSupportStr) {
		this.reqSupportStr = reqSupportStr;
	}
}
