import { motion, useMotionValue, useSpring, useTransform, useMotionTemplate } from "framer-motion"

export function TiltCard({ children, className }) {
  const x = useMotionValue(0.5)
  const y = useMotionValue(0.5)

  const springConfig = { stiffness: 150, damping: 18 }
  const rotateX = useSpring(useTransform(y, [0, 1], [8, -8]), springConfig)
  const rotateY = useSpring(useTransform(x, [0, 1], [-8, 8]), springConfig)
  const glareX = useTransform(x, [0, 1], ["0%", "100%"])
  const glareY = useTransform(y, [0, 1], ["0%", "100%"])
  const glareBackground = useMotionTemplate`radial-gradient(280px circle at ${glareX} ${glareY}, rgba(255,255,255,0.10), transparent 60%)`

  function handleMouseMove(e) {
    const rect = e.currentTarget.getBoundingClientRect()
    x.set((e.clientX - rect.left) / rect.width)
    y.set((e.clientY - rect.top) / rect.height)
  }

  function handleMouseLeave() {
    x.set(0.5)
    y.set(0.5)
  }

  return (
    <motion.div
      onMouseMove={handleMouseMove}
      onMouseLeave={handleMouseLeave}
      style={{ rotateX, rotateY, transformStyle: "preserve-3d", transformPerspective: 900 }}
      className={`relative ${className ?? ""}`}
    >
      {children}
      <motion.div
        className="pointer-events-none absolute inset-0 rounded-xl opacity-60"
        style={{ background: glareBackground }}
      />
    </motion.div>
  )
}