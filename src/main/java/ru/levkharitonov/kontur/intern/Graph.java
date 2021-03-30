package ru.levkharitonov.kontur.intern;

import java.util.*;

public class Graph {
    private static Map<String, List<Edge>> graph;

    public Graph() {
        graph = new HashMap<>();
    }

    private List<Edge> getEdges(String from) {
        return graph.get(from);
    }

    public void addRecord(String from, String to, Double rate) {
        if (graph.containsKey(from)) {
            graph.get(from).add(new Edge(to, rate));
        }
        else {
            List<Edge> edges = new ArrayList<>();
            edges.add(new Edge(to, rate));
            graph.put(from, edges);
        }
        if (graph.containsKey(to)) {
            graph.get(to).add(new Edge(from, 1/rate));
        }
        else {
            List<Edge> reverseEdges = new ArrayList<>();
            reverseEdges.add(new Edge(from, 1/rate));
            graph.put(to, reverseEdges);
        }
    }

    public Double getRate(String from, String to) throws IllegalArgumentException {
        if (!graph.containsKey(from) || !graph.containsKey(to)) {
            throw new IllegalArgumentException();
        }
        for (Edge e : this.getEdges(from)) {
            if (e.getDestination().equals(to)) {
                return e.getWeight();
            }
        }
        Edge conv = findConversion(from, to);
        return (conv == null ? null : conv.getWeight());
    }

    /*
     * Using depth-first traversal to find the required conversion rate.
     */
    private Edge findConversion(String from, String to) {
        Edge root = graph.get(from).get(0);
        Double rate = 1.;
        Set<String> visited = new LinkedHashSet<>();
        Stack<Edge> stack = new Stack<>();
        stack.push(root);
        while (!stack.isEmpty()) {
            Edge current = stack.pop();
            if (stack.isEmpty()) {
                rate = current.getWeight();
            }
            else {
                rate *= current.getWeight();
            }
            if (!visited.contains(current.getDestination())) {
                visited.add(current.getDestination());
                for (Edge e : this.getEdges(current.getDestination())) {
                    if(e.getDestination().equals(to)){
                        rate *= e.getWeight();
                        return new Edge(to, rate);
                    }
                    stack.push(e);
                }
            }
        }
        return null;
    }
}
