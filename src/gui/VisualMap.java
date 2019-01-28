package gui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;

import pieces.Piece;
import players.Empire;

public class VisualMap {

	private BufferedImage currentMap;
	private JLabel container;
	private final int PIECE_RADIUS = 20;
	private final int CENTER_RADIUS = 10;
	private final int HOLD_RADIUS = 26;

	private final String path = "img/diplomacy_map_without_units.v1.png";
	private LinkedHashMap<String, Integer[]> centerCoordinates = new LinkedHashMap<>();
	private LinkedHashMap<String, Integer[]> pieceCoordinates = new LinkedHashMap<>();
	private LinkedHashMap<String, Color> empireColors = new LinkedHashMap<>();
	private List<Empire> empires;
	private boolean show;

	public VisualMap(List<Empire> empires, boolean show) {
		this.show = show;
		if (show) {
			try {
				currentMap = ImageIO.read(new File(path));
				JFrame frame = new JFrame("Diplomacy GUI");
				container = new JLabel(new ImageIcon(currentMap));
				frame.getContentPane().setLayout(new FlowLayout());
				frame.getContentPane().add(container);
				frame.pack();
				frame.setVisible(true);
				frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			} catch (IOException e) {
				e.printStackTrace();
			}
			this.empires = empires;
			fillDataStructures();
			reset();
		}
	}

	public void reset() {
		if (show) {
			try {
				currentMap = ImageIO.read(new File(path));
				container.setIcon(new ImageIcon(currentMap));
			} catch (IOException e) {
				e.printStackTrace();
			}
			for (Empire empire : empires) {
				List<String> ownedCenters = empire.getOwnedCenterNames();
				List<Piece> army = empire.getArmy();
				String empireName = empire.getName();
				for (String ownedCenter : ownedCenters) {
					drawCenter(ownedCenter, empireName);
				}
				for (Piece piece : army) {
					drawPiece(piece.getPositionName(), empireName, piece.toString());
				}
			}
		}
	}

	public void drawCenter(String areaName, String empireName) throws IllegalArgumentException {
		if (show) {
			if (!empireColors.containsKey(empireName) || !centerCoordinates.containsKey(areaName)) {
				throw new IllegalArgumentException();
			}
			int x = centerCoordinates.get(areaName)[0];
			int y = centerCoordinates.get(areaName)[1];
			Graphics2D g2d = currentMap.createGraphics();
			g2d.setColor(empireColors.get(empireName));
			g2d.fillOval(x, y, CENTER_RADIUS, CENTER_RADIUS);
			g2d.setColor(Color.BLACK);
			g2d.drawOval(x, y, CENTER_RADIUS, CENTER_RADIUS);
			g2d.dispose();
			container.repaint();
		}
	}

	public void drawPiece(String areaName, String empireName, String pieceType) throws IllegalArgumentException {
		if (show) {
			if (!empireColors.containsKey(empireName) || !pieceCoordinates.containsKey(areaName)
					|| (!pieceType.equals("Infantry") && !pieceType.equals("Fleet"))) {
				throw new IllegalArgumentException();
			}
			int x = pieceCoordinates.get(areaName)[0];
			int y = pieceCoordinates.get(areaName)[1];
			Graphics2D g2d = currentMap.createGraphics();
			Color color = empireColors.get(empireName);
			Color opaqueColor = new Color(color.getRed(), color.getGreen(), color.getBlue(), 127);
			g2d.setColor(opaqueColor);
			if (pieceType.equals("Infantry")) {
				g2d.fillOval(x, y, PIECE_RADIUS, PIECE_RADIUS);
				g2d.setColor(Color.BLACK);
				g2d.drawOval(x, y, PIECE_RADIUS, PIECE_RADIUS);
			} else {
				g2d.fillPolygon(new Polygon(new int[] { x, x + PIECE_RADIUS / 2, x + PIECE_RADIUS },
						new int[] { y, y + PIECE_RADIUS, y }, 3));
				g2d.setColor(Color.BLACK);
				g2d.drawPolygon(new Polygon(new int[] { x, x + PIECE_RADIUS / 2, x + PIECE_RADIUS },
						new int[] { y, y + PIECE_RADIUS, y }, 3));
			}
			g2d.dispose();
			container.repaint();
		}
	}

	public void drawMove(String startingPosition, String destination, boolean successful)
			throws IllegalArgumentException {
		if (show) {
			if (!pieceCoordinates.containsKey(startingPosition) || !pieceCoordinates.containsKey(destination)) {
				throw new IllegalArgumentException();
			}
			int x1 = pieceCoordinates.get(startingPosition)[0] + PIECE_RADIUS / 2;
			int x2 = pieceCoordinates.get(destination)[0] + PIECE_RADIUS / 2
					+ determineOffset(x1, pieceCoordinates.get(destination)[0]);
			int y1 = pieceCoordinates.get(startingPosition)[1] + PIECE_RADIUS / 2;
			int y2 = pieceCoordinates.get(destination)[1] + PIECE_RADIUS / 2
					+ determineOffset(y1, pieceCoordinates.get(destination)[1]);
			Graphics2D g2d = currentMap.createGraphics();
			Color color;
			if (successful) {
				color = Color.BLACK;
			} else {
				color = Color.RED;
			}
			g2d.setColor(color);
			g2d.setStroke(new BasicStroke(3));
			g2d.drawLine(x1, y1, x2, y2);
			AffineTransform tx = new AffineTransform();
			Polygon arrowHead = new Polygon();
			arrowHead.addPoint(0, 5);
			arrowHead.addPoint(-5, -5);
			arrowHead.addPoint(5, -5);
			double angle = Math.atan2(y2 - y1, x2 - x1);
			tx.translate(x2, y2);
			tx.rotate((angle - Math.PI / 2d));
			g2d.setTransform(tx);
			g2d.fill(arrowHead);
			g2d.dispose();
			container.repaint();
		}
	}

	public void drawHold(String position, boolean successful) throws IllegalArgumentException {
		if (show) {
			if (!pieceCoordinates.containsKey(position)) {
				throw new IllegalArgumentException();
			}
			int x = pieceCoordinates.get(position)[0] - (HOLD_RADIUS - PIECE_RADIUS) / 2;
			int y = pieceCoordinates.get(position)[1] - (HOLD_RADIUS - PIECE_RADIUS) / 2;
			Graphics2D g2d = currentMap.createGraphics();
			Color color;
			if (successful) {
				color = Color.BLACK;
			} else {
				color = Color.RED;
			}
			g2d.setColor(color);
			g2d.setStroke(new BasicStroke(3));
			g2d.drawOval(x, y, HOLD_RADIUS, HOLD_RADIUS);
			g2d.dispose();
			container.repaint();
		}
	}

	// For move-supports
	public void drawSupport(String piecePos, String suppPiecePos, String suppPieceDest, boolean successful)
			throws IllegalArgumentException {
		if (show) {
			if (!pieceCoordinates.containsKey(piecePos) || !pieceCoordinates.containsKey(suppPiecePos)
					|| !pieceCoordinates.containsKey(suppPieceDest)) {
				throw new IllegalArgumentException();
			}
			int x1 = pieceCoordinates.get(piecePos)[0] + PIECE_RADIUS / 2;
			int x2 = pieceCoordinates.get(suppPiecePos)[0] + PIECE_RADIUS / 2
					+ determineOffset(x1, pieceCoordinates.get(suppPiecePos)[0]);
			int x3 = pieceCoordinates.get(suppPieceDest)[0] + PIECE_RADIUS / 2
					+ determineOffset(x2, pieceCoordinates.get(suppPieceDest)[0]);
			int y1 = pieceCoordinates.get(piecePos)[1] + PIECE_RADIUS / 2;
			int y2 = pieceCoordinates.get(suppPiecePos)[1] + PIECE_RADIUS / 2
					+ determineOffset(y1, pieceCoordinates.get(suppPiecePos)[1]);
			int y3 = pieceCoordinates.get(suppPieceDest)[1] + PIECE_RADIUS / 2
					+ determineOffset(y2, pieceCoordinates.get(suppPieceDest)[1]);
			Graphics2D g2d = currentMap.createGraphics();
			Stroke dashed = new BasicStroke(3, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[] { 9 }, 0);
			Color color;
			if (successful) {
				color = Color.BLACK;
			} else {
				color = Color.RED;
			}
			g2d.setColor(color);
			g2d.drawOval(x3, y3, PIECE_RADIUS / 2, PIECE_RADIUS / 2);
			g2d.setStroke(dashed);
			g2d.drawLine(x1, y1, x2, y2);
			g2d.drawLine(x2, y2, x3, y3);
			g2d.dispose();
			container.repaint();
		}
	}

	// For hold-supports
	public void drawSupport(String piecePos, String suppPiecePos, boolean successful) throws IllegalArgumentException {
		if (show) {
			if (!pieceCoordinates.containsKey(piecePos) || !pieceCoordinates.containsKey(suppPiecePos)) {
				throw new IllegalArgumentException();
			}
			int x1 = pieceCoordinates.get(piecePos)[0] + PIECE_RADIUS / 2;
			int x2 = pieceCoordinates.get(suppPiecePos)[0] + PIECE_RADIUS / 2;
			int y1 = pieceCoordinates.get(piecePos)[1] + PIECE_RADIUS / 2;
			int y2 = pieceCoordinates.get(suppPiecePos)[1] + PIECE_RADIUS / 2;
			Graphics2D g2d = currentMap.createGraphics();
			Stroke dashed = new BasicStroke(3, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[] { 9 }, 0);
			Color color;
			if (successful) {
				color = Color.BLACK;
			} else {
				color = Color.RED;
			}
			g2d.setColor(color);
			g2d.setStroke(dashed);
			g2d.drawLine(x1, y1, x2, y2);
			g2d.dispose();
			container.repaint();
		}
	}

	public void drawConvoy(String position, boolean successful) throws IllegalArgumentException {
		if (show) {
			if (!pieceCoordinates.containsKey(position)) {
				throw new IllegalArgumentException();
			}
			int x1 = pieceCoordinates.get(position)[0] - PIECE_RADIUS / 5;
			int x2 = x1 + PIECE_RADIUS + PIECE_RADIUS / 2;
			int y = pieceCoordinates.get(position)[1] - PIECE_RADIUS / 5;
			Graphics2D g2d = currentMap.createGraphics();
			Color color;
			if (successful) {
				color = Color.BLACK;
			} else {
				color = Color.RED;
			}
			g2d.setColor(color);
			g2d.setStroke(new BasicStroke(3));
			g2d.drawLine(x1, y, x2, y);
			g2d.dispose();
			container.repaint();
		}
	}

	private int determineOffset(int startValue, int destinationValue) {
		int offset;
		if (startValue < destinationValue) {
			offset = -PIECE_RADIUS / 2;
		} else {
			offset = PIECE_RADIUS / 2;
		}
		return offset;
	}

	private void fillDataStructures() {
		// Set empireColors
		empireColors.put("Austria", new Color(220, 0, 0));
		empireColors.put("England", new Color(0, 0, 172));
		empireColors.put("France", new Color(161, 161, 255));
		empireColors.put("Germany", new Color(0, 0, 0));
		empireColors.put("Italy", new Color(0, 173, 0));
		empireColors.put("Russia", new Color(197, 0, 197));
		empireColors.put("Turkey", new Color(201, 201, 0));
		// Set centerCoordinates
		centerCoordinates.put("Stp", new Integer[] { 615, 277 });
		centerCoordinates.put("Mos", new Integer[] { 708, 346 });
		centerCoordinates.put("Sev", new Integer[] { 711, 585 });
		centerCoordinates.put("Ank", new Integer[] { 710, 693 });
		centerCoordinates.put("Smy", new Integer[] { 624, 742 });
		centerCoordinates.put("Con", new Integer[] { 624, 702 });
		centerCoordinates.put("Bul", new Integer[] { 555, 657 });
		centerCoordinates.put("Gre", new Integer[] { 556, 750 });
		centerCoordinates.put("Rum", new Integer[] { 592, 611 });
		centerCoordinates.put("Ser", new Integer[] { 504, 620 });
		centerCoordinates.put("Bud", new Integer[] { 479, 556 });
		centerCoordinates.put("War", new Integer[] { 509, 447 });
		centerCoordinates.put("Vie", new Integer[] { 443, 537 });
		centerCoordinates.put("Tri", new Integer[] { 417, 585 });
		centerCoordinates.put("Ber", new Integer[] { 413, 441 });
		centerCoordinates.put("Swe", new Integer[] { 475, 290 });
		centerCoordinates.put("Nwy", new Integer[] { 397, 277 });
		centerCoordinates.put("Den", new Integer[] { 400, 373 });
		centerCoordinates.put("Kie", new Integer[] { 373, 412 });
		centerCoordinates.put("Mun", new Integer[] { 379, 531 });
		centerCoordinates.put("Ven", new Integer[] { 383, 587 });
		centerCoordinates.put("Rom", new Integer[] { 394, 663 });
		centerCoordinates.put("Nap", new Integer[] { 409, 693 });
		centerCoordinates.put("Tun", new Integer[] { 323, 782 });
		centerCoordinates.put("Hol", new Integer[] { 301, 420 });
		centerCoordinates.put("Bel", new Integer[] { 272, 451 });
		centerCoordinates.put("Mar", new Integer[] { 273, 617 });
		centerCoordinates.put("Par", new Integer[] { 253, 494 });
		centerCoordinates.put("Lon", new Integer[] { 237, 430 });
		centerCoordinates.put("Edi", new Integer[] { 225, 324 });
		centerCoordinates.put("Lvp", new Integer[] { 210, 380 });
		centerCoordinates.put("Bre", new Integer[] { 154, 477 });
		centerCoordinates.put("Spa", new Integer[] { 116, 639 });
		centerCoordinates.put("Por", new Integer[] { 20, 642 });
		// Set pieceCoordinates
		// Oceans
		pieceCoordinates.put("BAR", new Integer[] { 599, 32 });
		pieceCoordinates.put("NWG", new Integer[] { 367, 88 });
		pieceCoordinates.put("NTH", new Integer[] { 294, 291 });
		pieceCoordinates.put("HEL", new Integer[] { 321, 365 });
		pieceCoordinates.put("SKA", new Integer[] { 384, 313 });
		pieceCoordinates.put("BAL", new Integer[] { 470, 348 });
		pieceCoordinates.put("BOT", new Integer[] { 504, 295 });
		pieceCoordinates.put("ENG", new Integer[] { 153, 446 });
		pieceCoordinates.put("IRI", new Integer[] { 107, 400 });
		pieceCoordinates.put("NAO", new Integer[] { 66, 198 });
		pieceCoordinates.put("MAO", new Integer[] { 36, 466 });
		pieceCoordinates.put("WES", new Integer[] { 166, 723 });
		pieceCoordinates.put("LYO", new Integer[] { 246, 639 });
		pieceCoordinates.put("TYS", new Integer[] { 341, 690 });
		pieceCoordinates.put("ION", new Integer[] { 444, 791 });
		pieceCoordinates.put("ADR", new Integer[] { 413, 638 });
		pieceCoordinates.put("AEG", new Integer[] { 577, 775 });
		pieceCoordinates.put("EAS", new Integer[] { 713, 798 });
		pieceCoordinates.put("BLA", new Integer[] { 655, 622 });
		// Lands
		// first row
		pieceCoordinates.put("Stp", new Integer[] { 640, 224 });
		pieceCoordinates.put("Stp-nc", new Integer[] { 614, 84 });
		pieceCoordinates.put("Stp-sc", new Integer[] { 596, 295 });
		pieceCoordinates.put("Fin", new Integer[] { 558, 196 });
		pieceCoordinates.put("Swe", new Integer[] { 471, 173 });
		pieceCoordinates.put("Nwy", new Integer[] { 396, 225 });
		pieceCoordinates.put("Den", new Integer[] { 369, 361 });
		// england
		pieceCoordinates.put("Edi", new Integer[] { 221, 304 });
		pieceCoordinates.put("Cly", new Integer[] { 201, 273 });
		pieceCoordinates.put("Lvp", new Integer[] { 204, 354 });
		pieceCoordinates.put("Yor", new Integer[] { 227, 357 });
		pieceCoordinates.put("Lon", new Integer[] { 230, 411 });
		pieceCoordinates.put("Wal", new Integer[] { 190, 415 });
		// second row
		pieceCoordinates.put("Mos", new Integer[] { 757, 359 });
		pieceCoordinates.put("Lvn", new Integer[] { 554, 351 });
		pieceCoordinates.put("Pru", new Integer[] { 446, 417 });
		pieceCoordinates.put("Ber", new Integer[] { 400, 413 });
		pieceCoordinates.put("Kie", new Integer[] { 349, 440 });
		pieceCoordinates.put("Hol", new Integer[] { 291, 434 });
		// third row
		pieceCoordinates.put("Sev", new Integer[] { 714, 495 });
		pieceCoordinates.put("Ukr", new Integer[] { 613, 465 });
		pieceCoordinates.put("Gal", new Integer[] { 555, 505 });
		pieceCoordinates.put("War", new Integer[] { 494, 464 });
		pieceCoordinates.put("Sil", new Integer[] { 435, 459 });
		pieceCoordinates.put("Boh", new Integer[] { 415, 490 });
		pieceCoordinates.put("Mun", new Integer[] { 354, 499 });
		pieceCoordinates.put("Ruh", new Integer[] { 318, 464 });
		pieceCoordinates.put("Bel", new Integer[] { 280, 459 });
		pieceCoordinates.put("Bur", new Integer[] { 279, 524 });
		pieceCoordinates.put("Par", new Integer[] { 225, 505 });
		pieceCoordinates.put("Pic", new Integer[] { 233, 470 });
		pieceCoordinates.put("Bre", new Integer[] { 183, 490 });
		// fourth row
		pieceCoordinates.put("Rum", new Integer[] { 554, 608 });
		pieceCoordinates.put("Bud", new Integer[] { 509, 537 });
		pieceCoordinates.put("Vie", new Integer[] { 458, 524 });
		pieceCoordinates.put("Tyr", new Integer[] { 408, 547 });
		// final row
		pieceCoordinates.put("Arm", new Integer[] { 832, 657 });
		pieceCoordinates.put("Syr", new Integer[] { 826, 736 });
		pieceCoordinates.put("Ank", new Integer[] { 710, 664 });
		pieceCoordinates.put("Smy", new Integer[] { 723, 737 });
		pieceCoordinates.put("Con", new Integer[] { 649, 692 });
		pieceCoordinates.put("Bul", new Integer[] { 569, 640 });
		pieceCoordinates.put("Bul-ec", new Integer[] { 608, 629 });
		pieceCoordinates.put("Bul-sc", new Integer[] { 578, 673 });
		pieceCoordinates.put("Ser", new Integer[] { 506, 637 });
		pieceCoordinates.put("Gre", new Integer[] { 523, 705 });
		pieceCoordinates.put("Alb", new Integer[] { 496, 695 });
		pieceCoordinates.put("Tri", new Integer[] { 436, 596 });
		pieceCoordinates.put("Ven", new Integer[] { 367, 595 });
		pieceCoordinates.put("Apu", new Integer[] { 417, 677 });
		pieceCoordinates.put("Nap", new Integer[] { 435, 740 });
		pieceCoordinates.put("Rom", new Integer[] { 372, 657 });
		pieceCoordinates.put("Tus", new Integer[] { 359, 632 });
		pieceCoordinates.put("Pie", new Integer[] { 312, 581 });
		pieceCoordinates.put("Mar", new Integer[] { 269, 585 });
		pieceCoordinates.put("Gas", new Integer[] { 192, 560 });
		pieceCoordinates.put("Spa", new Integer[] { 97, 663 });
		pieceCoordinates.put("Spa-sc", new Integer[] { 102, 700 });
		pieceCoordinates.put("Spa-nc", new Integer[] { 92, 577 });
		pieceCoordinates.put("Por", new Integer[] { 47, 614 });
		// North africa
		pieceCoordinates.put("Naf", new Integer[] { 72, 786 });
		pieceCoordinates.put("Tun", new Integer[] { 299, 796 });
	}
}
