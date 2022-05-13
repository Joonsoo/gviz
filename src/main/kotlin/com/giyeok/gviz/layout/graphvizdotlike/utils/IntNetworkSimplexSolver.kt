package com.giyeok.gviz.layout.graphvizdotlike.utils

import com.giyeok.gviz.graph.BaseGraph
import java.util.*

/**
 * Graphviz dot 논문에서 설명하는 network simplex 알고리즘 구현.
 *
 * 실제로 network simplex 알고리즘인지 잘은 모르겠음..
 *
 *
 */
class IntNetworkSimplexSolver(
  val graph: BaseGraph,
  val edgeMinLengths: Map<String, Int>,
  val edgeWeights: Map<String, Double>,
) {
  // 각 노드의 랭크 계산. 랭크가 작을수록 위(혹은 왼쪽)에 나옴.
  // minRanks와 maxRanks는 여기서 반영함. 가장 작은 랭크는 0.
  // sameRanks는 여기서는 반영하지 않고 adjustRanks에서 적용함.
  fun solve(): Map<String, Int> {
    // l(Edge(start, end)) = ranking[end] - ranking[start]
    // weighted edge length: 모든 엣지 e에 대해, (l(e) * edgeWeights[e])의 합
    // 목표는 weighted edge length가 최소가 되는 rank를 계산하는 것.

    // network simplex
    // ranking이 feasible하다 = 모든 엣지 e에 대해 l(e) >= edgeMinLengths[e]이다
    // slack(Edge(start, end)) = l(e) - edgeMinLengths[e]
    // 엣지 e가 tight하다 = slack(e)가 0이다

    // feasible spanning 트리를 만들고 나면 각 엣지의 cut value를 계산 가능
    // cutvalue(e @ Edge(start, end)) =
    // e를 스패닝 트리에서 지웠을 때,
    // (start가 속하는 쪽의 컴포넌트에서 end가 속하는 쪽의 컴포넌트로 가는 모든 엣지의 weight의 합(e의 weight도 포함)) -
    // (end가 속하는 쪽의 컴포넌트에서 start가 속하는 쪽의 컴포넌트로 가는 모든 엣지의 weight의 합)

    // (항상은 아니지만) 일반적으로, 어떤 엣지 e의 cutvalue가 음수이면,
    // end-to-start(head-component-to-tail) 엣지 중 하나가 tight해질 때까지, e의 길이(rank(end)-rank(start))를 최대한 늘리면
    // weighted edge length sum이 줄어든다는 의미.
    // 이것은 cutvalue가 음수인 엣지 e를 없애고, 새로운 tight 엣지를 하나 추가해서 새로운 feasible spanning tree를 만드는 것과 같음.

    // 모든 엣지가 음이 아닌 cut value를 갖도록 하는 것이 알고리즘의 목표.

    // enter_edge(e: Edge) -> 스패닝 트리에서 e를 제거하고 새로 추가할 노드를 찾아서 반환
    // e.end의 컴포넌트에서 e.start의 컴포넌트로 가는 엣지들 중 slack이 가장 작은 엣지를 반환

    val initial = initialFeasibleTree()
    val tree = initial.spanningTree.toBuilder()
    val ranks = initial.ranks.toMutableMap()

    // tree에서 negaitve cut value를 가진 엣지가 있으면(없어질 때까지),
    // - end 컴포넌트 -> start 컴포넌트로 가는 엣지 중 slack이 가장 작은 edge로 치환
    // TODO 지금은 cut value를 매번 새로 계산하는데 개선 가능한듯?
    var leaveEdge: String? = findNegativeCutValueEdge(tree.build())
    while (leaveEdge != null) {
      val (components, replacement) = findReplacement(tree.build(), ranks, leaveEdge)

      // TODO 여기서 이렇게 하는게 맞나..?
      val delta = slackOfEdge(replacement, ranks)
      components.second.forEach { node ->
        ranks[node] = ranks.getValue(node) + delta
      }
      tree.edges.remove(leaveEdge)
      tree.edges.add(replacement)

      leaveEdge = findNegativeCutValueEdge(tree.build())
    }

    // 스패닝 트리를 만든 다음 거기서 랭크 구하기:
    // 1) 아무 노드 v나 찍고, rank[v]를 아무 값으로나 설정
    // 2) 각 (스패닝 트리 안에서 v에서 나가는 엣지) e에 대해, rank[e.end] = rank[v] + edgeMinLengths[e],
    // 3) 각 (스패닝 트리 안에서 v로 들어오는 엣지) e에 대해, rank[e.start] = rank[v] - edgeMinLengths[e]를 설정
    // 4) 아직 traverse하지 않은 노드 v에 대해 2)와 3)의 과정을 계속 반복

    // normalize: 가장 작은 랭크가 0이 되도록 노멀라이즈
    val minValue = ranks.values.minOrNull() ?: 0
    graph.nodes.forEach { node ->
      ranks[node] = ranks.getValue(node) - minValue
    }

    return ranks
  }

  fun initialFeasibleTree(): RankSpanningTree {
    // Figure 2-2. feasible_tree()

    val ranks = initRank().toMutableMap()

    // 코드랑 논문이랑 알고리즘이 조금 다른 것 같은데?
    // https://gitlab.com/graphviz/graphviz/-/blob/7c0d280b019114c57564c4cf2ac5766280447f00/lib/common/ns.c#L494
    // 코드쪽을 따라가자

    // ranks를 바탕으로, tight edge로 이루어진 spanning tree들(spanning forest)을 구한다
    val tightTrees = findTightTrees(ranks).mapIndexed { index, tree -> index to tree }
      .toMap().toMutableMap()
    val nodeToTreeId = tightTrees.flatMap { (index: Int, spanningTree: SpanningTree) ->
      spanningTree.nodes.map { node -> node to index }
    }.toMap().toMutableMap()

    // 그렇게 찾아진 tight spanning subtree들을 merge한다.
    // subtree T1과 T2를 merge할 때는:
    // - T1과 T2 사이의 엣지들 중 slack이 가장 작은 엣지를 찾는다(e라고 하자)
    // - e.end가 T1쪽에 속하면 T1에 속한 노드들의 rank값에 slack(e)를 더한다
    // - e.end가 T2쪽에 속하면 T2에 속한 노드들의 rank값에서 slack(e)를 뺀다
    while (tightTrees.size >= 2) {
      // edge id, slack값
      var leastSlackEdge: Pair<String, Int>? = null
      graph.directedEdges.forEach { (edgeName, edge) ->
        if (nodeToTreeId.getValue(edge.start) != nodeToTreeId.getValue(edge.end)) {
          val edgeSlack = slackOfEdge(edgeName, ranks)
          if (leastSlackEdge == null || leastSlackEdge!!.second > edgeSlack) {
            leastSlackEdge = Pair(edgeName, edgeSlack)
          }
        }
      }
      // 그래프가 connected이기 때문에 leastSlackEdge는 null일 수 없음
      val mergingEdgeName = leastSlackEdge!!.first
      val mergingEdge = graph.directedEdges.getValue(mergingEdgeName)
      val mergingSlack = leastSlackEdge!!.second

      val merging = nodeToTreeId.getValue(mergingEdge.start)
      val merged = nodeToTreeId.getValue(mergingEdge.end)
      // merged tree쪽에 속한 노드들의 랭크에 mergingSlack을 뺀다
      val mergingTree = tightTrees.getValue(merging)
      val mergedTree = tightTrees.getValue(merged)
      mergedTree.nodes.forEach { mergedNode ->
        ranks[mergedNode] = ranks.getValue(mergedNode) - mergingSlack
        nodeToTreeId[mergedNode] = merging
      }
      tightTrees[merging] = SpanningTree(
        mergingTree.nodes + mergedTree.nodes,
        mergingTree.edges + mergedTree.edges + mergingEdgeName
      )
      tightTrees.remove(merged)
    }
    check(tightTrees.size == 1)

    return RankSpanningTree(tightTrees.values.first(), ranks)

    // 논문상의 설명은:
    // ranks.fixedNodes들로부터 시작해서 ranks.ranks 기준으로 slack 값이 0인 스패닝 트리(포레스트)를 구하고,
    // 그런 트리가 모든 노드를 커버하지 못하면,
    // - e = 그런 트리(포레스트)에 접한 non-tree edge 중 slack값이 가장 작은 edge를 찾고,
    // - e.end가 그 트리 안에 속하면 트리에 속한 모든 노드의 rank에서 slack(e)를 빼고(실제로 숫자는 증가)
    // - e.end가 트리에 속하지 않으면 트리에 속한 모든 노드의 rank에서 slack(e)를 더한다(실제로 숫자는 증가)
    // 위의 과정을 반복
    // ranks.ranks 를 기준으로 fixed node
  }

  fun initRank(): Map<String, Int> {
    // init_rank:
    // graphEx에는 사이클이 없다고 가정
    // 아직 스캔되지 않은 in-edge가 없는 노드 v에 대해,
    // - v의 in-edge v' 각각에 대해, (rank(v')+edgeMinLength(v, v')) 중 max값을 rank(v)로 세팅하고
    // - v를 스캔된 것으로 설정한 다음
    // - 모든 노드가 스캔될 때까지 위의 과정 반복해서 노드의 랭크 계산해서 반환
    // (topological sort해서 순서대로 실행하는 것처럼 동작)

    val queue = LinkedList(graph.nodes.filter {
      (graph.incomingEdges[it] ?: setOf()).isEmpty()
    })
    val ranks = mutableMapOf<String, Int>()

    fun traverse() {
      if (queue.isNotEmpty()) {
        val node = queue.poll()
        val incomingEdges = (graph.incomingEdges[node] ?: setOf())
        ranks[node] = if (incomingEdges.isEmpty()) 0 else incomingEdges.maxOf { incomingEdge ->
          ranks.getValue(incomingEdge.edge.start) + edgeMinLengths.getValue(incomingEdge.edgeName)
        }
        val newNodes = graph.outgoingsFrom(node).filter { outgoing ->
          (graph.incomingsTo(outgoing) - ranks.keys).isEmpty()
        }
        queue.addAll(newNodes)
        traverse()
      }
    }
    traverse()
    return ranks
  }

  data class RankSpanningTree(
    val spanningTree: SpanningTree,
    val ranks: Map<String, Int>
  )

  data class SpanningTree(
    val nodes: Set<String>,
    val edges: Set<String>
  ) {
    class Builder(val nodes: MutableSet<String>, val edges: MutableSet<String>) {
      fun build() = SpanningTree(nodes, edges)
    }

    fun toBuilder() = Builder(nodes.toMutableSet(), edges.toMutableSet())
  }

  fun slackOfEdge(edgeName: String, ranks: Map<String, Int>): Int {
    val edge = graph.directedEdges.getValue(edgeName)
    val length = ranks.getValue(edge.end) - ranks.getValue(edge.start)
    return length - edgeMinLengths.getValue(edgeName)
  }

  fun findTightTrees(ranks: Map<String, Int>): List<SpanningTree> {
    val visited = mutableSetOf<String>()
    val trees = mutableListOf<SpanningTree>()

    fun findTightTree(queue: LinkedList<String>, cc: SpanningTree.Builder): SpanningTree =
      if (queue.isEmpty()) {
        cc.build()
      } else {
        val node = queue.poll()
        visited.add(node)
        val incomings = graph.incomingEdges[node] ?: listOf()
        val outgoings = graph.outgoingEdges[node] ?: listOf()
        val tightIncomings = incomings
          .filter { incoming -> !cc.nodes.contains(incoming.edge.start) }
          .filter { incoming -> slackOfEdge(incoming.edgeName, ranks) == 0 }
        val tightOutgoings = outgoings
          .filter { outgoing -> !cc.nodes.contains(outgoing.edge.end) }
          .filter { outgoing -> slackOfEdge(outgoing.edgeName, ranks) == 0 }
        val newNodes =
          (tightIncomings.map { it.edge.start } + tightOutgoings.map { it.edge.end }).toSet()
        val newEdges = (tightIncomings + tightOutgoings).map { it.edgeName }
        queue.addAll(newNodes - cc.nodes)
        cc.nodes.addAll(newNodes)
        cc.edges.addAll(newEdges)
        check(cc.nodes.size - 1 == cc.edges.size)
        findTightTree(queue, cc)
      }

    graph.nodes.forEach { node ->
      if (!visited.contains(node)) {
        val newTightTree = findTightTree(
          LinkedList(listOf(node)),
          SpanningTree.Builder(mutableSetOf(node), mutableSetOf())
        )
        trees.add(newTightTree)
      }
    }
    return trees
  }

  fun splitSpanningTree(
    spanningTree: SpanningTree,
    cutEdgeName: String
  ): Pair<Set<String>, Set<String>> {
    val edge = graph.directedEdges.getValue(cutEdgeName)
    val componentEdges = spanningTree.edges - cutEdgeName

    val cutGraph = BaseGraph(
      graph.nodes,
      mapOf(),
      componentEdges.associateWith { graph.directedEdges.getValue(it) })
    val startComponent = cutGraph.connectedNodesFrom(edge.start)
    val endComponent = cutGraph.connectedNodesFrom(edge.end)
    check(startComponent.intersect(endComponent).isEmpty())
    return Pair(startComponent, endComponent)
  }

  fun cutValue(spanningTree: SpanningTree, cutEdgeName: String): Double {
    // cutvalue(e @ Edge(start, end)) =
    // e를 스패닝 트리에서 지웠을 때,
    // (start가 속하는 쪽의 컴포넌트에서 end가 속하는 쪽의 컴포넌트로 가는 모든 엣지의 weight의 합(e의 weight도 포함)) -
    // (end가 속하는 쪽의 컴포넌트에서 start가 속하는 쪽의 컴포넌트로 가는 모든 엣지의 weight의 합)
    check(spanningTree.edges.contains(cutEdgeName))

    val (startComponent, endComponent) = splitSpanningTree(spanningTree, cutEdgeName)
    val cutValue = graph.directedEdges.entries.sumOf { (edgeName, edge) ->
      when {
        startComponent.contains(edge.start) && endComponent.contains(edge.end) ->
          edgeWeights.getValue(edgeName)
        endComponent.contains(edge.start) && startComponent.contains(edge.end) ->
          -edgeWeights.getValue(edgeName)
        else -> 0.0
      }
    }
    return cutValue
  }

  fun findNegativeCutValueEdge(spanningTree: SpanningTree): String? {
    // spanningTree에 속한 edge들 중 cut value가 음수인 엣지의 이름을 반환. 없으면 null 반환.
    val leaveEdge = spanningTree.edges.find { cutValue(spanningTree, it) < 0 }
    return leaveEdge
  }

  fun findReplacement(
    spanningTree: SpanningTree,
    ranks: Map<String, Int>,
    leaveEdge: String
  ): Pair<Pair<Set<String>, Set<String>>, String> {
    val components = splitSpanningTree(spanningTree, leaveEdge)
    val (startComponent, endComponent) = components
    // endComponent에서 startComponent로 향하는 엣지 중 slack값이 가장 작은 엣지를 반환
    val replacement = graph.directedEdges.entries.filter { (_, edge) ->
      endComponent.contains(edge.start) && startComponent.contains(edge.end)
    }.minByOrNull { (edgeName, _) -> slackOfEdge(edgeName, ranks) }!!.key
    return Pair(components, replacement)
  }
}
