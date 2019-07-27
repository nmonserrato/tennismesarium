package dev.paloma.tennismesarium.tournament

import java.io.IOException
import java.io.OutputStreamWriter
import java.io.PrintStream
import java.io.PrintWriter

class SingleEliminationTournamentPrinter2 {
    @Throws(IOException::class)
    fun print(root: Round, out: PrintStream) {
        val right = root.getRight()
        val left = root.getLeft()

        if (right != null) {
            printTree(right, out, true, "")
        }

        printNodeValue(root.toString(), out)

        if (left != null) {
            printTree(left, out, false, "")
        }
    }

    @Throws(IOException::class)
    private fun printNodeValue(value:String?, out: PrintStream) {
        if (value == null) {
            out.print("<null>")
        } else {
            out.print(value)
        }
        out.print('\n')
    }

    // use string and not stringbuffer on purpose as we need to change the indent at each recursion
    @Throws(IOException::class)
    private fun printTree(node: Round, out: PrintStream, isRight: Boolean, indent: String) {
        val right = node.getRight()
        val left = node.getLeft()

        if (right != null) {
            printTree(right, out, true, indent + if (isRight) "        " else " |      ")
        }
        out.print(indent)
        if (isRight) {
            out.print(" /")
        } else {
            out.print(" \\")
        }
        out.print("----- ")

        printNodeValue(node.toString(), out)

        if (left != null) {
            printTree(left, out, false, indent + if (isRight) " |      " else "        ")
        }
    }
}