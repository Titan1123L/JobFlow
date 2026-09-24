import { useRef } from "react";
import { useFrame } from "@react-three/fiber";
import {
  MeshDistortMaterial,
  Icosahedron,
  Octahedron,
} from "@react-three/drei";

export function FloatingShape() {
  const meshRef = useRef();

  useFrame((state) => {
    const t = state.clock.getElapsedTime();
    meshRef.current.rotation.x = t * 0.12;
    meshRef.current.rotation.y = t * 0.18;
    meshRef.current.position.y = 0.4 + Math.sin(t * 0.6) * 0.25;
  });

  return (
    <Icosahedron ref={meshRef} args={[0.85, 1]} position={[1.6, 0.4, -1]}>
      <MeshDistortMaterial
        color="#8b5cf6"
        attach="material"
        distort={0.35}
        speed={1.4}
        roughness={0.35}
        metalness={0.55}
        emissive="#4c1d95"
        emissiveIntensity={0.3}
      />
    </Icosahedron>
  );
}

export function CompanionShape() {
  const ref = useRef();

  useFrame((state) => {
    const t = state.clock.getElapsedTime();
    ref.current.rotation.x = t * 0.2;
    ref.current.rotation.y = t * 0.25;
    ref.current.rotation.z = t * 0.1;
    ref.current.position.y = -1.1 + Math.cos(t * 0.5) * 0.2;
  });

  return (
    <Octahedron ref={ref} args={[0.4, 2]} position={[-2.4, -1.1, -1.5]}>
      <MeshDistortMaterial
        color="#06b6d4"
        distort={0.3}
        speed={1.2}
        roughness={0.3}
        metalness={0.45}
        emissive="#06b6d4"
        emissiveIntensity={0.45}
      />
    </Octahedron>
  );
}
