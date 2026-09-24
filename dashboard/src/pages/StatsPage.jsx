import { useEffect, useState } from "react"
import { toast } from "sonner"
import { PieChart, Pie, Cell, ResponsiveContainer, Legend, Tooltip } from "recharts"
import { statsApi } from "@/api/stats"
import { Card, CardContent } from "@/components/ui/card"
import { AnimatedCounter } from "@/components/ui/animated-counter"

const COLORS = {
  Completed: "#22c55e",
  Failed: "#ef4444",
  Cancelled: "#9ca3af",
  Queued: "#eab308",
  Processing: "#a855f7",
}

export function StatsPage() {
  const [stats, setStats] = useState(null)

  useEffect(() => {
    statsApi.get().then(setStats).catch((err) => toast.error(err.message))
  }, [])

  if (!stats) return <p className="text-muted-foreground">Loading...</p>

  const chartData = [
    { name: "Completed", value: stats.completedJobs },
    { name: "Failed", value: stats.failedJobs },
    { name: "Cancelled", value: stats.cancelledJobs },
    { name: "Queued", value: stats.queuedJobs },
    { name: "Processing", value: stats.processingJobs },
  ].filter((d) => d.value > 0)

  return (
    <div className="flex flex-col gap-6">
      <h1 className="text-2xl font-bold">Your Stats</h1>

      <div className="grid grid-cols-4 gap-4">
        <Card>
          <CardContent className="pt-6">
            <p className="text-sm text-muted-foreground">Total Jobs</p>
            <p className="text-3xl font-bold"><AnimatedCounter value={stats.totalJobs} /></p>
          </CardContent>
        </Card>
        <Card>
          <CardContent className="pt-6">
            <p className="text-sm text-muted-foreground">Success Rate</p>
            <p className="text-3xl font-bold">
              <AnimatedCounter value={stats.successRate * 100} decimals={1} suffix="%" />
            </p>
          </CardContent>
        </Card>
        <Card>
          <CardContent className="pt-6">
            <p className="text-sm text-muted-foreground">Failure Rate</p>
            <p className="text-3xl font-bold">
              <AnimatedCounter value={stats.failureRate * 100} decimals={1} suffix="%" />
            </p>
          </CardContent>
        </Card>
        <Card>
          <CardContent className="pt-6">
            <p className="text-sm text-muted-foreground">Avg Processing Time</p>
            <p className="text-3xl font-bold">
              <AnimatedCounter value={stats.avgProcessingTimeMs} suffix="ms" />
            </p>
          </CardContent>
        </Card>
      </div>

      <div className="grid grid-cols-2 gap-6">
        <Card>
          <CardContent className="pt-6">
            <p className="text-sm text-muted-foreground mb-4">Job Status Breakdown</p>
            {chartData.length === 0 ? (
              <p className="text-sm text-muted-foreground text-center py-16">No jobs yet</p>
            ) : (
              <ResponsiveContainer width="100%" height={250}>
                <PieChart>
                  <Pie data={chartData} dataKey="value" nameKey="name" innerRadius={50} outerRadius={80} paddingAngle={3}>
                    {chartData.map((entry) => (
                      <Cell key={entry.name} fill={COLORS[entry.name]} />
                    ))}
                  </Pie>
                  <Tooltip />
                  <Legend />
                </PieChart>
              </ResponsiveContainer>
            )}
          </CardContent>
        </Card>

        <Card>
          <CardContent className="pt-6 space-y-3">
            <p className="text-sm text-muted-foreground mb-2">Breakdown</p>
            <div className="flex justify-between text-sm"><span>Queued</span><span>{stats.queuedJobs}</span></div>
            <div className="flex justify-between text-sm"><span>Processing</span><span>{stats.processingJobs}</span></div>
            <div className="flex justify-between text-sm"><span>Completed</span><span>{stats.completedJobs}</span></div>
            <div className="flex justify-between text-sm"><span>Failed</span><span>{stats.failedJobs}</span></div>
            <div className="flex justify-between text-sm"><span>Cancelled</span><span>{stats.cancelledJobs}</span></div>
            <div className="flex justify-between text-sm"><span>Queue Depth</span><span>{stats.queueDepth}</span></div>
          </CardContent>
        </Card>
      </div>
    </div>
  )
}