package com.jacekpietras.zoo.domain.feature.vrp.algorithms;

/**
 * This class is meant for representing the edges, it allows to store
 * the endpoints ids and compare the edges
 */
public class Edge implements Comparable<Edge> {
    /*
     * Instance variables
     */

    // The first node
    private int endPoint1;
    
    // The second node
    private int endPoint2;
    
    public Edge(int a, int b) {
        this.endPoint1 = a > b? a:b;
        this.endPoint2 = a > b? b:a;
    }

    public int get1() {
        return this.endPoint1;
    }

    public int get2() {
        return this.endPoint2;
    }

    public int compareTo(Edge e2) {
        if(this.get1() < e2.get1() || this.get1() == e2.get1() && this.get2() < e2.get2()) {
            return -1;
        } else if (this.equals(e2)) {
            return 0;
        } else {
            return 1;
        }

    }

    public boolean equals(Edge e2) {
    	if(e2 == null) return false;
        return (this.get1() == e2.get1()) && (this.get2() == e2.get2());
    }
    
    public String toString() {
    	return "("+ endPoint1 + ", " + endPoint2 + ")";
    }

}