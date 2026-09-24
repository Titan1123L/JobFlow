import { useCallback, useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { toast } from "sonner";
import {
  PlusCircle,
  Copy,
  X,
  RotateCcw,
  ChevronLeft,
  ChevronRight,
} from "lucide-react";
import { jobsApi } from "@/api/jobs";
import { usePolling } from "@/hooks/usePolling";
import { StatusBadge } from "@/components/jobs/StatusBadge";
import { JOB_TYPE_ICONS } from "@/lib/jobTypeIcons";
import { Button } from "@/components/ui/button";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";
import {
  Popover,
  PopoverContent,
  PopoverTrigger,
} from "@/components/ui/popover";
import { Skeleton } from "@/components/ui/skeleton";

const STATUS_OPTIONS = [
  "QUEUED",
  "SCHEDULED",
  "PROCESSING",
  "COMPLETED",
  "FAILED",
  "CANCELLED",
];

export function DashboardPage() {
  const [data, setData] = useState(null);
  const [loading, setLoading] = useState(true);
  const [statusFilter, setStatusFilter] = useState("");
  const [page, setPage] = useState(0);
  const [confirmCancelId, setConfirmCancelId] = useState(null);
  const navigate = useNavigate();

  const fetchJobs = useCallback(async () => {
    try {
      const result = await jobsApi.list({
        status: statusFilter || undefined,
        page,
      });
      setData(result);
    } catch (err) {
      toast.error(err.message);
    } finally {
      setLoading(false);
    }
  }, [statusFilter, page]);

  useEffect(() => {
    setLoading(true);
    fetchJobs();
  }, [fetchJobs]);

  usePolling(data?.content || [], fetchJobs);

  async function handleCancel(id) {
    try {
      await jobsApi.cancel(id);
      toast.success("Job cancelled");
      setConfirmCancelId(null);
      fetchJobs();
    } catch (err) {
      toast.error(err.message);
    }
  }

  async function handleRetry(id) {
    try {
      await jobsApi.retry(id);
      toast.success("Job re-queued");
      fetchJobs();
    } catch (err) {
      toast.error(err.message);
    }
  }

  function copyId(id) {
    navigator.clipboard.writeText(id);
    toast.success("Job ID copied");
  }

  const jobs = data?.content || [];
  const isEmpty = !loading && jobs.length === 0 && !statusFilter;

  return (
    <div className="flex flex-col gap-4">
      <div className="flex items-center justify-between">
        <h1 className="text-2xl font-bold">Your Jobs</h1>
        <Button onClick={() => navigate("/jobs/new")} className="gap-2">
          <PlusCircle className="h-4 w-4" /> Submit Job
        </Button>
      </div>

      <div className="flex items-center gap-2">
        <Select
          value={statusFilter || "ALL"}
          onValueChange={(v) => {
            setStatusFilter(v === "ALL" ? "" : v);
            setPage(0);
          }}
        >
          <SelectTrigger className="w-48">
            <SelectValue placeholder="Filter by status" />
          </SelectTrigger>
          <SelectContent>
            <SelectItem value="ALL">All statuses</SelectItem>
            {STATUS_OPTIONS.map((s) => (
              <SelectItem key={s} value={s}>
                {s}
              </SelectItem>
            ))}
          </SelectContent>
        </Select>
      </div>

      {loading ? (
        <div className="flex flex-col gap-2">
          {[...Array(5)].map((_, i) => (
            <Skeleton key={i} className="h-14 w-full rounded-lg" />
          ))}
        </div>
      ) : isEmpty ? (
        <div className="flex flex-col items-center justify-center gap-3 py-24 text-center border border-dashed rounded-lg">
          <p className="text-lg font-medium">No jobs yet</p>
          <p className="text-sm text-muted-foreground">
            Submit your first job to see it here.
          </p>
          <Button onClick={() => navigate("/jobs/new")} className="gap-2 mt-2">
            <PlusCircle className="h-4 w-4" /> Submit your first job
          </Button>
        </div>
      ) : (
        <div className="flex flex-col gap-2">
          {jobs.length === 0 && (
            <p className="text-sm text-muted-foreground py-8 text-center">
              No jobs match this filter.
            </p>
          )}
          {jobs.map((job) => {
            const Icon = JOB_TYPE_ICONS[job.jobType];
            return (
              <Popover key={job.id}>
                <div className="flex items-center gap-3 border rounded-lg px-4 py-3 hover:bg-muted/50 transition-colors">
                  <PopoverTrigger
                    className="flex items-center gap-3 flex-1 text-left cursor-pointer"
                    onClick={() => navigate(`/jobs/${job.id}`)}
                  >
                    {Icon && (
                      <Icon className="h-4 w-4 text-muted-foreground shrink-0" />
                    )}
                    <span className="text-sm font-mono text-muted-foreground shrink-0">
                      {job.id.slice(0, 8)}...
                    </span>
                    <span className="text-sm">{job.jobType}</span>
                  </PopoverTrigger>

                  <StatusBadge status={job.status} />

                  <button
                    onClick={() => copyId(job.id)}
                    className="text-muted-foreground hover:text-foreground"
                  >
                    <Copy className="h-3.5 w-3.5" />
                  </button>

                  {(job.status === "QUEUED" || job.status === "SCHEDULED") &&
                    (confirmCancelId === job.id ? (
                      <div className="flex items-center gap-1">
                        <Button
                          size="sm"
                          variant="destructive"
                          onClick={() => handleCancel(job.id)}
                        >
                          Confirm
                        </Button>
                        <Button
                          size="sm"
                          variant="ghost"
                          onClick={() => setConfirmCancelId(null)}
                        >
                          No
                        </Button>
                      </div>
                    ) : (
                      <Button
                        size="sm"
                        variant="outline"
                        onClick={() => setConfirmCancelId(job.id)}
                      >
                        <X className="h-3.5 w-3.5" />
                      </Button>
                    ))}
                  {job.status === "FAILED" && (
                    <Button
                      size="sm"
                      variant="outline"
                      onClick={() => handleRetry(job.id)}
                      className="gap-1"
                    >
                      <RotateCcw className="h-3.5 w-3.5" /> Retry
                    </Button>
                  )}
                </div>

                <PopoverContent className="w-72 text-sm space-y-1">
                  <p>
                    <span className="text-muted-foreground">Created:</span>{" "}
                    {new Date(job.createdAt).toLocaleString()}
                  </p>
                  {job.completedAt && (
                    <p>
                      <span className="text-muted-foreground">Completed:</span>{" "}
                      {new Date(job.completedAt).toLocaleString()}
                    </p>
                  )}
                  {job.errorMessage && (
                    <p className="text-red-500">{job.errorMessage}</p>
                  )}
                  <p>
                    <span className="text-muted-foreground">Retries:</span>{" "}
                    {job.retryCount}/{job.maxRetries}
                  </p>
                </PopoverContent>
              </Popover>
            );
          })}
        </div>
      )}

      {data && data.totalPages > 1 && (
        <div className="flex items-center justify-center gap-2 mt-4">
          <Button
            size="icon"
            variant="outline"
            disabled={page === 0}
            onClick={() => setPage((p) => p - 1)}
          >
            <ChevronLeft className="h-4 w-4" />
          </Button>
          <span className="text-sm text-muted-foreground">
            Page {page + 1} of {data.totalPages}
          </span>
          <Button
            size="icon"
            variant="outline"
            disabled={page >= data.totalPages - 1}
            onClick={() => setPage((p) => p + 1)}
          >
            <ChevronRight className="h-4 w-4" />
          </Button>
        </div>
      )}
    </div>
  );
}
