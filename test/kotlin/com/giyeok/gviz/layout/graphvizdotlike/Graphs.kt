package com.giyeok.gviz.layout.graphvizdotlike

import com.giyeok.gviz.graph.Edge
import com.giyeok.gviz.graph.SizedGraph
import com.giyeok.gviz.figure.Size

object Graphs {
  val graph1 = SizedGraph(
    mapOf(
      "node1" to Size(100.0, 30.0),
      "node2" to Size(100.0, 30.0),
      "node3" to Size(100.0, 30.0),
      "node4" to Size(100.0, 30.0),
      "node5" to Size(100.0, 30.0),
    ),
    mapOf(),
    mapOf(
      "dedge1" to Edge("node1", "node2"),
    ),
    mapOf(
      "uedge1" to Edge("node3", "node4"),
    )
  )

  fun genCharGraph(nodes: Collection<Char>, edges: List<String>): SizedGraph {
    val nodeIds = nodes.map { it.toString() }
    return SizedGraph(
      nodeIds.associateWith { Size(40.0, 30.0) },
      mapOf(),
      edges.associateWith { edge -> Edge(edge[0].toString(), edge[1].toString()) },
      mapOf()
    )
  }

  fun genCharGraph(vararg edges: String): SizedGraph {
    val nodes = edges.toList().flatMap { setOf(it[0], it[1]) }.toSet()
    return genCharGraph(nodes, edges.toList())
  }

  fun genStringGraph(nodes: Collection<String>, edges: List<String>): SizedGraph {
    val nodeIds = nodes.map { it.toString() }
    return SizedGraph(
      nodeIds.associateWith { Size(40.0, 30.0) },
      mapOf(),
      edges.associateWith { edge ->
        val splitted = edge.split("->")
        check(splitted.size == 2)
        Edge(splitted[0], splitted[1])
      },
      mapOf()
    )
  }

  fun genStringGraph(vararg edges: String): SizedGraph {
    val nodes = edges.toList().flatMap { it.split("->") }.toSet()
    return genStringGraph(nodes, edges.toList())
  }

  val graph2 = genCharGraph("gh", "eg", "fg", "ae", "af", "bc", "ab", "cd", "dh")

  val graph5 = genCharGraph("ab", "ac", "be", "cd", "de", "ae")

  /**
  { rank = same; 1976 Mashey Bourne; }
  { rank = same; 1978 Formshell csh; }
  { rank = same; 1980 esh vsh; }
  { rank = same; 1982 ksh "System-V"; }
  { rank = same; 1984 v9sh tcsh; }
  { rank = same; 1986 "ksh-i"; }
  { rank = same; 1988 KornShell Perl rc; }
  { rank = same; 1990 tcl Bash; }
  { rank = same; "future" POSIX "ksh-POSIX"; }
  Thompson -> {Mashey Bourne csh}; csh -> tcsh;
  Bourne -> {ksh esh vsh "System-V" v9sh}; v9sh -> rc;
  {Bourne "ksh-i" KornShell} -> Bash;
  {esh vsh Formshell csh} -> ksh;
  {KornShell "System-V"} -> POSIX;
  ksh -> "ksh-i" -> KornShell -> "ksh-POSIX";
  Bourne -> Formshell;
  /* ’invisible’ edges to adjust node placement */
  edge [style=invis];
  1984 -> v9sh -> tcsh ; 1988 -> rc -> KornShell;
  Formshell -> csh; KornShell -> Perl;
   */

  val graph3 = genStringGraph(
//    "1972->1976",
//    "1976->1978",
//    "1978->1980",
//    "1980->1982",
//    "1982->1984",
//    "1984->1986",
//    "1986->1988",
//    "1988->1990",
//    "1990->future",
    "Thompson->Mashey",
    "Thompson->Bourne",
    "Thompson->csh",
    "csh->tcsh",
    "Bourne->ksh",
    "Bourne->esh",
    "Bourne->vsh",
    "Bourne->System-V",
    "Bourne->v9sh",
    "v9sh->rc",
    "Bourne->Bash",
    "ksh-i->Bash",
    "KornShell->Bash",
    "esh->ksh",
    "vsh->ksh",
    "Formshell->ksh",
    "csh->ksh",
    "KornShell->POSIX",
    "System-V->POSIX",
    "ksh->ksh-i",
    "ksh-i->KornShell",
    "KornShell->ksh-POSIX",
    "Bourne->Formshell",
  )

  val graph4 = genStringGraph(
    "S8->9",
    "S24->27",
    "S24->25",
    "S1->10",
    "S1->2",
    "S35->36",
    "S35->43",
    "S30->31",
    "S30->33",
    "9->42",
    "9->T1",
    "25->T1",
    "25->26",
    "27->T24",
    "2->3",
    "2->16",
    "2->17",
    "2->T1",
    "2->18",
    "10->11",
    "10->14",
    "10->T1",
    "10->13",
    "10->12",
    "31->T1",
    "31->32",
    "33->T30",
    "33->34",
    "42->4",
    "26->4",
    "3->4",
    "16->15",
    "17->19",
    "18->29",
    "11->4",
    "14->15",
    "37->39",
    "37->41",
    "37->38",
    "37->40",
    "13->19",
    "12->29",
    "43->38",
    "43->40",
    "36->19",
    "32->23",
    "34->29",
    "39->15",
    "41->29",
    "38->4",
    "40->19",
    "4->5",
    "19->21",
    "19->20",
    "19->28",
    "5->6",
    "5->T35",
    "5->23",
    "21->22",
    "20->15",
    "28->29",
    "6->7",
    "15->T1",
    "22->23",
    "22->T35",
    "29->T30",
    "7->T8",
    "23->T24",
    "23->T1",
  )
}
