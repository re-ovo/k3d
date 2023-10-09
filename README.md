# K3D

K3D is an easy-to-use, lightweight 3D library designed for Android developers. The purpose is to
allow
Android developers to easily achieve various effects without touching the underlying graphics API.

> Still working on it and it's not ready for testing or use. But I am welcome to any suggestions or
> contributions.

## Roadmap/TODO

Check [TODO.md](todo.md) for more information.

## Getting Started

```kotlin
// settings.gradle.kts
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()

        // Add the snapshot repository to your settings.gradle.kts
        maven("https://s01.oss.sonatype.org/content/repositories/snapshots")
    }
}
```

```kotlin
// build.gradle.kts (app)
dependencies {
    // Add the dependency to your build.gradle.kts
    implementation("me.rerere:k3d:1.0.0-SNAPSHOT")
}
```

Check the official [documentation](https://k3d.rerere.me) for more information.

## Who is using K3D

- TODO

> Using K3D? Please let me know and I'll add your project to the list.

## Contributing

If you want to contribute to K3D, please read the [CONTRIBUTING.md](docs/contribution.md) first.

## License

```
                                 Apache License
                           Version 2.0, January 2004
                        http://www.apache.org/licenses/

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.

```