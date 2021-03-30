package ru.levkharitonov.kontur.intern;

public class Edge {
    private final String destination;
    private final Double weight;

    public Edge(String destination, Double weight) {
        this.destination = destination;
        this.weight = weight;
    }

    public String getDestination() {
        return destination;
    }

    public Double getWeight() {
        return weight;
    }
}
