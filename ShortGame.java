import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;


public class ShortGame {
	public static class Comp {
		public Comp(boolean gt, boolean lt) {
			super();
			this.gt = gt;
			this.lt = lt;
		}
		public boolean gt;
		public boolean lt;
	}
	
	public static HashMap<String, Game> sumMap;
	
	public static class Game {
		public List<Game> l, r;
		public boolean simplified;
		public boolean gt;
		public boolean lt;
		public boolean evaluated;
		
		public Game() {
			l = new ArrayList<Game>();
			r = new ArrayList<Game>();
			simplified = false;
			gt = false;
			lt = false;
			evaluated = false;
		}
		
		public Game inverse() {
			Game ng = new Game();
			for (Game cg : l) {
				ng.r.add(cg.inverse());
			}
			for (Game cg : r) {
				ng.l.add(cg.inverse());
			}
			return ng;
		}

		public String toString() {
			StringBuilder sb = new StringBuilder();
			sb.append("{");
			for (int i = 0; i < l.size(); ++i) {
				sb.append(l.get(i).toString());
				if (i != l.size() - 1)
					sb.append(",");
			}	
			sb.append("|");
			for (int i = 0; i < r.size(); ++i) {
				sb.append(r.get(i).toString());
				if (i != r.size() - 1)
					sb.append(",");
			}	
			sb.append("}");
			
			return sb.toString();
		}
	}

	/**
	 * @param args
	 * @throws IOException 
	 */
	@SuppressWarnings("resource")
	public static void main(String[] args) throws IOException {
		InputStream fis = new FileInputStream("short.in");
		OutputStream fos = new FileOutputStream("short.out");

		Scanner scanner = new Scanner(fis);
		String inStr = scanner.nextLine();
		Game start = null;
		try {
			start = parseGame(inStr, 0);
		} catch (ArrayIndexOutOfBoundsException e) {
			throw new RuntimeException("Wrong symbol in input string (too short)", e);
		}
		
		sumMap = new HashMap<String, Game>();
		
		Game finish = simplify(start);
		
		fos.write(finish.toString().getBytes());
		fos.write("\n".getBytes());
		System.exit(0);
	}

	
	private static Game simplify(Game g) {
		////System.out.println("S_" + g);	
		if (g == null || g.simplified)
			return g; 
	
		boolean someChanged = true;
		while (someChanged) {
			someChanged = false;
			
			Game gmax = g.l.size() > 0 ? g.l.get(0) : null;
			simplify(gmax);
			for (int i = 1; i < g.l.size(); ++i) {
				simplify(g.l.get(i));
				Comp cres = compareGames(g.l.get(i), gmax);
				if (cres.gt) {
					g.l.remove(gmax);
					--i;
					gmax = g.l.get(i);
					someChanged = true;
				}
			}
			
			for (int i = 0; i < g.l.size(); ++i) {
				Game revOpt = isReversible(g.l.get(i), g, false);
				if (revOpt != null) {
					g.l.remove(i);
					g.l.addAll(revOpt.l);
					--i;
					someChanged = true;
				}
			}
		}
		
		someChanged = true;
		while (someChanged) {
			someChanged = false;

			Game gmin = g.r.size() > 0 ? g.r.get(0) : null;;
			simplify(gmin);
			for (int i = 1; i < g.r.size(); ++i) {
				simplify(g.r.get(i));
				Comp cres = compareGames(g.r.get(i), gmin);
				if (cres.lt) {
					g.r.remove(gmin);
					--i;
					gmin = g.r.get(i);
					someChanged = true;
				}
			}
			
			for (int i = 0; i < g.r.size(); ++i) {
				Game revOpt = isReversible(g.r.get(i), g, true);
				if (revOpt != null) {
					g.r.remove(i);
					g.r.addAll(revOpt.r);
					--i;
					someChanged = true;
				}
			}

		}
		
		g.simplified = true;
		return g;
	}
	
	//null - not reversible
	public static Game isReversible(Game opt, Game par, boolean isRight) {
		//System.err.println ("isRev:: " + opt + " " + isRight);
		if (opt == null)
			return null;
		
		if (!isRight) { //L opt
			for (Game cg : opt.r) {
				Comp cres = compareGames(cg, par);
				//System.out.println("        " + cg.toString() + "   " + cres);
				if (cres.lt) {
					return cg;
				}
			} 
		} else { //R opt
			for (Game cg : opt.l) {
				Comp cres = compareGames(cg, par);
				if (cres.gt) {
					return cg;
				}
			}
		}
		
		return null;
	}

	public static Game sum(Game g1, Game g2) {
		if (g1 == null | g2 == null) 
			return null;
		if (g1.l.size() == 0 && g1.r.size() == 0)
			return g2;
		if (g2.l.size() == 0 && g2.r.size() == 0)
			return g1;
		
		String hashStr = g1.toString() + g2.toString();
		if (sumMap.containsKey(hashStr))
			return sumMap.get(hashStr);
		
		Game res = new Game();
		for (Game cg : g1.l) {
			res.l.add(sum(cg, g2));
		}
		for (Game cg : g2.l) {
			res.l.add(sum(g1, cg));
		}
		for (Game cg : g1.r) {
			res.r.add(sum(cg, g2));
		}
		for (Game cg : g2.r) {
			res.r.add(sum(g2, cg));
		}
		
		sumMap.put(hashStr, res);
		return res;
	}
	
	
	public static Comp compareGames(Game g1, Game g2) {
		//System.out.println("_Comp_" + g1 + "_" + g2);
		evalGame(g1);
		evalGame(g2);
		if (g1.gt && g2.lt) {
			if (g1.lt && g2.gt) {
				return new Comp(true, true); 
			}
			return new Comp(true, false); 
		}
		
		if (g1.lt && g2.gt) {
			return new Comp(false, true);
		}
		
		Game delta = sum(g1, g2.inverse());
		//System.out.println("_Comp_" + delta);
		evalGame(delta);
		if (delta.lt) {
			if (delta.gt) {
				return new Comp(true, true); 
			}
			return new Comp(false, true);
		}
		
		if (delta.gt) {
			new Comp(true, false);
		}
		
		return new Comp(false, false);
	}
	
	
	public static void evalGame(Game g) {
		if (g.evaluated)
			return;
		else
			g.evaluated = true;
		
		if (g.l == null && g.r == null) {
			g.gt = true;
			g.lt = true;
			return;
		}
		
		boolean lContGt = false;
		boolean lContLt = false;
		boolean rContGt = false;
		boolean rContLt = false;
		
		for (Game cg : g.l) {
			evalGame(cg);
			if (cg.gt)
				lContGt = true;
			if (cg.lt)
				lContLt = true;
		}
		
		for (Game cg : g.r) {
			evalGame(cg);
			if (cg.gt)
				rContGt = true;
			if (cg.lt)
				rContLt = true;
		}
		
		if (!rContLt && !lContGt) {
			g.gt = true;
			g.lt = true;
			return;
		}
		
		if (!rContLt && lContGt) {
			g.gt = true;
			g.lt = false;
			return;
		}
			
		if (rContLt && !lContGt) {
			g.gt = false;
			g.lt = true;
			return;
		}
			
		g.gt = false;
		g.lt = false;
	}
	

	public static int nextPos; //yes, this is shit
	public static Game parseGame(String gameStr, int pos) {
		Game g = new Game();
		
		if (gameStr.charAt(pos) == '{') {
			Game cgame = parseGame(gameStr, ++pos);
			if (cgame != null)
				g.l.add(cgame);
			pos = nextPos;
		} else {
			System.out.println("Wrong symbol (null game) in input string (start) " + 
					pos + " " + gameStr.charAt(pos) + "  " + gameStr);
			nextPos = pos;
			return null;
		}
		
		while (gameStr.charAt(pos) == ',') {
			Game cgame = parseGame(gameStr, ++pos);
			if (cgame != null)
				g.l.add(cgame);
			pos = nextPos;
		}
		
		if (gameStr.charAt(pos) == '|') {
			Game cgame = parseGame(gameStr, ++pos);
			if (cgame != null)
				g.r.add(cgame);
			pos = nextPos;
		} else {
			throw new RuntimeException("Wrong symbol in input string (mid) " + pos + " " + gameStr.charAt(pos) + "  " + gameStr);
		}
		
		while (gameStr.charAt(pos) == ',') {
			Game cgame = parseGame(gameStr, ++pos);
			if (cgame != null)
				g.r.add(cgame);
			pos = nextPos;
		}
		
		if (gameStr.charAt(pos) == '}') {
			nextPos = pos + 1;
			return g;
		} else {
			throw new RuntimeException("Wrong symbol in input string (end) " + pos + " " + gameStr.charAt(pos) + "  " + gameStr);
		}
	}
}

