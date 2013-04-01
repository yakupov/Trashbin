import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Scanner;


public class RBHack1 {
	static final BigInteger two = BigInteger.ONE.add(BigInteger.ONE);
	
	static class Edge {
		public Edge(boolean color, int dest) {
			super();
			this.color = color; //false == blue
			this.dest = dest;
		}
		public boolean color;
		public int dest;
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
	static boolean[] visited;
	
	@SuppressWarnings({ "unchecked", "resource" })
	public static void main(String[] args) throws IOException {
		InputStream fis = new FileInputStream("hackentree.in");
		OutputStream fos = new FileOutputStream("hackentree.out");
		Scanner scanner = new Scanner(fis);
		
		int count = scanner.nextInt(); //vert count
		graph = new ArrayList[count + 1];
		visited = new boolean[count + 1];
		for (int i = 0; i <= count; ++i) {
			graph[i] = new ArrayList<Edge>();
		}

		for (int i = 0; i < count - 1; ++i) {
			int from = scanner.nextInt();
			int to = scanner.nextInt();
			boolean color = scanner.nextInt() == 1; //true == red
			graph[from].add(new Edge(color, to));
			graph[to].add(new Edge(color, from));
		}
		
		fos.write((eval(1).toString() + "\n").getBytes());
	}

	static Num eval(int index) {
		visited[index] = true;
		
		if (graph[index].size() == 0)
			return Num.ZERO();
		
		ArrayList<Num> bOpts = new ArrayList<Num>();
		ArrayList<Num> rOpts = new ArrayList<Num>();
		
		for (Edge e : graph[index]) {
			if(visited[e.dest])
				continue;
			
			Num cres = eval(e.dest);
			if (e.color)
				rOpts.add(cres);
			else
				bOpts.add(cres);
		}
		
		Num res = Num.ZERO(); 
		for (Num n : bOpts) {
			res = res.add(insertBlueStick(n));
		}
		
		for (Num n : rOpts) {
			res = res.add(insertRedStick(n));
		}
		
		return res;
	}
	
	static Num insertBlueStick(Num n) {
		System.out.print("b");
		
		Num added = Num.ONE();
		while (n.add(added).compare(Num.ONE()) <= 0) {
			added.num = added.num.add(BigInteger.ONE);
		}
		
		BigInteger den = BigInteger.ONE; 
		for (BigInteger i = BigInteger.ONE; i.compareTo(added.num) < 0; i = i.add(BigInteger.ONE)) {
			System.out.println(added.num);
			den = den.multiply(two);			
		}
		
		Num res = n.add(added);
		res.den = res.den.multiply(den);
		res.shorten();
		return res;
	}
	
	static Num insertRedStick(Num n) {
		System.out.print("r");
		
		Num added = Num.NEG_ONE();
		while (n.add(added).compare(Num.NEG_ONE()) >= 0) {
			added.num = added.num.subtract(BigInteger.ONE);
		}
		
		BigInteger den = BigInteger.ONE; 
		for (BigInteger i = BigInteger.ONE; i.compareTo(added.num.negate()) < 0; i = i.add(BigInteger.ONE)) {
			System.out.println(added.num.negate());
			den = den.multiply(two);			
		}
		
		Num res = n.add(added);
		res.den = res.den.multiply(den);
		res.shorten();
		return res;
	}
}

