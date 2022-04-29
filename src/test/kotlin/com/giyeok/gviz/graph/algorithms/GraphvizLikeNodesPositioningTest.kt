package com.giyeok.gviz.graph.algorithms.com.giyeok.gviz.graph.algorithms

import com.giyeok.gviz.graph.Edge
import com.giyeok.gviz.graph.SizedNodesGraph
import com.giyeok.gviz.graph.algorithms.GraphvizLikeNodesPositioning
import com.giyeok.gviz.render.Size
import com.google.common.truth.Truth.assertThat
import kotlin.test.Test

class GraphvizLikeNodesPositioningTest {
  @Test
  fun testFindRoots1() {
    val graph = SizedNodesGraph(
      mapOf(
        "node1" to Size(100.0, 30.0),
        "node2" to Size(100.0, 30.0),
        "node3" to Size(100.0, 30.0),
        "node4" to Size(100.0, 30.0),
        "node5" to Size(100.0, 30.0),
      ),
      mapOf(
        "dedge1" to Edge("node1", "node2"),
      ),
      mapOf(
        "uedge1" to Edge("node3", "node4"),
      )
    )
    val positioning = GraphvizLikeNodesPositioning(graph)
    val roots = positioning.findRoots()
    assertThat(roots).containsExactly("node1", "node3", "node5").inOrder()
  }

  @Test
  fun testFindRoots2() {
    val graph = SizedNodesGraph(
      mapOf(
        "node1" to Size(100.0, 30.0),
        "node2" to Size(100.0, 30.0),
        "node3" to Size(100.0, 30.0),
        "node4" to Size(100.0, 30.0),
        "node5" to Size(100.0, 30.0),
        "node6" to Size(100.0, 30.0),
      ),
      mapOf(
        "dedge1" to Edge("node1", "node2"),
        "dedge2" to Edge("node2", "node6")
      ),
      mapOf(
        "uedge1" to Edge("node3", "node4"),
        "uedge2" to Edge("node3", "node6")
      )
    )
    val positioning = GraphvizLikeNodesPositioning(graph)
    val roots = positioning.findRoots()
    assertThat(roots).containsExactly("node1", "node5").inOrder()
  }

  @Test
  fun testCalculateRanks() {
    val graph = SizedNodesGraph(
      mapOf(
        "node1" to Size(100.0, 30.0),
        "node2" to Size(100.0, 30.0),
        "node3" to Size(100.0, 30.0),
        "node4" to Size(100.0, 30.0),
        "node5" to Size(100.0, 30.0),
        "node6" to Size(100.0, 30.0),
      ),
      mapOf(
        "dedge1" to Edge("node1", "node2"),
        "dedge2" to Edge("node2", "node6")
      ),
      mapOf(
        "uedge1" to Edge("node3", "node4"),
        "uedge2" to Edge("node3", "node6")
      )
    )
    val positioning = GraphvizLikeNodesPositioning(graph)
    val ranks = positioning.calculateRanks()
    assertThat(ranks).containsExactlyEntriesIn(
      mapOf(
        "node1" to 0,
        "node2" to 1,
        "node3" to 3,
        "node4" to 4,
        "node5" to 0,
        "node6" to 2,
      )
    )
  }
}
