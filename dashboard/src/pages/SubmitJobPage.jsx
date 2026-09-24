import { useState } from "react"
import { useNavigate } from "react-router-dom"
import { toast } from "sonner"
import { Mail, Image, Webhook } from "lucide-react"
import { jobsApi } from "@/api/jobs"
import { JobPayloadFields } from "@/components/jobs/JobPayloadFields"
import { Button } from "@/components/ui/button"
import { Card, CardContent } from "@/components/ui/card"
import { Switch } from "@/components/ui/switch"
import { Label } from "@/components/ui/label"
import { Input } from "@/components/ui/input"

const JOB_TYPES = [
  { value: "SEND_EMAIL", label: "Send Email", icon: Mail, description: "Send a real email" },
  { value: "RESIZE_IMAGE", label: "Resize Image", icon: Image, description: "Download, resize, store" },
  { value: "WEBHOOK", label: "Webhook", icon: Webhook, description: "Call any URL you provide" },
]

export function SubmitJobPage() {
  const [jobType, setJobType] = useState("SEND_EMAIL")
  const [payload, setPayload] = useState({})
  const [scheduledAt, setScheduledAt] = useState("")
  const [notifyOnCompletion, setNotifyOnCompletion] = useState(false)
  const [submitting, setSubmitting] = useState(false)
  const navigate = useNavigate()

  function selectType(type) {
    setJobType(type)
    setPayload({})
  }

  async function handleSubmit(e) {
    e.preventDefault()
    setSubmitting(true)
    try {
      // strip the internal-only _bodyRaw helper field before sending
      const { _bodyRaw, ...cleanPayload } = payload

      const body = {
        jobType,
        payload: cleanPayload,
        notifyOnCompletion,
      }
      if (scheduledAt) {
        body.scheduledAt = new Date(scheduledAt).toISOString()
      }

      const result = await jobsApi.create(body)
      toast.success(`Job ${result.status === "SCHEDULED" ? "scheduled" : "submitted"}!`)
      navigate(`/jobs/${result.id}`)
    } catch (err) {
      toast.error(err.message)
    } finally {
      setSubmitting(false)
    }
  }

  const previewPayload = (() => {
    const { _bodyRaw, ...clean } = payload
    return clean
  })()

  return (
    <div className="flex flex-col gap-6 max-w-4xl">
      <h1 className="text-2xl font-bold">Submit a Job</h1>

      <div className="grid grid-cols-3 gap-3">
        {JOB_TYPES.map(({ value, label, icon: Icon, description }) => (
          <button
            key={value}
            type="button"
            onClick={() => selectType(value)}
            className={`text-left border rounded-lg p-4 transition-colors ${
              jobType === value ? "border-primary bg-primary/5" : "hover:bg-muted/50"
            }`}
          >
            <Icon className="h-5 w-5 mb-2" />
            <p className="font-medium text-sm">{label}</p>
            <p className="text-xs text-muted-foreground">{description}</p>
          </button>
        ))}
      </div>

      <div className="grid grid-cols-2 gap-6">
        <Card>
          <CardContent className="pt-6">
            <form onSubmit={handleSubmit} className="flex flex-col gap-4">
              <JobPayloadFields jobType={jobType} payload={payload} setPayload={setPayload} />

              <div className="space-y-1.5">
                <Label>Schedule for later (optional)</Label>
                <Input
                  type="datetime-local"
                  value={scheduledAt}
                  onChange={(e) => setScheduledAt(e.target.value)}
                />
              </div>

              <div className="flex items-center gap-2">
                <Switch checked={notifyOnCompletion} onCheckedChange={setNotifyOnCompletion} id="notify" />
                <Label htmlFor="notify">Email me when this job completes or fails</Label>
              </div>

              <Button type="submit" disabled={submitting} className="mt-2">
                {submitting ? "Submitting..." : "Submit Job"}
              </Button>
            </form>
          </CardContent>
        </Card>

        <Card>
          <CardContent className="pt-6">
            <p className="text-xs text-muted-foreground mb-2">Live payload preview</p>
            <pre className="bg-muted rounded-md p-4 text-xs overflow-x-auto min-h-40">
              {JSON.stringify(
                { jobType, payload: previewPayload, scheduledAt: scheduledAt || null, notifyOnCompletion },
                null,
                2
              )}
            </pre>
          </CardContent>
        </Card>
      </div>
    </div>
  )
}