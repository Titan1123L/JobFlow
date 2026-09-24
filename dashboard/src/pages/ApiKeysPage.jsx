import { useEffect, useState } from "react";
import { toast } from "sonner";
import { Plus, Copy, Trash2, KeyRound, Check } from "lucide-react";
import { apiKeysApi } from "@/api/apiKeys";
import { Button } from "@/components/ui/button";
import { Card, CardContent } from "@/components/ui/card";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
  DialogFooter,
} from "@/components/ui/dialog";
import {
  AlertDialog,
  AlertDialogTrigger,
  AlertDialogContent,
  AlertDialogHeader,
  AlertDialogTitle,
  AlertDialogDescription,
  AlertDialogFooter,
  AlertDialogCancel,
  AlertDialogAction,
} from "@/components/ui/alert-dialog";

export function ApiKeysPage() {
  const [keys, setKeys] = useState([]);
  const [loading, setLoading] = useState(true);
  const [createOpen, setCreateOpen] = useState(false);
  const [newName, setNewName] = useState("");
  const [revealedKey, setRevealedKey] = useState(null);
  const [confirmedSaved, setConfirmedSaved] = useState(false);
  const [copied, setCopied] = useState(false);

  function fetchKeys() {
    apiKeysApi
      .list()
      .then(setKeys)
      .catch((err) => toast.error(err.message))
      .finally(() => setLoading(false));
  }

  useEffect(fetchKeys, []);

  async function handleCreate(e) {
    e.preventDefault();
    try {
      const result = await apiKeysApi.create(newName);
      setRevealedKey(result);
      setCreateOpen(false);
      setNewName("");
      fetchKeys();
    } catch (err) {
      toast.error(err.message);
    }
  }

  async function handleRevoke(id) {
    try {
      await apiKeysApi.revoke(id);
      toast.success("Key revoked");
      fetchKeys();
    } catch (err) {
      toast.error(err.message);
    }
  }

  function copyKey() {
    navigator.clipboard.writeText(revealedKey.key);
    setCopied(true);
    toast.success("Copied to clipboard");
    setTimeout(() => setCopied(false), 2000);
  }

  function closeRevealModal() {
    setRevealedKey(null);
    setConfirmedSaved(false);
    setCopied(false);
  }

  return (
    <div className="flex flex-col gap-6 max-w-2xl">
      <div className="flex items-center justify-between">
        <h1 className="text-2xl font-bold">API Keys</h1>
        <Dialog open={createOpen} onOpenChange={setCreateOpen}>
          <Button onClick={() => setCreateOpen(true)} className="gap-2">
            <Plus className="h-4 w-4" /> Generate Key
          </Button>
          <DialogContent>
            <DialogHeader>
              <DialogTitle>Generate a new API key</DialogTitle>
            </DialogHeader>
            <form onSubmit={handleCreate} className="flex flex-col gap-4">
              <div className="space-y-1.5">
                <Label>Name</Label>
                <Input
                  value={newName}
                  onChange={(e) => setNewName(e.target.value)}
                  placeholder="e.g. CI pipeline"
                  required
                />
              </div>
              <Button type="submit">Generate</Button>
            </form>
          </DialogContent>
        </Dialog>
      </div>

      {loading ? (
        <p className="text-muted-foreground">Loading...</p>
      ) : keys.length === 0 ? (
        <div className="flex flex-col items-center justify-center gap-3 py-16 text-center border border-dashed rounded-lg">
          <KeyRound className="h-8 w-8 text-muted-foreground" />
          <p className="text-sm text-muted-foreground">
            No API keys yet. Generate one for scripts or CI.
          </p>
        </div>
      ) : (
        <div className="flex flex-col gap-2">
          {keys.map((key) => (
            <Card key={key.id}>
              <CardContent className="pt-4 pb-4 flex items-center justify-between">
                <div>
                  <p className="font-medium text-sm">{key.name}</p>
                  <p className="text-xs font-mono text-muted-foreground">
                    {key.keyPrefix}••••••••••••••••
                  </p>
                  <p className="text-xs text-muted-foreground mt-1">
                    Created {new Date(key.createdAt).toLocaleDateString()}
                    {key.revokedAt && (
                      <span className="text-red-500"> · Revoked</span>
                    )}
                  </p>
                </div>
                {!key.revokedAt && (
                  <AlertDialog>
                    <AlertDialogTrigger className="inline-flex items-center justify-center rounded-md h-8 w-8 text-muted-foreground hover:text-red-500 hover:bg-muted">
                      <Trash2 className="h-4 w-4" />
                    </AlertDialogTrigger>
                    <AlertDialogContent>
                      <AlertDialogHeader>
                        <AlertDialogTitle>Revoke this key?</AlertDialogTitle>
                        <AlertDialogDescription>
                          Anything using "{key.name}" will stop working
                          immediately. This can't be undone.
                        </AlertDialogDescription>
                      </AlertDialogHeader>
                      <AlertDialogFooter>
                        <AlertDialogCancel>Cancel</AlertDialogCancel>
                        <AlertDialogAction onClick={() => handleRevoke(key.id)}>
                          Revoke
                        </AlertDialogAction>
                      </AlertDialogFooter>
                    </AlertDialogContent>
                  </AlertDialog>
                )}
              </CardContent>
            </Card>
          ))}
        </div>
      )}

      <Dialog
        open={!!revealedKey}
        onOpenChange={(open) => !open && confirmedSaved && closeRevealModal()}
      >
        <DialogContent
          onInteractOutside={(e) => !confirmedSaved && e.preventDefault()}
          onEscapeKeyDown={(e) => !confirmedSaved && e.preventDefault()}
        >
          <DialogHeader>
            <DialogTitle>Your new API key</DialogTitle>
          </DialogHeader>
          <div className="flex flex-col gap-3">
            <p className="text-sm text-muted-foreground">
              Copy this now — you won't be able to see it again.
            </p>
            <div className="flex items-center gap-2">
              <code className="flex-1 bg-muted rounded-md px-3 py-2 text-xs break-all">
                {revealedKey?.key}
              </code>
              <Button size="icon" variant="outline" onClick={copyKey}>
                {copied ? (
                  <Check className="h-4 w-4" />
                ) : (
                  <Copy className="h-4 w-4" />
                )}
              </Button>
            </div>
            <label className="flex items-center gap-2 text-sm mt-2">
              <input
                type="checkbox"
                checked={confirmedSaved}
                onChange={(e) => setConfirmedSaved(e.target.checked)}
              />
              I've saved this key somewhere safe
            </label>
          </div>
          <DialogFooter>
            <Button disabled={!confirmedSaved} onClick={closeRevealModal}>
              Done
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>
    </div>
  );
}
