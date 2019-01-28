package players;

import java.util.ArrayList;
import java.util.List;

import board.Area;
import board.Map;
import pieces.Fleet;
import pieces.Infantry;
import pieces.Piece;

public class Empire {

	private String name;
	private List<String> homeCenterNames = new ArrayList<String>();
	private List<Piece> army = new ArrayList<Piece>();
	private List<String> ownedCenterNames;

	public Empire(String name) {
		this.name = name;
		createEmpire();
		setOwnedCenterNames(homeCenterNames);
	}

	public String getName() {
		return name;
	}

	public List<String> getHomeCenterNames() {
		return homeCenterNames;
	}

	public List<Piece> getArmy() {
		return army;
	}

	public void setArmy(List<Piece> army) {
		this.army = army;
	}
	
	public List<String> getOwnedCenterNames() {
		return ownedCenterNames;
	}

	public void setOwnedCenterNames(List<String> ownedCenterNames) {
		this.ownedCenterNames = new ArrayList<>(ownedCenterNames);
	}

	private void createEmpire() {
		switch (name) {
		case "Austria":
			army.add(new Infantry("Vie", this));
			army.add(new Infantry("Bud", this));
			army.add(new Fleet("Tri", this));
			homeCenterNames.add("Vie");
			homeCenterNames.add("Bud");
			homeCenterNames.add("Tri");
			break;
		case "England":
			army.add(new Infantry("Lvp", this));
			army.add(new Fleet("Lon", this));
			army.add(new Fleet("Edi", this));
			homeCenterNames.add("Lvp");
			homeCenterNames.add("Lon");
			homeCenterNames.add("Edi");
			break;
		case "France":
			army.add(new Infantry("Par", this));
			army.add(new Infantry("Mar", this));
			army.add(new Fleet("Bre", this));
			homeCenterNames.add("Par");
			homeCenterNames.add("Mar");
			homeCenterNames.add("Bre");
			break;
		case "Germany":
			army.add(new Infantry("Ber", this));
			army.add(new Infantry("Mun", this));
			army.add(new Fleet("Kie", this));
			homeCenterNames.add("Ber");
			homeCenterNames.add("Mun");
			homeCenterNames.add("Kie");
			break;
		case "Italy":
			army.add(new Infantry("Ven", this));
			army.add(new Infantry("Rom", this));
			army.add(new Fleet("Nap", this));
			homeCenterNames.add("Ven");
			homeCenterNames.add("Rom");
			homeCenterNames.add("Nap");
			break;
		case "Russia":
			army.add(new Infantry("Mos", this));
			army.add(new Infantry("War", this));
			army.add(new Fleet("Stp-sc", this));
			army.add(new Fleet("Sev", this));
			homeCenterNames.add("Mos");
			homeCenterNames.add("War");
			homeCenterNames.add("Stp");
			homeCenterNames.add("Sev");
			break;
		case "Turkey":
			army.add(new Infantry("Con", this));
			army.add(new Infantry("Smy", this));
			army.add(new Fleet("Ank", this));
			homeCenterNames.add("Con");
			homeCenterNames.add("Smy");
			homeCenterNames.add("Ank");
			break;
		default:
			System.out.println("Invalid empire name: " + name);
			System.exit(0);
			break;
		}
	}
}
