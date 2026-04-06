import { wireIntake } from "./intake";
import { hydratePage } from "./pages";
import { PageRoutes } from "./routes";
function wireScrollReveal() {
    const reveal = () => {
        const elements = document.querySelectorAll(".scroll-reveal");
        const threshold = window.innerHeight * 0.9;
        elements.forEach(el => {
            const rect = el.getBoundingClientRect();
            if (rect.top < threshold) {
                el.classList.add("visible");
            }
        });
    };
    reveal();
    window.addEventListener("scroll", reveal, { passive: true });
}
function wireActiveNav(page) {
    const pageToHref = {
        home: PageRoutes.home,
        "hire-direct": PageRoutes.hireDirect,
        portfolios: PageRoutes.portfolios,
        freelancer: PageRoutes.freelancer,
        "client-dashboard": PageRoutes.clientDashboard,
        "freelancer-dashboard": PageRoutes.freelancerDashboard
    };
    const activeHref = pageToHref[page] ?? "";
    const navLinks = document.querySelectorAll(".nav-links .nav-item");
    navLinks.forEach(link => {
        const href = link.getAttribute("href") ?? "";
        if (href === activeHref) {
            link.classList.add("active");
        }
    });
}
async function boot() {
    const page = document.body.dataset.page ?? "home";
    wireActiveNav(page);
    wireScrollReveal();
    await Promise.all([
        wireIntake(),
        hydratePage(page)
    ]);
}
void boot();
//# sourceMappingURL=main.js.map