package com.giyeok.gviz.graph

import java.util.*

open class BaseGraph(
  val nodes: Set<String>,
  val directedEdges: Map<String, Edge>,
  val undirectedEdges: Map<String, Edge>,
) {
  init {
    check(directedEdges.keys.intersect(undirectedEdges.keys).isEmpty())
    check(nodes.containsAll(directedEdges.flatMap { listOf(it.value.start, it.value.end) }))
    check(nodes.containsAll(undirectedEdges.flatMap { listOf(it.value.start, it.value.end) }))
  }

  val edgeLabels = directedEdges.keys + undirectedEdges.keys

  fun Map.Entry<String, Edge>.toNamedEdge() = NamedEdge(this.key, this.value)

  val outgoingEdges: Map<String, List<NamedEdge>> =
    (directedEdges.entries.groupBy { it.value.start } +
      ((nodes - directedEdges.values.map { it.start }.toSet()).map { node -> node to listOf() }))
      .mapValues { edges -> edges.value.map { it.toNamedEdge() } }
  val incomingEdges: Map<String, List<NamedEdge>> =
    (directedEdges.entries.groupBy { it.value.end } +
      ((nodes - directedEdges.values.map { it.end }.toSet()).map { node -> node to listOf() }))
      .mapValues { edges -> edges.value.map { it.toNamedEdge() } }
  val connectedEdges: Map<String, List<NamedEdge>> =
    (mergeMaps(
      undirectedEdges.entries.groupBy { it.value.start },
      undirectedEdges.entries.groupBy { it.value.end }
    ) + ((nodes - undirectedEdges.values.flatMap { listOf(it.start, it.end) }.toSet())
      .map { node -> node to listOf() })
      ).mapValues { edges -> edges.value.map { it.toNamedEdge() } }

  fun <T> mergeMaps(
    a: Map<String, List<T>>,
    b: Map<String, List<T>>
  ): Map<String, List<T>> =
    (a.keys + b.keys).associateWith { key -> (a[key] ?: listOf()) + (b[key] ?: listOf()) }

  fun outgoingEdgesFrom(node: String): Set<NamedEdge> {
    val outgoings = (outgoingEdges[node] ?: setOf())
    val connects = (connectedEdges[node] ?: setOf()).map { edge ->
      if (edge.edge.start == node) edge else NamedEdge(edge.edgeName, edge.edge.reverse())
    }
    return (outgoings + connects).toSet()
  }

  // directed edge와 undirected edge를 통틀어서, `node`에서 나가는/연결된 노드를 반환
  fun outgoingsFrom(node: String): Set<String> {
    val outgoings = (outgoingEdges[node] ?: setOf()).map { it.edge.end }.toSet()
    val connects = (connectedEdges[node] ?: setOf()).map {
      if (node == it.edge.start) it.edge.end else it.edge.start
    }.toSet()
    return outgoings + connects
  }

  // directed edge와 undirected edge를 통틀어서, `node`로 들어가는/연결된 노드를 반환
  fun incomingsTo(node: String): Set<String> {
    val incomings = (incomingEdges[node] ?: setOf()).map { it.edge.start }.toSet()
    val connects = (connectedEdges[node] ?: setOf()).map {
      if (node == it.edge.start) it.edge.end else it.edge.start
    }.toSet()
    return incomings + connects
  }

  // 엣지의 종류를 불문하고(node에서 나가든, node로 들어오든, node와 연결된 무방향성 그래프이든) node와 연결된
  // 노드의 목록을 반환
  fun connectedTo(node: String): Set<String> {
    val incomings = (incomingEdges[node] ?: setOf()).map { it.edge.start }.toSet()
    val outgoings = (outgoingEdges[node] ?: setOf()).map { it.edge.end }.toSet()
    val connects = (connectedEdges[node] ?: setOf()).map {
      if (node == it.edge.start) it.edge.end else it.edge.start
    }.toSet()
    return incomings + outgoings + connects
  }

  fun reachableNodesByDirectedEdgesFrom(node: String): Set<String> {
    val nodes = mutableSetOf(node)
    fun traverse(queue: LinkedList<String>) {
      if (queue.isNotEmpty()) {
        val head = queue.pop()
        val outgoings = (outgoingEdges[head] ?: setOf()).map { it.edge.end } - nodes
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
          if (head == it.edge.start) it.edge.end else it.edge.start
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

  fun connectedNodesFrom(node: String): Set<String> {
    val nodes = mutableSetOf(node)
    fun traverse(queue: LinkedList<String>) {
      if (queue.isNotEmpty()) {
        val head = queue.pop()
        val connected = connectedTo(head)
        queue.addAll(connected - nodes)
        nodes.addAll(connected)
        traverse(queue)
      }
    }
    traverse(LinkedList(listOf(node)))
    return nodes
  }

  override fun toString(): String =
    "BaseGraph(nodes=$nodes, directedEdges=$directedEdges, undirectedEdges=$undirectedEdges)"
}
