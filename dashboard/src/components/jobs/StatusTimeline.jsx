import { Check, Loader2, X, Clock } from "lucide-react"

const STEPS = ["SCHEDULED", "QUEUED", "PROCESSING", "COMPLETED"]

function getStepState(status, step, index, currentIndex) {
  if (status === "FAILED" && step === "COMPLETED") return "failed"
  if (status === "CANCELLED") return index <= currentIndex ? "done" : "pending"
  if (index < currentIndex) return "done"
  if (index === currentIndex) return status === "PROCESSING" ? "active" : "done"
  return "pending"
}

export function StatusTimeline({ status }) {
  const effectiveStatus = status === "FAILED" ? "PROCESSING" : status
  const currentIndex = STEPS.indexOf(effectiveStatus === "CANCELLED" ? "QUEUED" : effectiveStatus)

  return (
    <div className="flex items-center">
      {STEPS.map((step, index) => {
        const state = getStepState(status, step, index, currentIndex)
        const isLast = index === STEPS.length - 1
        const showFailed = status === "FAILED" && step === "COMPLETED"

        return (
          <div key={step} className="flex items-center flex-1 last:flex-none">
            <div className="flex flex-col items-center gap-1">
              <div
                className={`h-8 w-8 rounded-full flex items-center justify-center border-2 ${
                  showFailed
                    ? "border-red-500 bg-red-500/10 text-red-500"
                    : state === "done"
                    ? "border-green-500 bg-green-500 text-white"
                    : state === "active"
                    ? "border-purple-500 bg-purple-500/10 text-purple-500"
                    : "border-muted-foreground/30 text-muted-foreground/50"
                }`}
              >
                {showFailed ? (
                  <X className="h-4 w-4" />
                ) : state === "done" ? (
                  <Check className="h-4 w-4" />
                ) : state === "active" ? (
                  <Loader2 className="h-4 w-4 animate-spin" />
                ) : (
                  <Clock className="h-4 w-4" />
                )}
              </div>
              <span className="text-xs text-muted-foreground">
                {showFailed ? "FAILED" : step}
              </span>
            </div>
            {!isLast && (
              <div
                className={`h-0.5 flex-1 mx-2 ${
                  index < currentIndex ? "bg-green-500" : "bg-muted-foreground/20"
                }`}
              />
            )}
          </div>
        )
      })}
    </div>
  )
}