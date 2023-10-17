package me.rerere.k3d.dsl

import android.view.ViewGroup
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import me.rerere.k3d.controller.OrbitController
import me.rerere.k3d.loader.GltfLoadResult
import me.rerere.k3d.loader.GltfLoader
import me.rerere.k3d.renderer.GLES3Renderer
import me.rerere.k3d.renderer.Renderer
import me.rerere.k3d.scene.actor.ActorGroup
import me.rerere.k3d.scene.actor.Mesh
import me.rerere.k3d.scene.actor.Scene
import me.rerere.k3d.scene.camera.Camera
import me.rerere.k3d.scene.camera.PerspectiveCamera
import me.rerere.k3d.scene.geometry.CubeGeometry
import me.rerere.k3d.scene.light.AmbientLight
import me.rerere.k3d.scene.light.DirectionalLight
import me.rerere.k3d.scene.material.CookTorranceMaterial
import me.rerere.k3d.ui.K3DView
import me.rerere.k3d.util.Color
import me.rerere.k3d.util.math.Vec3
import me.rerere.k3d.util.system.disposeAll

internal val LocalK3DRendererProvider = staticCompositionLocalOf<Renderer> {
    error("No K3DRenderer provided")
}

internal val LocalK3DNodeProvider = staticCompositionLocalOf<ActorGroup> {
    error("No K3DScene provided")
}

@Composable
fun rememberCameraState(): PerspectiveCamera {
    val camera = remember {
        PerspectiveCamera().apply {
            position.set(0f, 2f, 5f)
            far = 1000f
            near = 0.1f
        }
    }
    return camera
}

@Composable
fun K3DCanvas(
    modifier: Modifier = Modifier,
    camera: PerspectiveCamera = rememberCameraState(),
    content: @Composable () -> Unit
) {
    val renderer = remember {
        GLES3Renderer()
    }
    val scene = remember {
        Scene()
    }
    val controls = remember {
        OrbitController(camera, Vec3(0f, 0f, 0f), 1f)
    }

    DisposableEffect(Unit) {
        onDispose {
            scene.disposeAll()
            renderer.disposeAll()
        }
    }

    val density = LocalDensity.current
    CompositionLocalProvider(
        LocalK3DRendererProvider provides renderer,
        LocalK3DNodeProvider provides scene
    ) {
        BoxWithConstraints(modifier = modifier) {
            AndroidView(
                factory = {
                    K3DView(
                        context = it,
                        onSurfaceChanged = { gl, w, h ->
                            camera.aspect = w.toFloat() / h.toFloat()
                            renderer.resize(w, h)
                        },
                        onDrawFrame = {
                            renderer.render(scene, camera)
                        },
                        onTouch = {
                            controls.handleEvent(it)
                        }
                    ).apply {
                        layoutParams = ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT
                        )

                        with(density) {
                            controls.elementHeight = maxHeight.toPx()
                        }
                    }
                },
                modifier = Modifier.matchParentSize()
            )
            content()
        }
    }
}

@Composable
fun CubeMesh() {
    val mesh = remember {
        Mesh(
            geometry = CubeGeometry(1f, 1f, 1f),
            material = CookTorranceMaterial().apply {
                baseColor = Color.fromRGBHex("#FF0000")
                roughness = 0.5f
                metallic = 0.5f
            }
        )
    }
    val parent = LocalK3DNodeProvider.current
    DisposableEffect(Unit) {
        parent.addChild(mesh)
        onDispose {
            parent.removeChild(mesh)
            mesh.disposeAll()
        }
    }
}

@Composable
fun GltfModel(path: String) {
    val loader = remember {
        GltfLoader()
    }
    var gltf by remember {
        mutableStateOf<GltfLoadResult?>(null)
    }
    val parent = LocalK3DNodeProvider.current
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    DisposableEffect(Unit) {
        scope.launch(Dispatchers.IO) {
            val model = loader.load(context.assets.open(path))
            gltf = model
            parent.addChild(model.defaultScene)
        }
        onDispose {
            gltf?.defaultScene?.let {
                parent.removeChild(it)
                it.disposeAll()
            }
        }
    }
}

@Composable
fun LightAmbient() {
    val light = remember {
        AmbientLight(
            color = Color.fromRGBHex("#FFFFFF"),
            intensity = 0.15f
        )
    }
    val parent = LocalK3DNodeProvider.current
    DisposableEffect(Unit) {
        parent.addChild(light)
        onDispose {
            parent.removeChild(light)
            light.disposeAll()
        }
    }
}

@Composable
fun LightDirectional() {
    val light = remember {
        DirectionalLight(
            color = Color.fromRGBHex("#FFFFFF"),
            intensity = 1f,
            target = Vec3(0f, 0f, 0f),
        ).apply {
            position.set(0f, 5f, 5f)
        }
    }
    val parent = LocalK3DNodeProvider.current
    DisposableEffect(Unit) {
        parent.addChild(light)
        onDispose {
            parent.removeChild(light)
            light.disposeAll()
        }
    }
}