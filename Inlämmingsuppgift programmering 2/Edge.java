//PROG2 VT2023, Inlämningsuppgift, del 1

import java.io.Serializable;

public class Edge<T> implements Serializable {
    private final T destination;
    private int weight;
    private final T name;

    public Edge(T destination, int weight, T name) {
        this.destination = destination;
        this.name = name;
        this.weight = weight;
    }

    public T getDestination() {
        return this.destination;
    }

    public int getWeight() {
        return this.weight;
    }

    public void setWeight(int w) {
        this.weight = w;
    }

    public T getName() {
        return this.name;
    }

    @Override
    public String toString() {
        return "to " + getDestination()  + " by " + this.getName()+ " takes " + this.getWeight();
    }

}