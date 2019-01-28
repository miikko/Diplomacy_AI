package intents;

import pieces.Piece;

public class Support extends Intent {

	private Intent supportedIntent;
	
	public Support(Piece owner, Intent supportedIntent) {
		super(owner);
		if (!supportedIntent.toString().equals("Move")) {
			this.supportedIntent = new Hold(supportedIntent.getOwner());
		} else {
			this.supportedIntent = supportedIntent;
		}
	}

	public Intent getSupportedIntent() {
		return supportedIntent;
	}

	@Override
	public String toString() {
		return "Support";
	}
}
