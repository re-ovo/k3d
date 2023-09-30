package me.rerere.k3d

import android.content.Context
import android.opengl.GLSurfaceView
import android.os.Bundle
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.ViewGroup
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.view.WindowCompat
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import me.rerere.k3d.controller.OrbitController
import me.rerere.k3d.loader.GltfLoader
import me.rerere.k3d.renderer.Clock
import me.rerere.k3d.renderer.GLES3Renderer
import me.rerere.k3d.renderer.GLESAutoConfigChooser
import me.rerere.k3d.renderer.ViewportSize
import me.rerere.k3d.scene.actor.Mesh
import me.rerere.k3d.scene.actor.Scene
import me.rerere.k3d.scene.camera.PerspectiveCamera
import me.rerere.k3d.scene.geometry.CubeGeometry
import me.rerere.k3d.scene.geometry.PlaneGeometry
import me.rerere.k3d.scene.light.AmbientLight
import me.rerere.k3d.scene.light.DirectionalLight
import me.rerere.k3d.scene.light.PointLight
import me.rerere.k3d.scene.light.SpotLight
import me.rerere.k3d.scene.material.BlinnPhongMaterial
import me.rerere.k3d.scene.material.StandardMaterial
import me.rerere.k3d.ui.theme.K3dTheme
import me.rerere.k3d.util.Color
import me.rerere.k3d.util.math.Vec3
import me.rerere.k3d.util.math.rotation.Euler
import me.rerere.k3d.util.math.rotation.toRadian
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class MainActivity : ComponentActivity() {
    private val render = GLES3Renderer()
    private val camera = PerspectiveCamera().apply {
        position.set(0f, 5f, 5f)
    }

    private val plane = Mesh(
        geometry = PlaneGeometry(5f, 5f),
        material = StandardMaterial().apply {
            baseColor = Color.fromRGBHex("#ff0000")
            roughness = 0.1f
            metallic = 0.8f
        },
        count = 6
    ).apply {
        position.set(0f, 0f, 0f)
    }

    private var model: Scene? = null

    private val ambientLight = AmbientLight(
        color = Color.fromRGBHex("#ffffff"),
        intensity = 0.1f
    )
    private val directionalLight = DirectionalLight(
        color = Color.fromRGBHex("#ffffff"),
        intensity = 1.9f,
        target = Vec3(0f, 0f, 0f)
    ).apply {
        position.set(30f, 30f, 30f)
    }
    private val pointLight = PointLight(
        color = Color.fromRGBHex("#ffffff"),
        intensity = 0f
    ).apply {
        position.set(0f, 5f, 0f)
    }
    private val spotLight = SpotLight(
        color = Color.fromRGBHex("#ffffff"),
        target = Vec3(0f, 0f, 0f),
        intensity = 0f,
        angle = 45f.toRadian()
    ).apply {
        position.set(0f, 5f, 0f)
    }

    private val scene = Scene().apply {
        // addChild(plane)
        addChild(ambientLight)
        addChild(directionalLight)
        addChild(pointLight)
        addChild(spotLight)
    }
    private lateinit var controls: OrbitController

    override fun onCreate(savedInstanceState: Bundle?) {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        super.onCreate(savedInstanceState)
        setContent {
            K3dTheme {
                Home()
            }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun Home() {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("K3D Demo") },
                    actions = {
                        var showLoaderMenu by remember {
                            mutableStateOf(false)
                        }
                        val modelList = remember {
                            mutableStateListOf<String>()
                        }
                        var loading by remember {
                            mutableStateOf(false)
                        }
                        LaunchedEffect(Unit) {
                            modelList.clear()
                            assets.list("")?.forEach {
                                if (it.endsWith(".gltf") || it.endsWith(".glb")) {
                                    modelList.add(it)
                                }
                            }
                        }
                        DropdownMenu(
                            expanded = showLoaderMenu,
                            onDismissRequest = { showLoaderMenu = false }
                        ) {
                            modelList.forEach {
                                DropdownMenuItem(
                                    text = { Text(text = it) },
                                    onClick = {
                                        lifecycleScope.launch(Dispatchers.IO) {
                                            loading = true
                                            showLoaderMenu = false

                                            val result = GltfLoader(this@MainActivity).load(
                                                inputStream = assets.open(it)
                                            )
                                            //result.defaultScene.scale.set(0.1f, 0.1f, 0.1f)
                                            scene.addChild(result.defaultScene)
                                            model = result.defaultScene

                                            loading = false
                                        }
                                    }
                                )
                            }
                        }
                        if(loading) {
                            CircularProgressIndicator()
                        } else {
                            TextButton(
                                onClick = {
                                    showLoaderMenu = true
                                }
                            ) {
                                Text("Load")
                            }
                        }
                    }
                )
            }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(it)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxSize()
                ) {
                    AndroidView(
                        factory = { ctx ->
                            K3DView(ctx).apply {
                                layoutParams = ViewGroup.LayoutParams(
                                    ViewGroup.LayoutParams.MATCH_PARENT,
                                    ViewGroup.LayoutParams.MATCH_PARENT
                                )

                                controls = OrbitController(
                                    camera,
                                    Vec3(0f,0f,0f),
                                    this
                                )
                            }
                        },
                        modifier = Modifier.matchParentSize()
                    )
                }

                K3DController {
                    K3DFloatController(
                        label = "Scale",
                        getter = { model?.scale?.x ?: 0f },
                        setter = {
                            model?.scale?.set(it, it, it)
                        },
                    )

                    K3DFloatController(
                        label = "Directional Light",
                        getter = { directionalLight.intensity },
                        setter = {
                            directionalLight.intensity = it
                        },
                        max = 15.0f
                    )

                    K3DFloatController(
                        label = "Ambient Light",
                        getter = { ambientLight.intensity },
                        setter = {
                            ambientLight.intensity = it
                        },
                        max = 10f
                    )

                    K3DFloatController(
                        label = "Point Light (Intensity)",
                        getter = { pointLight.intensity },
                        setter = {
                            pointLight.intensity = it
                        },
                        max = 10f
                    )

                    K3DFloatController(
                        label = "Spot Light (Intensity)",
                        getter = { spotLight.intensity },
                        setter = {
                            spotLight.intensity = it
                        },
                        max = 10f
                    )

                    K3DFloatController(
                        label = "Spot Light (Angle)",
                        getter = { spotLight.angle },
                        setter = {
                            spotLight.angle = it
                        },
                        max = 90f.toRadian()
                    )

                    K3DFloatController(
                        label = "Spot Light (penumbra)",
                        getter = { spotLight.penumbra },
                        setter = {
                            spotLight.penumbra = it
                        },
                        max = 1f
                    )
                }
            }
        }
    }

    inner class K3DView(context: Context?, attrs: AttributeSet? = null) :
        GLSurfaceView(context, attrs) {
        private val clock = Clock()

        init {
            setEGLContextClientVersion(3)
            setEGLConfigChooser(GLESAutoConfigChooser)
            // holder.setFormat(PixelFormat.RGBA_8888)
            // setZOrderOnTop(true)

            setRenderer(object : Renderer {
                override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
                    println("Surface created")
                }

                override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
                    println("Surface changed: $width, $height")
                    render.viewportSize = ViewportSize(width, height)
                    camera.aspect = width.toFloat() / height.toFloat()
                }

                override fun onDrawFrame(gl: GL10?) {
                    clock.tick()

                    val speed = 360f.toRadian() * clock.getDelta()
                    // model?.rotation?.applyRotation(Euler(0f, speed, 0f).toQuaternion())

                    render.render(scene, camera)
                }
            })
        }

        override fun onTouchEvent(event: MotionEvent): Boolean {
            controls.handleEvent(event)
            return true
        }
    }
}