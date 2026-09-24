import { useEffect, useState } from "react"
import { motion, useMotionValue, useTransform, animate } from "framer-motion"

export function AnimatedCounter({ value, decimals = 0, suffix = "" }) {
  const [display, setDisplay] = useState(0)
  const motionValue = useMotionValue(0)
  const rounded = useTransform(motionValue, (v) => v.toFixed(decimals))

  useEffect(() => {
    const controls = animate(motionValue, value, { duration: 1, ease: "easeOut" })
    const unsubscribe = rounded.on("change", (v) => setDisplay(v))
    return () => {
      controls.stop()
      unsubscribe()
    }
  }, [value])

  return <motion.span>{display}{suffix}</motion.span>
}