import { useState } from "react"
import { Link } from "react-router-dom"
import { motion } from "framer-motion"
import { authApi } from "@/api/auth"
import { AuroraBackground } from "@/components/layout/AuroraBackground"
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card"
import { Input } from "@/components/ui/input"
import { Label } from "@/components/ui/label"
import { TiltCard } from "@/components/layout/TiltCard"
import { Button } from "@/components/ui/button"

export function ForgotPasswordPage() {
  const [email, setEmail] = useState("")
  const [sent, setSent] = useState(false)

  async function handleSubmit(e) {
    e.preventDefault()
    await authApi.requestPasswordReset(email)
    // Backend intentionally always responds the same way, whether or not the email exists -
    // we show the same generic message regardless, matching that privacy behavior
    setSent(true)
  }

  return (
    <AuroraBackground>
      <motion.div initial={{ opacity: 0, y: 20 }} animate={{ opacity: 1, y: 0 }} transition={{ duration: 0.5 }}>
        <TiltCard className="w-96"><Card className=" backdrop-blur-xl bg-card/70 border-white/10 shadow-2xl">
          <CardHeader>
            <CardTitle className="text-2xl">Reset your password</CardTitle>
          </CardHeader>
          <CardContent>
            {sent ? (
              <p className="text-sm text-muted-foreground">
                If an account exists for that email, a reset link has been sent. Check your inbox.
              </p>
            ) : (
              <form onSubmit={handleSubmit} className="flex flex-col gap-4">
                <div className="space-y-1.5">
                  <Label htmlFor="email">Email</Label>
                  <Input id="email" type="email" value={email}
                         onChange={(e) => setEmail(e.target.value)} required />
                </div>
                <Button type="submit" className="w-full">Send reset link</Button>
              </form>
            )}
            <Link to="/login" className="text-sm text-center text-muted-foreground hover:underline block mt-4">
              Back to login
            </Link>
          </CardContent>
        </Card>
        </TiltCard>
      </motion.div>
    </AuroraBackground>
  )
}