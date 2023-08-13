# K3D
K3D is an easy-to-use, lightweight 3D library designed for Android developers. The purpose is to allow
Android developers to easily achieve various effects without touching the underlying graphics API.

## Features
- Easy to understand and use API
- Multiple 3D format support (OBJ, GLTF, FBX)
- Lightweight and fast
- Animation support

## Usage
```kotlin
val scene = Scene()

val renderer = Renderer()

// Add a green cube to the scene
val cube = Mesh(
    BoxGeometry(1f, 1f, 1f),
    MeshBasicMaterial(Color(0x00ff00))
)
scene.add(cube)


// Add a light to the scene
val light = AmbientLight(Color(0xffffff))
scene.add(light)

// Initialize the camera
val camera = PerspectiveCamera(75f, 1f, 0.1f, 100f)
camera.setPositionZ(5f)

val renderer = OpenGLRenderer()
renderer.setRenderSize(500, 500)
renderer.setRenderCallback {
    // Rotate the cube
    cube.rotateY(0.01f)
    renderer.render(scene, camera)
}
```
