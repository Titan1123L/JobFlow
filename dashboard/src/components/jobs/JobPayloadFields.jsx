import { Input } from "@/components/ui/input"
import { Textarea } from "@/components/ui/textarea"
import { Label } from "@/components/ui/label"

export function JobPayloadFields({ jobType, payload, setPayload }) {
  function update(field, value) {
    setPayload((prev) => ({ ...prev, [field]: value }))
  }

  if (jobType === "SEND_EMAIL") {
    return (
      <div className="flex flex-col gap-3">
        <div className="space-y-1.5">
          <Label>To</Label>
          <Input type="email" value={payload.to || ""} onChange={(e) => update("to", e.target.value)} required />
        </div>
        <div className="space-y-1.5">
          <Label>Subject</Label>
          <Input value={payload.subject || ""} onChange={(e) => update("subject", e.target.value)} required />
        </div>
        <div className="space-y-1.5">
          <Label>Body</Label>
          <Textarea value={payload.body || ""} onChange={(e) => update("body", e.target.value)} required rows={4} />
        </div>
      </div>
    )
  }

  if (jobType === "RESIZE_IMAGE") {
    return (
      <div className="flex flex-col gap-3">
        <div className="space-y-1.5">
          <Label>Source image URL</Label>
          <Input type="url" value={payload.sourceUrl || ""} onChange={(e) => update("sourceUrl", e.target.value)} required />
        </div>
        <div className="flex gap-3">
          <div className="space-y-1.5 flex-1">
            <Label>Target width</Label>
            <Input type="number" min={1} max={4096} value={payload.targetWidth || ""}
                   onChange={(e) => update("targetWidth", Number(e.target.value))} required />
          </div>
          <div className="space-y-1.5 flex-1">
            <Label>Target height</Label>
            <Input type="number" min={1} max={4096} value={payload.targetHeight || ""}
                   onChange={(e) => update("targetHeight", Number(e.target.value))} required />
          </div>
        </div>
      </div>
    )
  }

  if (jobType === "WEBHOOK") {
    return (
      <div className="flex flex-col gap-3">
        <div className="space-y-1.5">
          <Label>URL</Label>
          <Input type="url" value={payload.url || ""} onChange={(e) => update("url", e.target.value)} required />
          <p className="text-xs text-muted-foreground">Private/internal addresses are blocked for security.</p>
        </div>
        <div className="space-y-1.5">
          <Label>Method</Label>
          <select
            className="border rounded-md h-9 px-3 bg-transparent text-sm"
            value={payload.method || "POST"}
            onChange={(e) => update("method", e.target.value)}
          >
            <option value="GET">GET</option>
            <option value="POST">POST</option>
            <option value="PUT">PUT</option>
          </select>
        </div>
        <div className="space-y-1.5">
          <Label>Body (JSON, optional)</Label>
          <Textarea
            value={payload._bodyRaw !== undefined ? payload._bodyRaw : (payload.body ? JSON.stringify(payload.body) : "")}
            onChange={(e) => {
              const raw = e.target.value
              try {
                const parsed = raw.trim() ? JSON.parse(raw) : undefined
                setPayload((prev) => ({ ...prev, body: parsed, _bodyRaw: raw }))
              } catch {
                setPayload((prev) => ({ ...prev, _bodyRaw: raw }))
              }
            }}
            rows={3}
            placeholder='{"key": "value"}'
          />
        </div>
      </div>
    )
  }

  return null
}