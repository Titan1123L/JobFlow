import { useCallback, useEffect, useRef, useState } from "react";
import { useParams, useNavigate } from "react-router-dom";
import { toast } from "sonner";
import confetti from "canvas-confetti";
import {
  ReactCompareSlider,
  ReactCompareSliderImage,
} from "react-compare-slider";
import { ArrowLeft, Copy, RotateCcw, X, Download } from "lucide-react";
import { jobsApi } from "@/api/jobs";
import { usePolling } from "@/hooks/usePolling";
import { StatusBadge } from "@/components/jobs/StatusBadge";
import { StatusTimeline } from "@/components/jobs/StatusTimeline";
import { Button } from "@/components/ui/button";
import { Card, CardContent } from "@/components/ui/card";
import { api } from "@/api/client";

const API_BASE_URL =
  import.meta.env.VITE_API_BASE_URL || "http://localhost:8080";

export function JobDetailPage() {
  const { id } = useParams();
  const navigate = useNavigate();
  const [job, setJob] = useState(null);
  const [loading, setLoading] = useState(true);
  const [resultUrl, setResultUrl] = useState(null);
  const prevStatusRef = useRef(null);

  const fetchJob = useCallback(async () => {
    try {
      const result = await jobsApi.get(id);
      setJob(result);
      if (result.jobType === "RESIZE_IMAGE" && result.status === "COMPLETED") {
        try {
          const url = await api.getBlobUrl(`/api/jobs/${id}/result`);
          setResultUrl(url);
        } catch {
          // image not available yet or failed - not critical
        }
      }
      if (
        prevStatusRef.current &&
        prevStatusRef.current !== "COMPLETED" &&
        result.status === "COMPLETED"
      ) {
        confetti({ particleCount: 120, spread: 80, origin: { y: 0.6 } });
        toast.success("Job completed!");
      }
      prevStatusRef.current = result.status;
    } catch (err) {
      toast.error(err.message);
    } finally {
      setLoading(false);
    }
  }, [id]);

  useEffect(() => {
    fetchJob();
  }, [fetchJob]);

  usePolling(job ? [job] : [], fetchJob);

  async function handleCancel() {
    try {
      await jobsApi.cancel(id);
      toast.success("Job cancelled");
      fetchJob();
    } catch (err) {
      toast.error(err.message);
    }
  }

  async function handleRetry() {
    try {
      await jobsApi.retry(id);
      toast.success("Job re-queued");
      fetchJob();
    } catch (err) {
      toast.error(err.message);
    }
  }

  function copyId() {
    navigator.clipboard.writeText(id);
    toast.success("Job ID copied");
  }

  if (loading) return <p className="text-muted-foreground">Loading...</p>;
  if (!job) return <p className="text-muted-foreground">Job not found.</p>;

  const canCancel = job.status === "QUEUED" || job.status === "SCHEDULED";
  const canRetry = job.status === "FAILED";
  const canDownload =
    job.jobType === "RESIZE_IMAGE" && job.status === "COMPLETED";

  return (
    <div className="flex flex-col gap-6 max-w-3xl">
      <button
        onClick={() => navigate("/dashboard")}
        className="flex items-center gap-1 text-sm text-muted-foreground hover:text-foreground w-fit"
      >
        <ArrowLeft className="h-4 w-4" /> Back to jobs
      </button>

      <div className="flex items-center justify-between">
        <div className="flex items-center gap-3">
          <h1 className="text-2xl font-bold">{job.jobType}</h1>
          <StatusBadge status={job.status} />
        </div>
        <div className="flex gap-2">
          {canCancel && (
            <Button
              variant="outline"
              size="sm"
              onClick={handleCancel}
              className="gap-1"
            >
              <X className="h-3.5 w-3.5" /> Cancel
            </Button>
          )}
          {canRetry && (
            <Button
              variant="outline"
              size="sm"
              onClick={handleRetry}
              className="gap-1"
            >
              <RotateCcw className="h-3.5 w-3.5" /> Retry
            </Button>
          )}
        </div>
      </div>

      <Card>
        <CardContent className="pt-6">
          <StatusTimeline status={job.status} />
        </CardContent>
      </Card>

      <Card>
        <CardContent className="pt-6 space-y-2 text-sm">
          <div className="flex justify-between">
            <span className="text-muted-foreground">Job ID</span>
            <button
              onClick={copyId}
              className="flex items-center gap-1 font-mono hover:text-primary"
            >
              {job.id} <Copy className="h-3 w-3" />
            </button>
          </div>
          <div className="flex justify-between">
            <span className="text-muted-foreground">Created</span>
            <span>{new Date(job.createdAt).toLocaleString()}</span>
          </div>
          {job.scheduledAt && (
            <div className="flex justify-between">
              <span className="text-muted-foreground">Scheduled for</span>
              <span>{new Date(job.scheduledAt).toLocaleString()}</span>
            </div>
          )}
          {job.startedAt && (
            <div className="flex justify-between">
              <span className="text-muted-foreground">Started</span>
              <span>{new Date(job.startedAt).toLocaleString()}</span>
            </div>
          )}
          {job.completedAt && (
            <div className="flex justify-between">
              <span className="text-muted-foreground">Completed</span>
              <span>{new Date(job.completedAt).toLocaleString()}</span>
            </div>
          )}
          <div className="flex justify-between">
            <span className="text-muted-foreground">Retries</span>
            <span>
              {job.retryCount} / {job.maxRetries}
            </span>
          </div>
          {job.errorMessage && (
            <div className="pt-2">
              <p className="text-red-500">{job.errorMessage}</p>
            </div>
          )}
        </CardContent>
      </Card>

      {canDownload && resultUrl && (
        <Card>
          <CardContent className="pt-6 flex flex-col gap-3">
            <p className="text-sm text-muted-foreground">Result</p>
            <ReactCompareSlider
              itemOne={
                <div className="flex items-center justify-center h-full bg-muted text-xs text-muted-foreground">
                  Original
                </div>
              }
              itemTwo={
                <ReactCompareSliderImage src={resultUrl} alt="Resized result" />
              }
              className="rounded-lg overflow-hidden border"
            />
            <a href={resultUrl} download={`${job.id}.png`}>
              <Button variant="outline" size="sm" className="gap-1">
                <Download className="h-3.5 w-3.5" /> Download result
              </Button>
            </a>
          </CardContent>
        </Card>
      )}
    </div>
  );
}
