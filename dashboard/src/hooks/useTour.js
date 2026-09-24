import { driver } from "driver.js"
import "driver.js/dist/driver.css"

export function startTour() {
  const driverObj = driver({
    showProgress: true,
    steps: [
      {
        element: "#tour-job-list",
        popover: {
          title: "Your Jobs",
          description: "Every job you submit shows up here, live — status updates automatically while a job is running.",
        },
      },
      {
        element: "#tour-submit-job",
        popover: {
          title: "Submit a new job",
          description: "Send an email, resize an image, or call any webhook — click here to submit one.",
        },
      },
      {
        element: "#tour-stats",
        popover: {
          title: "Stats",
          description: "See your success rate, average processing time, and a breakdown of all your jobs.",
        },
      },
      {
        element: "#tour-api-keys",
        popover: {
          title: "API Keys",
          description: "Generate a key here to submit jobs from scripts or CI, without logging in every time.",
        },
      },
      {
        element: "#tour-command-palette",
        popover: {
          title: "Quick search",
          description: "Press Ctrl+K anytime to jump to any page or run an action instantly.",
        },
      },
    ],
  })
  driverObj.drive()
}