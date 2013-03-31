import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Scanner;


public class ShortGame {
	//public static enum 
	
	public static class Game {
		public ArrayList<Game> l, r;
		
		public Game() {
			l = new ArrayList<Game>();
			r = new ArrayList<Game>();
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
		
		Game finish = simplify(start);
		
		fos.write(finish.toString().getBytes());
		fos.write("\n".getBytes());
		System.exit(0);
	}

	
	private static Game simplify(Game g) {
		if (g == null)
			return g; 
	
		Game gmax = g.l.size() > 0 ? g.l.get(0) : null;
		simplify(gmax);
		Game ng = isReversible(gmax, g, false);
		if (isReversible) {
			if (ng != null) {
				g.l.set(0, ng);
			} else {
				g.l.remove(gmax);
			}
			gmax = g.l.size() > 0 ? g.l.get(0) : null;
		}
		for (int i = 1; i < g.l.size(); ++i) {
			simplify(g.l.get(i));
			int cres = compareGames(g.l.get(i), gmax);
			if (cres >= 0 && cres < 42) {
				g.l.remove(gmax);
				--i;
				gmax = g.l.get(i);
			}
			
			ng = isReversible(g.l.get(i), g, false);
			if (isReversible) {
				if (ng != null) {
					g.l.set(i, ng);
				} else {
					g.l.remove(i);
				}
				gmax = g.l.size() > 0 ? g.l.get(0) : null;
				i = 0;
			}
		}
		
		Game gmin = g.r.size() > 0 ? g.r.get(0) : null;;
		simplify(gmin);
		ng = isReversible(gmin, g, true);
		if (isReversible) {
			if (ng != null) {
				g.r.set(0, ng);
			} else {
				g.r.remove(gmin);
			}
			gmin = g.r.size() > 0 ? g.r.get(0) : null;
		}
		for (int i = 1; i < g.r.size(); ++i) {
			simplify(g.r.get(i));
			int cres = compareGames(g.r.get(i), gmin);
			if (cres <= 0) {
				g.r.remove(gmin);
				--i;
				gmin = g.r.get(i);
			}
			
			ng = isReversible(g.r.get(i), g, true);
			if (isReversible) {
				if (ng != null) {
					g.r.set(i, ng);
				} else {
					g.r.remove(i);
				}
				gmin = g.r.size() > 0 ? g.r.get(0) : null;;
				i = 0;
			}
		}
		
		return g;
	}
	
	//null - not reversible
	static boolean isReversible;
	public static Game isReversible(Game opt, Game par, boolean isRight) {
		isReversible = false;
		if (opt == null)
			return null;
		System.err.println ("isRev:: " + opt.toString() + " " + isRight);
		
		if (!isRight) { //L opt
			for (Game cg : opt.r) {
				int cres = compareGames(cg, par);
				System.out.println("        " + cg.toString() + "   " + cres);
				if (cres <= 0) {
					isReversible = true;
					if (cg.l.size() > 0)
						return opt.l.get(0);
					return null;
				}
			} 
		} else { //R opt
			for (Game cg : opt.l) {
				int cres = compareGames(par, cg);
				if (cres <= 0) {
					isReversible = true;
					if (cg.r.size() > 0)
						return opt.r.get(0);
					return null;
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
		
		return res;
	}
	
	
	public static int compareGames(Game g1, Game g2) {
		Game delta = sum(g1, g2.inverse());
		return evalGame(delta);
	}
	
	
	/**
	 * 42 == * 
	 * @param g
	 * @return
	 */
	public static int evalGame(Game g) {
		if (g.l == null && g.r == null)
			return 0;
		
		boolean lContGt = false;
		boolean lContLt = false;
		boolean rContGt = false;
		boolean rContLt = false;
		
		for (Game cg : g.l) {
			int currRes = evalGame(cg);
			if (currRes >= 0 &&  currRes != 42)
				lContGt = true;
			if (currRes <= 0 &&  currRes != 42)
				lContLt = true;
		}
		
		for (Game cg : g.r) {
			int currRes = evalGame(cg);
			if (currRes >= 0 &&  currRes != 42)
				rContGt = true;
			if (currRes <= 0 &&  currRes != 42)
				rContLt = true;
		}
		
		if (!rContLt && !lContGt)
			return 0;
		
		if (!rContLt && lContGt)
			return 1;
		
		if (rContLt && !lContGt)
			return -1;
		
		return 42;
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

