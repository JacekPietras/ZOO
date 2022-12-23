package com.jacekpietras.zoo.domain.feature.pathfinder

import java.util.PriorityQueue

// Importing utility classes
// Main class DPQ
class GFG(  // Number of vertices
    private val V: Int
) {
    // Member variables of this class
    private val dist: IntArray = IntArray(V)
    private val settled: MutableSet<Int> = HashSet()
    private val pq: PriorityQueue<Node> = PriorityQueue(V, Node())
    var adj: List<MutableList<Node>>? = null

    // Method 1
    // Dijkstra's Algorithm
    fun dijkstra(adj: List<MutableList<Node>>?, src: Int) {
        this.adj = adj
        for (i in 0 until V) dist[i] = Int.MAX_VALUE

// Add source node to the priority queue
        pq.add(Node(src, 0))

// Distance to the source is 0
        dist[src] = 0
        while (settled.size != V) {

// Terminating condition check when
// the priority queue is empty, return
            if (pq.isEmpty()) return

// Removing the minimum distance node
// from the priority queue
            val u = pq.remove().node

// Adding the node whose distance is
// finalized
            if (settled.contains(u)) // Continue keyword skips execution for
            // following check
                continue

// We don't have to call e_Neighbors(u)
// if u is already present in the settled set.
            settled.add(u)
            e_Neighbours(u)
        }
    }

    // Method 2
    // To process all the neighbours
    // of the passed node
    private fun e_Neighbours(u: Int) {
        var edgeDistance = -1
        var newDistance = -1

// All the neighbors of v
        for (i in adj!![u].indices) {
            val v = adj!![u][i]

// If current node hasn't already been processed
            if (!settled.contains(v.node)) {
                edgeDistance = v.cost
                newDistance = dist[u] + edgeDistance

// If new distance is cheaper in cost
                if (newDistance < dist[v.node]) dist[v.node] = newDistance

// Add the current node to the queue
                pq.add(Node(v.node, dist[v.node]))
            }
        }
    }

    companion object {
        // Main driver method
        @JvmStatic
        fun main(arg: Array<String>) {
            val V = 5
            val source = 0

// Adjacency list representation of the
// connected edges by declaring List class object
// Declaring object of type List<Node>
            val adj: MutableList<MutableList<Node>> = ArrayList()

// Initialize list for every node
            for (i in 0 until V) {
                val item: MutableList<Node> = ArrayList()
                adj.add(item)
            }

// Inputs for the GFG(dpq) graph
            adj[0].add(Node(1, 9))
            adj[0].add(Node(2, 6))
            adj[0].add(Node(3, 5))
            adj[0].add(Node(4, 3))
            adj[2].add(Node(1, 2))
            adj[2].add(Node(3, 4))

// Calculating the single source shortest path
            val dpq = GFG(V)
            dpq.dijkstra(adj, source)

// Printing the shortest path to all the nodes
// from the source node
            println("The shorted path from node :")
            for (i in dpq.dist.indices) println(
                source.toString() + " to " + i + " is "
                        + dpq.dist[i]
            )
        }
    }
} // Class 2

// Helper class implementing Comparator interface
// Representing a node in the graph
class Node : Comparator<Node> {
    // Member variables of this class
    var node = 0
    var cost = 0

    // Constructors of this class
    // Constructor 1
    constructor() {}

    // Constructor 2
    constructor(node: Int, cost: Int) {

// This keyword refers to current instance itself
        this.node = node
        this.cost = cost
    }

    // Method 1
    override fun compare(node1: Node, node2: Node): Int {
        if (node1.cost < node2.cost) return -1
        return if (node1.cost > node2.cost) 1 else 0
    }
}