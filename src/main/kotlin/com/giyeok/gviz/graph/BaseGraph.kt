package com.giyeok.gviz.graph

import java.util.*

sealed class BaseGraph(
  val nodes: Set<String>,
  val directedEdges: Map<String, Edge>,
  val undirectedEdges: Map<String, Edge>,
) {
  init {
    check(directedEdges.keys.intersect(undirectedEdges.keys).isEmpty())
    check(nodes.containsAll(directedEdges.flatMap { listOf(it.value.start, it.value.end) }))
    check(nodes.containsAll(undirectedEdges.flatMap { listOf(it.value.start, it.value.end) }))
  }

  val outgoingEdges: Map<String, List<Map.Entry<String, Edge>>> =
    directedEdges.entries.groupBy { it.value.start } +
      ((nodes - directedEdges.values.map { it.start }.toSet()).map { node -> node to listOf() })
  val incomingEdges: Map<String, List<Map.Entry<String, Edge>>> =
    directedEdges.entries.groupBy { it.value.end } +
      ((nodes - directedEdges.values.map { it.end }.toSet()).map { node -> node to listOf() })
  val connectedEdges: Map<String, List<Map.Entry<String, Edge>>> =
    undirectedEdges.entries.groupBy { it.value.start } +
      undirectedEdges.entries.groupBy { it.value.end } +
      ((nodes - undirectedEdges.values
        .flatMap { listOf(it.start, it.end) }.toSet())
        .map { node -> node to listOf() })

  fun outgoingEdgesFrom(node: String): Set<NamedEdge> {
    val outgoings = (outgoingEdges[node] ?: setOf())
    val connects = (connectedEdges[node] ?: setOf())
    return (outgoings + connects).map { entry -> NamedEdge(entry.key, entry.value) }.toSet()
  }

  // directed edge와 undirected edge를 통틀어서, `node`에서 나가는/연결된 노드를 반환
  fun outgoingsFrom(node: String): Set<String> {
    val outgoings = (outgoingEdges[node] ?: setOf()).map { it.value.end }.toSet()
    val connects = (connectedEdges[node] ?: setOf()).map {
      if (node == it.value.start) it.value.end else it.value.start
    }.toSet()
    return outgoings + connects
  }

  fun reachableNodesByDirectedEdgesFrom(node: String): Set<String> {
    val nodes = mutableSetOf(node)
    fun traverse(queue: LinkedList<String>) {
      if (queue.isNotEmpty()) {
        val head = queue.pop()
        val outgoings = (outgoingEdges[head] ?: setOf()).map { it.value.end } - nodes
        queue.addAll(outgoings)
        nodes.addAll(outgoings)
        traverse(queue)
      }
    }
    traverse(LinkedList(listOf(node)))
    return nodes
  }

  fun reachableNodesByUndirectedEdgesFrom(node: String): Set<String> {
    val nodes = mutableSetOf(node)
    fun traverse(queue: LinkedList<String>) {
      if (queue.isNotEmpty()) {
        val head = queue.pop()
        val connects = (connectedEdges[head] ?: setOf()).map {
          if (head == it.value.start) it.value.end else it.value.start
        } - nodes
        queue.addAll(connects)
        nodes.addAll(connects)
        traverse(queue)
      }
    }
    traverse(LinkedList(listOf(node)))
    return nodes
  }

  fun reachableNodesFrom(node: String): Set<String> {
    val nodes = mutableSetOf(node)
    fun traverse(queue: LinkedList<String>) {
      if (queue.isNotEmpty()) {
        val head = queue.pop()
        val outgoings = outgoingsFrom(head)
        queue.addAll(outgoings - nodes)
        nodes.addAll(outgoings)
        traverse(queue)
      }
    }
    traverse(LinkedList(listOf(node)))
    return nodes
  }

  class Builder(
    private val nodes: MutableSet<String> = mutableSetOf(),
    private val edges: MutableMap<String, Edge> = mutableMapOf()
  ) {
    fun build(): BaseGraph = TODO()
  }
}
