package dev.paloma.tennismesarium.tournament

import java.util.ArrayList
import kotlin.math.ceil
import kotlin.math.floor

class SingleEliminationTournamentPrinter {
    fun print(root: Round) {
        val lines = ArrayList<List<String?>>()

        var level: MutableList<Round?> = ArrayList()
        var next: MutableList<Round?> = ArrayList()

        level.add(root)
        var nn = 1

        var widest = 0

        while (nn != 0) {
            val line = ArrayList<String?>()

            nn = 0

            for (n in level) {
                if (n == null) {
                    line.add(null)

                    next.add(null)
                    next.add(null)
                } else {
                    val aa = n.toString()
                    line.add(aa)
                    if (aa.length > widest) widest = aa.length

                    next.add(n.getLeft())
                    next.add(n.getRight())

                    if (n.getLeft() != null) nn++
                    if (n.getRight() != null) nn++
                }
            }

            if (widest % 2 == 1) widest++

            lines.add(line)

            val tmp = level
            level = next
            next = tmp
            next.clear()
        }

        var perpiece = lines[lines.size - 1].size * (widest + 4)
        for (i in lines.indices) {
            val line = lines[i]
            val hpw = floor((perpiece / 2f).toDouble()).toInt() - 1

            if (i > 0) {
                for (j in line.indices) {

                    // split node
                    var c = ' '
                    if (j % 2 == 1) {
                        if (line[j - 1] != null) {
                            c = if (line[j] != null) '┴' else '┘'
                        } else {
                            if (j < line.size && line[j] != null) c = '└'
                        }
                    }
                    print(c)

                    // lines and spaces
                    if (line[j] == null) {
                        for (k in 0 until perpiece - 1) {
                            print(" ")
                        }
                    } else {

                        for (k in 0 until hpw) {
                            print(if (j % 2 == 0) " " else "─")
                        }
                        print(if (j % 2 == 0) "┌" else "┐")
                        for (k in 0 until hpw) {
                            print(if (j % 2 == 0) "─" else " ")
                        }
                    }
                }
                println()
            }

            // print line of numbers
            for (j in line.indices) {

                var f: String? = line[j]
                if (f == null) f = ""
                val gap1 = ceil((perpiece / 2f - f.length / 2f).toDouble()).toInt()
                val gap2 = floor((perpiece / 2f - f.length / 2f).toDouble()).toInt()

                // a number
                for (k in 0 until gap1) {
                    print(" ")
                }
                print(f)
                for (k in 0 until gap2) {
                    print(" ")
                }
            }
            println()

            perpiece /= 2
        }
    }
}