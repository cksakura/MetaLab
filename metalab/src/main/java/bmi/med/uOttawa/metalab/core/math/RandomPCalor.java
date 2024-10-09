/**
 * 
 */
package bmi.med.uOttawa.metalab.core.math;

/**
 * @author Kai Cheng
 *
 */
public class RandomPCalor {

	/**
	 * -10*Math.log10(randomprobility)
	 * 
	 * @param randomprobility
	 * 
	 * @return
	 */
	public static double getScore(double randomprobility) {
		return -10.0 * Math.log10(randomprobility);
	}

	/**
	 * -10*Math.log10(randomprobility)
	 * 
	 * @param trials
	 * 
	 * @param success
	 * 
	 * @param singlep
	 * 
	 * @return
	 */
	public static double getScore(int trials, int success, double singlep) {
		return getScore(getProbility(trials, success, singlep));
	}

	/**
	 * 
	 * @param trials
	 * 
	 * @param success
	 * 
	 * @param singlep
	 * 
	 * @return
	 */
	public static double getProbility(int trials, int success, double singlep) {
		double p = 1.0;
		if (success == 0)
			return p;

		p = nk(trials, success) * Math.pow(singlep, success) * Math.pow((1.0 - singlep), (trials - success));
		return p;
	}

	/**
	 * 
	 * (n) (k) = n!/(k!*(n-k)!);
	 * 
	 * @param k&n
	 *            >0;
	 * @return n!/(k!*(n-k)!)
	 */
	public static double nk(int n, int k) {

		if (n <= 0 || n < k || k <= 0)
			throw new RuntimeException("N k format error: n-" + n + ", k-" + k);

		if (k == n)
			return 1;

		return calculatenk(n, k);

	}

	private static double calculatenk(int n, int k) {
		double value = 1.0;
		int j = n - k;

		if (2 * k < n) {
			for (int i = n; i > j; i--) {
				value *= i;
			}

			for (int i = 2; i <= k; i++) {
				value /= i;
			}
		} else {
			for (int i = n; i > k; i--) {
				value *= i;
			}

			for (int i = 2; i <= j; i++) {
				value /= i;
			}
		}

		return value;
	}

}
