import java.util.*;

public class FirstFollowSet {

    /*
     * calculate First Set
     * using fixed point method
     */
    public static Map<GrammarNode, Set<String>> calFirstSets(GrammarNode grammer) {
        Map<GrammarNode, Set<String>> firstSets = new HashMap<GrammarNode, Set<String>>();

        return firstSets;
    }

    /**
     * calculate to FirstSets
     * 
     * @param grammer
     * @param firstSets
     * @return if FirstSet not changed through once calculate, return true
     */
    public static boolean calcFirstSets(GrammerNode grammer, Map<GrammerNode, Set<String>> firstSets) {

    }

    /**
     * calculate followSets
     * 
     * @param grammer
     * @param firstSets
     * @return
     */
    public static Map<GrammerNode, Set<String>> caclFollowSets(GrammerNode grammer,
            Map<GrammerNode, Set<String>> firstSets) {
        Map<GrammarNode, Set<String>> followSets = new HashMap<GrammarNode, Set<String>>();

        return firstSets;
    }

    /**
     * calculate Follow Node
     * @param grammar
     * @param followSets
     * @param rightChildrenSets
     * @param firstSets
     * @param calculated
     * @return
     */
    private static boolean caclFollowSets(GrammarNode grammar, Map<GrammarNode, Set<String>> followSets,
            Map<GrammarNode, Set<GrammarNode>> rightChildrenSets, Map<GrammarNode, Set<String>> firstSets,
            Set<GrammarNode> calculated) {
                return ture;
    }

    public static void dumpFirstFollowSets(Map<GrammarNode Set<String>> sets) {

    }
}