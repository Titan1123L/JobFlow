import { Outlet, NavLink } from "react-router-dom";
import {
  LayoutDashboard,
  PlusCircle,
  BarChart3,
  KeyRound,
  Command,
  LogOut,
} from "lucide-react";
import { Button } from "@/components/ui/button";
import { ThemeToggle } from "./ThemeToggle";
import { NotificationBell } from "./NotificationBell";
import { CommandPalette } from "./CommandPalette";
import { useAuth } from "@/context/AuthContext";
import { useEffect } from "react";
import { HelpCircle } from "lucide-react";
import { PageTransition } from "./PageTransition"
import { startTour } from "@/hooks/useTour";

const NAV_ITEMS = [
  {
    to: "/dashboard",
    label: "Dashboard",
    icon: LayoutDashboard,
    id: "tour-job-list",
  },
  {
    to: "/jobs/new",
    label: "Submit Job",
    icon: PlusCircle,
    id: "tour-submit-job",
  },
  { to: "/stats", label: "Stats", icon: BarChart3, id: "tour-stats" },
  { to: "/api-keys", label: "API Keys", icon: KeyRound, id: "tour-api-keys" },
];

export function AppShell() {
  const { logout } = useAuth();

    useEffect(() => {
    const justRegistered = sessionStorage.getItem("justRegistered")
    if (justRegistered) {
      const timer = setTimeout(() => {
        sessionStorage.removeItem("justRegistered")
        startTour()
      }, 600)
      return () => clearTimeout(timer)
    }
  }, [])

  return (
    <div className="min-h-screen flex bg-background text-foreground">
      <CommandPalette />

      <aside className="w-60 border-r border-border flex flex-col p-4 gap-6">
        <div className="text-xl font-bold px-2">JobFlow</div>
        <nav className="flex flex-col gap-1">
          {NAV_ITEMS.map(({ to, label, icon: Icon, id }) => (
            <NavLink
              key={to}
              id={id}
              to={to}
              className={({ isActive }) =>
                `flex items-center gap-2 px-3 py-2 rounded-md text-sm transition-colors ${
                  isActive
                    ? "bg-primary text-primary-foreground"
                    : "hover:bg-muted"
                }`
              }
            >
              <Icon className="h-4 w-4" />
              {label}
            </NavLink>
          ))}
        </nav>
        <div className="mt-auto flex flex-col gap-2">
          <Button
            variant="ghost"
            size="sm"
            className="justify-start gap-2 text-muted-foreground"
            onClick={startTour}
          >
            <HelpCircle className="h-4 w-4" /> Replay tour
          </Button>
          <Button
            variant="outline"
            size="sm"
            className="justify-start gap-2"
            onClick={logout}
          >
            <LogOut className="h-4 w-4" /> Log out
          </Button>
        </div>
      </aside>

      <div className="flex-1 flex flex-col">
        <header className="h-14 border-b border-border flex items-center justify-between px-6">
          <Button
            id="tour-command-palette"
            variant="outline"
            size="sm"
            className="gap-2 text-muted-foreground"
            onClick={() =>
              document.dispatchEvent(
                new KeyboardEvent("keydown", { key: "k", ctrlKey: true }),
              )
            }
          >
            <Command className="h-3.5 w-3.5" /> Search...{" "}
            <kbd className="text-xs">Ctrl K</kbd>
          </Button>
          <div className="flex items-center gap-2">
            <NotificationBell />
            <ThemeToggle />
          </div>
        </header>
                <main className="flex-1 p-6">
          <PageTransition>
            <Outlet />
          </PageTransition>
        </main>
      </div>
    </div>
  );
}
