import { Suspense, useState } from "react"
import { Canvas, useFrame } from "@react-three/fiber"
import { Sparkles } from "@react-three/drei"
import { FloatingShape, CompanionShape } from "./FloatingShape"

// Nudges the camera toward the cursor every frame, so the whole scene
// gets a subtle parallax feel when the user moves their mouse
function CursorParallax() {
  useFrame((state) => {
    const targetX = state.pointer.x * 0.5
    const targetY = state.pointer.y * 0.35
    state.camera.position.x += (targetX - state.camera.position.x) * 0.04
    state.camera.position.y += (targetY - state.camera.position.y) * 0.04
    state.camera.lookAt(0, 0, 0)
  })
  return null
}

export function Hero3D() {
  const [failed, setFailed] = useState(false)

  if (failed) {
    return (
      <div className="absolute inset-0 -z-10 flex items-center justify-center">
        <div className="h-80 w-80 rounded-full bg-gradient-to-br from-purple-600/30 to-pink-500/20 blur-3xl" />
      </div>
    )
  }

  return (
    <div className="absolute inset-0 -z-10">
      <Canvas
        camera={{ position: [0, 0, 6], fov: 42 }}
        dpr={[1, 1.5]}
        gl={{ antialias: false, powerPreference: "default", alpha: true }}
        onCreated={({ gl }) => {
          gl.domElement.addEventListener("webglcontextlost", (e) => {
            e.preventDefault()
            setFailed(true)
          })
        }}
      >
        <Suspense fallback={null}>
          <ambientLight intensity={0.4} />
          <directionalLight position={[3, 3, 3]} intensity={1} color="#a78bfa" />
          <pointLight position={[-3, -2, 2]} intensity={1.1} color="#ec4899" />
          <pointLight position={[2, -3, 1]} intensity={0.8} color="#06b6d4" />
          <FloatingShape />
          <CompanionShape />
          <Sparkles count={60} scale={7} size={1.8} speed={0.25} color="#a78bfa" opacity={0.5} />
        </Suspense>
        <CursorParallax />
      </Canvas>
    </div>
  )
}