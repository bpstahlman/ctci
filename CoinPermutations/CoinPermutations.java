
// From Cracking the Coding Interview (Question 9.8)
// Given infinite number of quarters, dimes, nickels and pennies, calculate number of ways to represent n cents.


import java.util.*;


public class CoinPermutations {
    static int[] coinVals = {25, 10, 5, 1};
    static char[] coinSyms = {'Q', 'D', 'N', 'P'};
    String displayCoins(int[] coins) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 4; i++) {
            if (i > 0) sb.append('\t');
            sb.append(String.format("%3d %c", coins[i], coinSyms[i]));
        }
        return sb.toString();
    }
    // Optimization TODO: memoize on i, n
    void makeChange(int i, int n, int[] coins, List<int[]> perms) {
        int coinVal = coinVals[i];
        if (i == 3) {
            // Lowest coin denom - no recursion.
            coins[i] = n;
        } else {
            for (int j = 0; n >= 0; j++, n -= coinVal) {
                coins[i] = j;
                if (n > 0)
                    makeChange(i + 1, n, coins, perms);
                else if (n == 0) {
                    // Made exact change with no need of lower denominations.
                    // Zero out the remaining denoms (and set i to
                    // short-circuit).
                    Arrays.fill(coins, i + 1, 4, 0);
                    i = 3;
                    break;
                }
            }
        }
        if (i == 3)
            perms.add(Arrays.copyOf(coins, coins.length));

    }
    // Usage:
    // java CoinPermutations <total-cents>
    // Example:
    // java CoinPermutations 15
    // Output:
    //   0 Q     0 D     0 N    15 P
    //   0 Q     0 D     1 N    10 P
    //   0 Q     0 D     2 N     5 P
    //   0 Q     0 D     3 N     0 P
    //   0 Q     1 D     0 N     5 P
    //   0 Q     1 D     1 N     0 P
    // Total # of combinations: 6
    public static void main(String[] args) {
        int n = Integer.parseInt(args[0]);
        int[] coins = new int[4];
        List<int[]> perms = new ArrayList<int[]>();
        CoinPermutations ob = new CoinPermutations();
        ob.makeChange(0, Integer.parseInt(args[0]), coins, perms); 
        for (int[] perm : perms)
            System.out.println(ob.displayCoins(perm));
        System.out.println("Total # of combinations: " + perms.size());

    }

}
// vim:ts=4:sw=4:et:tw=78
