import { useEffect, useState } from "react"
import { useNavigate } from "react-router-dom"
import { LayoutDashboard, PlusCircle, BarChart3, KeyRound, Sun, Moon, LogOut } from "lucide-react"
import {
  CommandDialog,
  CommandEmpty,
  CommandGroup,
  CommandInput,
  CommandItem,
  CommandList,
} from "@/components/ui/command"
import { useTheme } from "./ThemeProvider"
import { useAuth } from "@/context/AuthContext"

export function CommandPalette() {
  const [open, setOpen] = useState(false)
  const navigate = useNavigate()
  const { theme, setTheme } = useTheme()
  const { logout } = useAuth()

  useEffect(() => {
    function handleKeyDown(e) {
      if ((e.metaKey || e.ctrlKey) && e.key === "k") {
        e.preventDefault()
        setOpen((prev) => !prev)
      }
    }
    document.addEventListener("keydown", handleKeyDown)
    return () => document.removeEventListener("keydown", handleKeyDown)
  }, [])

  function run(action) {
    setOpen(false)
    action()
  }

  return (
    <CommandDialog open={open} onOpenChange={setOpen}>
      <CommandInput placeholder="Type a command or search..." />
      <CommandList>
        <CommandEmpty>No results found.</CommandEmpty>
        <CommandGroup heading="Navigate">
          <CommandItem onSelect={() => run(() => navigate("/dashboard"))}>
            <LayoutDashboard className="mr-2 h-4 w-4" /> Dashboard
          </CommandItem>
          <CommandItem onSelect={() => run(() => navigate("/jobs/new"))}>
            <PlusCircle className="mr-2 h-4 w-4" /> Submit new job
          </CommandItem>
          <CommandItem onSelect={() => run(() => navigate("/stats"))}>
            <BarChart3 className="mr-2 h-4 w-4" /> Stats
          </CommandItem>
          <CommandItem onSelect={() => run(() => navigate("/api-keys"))}>
            <KeyRound className="mr-2 h-4 w-4" /> API Keys
          </CommandItem>
        </CommandGroup>
        <CommandGroup heading="Actions">
          <CommandItem onSelect={() => run(() => setTheme(theme === "dark" ? "light" : "dark"))}>
            {theme === "dark" ? <Sun className="mr-2 h-4 w-4" /> : <Moon className="mr-2 h-4 w-4" />}
            Toggle theme
          </CommandItem>
          <CommandItem onSelect={() => run(logout)}>
            <LogOut className="mr-2 h-4 w-4" /> Log out
          </CommandItem>
        </CommandGroup>
      </CommandList>
    </CommandDialog>
  )
}