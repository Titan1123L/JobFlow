import { Link } from "react-router-dom"
import { motion, useMotionValue, useMotionTemplate } from "framer-motion"
import { Zap, Mail, Image, Webhook, ShieldCheck, TrendingUp } from "lucide-react"
import { Hero3D } from "@/components/landing/Hero3D"
import { Button } from "@/components/ui/button"
import { Badge } from "@/components/ui/badge"

const FEATURES = [
  { icon: Mail, title: "Send Email", desc: "Real emails, delivered reliably with automatic retries." },
  { icon: Image, title: "Resize Images", desc: "Fetch, resize, and store — done in the background." },
  { icon: Webhook, title: "Call Any Webhook", desc: "Trigger any endpoint you control, safely and reliably." },
  { icon: ShieldCheck, title: "Built for Reliability", desc: "Retry with backoff, dead-letter queues, zero lost jobs." },
  { icon: TrendingUp, title: "Scales Horizontally", desc: "Add workers, get more throughput. No code changes." },
  { icon: Zap, title: "Real-time Status", desc: "Watch every job move from queued to completed, live." },
]

// Radial glow that tracks the cursor across the whole hero section
function HeroSpotlight({ mouseX, mouseY }) {
  const background = useMotionTemplate`radial-gradient(500px circle at ${mouseX}px ${mouseY}px, rgba(167,139,250,0.14), transparent 70%)`
  return <motion.div className="pointer-events-none absolute inset-0 -z-10" style={{ background }} />
}

// Feature card with a glow that follows the cursor while hovered
function FeatureCard({ icon: Icon, title, desc, index }) {
  const cardX = useMotionValue(0)
  const cardY = useMotionValue(0)
  const cardGlow = useMotionTemplate`radial-gradient(220px circle at ${cardX}px ${cardY}px, rgba(139,92,246,0.18), transparent 80%)`

  function handleMove(e) {
    const rect = e.currentTarget.getBoundingClientRect()
    cardX.set(e.clientX - rect.left)
    cardY.set(e.clientY - rect.top)
  }

  return (
    <motion.div
      onMouseMove={handleMove}
      initial={{ opacity: 0, y: 20 }}
      whileInView={{ opacity: 1, y: 0 }}
      viewport={{ once: true }}
      transition={{ delay: index * 0.08 }}
      whileHover={{ y: -4 }}
      className="group relative overflow-hidden rounded-xl border border-white/10 bg-card/50 p-6 backdrop-blur-sm transition-colors hover:border-primary/50"
    >
      <motion.div
        className="pointer-events-none absolute inset-0 opacity-0 transition-opacity duration-300 group-hover:opacity-100"
        style={{ background: cardGlow }}
      />
      <div className="relative z-10">
        <Icon className="h-8 w-8 text-primary mb-3" />
        <h3 className="font-semibold mb-1">{title}</h3>
        <p className="text-sm text-muted-foreground">{desc}</p>
      </div>
    </motion.div>
  )
}

const buttonFeel = "transition-transform hover:scale-105 active:scale-95"

export function LandingPage() {
  const heroMouseX = useMotionValue(0)
  const heroMouseY = useMotionValue(0)

  function handleHeroMouseMove(e) {
    const rect = e.currentTarget.getBoundingClientRect()
    heroMouseX.set(e.clientX - rect.left)
    heroMouseY.set(e.clientY - rect.top)
  }

  return (
    <div className="min-h-screen bg-background text-foreground overflow-x-hidden">
      <nav className="fixed top-0 w-full z-20 flex items-center justify-between px-8 py-4 backdrop-blur-md bg-background/60 border-b border-white/5">
        <div className="text-xl font-bold flex items-center gap-2">
          <Zap className="h-5 w-5 text-primary" /> JobFlow
        </div>
        <div className="flex gap-2">
          <Link to="/login"><Button variant="ghost" className={buttonFeel}>Log in</Button></Link>
          <Link to="/register"><Button className={buttonFeel}>Get started</Button></Link>
        </div>
      </nav>

      <section
        onMouseMove={handleHeroMouseMove}
        className="relative isolate h-screen flex items-center justify-center px-6"
      >
        <Hero3D />
        <HeroSpotlight mouseX={heroMouseX} mouseY={heroMouseY} />
        <div className="absolute inset-0 bg-gradient-to-b from-background/70 via-background/30 to-background -z-10" />
        <motion.div
          initial={{ opacity: 0, y: 30 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ duration: 0.8 }}
          className="text-center max-w-2xl rounded-3xl border border-white/10 bg-background/40 backdrop-blur-xl px-8 py-10 shadow-2xl"
        >
          <Badge variant="outline" className="mb-4 backdrop-blur-md">Distributed job processing, done right</Badge>
          <h1 className="text-6xl font-bold tracking-tight mb-4 bg-clip-text text-transparent bg-gradient-to-r from-white via-purple-200 to-purple-400">
            Ship background jobs<br />without the headache
          </h1>
          <p className="text-lg text-muted-foreground mb-8">
            Submit a job, get an ID back instantly. JobFlow handles retries, scaling,
            and delivery — so you don't have to.
          </p>
          <div className="flex gap-3 justify-center">
            <Link to="/register">
              <Button size="lg" className={`gap-2 ${buttonFeel}`}>Get started free <Zap className="h-4 w-4" /></Button>
            </Link>
            <Link to="/login">
              <Button size="lg" variant="outline" className={buttonFeel}>Log in</Button>
            </Link>
          </div>
        </motion.div>
      </section>

      <section className="py-24 px-6 max-w-6xl mx-auto">
        <motion.h2
          initial={{ opacity: 0, y: 20 }}
          whileInView={{ opacity: 1, y: 0 }}
          viewport={{ once: true }}
          className="text-3xl font-bold text-center mb-12"
        >
          Everything you need to run background work
        </motion.h2>
        <div className="grid grid-cols-3 gap-6">
          {FEATURES.map((f, i) => (
            <FeatureCard key={f.title} index={i} {...f} />
          ))}
        </div>
      </section>

      <section className="py-24 px-6 text-center">
        <motion.div
          initial={{ opacity: 0, scale: 0.95 }}
          whileInView={{ opacity: 1, scale: 1 }}
          viewport={{ once: true }}
        >
          <h2 className="text-3xl font-bold mb-4">Ready to ship reliable background jobs?</h2>
          <Link to="/register">
            <Button size="lg" className={`gap-2 ${buttonFeel}`}>Create your account <Zap className="h-4 w-4" /></Button>
          </Link>
        </motion.div>
      </section>
    </div>
  )
} 