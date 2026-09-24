import { useState } from "react";
import { useSearchParams, useNavigate, Link } from "react-router-dom";
import { motion } from "framer-motion";
import { authApi } from "@/api/auth";
import { AuroraBackground } from "@/components/layout/AuroraBackground";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { PasswordInput } from "@/components/ui/password-input";
import { PasswordStrength } from "@/components/ui/password-strength";
import { Label } from "@/components/ui/label";
import { Button } from "@/components/ui/button";
import { TiltCard } from "@/components/layout/TiltCard";

export function ResetPasswordPage() {
  const [searchParams] = useSearchParams();
  const token = searchParams.get("token");
  const [password, setPassword] = useState("");
  const [error, setError] = useState("");
  const [success, setSuccess] = useState(false);
  const navigate = useNavigate();

  async function handleSubmit(e) {
    e.preventDefault();
    setError("");
    try {
      await authApi.confirmPasswordReset(token, password);
      setSuccess(true);
      setTimeout(() => navigate("/login"), 1500);
    } catch (err) {
      setError(err.message);
    }
  }

  if (!token) {
    return (
      <AuroraBackground>
        <TiltCard classNmae="w-96">
          <Card className="w-96 backdrop-blur-xl bg-card/70 border-white/10 shadow-2xl">
            <CardContent className="pt-6">
              <p className="text-sm text-red-500">
                No reset token found. Please use the link from your email.
              </p>
              <Link
                to="/forgot-password"
                className="text-sm text-muted-foreground hover:underline block mt-4"
              >
                Request a new link
              </Link>
            </CardContent>
          </Card>
        </TiltCard>
      </AuroraBackground>
    );
  }

  return (
    <AuroraBackground>
      <motion.div
        initial={{ opacity: 0, y: 20 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{ duration: 0.5 }}
      >
        <TiltCard className="w-96">
          <Card className="w-96 backdrop-blur-xl bg-card/70 border-white/10 shadow-2xl">
            <CardHeader>
              <CardTitle className="text-2xl">Set a new password</CardTitle>
            </CardHeader>
            <CardContent>
              {success ? (
                <p className="text-sm text-green-500">
                  Password updated! Redirecting to login...
                </p>
              ) : (
                <form onSubmit={handleSubmit} className="flex flex-col gap-4">
                  <div className="space-y-1.5">
                    <Label htmlFor="password">New password</Label>
                    <PasswordInput
                      id="password"
                      value={password}
                      onChange={(e) => setPassword(e.target.value)}
                      required
                      minLength={8}
                    />
                    <PasswordStrength password={password} />
                  </div>
                  {error && <p className="text-sm text-red-500">{error}</p>}
                  <Button type="submit" className="w-full">
                    Set new password
                  </Button>
                </form>
              )}
            </CardContent>
          </Card>
        </TiltCard>
      </motion.div>
    </AuroraBackground>
  );
}
