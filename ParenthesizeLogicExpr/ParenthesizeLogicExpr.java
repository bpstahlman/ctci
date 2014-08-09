
// From Cracking the Coding Interview (Question 9.11)
// Completed on 09 Aug 2014: Appears to be working, though it's no doubt
// longer than the solution in ctci (which I haven't checked yet).

import java.util.*;
import java.io.*;
import java.util.regex.*;


class Stack {
    StringBuilder sb;
    Stack(Stack stack) {
        sb = new StringBuilder(stack.sb.toString());
    }
    Stack(String s) {
        sb = new StringBuilder(s);
    }
    Stack() {
        sb = new StringBuilder();
    }
    boolean isOp(char ch) {
        return ch == '^' || ch == '|' || ch == '&';
    }
    boolean isVal(char ch) {
        return ch == '0' || ch == '1';
    }
    public String toString() {
        return sb.toString();
    }
    int findStart(int end) {
        int i = end;
        for (int nest = 0; ; i--) {
            char c = sb.charAt(i);
            if (c == ')')
                nest++;
            else if (c == '(')
                nest--;
            if ((isVal(c) || c == '(') && nest == 0)
                // Found start of previous term.
                break;
        }
        return i;
    }
    boolean reduce(boolean isFinal) {
        // Perform a single reduction if possible.
        int i = sb.length() - 1;
        i = findStart(i);
        i -= 2;
        i = findStart(i);

        // Note: In interest of time, skip error detection...
        // Wrap with parens to indicate reduction.
        if (i > 0 || !isFinal) {
            sb.insert(i, '(');
            sb.append(')');
        }
        // Return true iff further reduction is possible.
        return i != 0;
    }
    void push(String s) {
        sb.append(s);
    }
    void push(char ch) {
        sb.append(ch);
    }

}
public class ParenthesizeLogicExpr {
    private HashMap<String, Integer> binExprs = new HashMap<>();
    {
        binExprs.put("0&0", 0);
        binExprs.put("0&1", 0);
        binExprs.put("1&0", 0);
        binExprs.put("1&1", 1);
        binExprs.put("0|0", 0);
        binExprs.put("0|1", 1);
        binExprs.put("1|0", 1);
        binExprs.put("1|1", 1);
        binExprs.put("0^0", 0);
        binExprs.put("0^1", 1);
        binExprs.put("1^0", 1);
        binExprs.put("1^1", 0);
    }
    void build(String expr, Set<String> pexprs) {
        if (expr.length() == 1) {
            // Shouldn't happen...
            pexprs.add(expr);
            return;
        }
        // Skip validation...
        String stack = expr.substring(0, 2);
        build(2, expr, stack, pexprs);

    }
    void build(int i, String expr, String stack, Set<String> pexprs) {
        int ln = expr.length();
        Stack stackLeft = new Stack(stack), stackRight = null;

        if (i < ln) {
            // Get a val and the subsequent operator (if applicable)
            char val = expr.charAt(i++);
            Character op = null;
            if (i < ln)
                op = expr.charAt(i++);
            // Push the val in preparation for left reduction(s).
            stackLeft.push(val);
            boolean canReduce;
            // If this is not the penultimate call (i.e., the one that
            // processes final val) recurse on all possible reductions (some
            // of them incomplete).
            // Example:
            //   stack: 0^0^0^0
            //   reductions:
            //     0^0^(0^0)
            //     0^(0^(0^0))
            // If the expression contains more than 4 vals, each of the
            // reductions shown above would be used as the stack in a distinct
            // recursion.
            // Note: isFinal arg passed to reduce ensures we don't get useless
            // parens around entire expression.
            do {
                canReduce = stackLeft.reduce(i >= ln);
                if (!canReduce || i < ln)
                    build(i, expr, stackLeft.toString() + (i < ln ? op : ""), pexprs);
            } while(canReduce);

            // Note: Right-associativity is not applicable to final val.
            if (i < ln) {
                // Push right
                // This is what ensures the left recursion loop above will
                // have multiple recursions for a given stack.
                stackRight = new Stack(stack);
                stackRight.push(val);
                stackRight.push(op);
                build(i, expr, stackRight.toString(), pexprs);
            }

        } else {
            pexprs.add(stack);
        }
    }

    // Convert expression like "0|(1&0)" to "0"
    String replaceAll(String expr) {
        Pattern p = Pattern.compile("(?x) \\(? ([01] [|&^] [01]) \\)?");
        boolean didReplace;
        StringBuffer sb;
        do {
            Matcher m = p.matcher(expr);
            didReplace = false;
            sb = new StringBuffer();
            while (m.find()) {
                didReplace = true;
                String k = m.group(1);
                Integer v = binExprs.get(k);
                m.appendReplacement(sb, v.toString());
            }
            m.appendTail(sb);
            expr = sb.toString();
        } while (didReplace);
        return sb.toString();
    }

    // TODO: For practice, also use straight iterator, rather than Collection
    // stream filter...
    void filterBang(Set<String> pexprs, int value) {
        for (Iterator<String> iter = pexprs.iterator(); iter.hasNext(); ) {
            String expr = iter.next();
            expr = replaceAll(expr);
            if (Integer.parseInt(expr) != value)
                iter.remove();
        }
    }
    TreeSet<String> filter(Set<String> pexprs, int value) {
        TreeSet<String> ts = pexprs.stream().filter(expr -> {
            expr = replaceAll(expr);
            return Integer.parseInt(expr) == value;
        }).collect(TreeSet::new, TreeSet::add, TreeSet::addAll);
        return ts;
    }

    // Usage:
    // java ParenthesizeLogicExpr EXPR VALUE
    // Example:
    // java ParenthesizeLogicExpr "1|1^1&1" 1
    // Output:
    // (1|(1^1))&1
    // 1|((1^1)&1)
    // 1|(1^(1&1))
    public static void main(String[] args) {
        String expr = args[0];
        int value = Integer.parseInt(args[1]);
        ParenthesizeLogicExpr ob = new ParenthesizeLogicExpr();
        Set<String> pexprs = new TreeSet<String>();
        // Recurse to find all combinations.
        ob.build(expr, pexprs);
        // Throw out the expressions that don't evaluate to the specified
        // value.
        // Filter using collection streams
        //pexprs = ob.filter(pexprs, value);
        // Filter destructively using simple iterator.
        ob.filterBang(pexprs, value);
        System.out.println("Original expression: " + expr);
        System.out.println("Desired result: " + value);
        System.out.println("Parenthesizatons yielding desired result:");
        for (String s : pexprs)
            System.out.println(s);
        System.out.println("-----------------------------------------");
        System.out.println("Total: " + pexprs.size());


    }

}
// vim:ts=4:sw=4:et:tw=78
