import { useEffect, useRef } from "react"

const ACTIVE_STATUSES = ["QUEUED", "SCHEDULED", "PROCESSING"]

export function usePolling(jobs, refetch, intervalMs = 3500) {
  const timerRef = useRef(null)

  useEffect(() => {
    const hasActiveJob = jobs.some((job) => ACTIVE_STATUSES.includes(job.status))

    if (hasActiveJob) {
      timerRef.current = setInterval(refetch, intervalMs)
    }

    return () => {
      if (timerRef.current) clearInterval(timerRef.current)
    }
  }, [jobs, refetch, intervalMs])
}