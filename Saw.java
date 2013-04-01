import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigInteger;
import java.util.Scanner;

public class Saw {
	@SuppressWarnings("resource")
	public static void main(String[] args) throws IOException {
		InputStream fis = new FileInputStream("chocolate.in");
		OutputStream fos = new FileOutputStream("chocolate.out");
		Scanner scanner = new Scanner(fis);
		
		int[][] fld = new int[400][400];
		final int MAX = 350;
		
		int step = 1;
		int afterStep = 0;
		for (int i = 1; i <= MAX; ++i) {
			int cval = 0;
			for (int j = step; j <= MAX; j += step) {
				for (int k = 0; k < step; ++k)
					fld[i][j + k] = cval;
				cval++;
			}
			if (++afterStep >= step) {
				step = (int) Math.pow(2, step);
				afterStep = 0;
			}
		}
		/*
		for (int i = 1; i <= MAX; ++i) {
			for (int j = 1; j <= MAX; j++)
				System.out.printf("%03d ", val[i][j]);
			System.out.println();
		}
		*/
		
		BigInteger ans = BigInteger.ZERO;
		int cnt = scanner.nextInt();
		for (int i = 0; i < cnt; ++i) {
			long cl = eval(fld, scanner.nextInt(), scanner.nextInt());
			System.out.println(cl);
			ans = ans.add(BigInteger.valueOf(cl));
		}
		
		System.out.println(ans);

		if (ans.compareTo(BigInteger.ZERO) <= 0)
			fos.write("Gena".getBytes());
		else
			fos.write("Vova".getBytes());
	}

	public static int eval(int[][] fld, int x, int y) {
		System.out.printf("  eval %d %d\n", x, y);
		
		if (x >= y) {
			return -fld[y][x];
		} else {
			return fld[x][y];
		}
	}
}

