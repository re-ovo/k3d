package me.rerere.k3d

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import me.rerere.k3d.dsl.CubeMesh
import me.rerere.k3d.dsl.GltfModel
import me.rerere.k3d.dsl.K3DCanvas
import me.rerere.k3d.dsl.LightAmbient
import me.rerere.k3d.dsl.LightDirectional

@Composable
fun K3DDslExample() {
    K3DCanvas(
        modifier = Modifier.fillMaxSize()
    ) {
        LightAmbient()
        LightDirectional()

        CubeMesh()
        GltfModel(path = "axe.glb")
    }
}