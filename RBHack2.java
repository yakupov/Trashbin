import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;

public class RBHack2 {
	static class Num {
		long num;
		long den;
		
		public static final Num ZERO() {
			return new Num(0L, 1L);
		}
		public static final Num ONE() {
			return new Num(1L, 1L);
		}
		public static final Num NEG_ONE() {
			return new Num(-1L, 1L);
		}
		
		public Num(long num, long den) {
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
			//System.out.println(this + " ? " + arg);
			long l = num * arg.den;
			long r = den * arg.num;
			if (l == r)
				return 0;
			if (l > r)
				return 1;
			return -1;
		}
		
		public Num add(Num arg) {
			Num res = new Num(num * arg.den, den * arg.den);
			res.num = res.num + arg.num * den;
			
			res.shorten();
			return res;
		}
		
		public void shorten() {
			while (num != 0 && num % 2 == 0L && den % 2 == 0L) {
				if (den == 0L)
					throw new RuntimeException();
				num /= 2;
				den /= 2;
			}
		}
	}
	
	static ArrayList<Integer>[] graph;
	static int edgCount;
	static boolean[] colors;
	static int[][] edgIndex;
	static Num[] maskResults;
	static int[] normUseMasks;
	
	@SuppressWarnings({ "unchecked", "resource" })
	public static void main(String[] args) throws IOException {
		InputStream fis = new FileInputStream("bluered.in");
		OutputStream fos = new FileOutputStream("bluered.out");
		Scanner scanner = new Scanner(fis);
		
		int vertCount = scanner.nextInt();
		edgCount = scanner.nextInt();
		
		edgIndex = new int[vertCount + 1][vertCount + 1];
		colors = new boolean[edgCount + 1];
		graph = new ArrayList[vertCount + 1];
		for (int i = 0; i <= vertCount; ++i) {
			graph[i] = new ArrayList<Integer>();
		}
		maskResults = new Num[(int) Math.pow(2, edgCount + 1)];
		
		for (int i = 0; i < edgCount; ++i) {
			int from = scanner.nextInt();
			int to = scanner.nextInt();
			boolean color = scanner.nextInt() == 1; //true == red
			graph[from].add(to);			
			graph[to].add(from);
			edgIndex[from][to] = i;
			edgIndex[to][from] = i;
			colors[i] = color;
		}
		
		normUseMasks = new int[(1 << edgCount)];
		Arrays.fill(normUseMasks, -1);
		normUseMasks[0] = 0;
		for (int i = 1; i < (1 << edgCount); i++) {
			normUseMasks[i] = normalizeDfs(i, 1, 0);
		}
		
		int initMask = (1 << edgCount) - 1;		
		Num res = eval(initMask);
		res.shorten();
		fos.write((res.toString() + "\n").getBytes());
	}

	static Num eval(int mask) {
		//System.out.println("eval.mask " + Integer.toString(mask, 2));		
		if (mask == 0)
			return Num.ZERO();

		//LinkedList<Num> redResults = new LinkedList<Num>();
		Num minR = null;
		//LinkedList<Num> blueResults = new LinkedList<Num>();
		Num maxB = null;
		
		for (int i = 0; i < edgCount; ++i) {
			int cbit = (1 << i);
			if ((cbit | mask) == mask) {
				int nmask = cbit ^ mask;
				//System.out.println("  eval.cbit " + Integer.toString(cbit, 2));
				//System.out.println("  eval.nmask " + Integer.toString(nmask, 2));
				int useMask = normUseMasks[nmask];//normalizeDfs(nmask, 1, 0);
				//System.out.println("  eval.useMask " + Integer.toString(useMask, 2));

				nmask &= useMask;
				
				Num cres = null;
				if (maskResults[nmask] != null) {
					cres = maskResults[nmask];
					//System.out.println("  eval.evres from arr: " + cres);
				} else {
					cres = eval(nmask);
					//System.out.println("  eval.evres from eval: " + cres);
					maskResults[nmask] = cres;
				}
				
				if (colors[i]) {
					//redResults.add(cres);
					if (minR == null || cres.compare(minR) < 0)
						minR = cres;
				} else {
					//blueResults.add(cres);
					if (maxB == null || cres.compare(maxB) > 0)
						maxB = cres;
				}
			}
		}
		
		//System.out.println("  eval.currWorkingMask " + Integer.toString(mask, 2));
		
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
		
		//System.out.println("maxB "+maxB);
		//System.out.println("minR "+minR);
		//System.out.println("lborder "+lborder);
		//System.out.println("rborder "+rborder);
		//System.out.println("mask " + Integer.toString(mask, 2));

		while (true) {
			//System.out.println("while_1");

			Num nborder = lborder.add(rborder); 
			nborder.den = nborder.den * 2; //(l+r)/2
			
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
		if (normUseMasks[mask] >= 0)
			return normUseMasks[mask];
		
		for (int dest : graph[cVertex]) {
			int currIndex = edgIndex[cVertex][dest];
			int cbit = (1 << currIndex);

			////System.out.println("norm.eCount " + graph[cVertex].size());
			//System.out.println("    norm.src dest " + cVertex + " " + dest);
			////System.out.println("norm.index " + edgListIndex);
			//System.out.println("    norm.mask " + Integer.toString(mask, 2));
			//System.out.println("    norm.cbit " + Integer.toString(cbit, 2));

			if ((cbit | mask) != mask) //no edge
				continue;
			
			int nUMask = cbit | edgUsedMask;
			if (nUMask == edgUsedMask) //used
				continue;
			
			edgUsedMask = normalizeDfs(mask, dest, nUMask);
		}
		//System.out.println("    norm.usemask " + Integer.toString(edgUsedMask, 2));
		return edgUsedMask;
	}
}

