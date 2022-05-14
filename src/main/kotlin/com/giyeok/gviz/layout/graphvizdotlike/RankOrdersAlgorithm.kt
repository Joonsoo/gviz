package com.giyeok.gviz.layout.graphvizdotlike

import com.giyeok.gviz.graph.BaseGraph

/**
 * 2단계. 같은 rank의 노드들을 정렬한다.
 *
 * 여기서 입력으로 받는 그래프에서는 모든 엣지 e=v->w에서 rank(v)와 rank(w)의 차이가 1 이하가 되도록 원본 그래프에
 * 임시 노드와 임시 엣지가 추가된 그래프이다.
 *
 * 순서를 결정하는 기준은 교차하는 엣지의 수를 최소화하는 것인데, 이 문제를 완벽하게 푸는 것은 NP-complete 문제여서
 * 휴리스틱을 도입한다.
 *
 * 휴리스틱은
 * 1. DFS나 BFS로 초안을 만든다.
 * 2. 각 노드에 대해, 그 노드와 연결된 상위 랭크 엣지 위치들의 중간값(아래서 자세히 설명)을 계산하고, 같은 랭크 내의
 * 노드들을 계산된 중간값을 기준으로 정렬한다. 이 때 중간값은 그냥 중간값이 아니고 상위 랭크에 연결된 노드가 없으면 -1,
 * 상위 랭크에 연결된 노드가 홀수개이면 실제 중간값을, 상위 랭크에 연결된 노드가 2개이면 두 개 위치의 평균을(정수가
 * 아니게 되고, 그래서 정렬이 필요함), 상위 랭크에 연결된 노드가 4 이상의 짝수개이면 (뭔지 잘 모르겠는..) 수식으로
 * 계산해서 정한다.
 * - 상위 랭크에 연결된 노드가 없으면 -1으로 설정하는 이유는 그런 노드를 최대한 왼쪽으로 보내기 위함
 * 3. 그런 다음에는 각 랭크 내에서 인접한 두 노드를 교환해보고 교차하는 엣지 수가 감소하면 해답을 갱신한다.
 * 4. 최대 정해진 횟수만큼(graphviz에서는 최대 24회) 2~3의 과정을 반복한다. 만약 2~3을 수행하는 과정에서 해가
 * 개선되지 않았으면 중단한다. 2단계는 한번은 상위->하위 랭크로, 한번은 하위->상위 랭크로 수행하면 더 성능이 좋다.
 */
class RankOrdersAlgorithm(
  val graph: BaseGraph,
  val ranks: Map<String, Int>,
  val maxIterations: Int = 24,
) {
  data class NodesOrder(
    val rank: Int,
    val nodes: List<String>
  )

  data class RankOrders(
    val ranks: List<NodesOrder>
  ) {
    init {
      check(ranks.map { it.rank }.windowed(2).all { (a, b) -> a < b })
    }

    fun switchNodesAt(rankIdx: Int, left: Int, right: Int): RankOrders {
      val mranks = ranks.toMutableList()
      val oldNodes = mranks[rankIdx].nodes.toMutableList()
      val tmp = oldNodes[left]
      oldNodes[left] = oldNodes[right]
      oldNodes[right] = tmp
      mranks[rankIdx] = mranks[rankIdx].copy(nodes = oldNodes)
      return RankOrders(mranks)
    }

    fun updateFromList(list: List<List<String>>): RankOrders =
      RankOrders(ranks.zip(list).map { (rank, order) ->
        check(rank.nodes.toSet() == order.toSet())
        NodesOrder(rank.rank, order)
      })
  }

  fun solve(): RankOrders {
    var best = initial()
    var bestCrossings = crossings(best)

    for (x in 0 until maxIterations) {
      var improved = false

      fun candidate(orders: RankOrders) {
        val newCrossings = crossings(orders)
        if (bestCrossings > newCrossings) {
          best = orders
          bestCrossings = newCrossings
          improved = true
        }
      }

      val wmedian = wmedian(best, maxIterations % 2 > 0)
      candidate(wmedian)

      best.ranks.forEachIndexed { rankIdx, rank ->
        for (idx in 0 until rank.nodes.size - 1) {
          candidate(best.switchNodesAt(rankIdx, idx, idx + 1))
        }
      }

      if (!improved || bestCrossings == 0) {
        break
      }
    }
    return best
  }

  fun crossings(orders: RankOrders): Int {
    // 인접한 각 랭크에 대해
    return orders.ranks.windowed(2).sumOf { rank2 ->
      val upperRank = rank2[0].nodes
      val lowerRank = rank2[1].nodes
      // 이 랭크에 걸려있는 엣지
      val edges = graph.directedEdges.mapNotNull { (_, edge) ->
        val upperIdx = upperRank.indexOf(edge.start)
        val lowerIdx = lowerRank.indexOf(edge.end)
        if (upperIdx >= 0 && lowerIdx >= 0) {
          Pair(upperIdx, lowerIdx)
        } else {
          val upperIdx2 = upperRank.indexOf(edge.end)
          val lowerIdx2 = lowerRank.indexOf(edge.start)
          if (upperIdx2 >= 0 && lowerIdx2 >= 0) {
            Pair(upperIdx2, lowerIdx2)
          } else null
        }
      }.sortedWith(compareBy<Pair<Int, Int>> { it.first }.thenBy { it.second })
      // TODO 이거 더 잘 할수는 없을까?
      edges.flatMapIndexed { index, left ->
        edges.drop(index + 1).filter { right ->
          left.second > right.second
        }
      }.count()
    }
  }

  fun initial(): RankOrders {
    val ranks = ranks.entries.groupBy { it.value }.map { entry ->
      NodesOrder(entry.key, entry.value.map { it.key }.sorted())
    }.sortedBy { it.rank }

    val visited = mutableSetOf<String>()
    val builder = MutableList<MutableList<String>>(ranks.size) { mutableListOf() }

    fun traverse(rankIdx: Int, node: String) {
      visited.add(node)
      builder[rankIdx].add(node)
      if (rankIdx + 1 < ranks.size) {
        (graph.connectedTo(node) - visited).intersect(ranks[rankIdx + 1].nodes.toSet())
          .forEach { connectedNode ->
            traverse(rankIdx + 1, connectedNode)
          }
      }
    }

    ranks.forEachIndexed { rankIdx, rank ->
      rank.nodes.forEach { node ->
        if (!builder[rankIdx].contains(node)) {
          traverse(rankIdx, node)
        }
      }
    }

    return RankOrders(ranks).updateFromList(builder)
  }

  fun wmedian(orders: RankOrders, reverse: Boolean): RankOrders {
    val builder = MutableList<MutableList<String>>(orders.ranks.size) { mutableListOf() }

    if (reverse) {
      builder[builder.size - 1].addAll(orders.ranks[builder.size - 1].nodes)
    } else {
      builder[0].addAll(orders.ranks[0].nodes)
    }

    // node와 연결된 노드들 중 rankIdx에 속한 노드들의 rankIdx 내에서의 index를 반환
    fun adjPositions(node: String, rankIdx: Int): List<Int> {
      val upperRank = graph.connectedTo(node)
      return upperRank.map { builder[rankIdx].indexOf(it) }.filter { it >= 0 }.sorted()
    }

    val (rankRange, prevRank) = if (reverse) {
      Pair(builder.size - 2 downTo 0, 1)
    } else {
      Pair(1 until builder.size, -1)
    }
    for (rankIdx in rankRange) {
      val newNodePositions = mutableListOf<Pair<Double, String>>()
      orders.ranks[rankIdx].nodes.forEach { node ->
        val positions = adjPositions(node, rankIdx + prevRank).map { it.toDouble() }
        when {
          positions.isEmpty() ->
            newNodePositions.add(Pair(-1.0, node))
          positions.size == 2 ->
            newNodePositions.add(Pair((positions[0] + positions[1]) / 2, node))
          positions.size % 2 == 1 ->
            newNodePositions.add(Pair(positions[positions.size / 2], node))
          else -> {
            val middle = positions.size / 2
            val left = (positions[middle - 1] - positions.first())
            val right = (positions.last() - positions[middle])
            val newPos = (positions[middle - 1] * right + positions[middle] * left) / (left + right)
            newNodePositions.add(Pair(newPos, node))
          }
        }
      }
      builder[rankIdx].addAll(newNodePositions.sortedBy { it.first }.map { it.second })
    }

    return orders.updateFromList(builder)
  }
}
