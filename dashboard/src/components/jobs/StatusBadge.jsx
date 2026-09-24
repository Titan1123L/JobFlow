import { Badge } from "@/components/ui/badge"

const STATUS_STYLES = {
  SCHEDULED: "bg-blue-500/15 text-blue-500 border-blue-500/30",
  QUEUED: "bg-yellow-500/15 text-yellow-500 border-yellow-500/30",
  PROCESSING: "bg-purple-500/15 text-purple-500 border-purple-500/30",
  COMPLETED: "bg-green-500/15 text-green-500 border-green-500/30",
  FAILED: "bg-red-500/15 text-red-500 border-red-500/30",
  CANCELLED: "bg-gray-500/15 text-gray-400 border-gray-500/30",
}

const ACTIVE_STATUSES = ["QUEUED", "SCHEDULED", "PROCESSING"]

export function StatusBadge({ status }) {
  const isActive = ACTIVE_STATUSES.includes(status)

  return (
    <Badge variant="outline" className={`gap-1.5 ${STATUS_STYLES[status] || ""}`}>
      {isActive && (
        <span className="relative flex h-1.5 w-1.5">
          <span className="animate-ping absolute inline-flex h-full w-full rounded-full bg-current opacity-75" />
          <span className="relative inline-flex h-1.5 w-1.5 rounded-full bg-current" />
        </span>
      )}
      {status}
    </Badge>
  )
}