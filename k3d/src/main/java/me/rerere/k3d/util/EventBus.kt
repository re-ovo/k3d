package me.rerere.k3d.util

/**
 * Global EventBus
 *
 * Used for communication between different components, the most common use case is communication of
 * a disposing event from a component to the renderer, so the renderer can remove the bound resources
 *
 * @see me.rerere.k3d.renderer.Renderer
 */
object EventBus {
    private val listeners = mutableMapOf<Class<*>, MutableList<K3DEventListener<*>>>()

    fun <T : K3DEvent> register(clazz: Class<T>, listener: K3DEventListener<T>) {
        listeners.getOrPut(clazz) { mutableListOf() }.add(listener)
    }

    fun <T : K3DEvent> unregister(clazz: Class<T>, listener: K3DEventListener<T>) {
        listeners[clazz]?.remove(listener)
    }

    fun <T : K3DEvent> post(event: T) {
        listeners[event::class.java]?.forEach {
            @Suppress("UNCHECKED_CAST")
            (it as K3DEventListener<T>).onEvent(event)
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
interface K3DEventListener<T : K3DEvent> {
    fun onEvent(event: T)
}