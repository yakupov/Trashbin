import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;


public class RBHack2 {
	static final BigInteger two = BigInteger.ONE.add(BigInteger.ONE);
	
	static class Edge {
		public Edge(boolean color, int dest) {
			super();
			this.color = color; //false == blue
			this.dest = dest;
		}
		public Edge(boolean color, int src, int dest) {
			super();
			this.color = color;
			this.src = src;
			this.dest = dest;
		}
		public boolean color;
		public int src;
		public int dest;
		
		public String toString() {
			return src + " -" + (color ? "R" : "B") + "-> " + dest;
		}
	}
	
	static class Num {
		BigInteger num;
		BigInteger den;
		
		public static final Num ZERO() {
			return new Num(BigInteger.ZERO, BigInteger.ONE);
		}
		public static final Num ONE() {
			return new Num(BigInteger.ONE, BigInteger.ONE);
		}
		public static final Num NEG_ONE() {
			return new Num(BigInteger.ONE.negate(), BigInteger.ONE);
		}
		
		public Num(BigInteger num, BigInteger den) {
			super();
			this.num = num;
			this.den = den;
		}

		public String toString() {
			return num + " " + den;
		}
		
		public int compare(Num arg) {
			//a/b ? c/d      |*bd
			//ad ? bc
			System.out.println(this + " ? " + arg);
			return num.multiply(arg.den).compareTo(den.multiply(arg.num));
		}
		
		public Num add(Num arg) {
			Num res = new Num(num.multiply(arg.den), den.multiply(arg.den));
			res.num = res.num.add(arg.num.multiply(den));
			
			res.shorten();
			return res;
		}
		
		public void shorten() {
			//System.out.print("s" + two);
			while (!(num.compareTo(BigInteger.ZERO) == 0) &&
					num.divideAndRemainder(two)[1].equals(BigInteger.ZERO) && 
					den.divideAndRemainder(two)[1].equals(BigInteger.ZERO)) {
				if (den.compareTo(BigInteger.ZERO) == 0)
					throw new RuntimeException();
				num = num.divide(two);
				den = den.divide(two);
			}
		}
	}
	
	static ArrayList<Edge>[] graph;
	static ArrayList<Edge> edgList;
	static int[][] posInEdgList;
	//static boolean[] visited;
	static Num[] maskResults;
	
	@SuppressWarnings({ "unchecked", "resource" })
	public static void main(String[] args) throws IOException {
		InputStream fis = new FileInputStream("bluered.in");
		OutputStream fos = new FileOutputStream("bluered.out");
		Scanner scanner = new Scanner(fis);
		
		int vertCount = scanner.nextInt();
		int edgCount = scanner.nextInt();
		
		posInEdgList = new int[vertCount + 1][vertCount + 1];
		for (int i = 0; i < posInEdgList.length; ++i) {
			Arrays.fill(posInEdgList[i], -1);			
		}
		graph = new ArrayList[vertCount + 1];
		for (int i = 0; i <= vertCount; ++i) {
			graph[i] = new ArrayList<Edge>();
		}
		//visited = new boolean[vertCount + 1];
		maskResults = new Num[(int) Math.pow(2, edgCount + 1)];
		edgList = new ArrayList<Edge>();
		
		for (int i = 0; i < edgCount; ++i) {
			int from = scanner.nextInt();
			int to = scanner.nextInt();
			boolean color = scanner.nextInt() == 1; //true == red
			graph[from].add(new Edge(color, from, to));
			//graph[to].add(new Edge(color, to, from)); //TODO: check if we need this symmetry
			
			posInEdgList[from][to] = edgList.size();
			edgList.add(new Edge(color, from, to));
			//posInEdgList[to][from] = edgList.size();
			//edgList.add(new Edge(color, to, from));
		}
		
		int initMask = 0;
		for (int i = 0; i < edgCount; ++i) {
			initMask |= (1 << i);
		}
		fos.write((eval(initMask).toString() + "\n").getBytes());
	}

	static Num eval(int mask) {
		System.out.println("eval.mask " + Integer.toString(mask, 2));		
		if (mask == 0)
			return Num.ZERO();

		ArrayList<Num> redResults = new ArrayList<Num>();
		ArrayList<Num> blueResults = new ArrayList<Num>();
		
		for (int i = 0; i < edgList.size(); ++i) {
			int cbit = (1 << i);
			if ((cbit | mask) == mask) {
				int nmask = cbit ^ mask;
				System.out.println("  eval.cbit " + Integer.toString(cbit, 2));
				System.out.println("  eval.nmask " + Integer.toString(nmask, 2));
				int useMask = normalizeDfs(nmask, 1, 0);
				System.out.println("  eval.useMask " + Integer.toString(useMask, 2));

				nmask &= useMask;
				
				Num cres = null;
				if (maskResults[nmask] != null) {
					cres = maskResults[nmask];
					System.out.println("  eval.evres from arr: " + cres);
				} else {
					cres = eval(nmask);
					System.out.println("  eval.evres from eval: " + cres);
					maskResults[nmask] = cres;
				}
				
				if (edgList.get(i).color)
					redResults.add(cres);
				else
					blueResults.add(cres);
			}
		}
		
		Num maxB = blueResults.size() > 0 ? blueResults.get(0) : null;
		for (Num n : blueResults) {
			if (n.compare(maxB) > 0)
				maxB = n;
			System.out.println("  B "+n);
		}
		
		Num minR = redResults.size() > 0 ? redResults.get(0) : null;
		for (Num n : redResults) {
			if (n.compare(minR) < 0)
				minR = n;
			System.out.println("  R "+n);
		}
		
		System.out.println("  eval.currWorkingMask " + Integer.toString(mask, 2));
		
		if (minR == null) {
			return maxB.add(Num.ONE());
		}
		
		if (maxB == null) {
			return minR.add(Num.NEG_ONE());
		}
		
		if (minR.compare(Num.ZERO()) > 0 && maxB.compare(Num.ZERO()) < 0) {
			return Num.ZERO();
		}
		
		Num lborder = Num.ZERO(), rborder = Num.ZERO();
		if (minR.compare(Num.ZERO()) >= 0 && maxB.compare(Num.ZERO()) >= 0) {
			while (minR.compare(rborder) >= 0) {
				rborder = rborder.add(Num.ONE());				
			}
		} else {
			while (maxB.compare(lborder) <= 0) {
				lborder = lborder.add(Num.NEG_ONE());
			}
		}
		
		System.out.println("maxB "+maxB);
		System.out.println("minR "+minR);
		System.out.println("lborder "+lborder);
		System.out.println("rborder "+rborder);
		System.out.println("mask " + Integer.toString(mask, 2));

		return f(lborder, rborder, maxB, minR);
	}
	
	private static Num f(Num lborder, Num rborder, final Num maxB, final Num minR) {
		while (true) {
			System.out.println("while_1");

			Num nborder = lborder.add(rborder); 
			nborder.den = nborder.den.multiply(two); //(l+r)/2
			
			if (nborder.compare(maxB) > 0 && nborder.compare(minR) < 0)
				return nborder;
			
			if (nborder.compare(maxB) <= 0 && nborder.compare(minR) <= 0) {
				lborder = nborder;
			}
			
			if (nborder.compare(maxB) >= 0 && nborder.compare(minR) >= 0) {
				rborder = nborder;
			}
		}
	}

	private static int normalizeDfs(int mask, int cVertex, int edgUsedMask) {
		for (Edge e : graph[cVertex]) {
			int edgListIndex = posInEdgList[cVertex][e.dest];
			int cbit = (1 << edgListIndex);

			//System.out.println("norm.eCount " + graph[cVertex].size());
			System.out.println("    norm.e " + e);
			//System.out.println("norm.index " + edgListIndex);
			//System.out.println("norm.mask " + Integer.toString(mask, 2));
			System.out.println("    norm.cbit " + Integer.toString(cbit, 2));

			if ((cbit | mask) != mask) //no edge
				continue;
			
			int nUMask = cbit | edgUsedMask;
			if (nUMask == edgUsedMask) //used
				continue;
			
			edgUsedMask = normalizeDfs(mask, e.dest, nUMask);
		}
		return edgUsedMask;
	}
}
