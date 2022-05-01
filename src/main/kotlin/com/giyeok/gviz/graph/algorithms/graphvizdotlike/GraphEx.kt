package com.giyeok.gviz.graph.algorithms.graphvizdotlike

import com.giyeok.gviz.graph.BaseGraph
import com.giyeok.gviz.graph.Edge
import java.util.concurrent.atomic.AtomicInteger

data class GraphEx(
  val graph: BaseGraph,
  val edgeWeights: Map<String, Double>,
  val edgeMinLengths: Map<String, Int>,
  val virtualNodes: Set<String> = setOf(),
  val virtualEdges: Set<String> = setOf(),
  // `graph`의 node id -> 이 노드가 merge/치환한 원본 그래프의 node id들
  // (원본 그래프의 node id가 아닐 수도 있음)
  val replacedNodes: Map<String, Set<String>> = mapOf(),
  // `graph`의 edge id -> 이 엣지가 merge/치환한 원본 그래프의 edge id들
  // (원본 그래프의 edge id가 아닐 수도 있음)
  val replacedEdges: Map<String, Set<String>> = mapOf(),
  val oldNodes: Set<String> = setOf(),
  val oldEdges: Map<String, Edge> = mapOf(),
  val splitEdges: Map<String, List<String>> = mapOf(),
  private val idCounter: AtomicInteger = AtomicInteger(0),
) {
  fun newId(): String {
    var nextId = "id${idCounter.incrementAndGet()}"
    while (graph.nodes.contains(nextId) || graph.directedEdges.contains(nextId)) {
      nextId = "id${idCounter.incrementAndGet()}"
    }
    return nextId
  }

  fun checkIsNewNode(nodeName: String) {
    check(!graph.nodes.contains(nodeName))
    check(!virtualNodes.contains(nodeName))
    check(!oldNodes.contains(nodeName))
    check(!replacedNodes.containsKey(nodeName))
    check(!replacedNodes.any { it.value.contains(nodeName) })
  }

  fun checkIsNewEdge(edgeName: String) {
    check(!graph.edgeLabels.contains(edgeName))
    check(!virtualEdges.contains(edgeName))
    check(!oldEdges.containsKey(edgeName))
    check(!replacedEdges.containsKey(edgeName))
    check(!replacedEdges.any { it.value.contains(edgeName) })
  }

  fun BaseGraph.replaceNodes(nodesToReplace: Set<String>, newNodeName: String): BaseGraph =
    BaseGraph(
      (this.nodes - nodesToReplace) + newNodeName,
      this.directedEdges.mapValues { it.value.replaceNodes(nodesToReplace, newNodeName) },
      this.undirectedEdges.mapValues { it.value.replaceNodes(nodesToReplace, newNodeName) },
    )

  fun Edge.replaceNodes(nodesToReplace: Set<String>, newNodeName: String): Edge {
    val newStart = if (nodesToReplace.contains(this.start)) newNodeName else this.start
    val newEnd = if (nodesToReplace.contains(this.end)) newNodeName else this.end
    return Edge(newStart, newEnd)
  }

  fun mergeNodes(nodes: Set<String>, newNodeName: String): GraphEx {
    check(nodes.isNotEmpty())
    checkIsNewNode(newNodeName)
    val newGraph = graph.replaceNodes(nodes, newNodeName)
    return copy(
      graph = newGraph,
      replacedNodes = replacedNodes + (newNodeName to nodes),
      oldNodes = oldNodes + nodes
    )
  }

  fun edgeWeight(edgeName: String) = edgeWeights[edgeName] ?: 1.0

  fun edgeMinLength(edgeName: String) = edgeMinLengths[edgeName] ?: 1

  fun mergeDirectedEdges(edges: Set<String>, newEdgeName: String): GraphEx {
    check(edges.isNotEmpty())
    checkIsNewEdge(newEdgeName)
    val firstEdge = graph.directedEdges.getValue(edges.first())
    check(edges.all { graph.directedEdges.getValue(it) == firstEdge })
    val newEdgeWeight = edges.sumOf { edgeWeight(it) }
    val newEdgeMinLength = edges.maxOf { edgeMinLength(it) }
    val newGraph = BaseGraph(
      nodes = graph.nodes,
      directedEdges = graph.directedEdges - edges + (newEdgeName to firstEdge),
      undirectedEdges = graph.undirectedEdges
    )
    return copy(
      graph = newGraph,
      edgeWeights = edgeWeights - edges + (newEdgeName to newEdgeWeight),
      edgeMinLengths = edgeMinLengths - edges + (newEdgeName to newEdgeMinLength),
      virtualEdges = virtualEdges + newEdgeName,
      replacedEdges = replacedEdges + (newEdgeName to edges),
      oldEdges = oldEdges +
        (edges.map { edgeName -> edgeName to graph.directedEdges.getValue(edgeName) })
    )
  }

  fun reverseEdge(edgeName: String, newEdgeName: String): GraphEx {
    checkIsNewEdge(newEdgeName)
    val edge = graph.directedEdges.getValue(edgeName)
    return copy(
      graph = BaseGraph(
        nodes = graph.nodes,
        directedEdges = graph.directedEdges - edgeName + (newEdgeName to edge.reverse()),
        undirectedEdges = graph.undirectedEdges,
      ),
      replacedEdges = replacedEdges + (newEdgeName to setOf(edgeName)),
      oldEdges = oldEdges + (edgeName to edge)
    )
  }

  fun addVirtualEdge(newEdgeName: String, edge: Edge): GraphEx {
    checkIsNewEdge(newEdgeName)
    return copy(
      graph = BaseGraph(
        nodes = graph.nodes,
        directedEdges = graph.directedEdges + (newEdgeName to edge),
        undirectedEdges = graph.undirectedEdges
      ),
      virtualEdges = virtualEdges + newEdgeName
    )
  }

  fun addVirtualEdge(edge: Edge): GraphEx = addVirtualEdge(newId(), edge)

  fun splitEdge(edge: String, newEdges: List<Edge>): GraphEx {
    // newEdges는 모두 virtual edge들
    TODO()
  }

  fun slackOfEdge(edgeName: String, ranks: Map<String, Int>): Int {
    val edge = graph.directedEdges.getValue(edgeName)
    val length = ranks.getValue(edge.end) - ranks.getValue(edge.start)
    return length - edgeMinLength(edgeName)
  }
}
