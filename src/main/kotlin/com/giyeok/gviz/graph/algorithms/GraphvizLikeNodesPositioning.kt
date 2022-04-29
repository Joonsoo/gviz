package com.giyeok.gviz.graph.algorithms

import com.giyeok.gviz.graph.NodesPositioningAlgorithm
import com.giyeok.gviz.graph.SizedNodesGraph
import com.giyeok.gviz.render.Position
import java.util.*
import java.util.Comparator.comparing

// TODO subgraph는 이 클래스를 활용해서 구현할 수 있을 것 같음
// -> subgraph를 하나의 큰 노드로 보고 포지셔닝을 한 다음,
//    서브그래프 내의 노드들은 상대 위치로 옮겨준 다음,
//    엣지만 잘 그려주면 되지 않을까?
class GraphvizLikeNodesPositioning(
  // 노드 중에는 node id가 작은 것이 우선순위가 높다고 가정
  override val graph: SizedNodesGraph,
  val minRanks: Set<String> = setOf(),
  val maxRanks: Set<String> = setOf(),
  val sameRanks: List<Set<String>> = listOf(),
  val prelocated: Map<String, Position> = mapOf(),
) : NodesPositioningAlgorithm {
  init {
    check(graph.nodes.containsAll(minRanks))
    check(graph.nodes.containsAll(maxRanks))
    check(minRanks.intersect(maxRanks).isEmpty())
    check(graph.nodes.containsAll(sameRanks.flatten()))
    check(graph.nodes.containsAll(prelocated.keys))
  }

  // 가장 탑 랭킹이 될 노드들을 반환
  fun findRoots(): List<String> {
    val roots = mutableListOf<String>()
    val covered = mutableSetOf<String>()
    fun addRoot(node: String) {
      if (!covered.contains(node)) {
        roots.add(node)
        covered.addAll(graph.reachableNodesFrom(node))
      }
    }

    // directedEdges에서 incoming edge가 가장 적은 노드들
    val directConnectedNodes =
      graph.incomingEdges.values.flatten().flatMap { listOf(it.value.start, it.value.end) }.toSet()
    val nodesByIncomingEdges = graph.incomingEdges
      .filter { directConnectedNodes.contains(it.key) }
      .mapValues { it.value.size }.entries
      .sortedWith(comparing<Map.Entry<String, Int>, Int> { it.value }.thenComparing(comparing { it.key }))
    nodesByIncomingEdges.forEach { rootCandidate ->
      addRoot(rootCandidate.key)
    }

    // 아직 커버되지 않은 노드들에 대해서는, 연결된 undirected edges가 적은 노드들을 루트로
    val undirectConnectedNodes =
      graph.connectedEdges.values.flatten().flatMap { listOf(it.value.start, it.value.end) }.toSet()
    val uncoveredsByConnectedEdges = graph.connectedEdges
      .filter { !directConnectedNodes.contains(it.key) && undirectConnectedNodes.contains(it.key) }
      .mapValues { it.value.size }.entries
      .sortedWith(comparing<Map.Entry<String, Int>?, Int?> { it.value }.thenComparing(comparing { it.key }))
    uncoveredsByConnectedEdges.forEach { rootCandidate ->
      addRoot(rootCandidate.key)
    }

    // 어떤 directed edge와 undirected edge에도 연결되지 않은 노드들은 모두 루트로 추가
    val isolated = graph.nodes - covered
    isolated.sorted().forEach { addRoot(it) }

    return roots
  }

  // 각 노드의 랭크 계산. 랭크가 작을수록 위(혹은 왼쪽)에 나옴.
  // minRanks와 maxRanks는 여기서 반영함. 가장 작은 랭크는 0.
  // sameRanks는 여기서는 반영하지 않고 adjustRanks에서 적용함.
  fun calculateRanks(): Map<String, Int> {
    val ranks = mutableMapOf<String, Int>()

    minRanks.forEach { ranks[it] = 0 }

    val roots = findRoots()
    (roots - ranks.keys).forEach { ranks[it] = 0 }

    fun traverse(queue: LinkedList<String>) {
      if (queue.isNotEmpty()) {
        val head = queue.pop()
        val headRank = ranks.getValue(head)
        val outgoings = graph.outgoingsFrom(head) - ranks.keys
        outgoings.forEach { outgoing -> ranks[outgoing] = headRank + 1 }
        queue.addAll(outgoings)
        traverse(queue)
      }
    }
    traverse(LinkedList(ranks.keys))

    // TODO maxRanks 적용

    return ranks
  }

  // 인자로 받은 `ranks`에 `sameRanks` 적용.
  // `sameRanks`에 의해 같은 랭크에 있어야 한다는 조건이 붙은 두 노드의 랭크가 다르면,
  // 그 중 낮은 랭크(숫자가 큰 쪽)의 노드의 랭크를 높은 랭크(숫자가 작은 쪽)으로 바꿔준다.
  fun adjustRanks(ranks: Map<String, Int>): Map<String, Int> {
    TODO()
  }

  // 작은 랭크부터, 각 랭크 내에서 왼쪽(혹은 위쪽)에 나와야할 노드의 ID를 반환.
  fun calculateRankOrders(ranks: Map<String, Int>): List<List<String>> {
    TODO()
  }

  data class RankOccupation(
    // rank의 높이(혹은 폭)
    val height: Double,
    // rank 안에 속하는 노드들, 순서대로
    val order: List<String>,
    // 각 rank가 이 rank 안에서 차지하는 폭(혹은 높이)
    val occupations: Map<String, Double>
  )

  // 각 랭크 내에서 각 노드가 차지하는 폭(혹은 높이)를 반환.
  fun calculateRankOccupations(rankOrders: List<List<String>>): List<RankOccupation> {
    TODO()
  }

  // 최종적으로 노드 포지셔닝.

  override fun positionNodes(): Map<String, Position> {
    val ranks = calculateRanks()
    val adjustedRanks = adjustRanks(ranks)
    val rankOrders = calculateRankOrders(adjustedRanks)
    val rankOccupations = calculateRankOccupations(rankOrders)
    TODO("Not yet implemented")
  }
}
