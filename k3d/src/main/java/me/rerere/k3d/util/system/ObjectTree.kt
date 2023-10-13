package me.rerere.k3d.util.system

private val ROOT_DISPOSABLE = object : Disposable {
    override fun dispose() {
        // Do nothing
    }
}

internal class ObjectTree {
    val rootNode = Node(ROOT_DISPOSABLE)
    private val object2ParentNode = identityMapOf<Disposable, Node>()

    fun register(parent: Disposable, child: Disposable) {
        require(parent !== child) {
            "Cannot register a disposable to itself"
        }

        synchronized(rootNode) {
            val parentNode: Node = getParentNode(parent).findOrCreateChildNode(parent)
            val childNode: Node = getParentNode(child).moveChildToAnotherParent(child, parentNode)

            object2ParentNode[child] = parentNode

            require(childNode.value == child)
        }
    }

    private fun getParentNode(disposable: Disposable): Node {
        return object2ParentNode[disposable] ?: rootNode
    }

    fun disposeEntireTree() {
        disposeNode(rootNode)
    }

    fun disposeAll(obj: Disposable) {
        val parentNode = getParentNode(obj)
        val node = parentNode.findChild(obj) ?: run {
            obj.dispose() // Dispose it if it's not exists in tree node
            return
        }

        parentNode.children.remove(obj)

        disposeNode(node)
    }

    private fun disposeNode(node: Node) {
        synchronized(rootNode) {
            node.value.dispose()
            object2ParentNode.remove(node.value)

            node.children.values.forEach(::disposeNode)
        }
    }
}

internal class Node(val value: Disposable) {
    val children = identityMapOf<Disposable, Node>()

    fun findChild(disposable: Disposable): Node? {
        return children[disposable]
    }

    fun findOrCreateChildNode(disposable: Disposable): Node {
        return findChild(disposable) ?: Node(disposable).also {
            children[disposable] = it
        }
    }

    fun moveChildToAnotherParent(disposable: Disposable, newParent: Node): Node {
        val childNode = children.remove(disposable) ?: Node(disposable)
        newParent.children[disposable] = childNode
        return childNode
    }
}

fun dumpDisposeTree() {
    println("ObjectTree dump:")
    dumpNode(AutoDispose.tree.rootNode, 0)
}

internal fun dumpNode(node: Node, depth: Int) {
    println(" ".repeat(depth * 2) + node.value.javaClass.simpleName)
    node.children.values.forEach { dumpNode(it, depth + 1) }
}
