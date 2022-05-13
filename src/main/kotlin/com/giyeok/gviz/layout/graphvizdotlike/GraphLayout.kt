package com.giyeok.gviz.layout.graphvizdotlike

import com.giyeok.gviz.graph.*
import com.giyeok.gviz.layout.graphvizdotlike.utils.GraphExUtils.adjacentGraph
import com.giyeok.gviz.draw.Position

// TODO subgraph는 이 클래스를 활용해서 구현할 수 있을 것 같음
// -> subgraph를 하나의 큰 노드로 보고 포지셔닝을 한 다음,
//    서브그래프 내의 노드들은 상대 위치로 옮겨준 다음,
//    엣지만 잘 그려주면 되지 않을까?
// TODO 우선은 노드나 엣지를 옮기거나 수정한 경우는 생각하지 말자. 너무 복잡하다
// http://citeseerx.ist.psu.edu/viewdoc/summary?doi=10.1.1.3.8982
// TODO 현재는 그래프가 모두 연결돼있다고 가정. 분리된 그래프는 처리 추가
class GraphLayout(
  // 노드 중에는 node id가 작은 것이 우선순위가 높다고 가정
  override val graph: SizedGraph,
  val mainAxisMinSeparation: Double,
  val subAxisMinSeparation: Double,
  // edge의 weight. 지정하지 않으면 기본값은 1. 음수이면 안됨.
  val edgeWeights: Map<String, Double> = mapOf(),
  // edge의 최소 길이. 지정하지 않으면 기본값은 1. 음수이면 안됨.
  val edgeMinLengths: Map<String, Int> = mapOf(),
  val minRanks: Set<String> = setOf(),
  val maxRanks: Set<String> = setOf(),
  val sameRanks: List<Set<String>> = listOf(),
) : GraphLayoutAlgorithm {
  init {
    // DOT 알고리즘은 방향 그래프만 지원
    check(graph.undirectedEdges.isEmpty())
    check(graph.nodes.containsAll(minRanks))
    check(graph.nodes.containsAll(maxRanks))
    check(graph.edgeLabels.containsAll(edgeWeights.keys))
    check(graph.edgeLabels.containsAll(edgeMinLengths.keys))
    check(minRanks.intersect(maxRanks).isEmpty())
    // TODO minRanks, maxRanks, sameRanks 중에는 겹치는 것이 있으면 안됨
    check(graph.nodes.containsAll(sameRanks.flatten()))
    check(edgeWeights.values.all { it >= 0 })
    check(edgeMinLengths.values.all { it >= 0 })
  }

  val initialGraphEx: GraphEx = GraphEx(graph, edgeWeights, edgeMinLengths)

  fun calculateRanks() =
    RankingAlgorithm(initialGraphEx, edgeWeights, edgeMinLengths, minRanks, maxRanks, sameRanks)
      .calculateRanks()

  // 작은 랭크부터, 각 랭크 내에서 왼쪽(혹은 위쪽)에 나와야할 노드의 ID를 반환.
  fun calculateRankOrders(graph: GraphEx, ranks: Map<String, Int>): List<List<String>> {
    // edge crossing을 줄이는 것이 목표
    // 최적 해를 구하는 것은 NP-complete이기 때문에 휴리스틱을 사용한다

    // 먼저 DFS 등으로 init_order를 만들고,
    // 상위 랭크에서 아래쪽으로 내려가면서
    // 직전 상위 랭크의 순서는 결정되었다고 보고,
    // 정해진 횟수만큼 wmedian과 transpose를 반복해서, 그 중에서 교차하는 엣지의 수가 최소가 되는 순서를 반환한다
    // wmedian은:
    // - 현재 랭크 내의 각 노드 n에 대해,
    // - 직전 상위 랭크에서 n과 인접한 노드들의 위치의 중간값으로 n을 위치시킨다.
    // transpose는:
    // - 현재 랭크 내의 인접한 노드들을 바꾸면 교차하는 엣지의 수가 줄어드는 경우 바꾸고, 아니면 그대로 둔다.

    // 상위 랭크->하위 랭크로 한번, 하위 랭크->상위 랭크로 한번 돌려서 나은 결과를 사용하는게 좋다고 함

    val orders = RankOrdersAlgorithm(graph.graph, ranks).solve()
    return orders.ranks.map { it.nodes }
  }

  fun position(mainAxisCoord: Double, subAxisCoord: Double): Position =
    Position(subAxisCoord, mainAxisCoord)

  fun composeNodeCoords(
    rankOrders: List<List<String>>,
    mainCoords: List<Double>,
    subCoords: Map<String, Double>,
  ): Map<String, Position> {
    check(rankOrders.size == mainCoords.size)
    return rankOrders.zip(mainCoords).flatMap { (rank, mainCoord) ->
      rank.map { node ->
        node to position(mainAxisMinSeparation, subCoords.getValue(node))
      }
    }.toMap()
  }

  override fun layoutGraph(): GraphLayoutResult {
    val ranks = calculateRanks()
    // 인접하지 않은(2이상 떨어진) 랭크 사이의 엣지는 가상 노드를 추가해서 모든 엣지가 인접한 랭크 사이에만 존재하도록 만들기
    val (adjacentGraph, aranks) = adjacentGraph(initialGraphEx, ranks)
    val rankOrders = calculateRankOrders(adjacentGraph, aranks)
    val nodeCoordAlgorithm =
      NodeCoordAlgorithm(adjacentGraph, graph.nodeSizes, rankOrders, 30.0, 20.0)
    val mainAxisCoords = nodeCoordAlgorithm.calculateMainAxisCoords()
    val subAxisCoords = nodeCoordAlgorithm.calculateSubAxisCoords()
    val nodeCoords = composeNodeCoords(rankOrders, mainAxisCoords, subAxisCoords)
    // TODO 중복 엣지 처리
    val edgeSplines = EdgeSplineAlgorithm().calculateEdgeSplines(graph, adjacentGraph, nodeCoords)
    return GraphLayoutResult(nodeCoords.filterKeys { graph.nodes.contains(it) }, edgeSplines)
  }
}
