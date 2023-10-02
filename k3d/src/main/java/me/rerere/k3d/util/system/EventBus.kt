package me.rerere.k3d.util.system

/**
 * Global EventBus
 *
 * Used for communication between different components, the most common use case is communication of
 * a disposing event from a component to the renderer, so the renderer can remove the bound resources
 *
 * @see me.rerere.k3d.renderer.Renderer
 */
object EventBus {
    private val listeners = mutableMapOf<Class<*>, MutableList<K3DEventListener>>()

    fun register(clazz: Class<*>, listener: K3DEventListener) {
        listeners.getOrPut(clazz) { mutableListOf() }.add(listener)
    }

    fun unregister(clazz: Class<*>, listener: K3DEventListener) {
        listeners[clazz]?.remove(listener)
    }

    fun <T : K3DEvent> post(event: T) {
        listeners[event::class.java]?.forEach {
            it.onEvent(event)
        }
    }
}

/**
 * Base class of all events
 */
interface K3DEvent

/**
 * Event listener
 */
interface K3DEventListener {
    fun onEvent(event: K3DEvent)
}