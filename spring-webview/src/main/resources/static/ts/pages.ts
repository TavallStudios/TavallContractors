import { MarketplaceApi } from "./api";
import { PageRoutes } from "./routes";
import { FreelancerWorkspaceUpdate, PortfolioContent, PortfolioProject } from "./types";

function byId(id: string): HTMLElement | null {
  return document.getElementById(id);
}

function escapeHtml(value: string | number | null | undefined): string {
  return String(value ?? "")
    .replace(/&/g, "&amp;")
    .replace(/</g, "&lt;")
    .replace(/>/g, "&gt;")
    .replace(/"/g, "&quot;")
    .replace(/'/g, "&#39;");
}

function normalizePortfolio(content: PortfolioContent): PortfolioContent {
  return {
    tagline: content.tagline ?? "",
    summary: content.summary ?? "",
    location: content.location ?? "",
    availability: content.availability ?? "",
    skills: content.skills ?? [],
    projects: content.projects ?? [],
    links: content.links ?? []
  };
}

function parseSkills(raw: string): string[] {
  return raw
    .split(",")
    .map(item => item.trim())
    .filter(item => item.length > 0);
}

function projectFields(projects: PortfolioProject[]): string {
  const rows = [0, 1, 2].map(index => projects[index] ?? {
    title: "",
    summary: "",
    tech: "",
    imageUrl: ""
  });

  return rows.map((project, index) => `
    <div class="card">
      <h3>Project ${index + 1}</h3>
      <label class="form-field" for="project-title-${index}">
        <span>Title</span>
        <input id="project-title-${index}" class="form-input" value="${escapeHtml(project.title)}" />
      </label>
      <label class="form-field" for="project-tech-${index}">
        <span>Tech</span>
        <input id="project-tech-${index}" class="form-input" value="${escapeHtml(project.tech)}" />
      </label>
      <label class="form-field" for="project-image-${index}">
        <span>Image URL</span>
        <input id="project-image-${index}" class="form-input" value="${escapeHtml(project.imageUrl)}" />
      </label>
      <label class="form-field" for="project-summary-${index}">
        <span>Summary</span>
        <textarea id="project-summary-${index}" class="form-input form-textarea">${escapeHtml(project.summary)}</textarea>
      </label>
    </div>
  `).join("");
}

function renderPortfolioPage(userId: number, container: HTMLElement): Promise<void> {
  return MarketplaceApi.freelancerPortfolio(userId).then(view => {
    const portfolio = normalizePortfolio(view.portfolio);
    const skills = portfolio.skills.map(skill =>
      `<span class="badge badge-blue">${escapeHtml(skill)}</span>`
    ).join("");
    const projects = portfolio.projects.map(project => {
      const imageUrl = escapeHtml(project.imageUrl);
      const background = imageUrl
        ? `style="background-image:url('${imageUrl}');"`
        : `style="background: linear-gradient(135deg, rgba(41,121,255,0.25), rgba(0,229,255,0.1));"`;
      return `
        <article class="sexy-card">
          <div class="sexy-card-bg" ${background}></div>
          <div class="card-content">
            <h3>${escapeHtml(project.title)}</h3>
            <p>${escapeHtml(project.summary)}</p>
            <div class="badge badge-blue">${escapeHtml(project.tech)}</div>
          </div>
        </article>
      `;
    }).join("");
    const links = portfolio.links.map(link =>
      `<a class="btn btn-ghost btn-sm" href="${escapeHtml(link.url)}" target="_blank" rel="noopener noreferrer">${escapeHtml(link.label)}</a>`
    ).join("");

    container.innerHTML = `
      <section class="profile-header">
        <div class="container">
          <span class="badge badge-blue">Freelancer</span>
          <h1>${escapeHtml(view.displayName)}</h1>
          <p>${escapeHtml(portfolio.tagline)}</p>
          <div class="profile-stats">
            <span>${escapeHtml(view.headline)}</span>
            <span>$${escapeHtml(view.hourlyRate)}/hr</span>
            <span>${escapeHtml(portfolio.location)}</span>
            <span>${escapeHtml(portfolio.availability)}</span>
          </div>
        </div>
      </section>
      <section class="section">
        <div class="container grid-2">
          <article class="card">
            <h2>About This Freelancer</h2>
            <p>${escapeHtml(portfolio.summary)}</p>
            <h3>Skills</h3>
            <div class="tag-list">${skills}</div>
          </article>
          <article class="card">
            <h2>Links</h2>
            <div style="display:flex; gap:10px; flex-wrap:wrap;">${links}</div>
          </article>
        </div>
      </section>
      <section class="section">
        <div class="container">
          <h2>Selected Work</h2>
          <div class="grid-3">${projects}</div>
        </div>
      </section>
    `;
  });
}

async function renderFreelancerWorkspace(container: HTMLElement): Promise<void> {
  let workspace = await MarketplaceApi.freelancerWorkspace();

  const render = (): void => {
    const portfolio = normalizePortfolio(workspace.portfolio);
    const headline = workspace.talentProfile?.headline ?? "";
    const hourlyRate = workspace.talentProfile?.hourlyRate ?? 0;
    const skills = (workspace.talentProfile?.skills ?? []).join(", ");

    if (!workspace.activated) {
      container.innerHTML = `
        <section class="card">
          <h2>Activate Freelancer Account</h2>
          <p class="form-status">
            Your account is currently <strong>${escapeHtml(workspace.role)}</strong>.
            Activate freelancer mode to generate your public portfolio page and edit it here.
          </p>
          <div class="form-actions">
            <button class="btn btn-accent" id="activate-workspace-btn">Activate + Generate Page</button>
          </div>
        </section>
      `;
      const activateButton = byId("activate-workspace-btn");
      activateButton?.addEventListener("click", async () => {
        activateButton.setAttribute("disabled", "true");
        workspace = await MarketplaceApi.activateFreelancerWorkspace();
        render();
      });
      return;
    }

    container.innerHTML = `
      <section class="card">
        <h2>Freelancer Page Workspace</h2>
        <p class="form-status">Generated page: <a href="${escapeHtml(workspace.portfolioUrl)}" target="_blank" rel="noopener noreferrer">${escapeHtml(workspace.portfolioUrl)}</a></p>
        <div class="form-actions">
          <a class="btn btn-ghost btn-sm" href="${escapeHtml(workspace.portfolioUrl)}" target="_blank" rel="noopener noreferrer">Open My Public Portfolio</a>
        </div>
      </section>
      <section class="card form-shell">
        <h3>Portfolio Details</h3>
        <div class="form-grid">
          <label class="form-field" for="workspace-display-name">
            <span>Display Name</span>
            <input id="workspace-display-name" class="form-input" value="${escapeHtml(workspace.displayName)}" />
          </label>
          <label class="form-field" for="workspace-headline">
            <span>Headline</span>
            <input id="workspace-headline" class="form-input" value="${escapeHtml(headline)}" />
          </label>
          <label class="form-field" for="workspace-rate">
            <span>Hourly Rate (USD)</span>
            <input id="workspace-rate" class="form-input" type="number" min="0" step="1" value="${escapeHtml(hourlyRate)}" />
          </label>
          <label class="form-field" for="workspace-location">
            <span>Location</span>
            <input id="workspace-location" class="form-input" value="${escapeHtml(portfolio.location)}" />
          </label>
          <label class="form-field" for="workspace-availability">
            <span>Availability</span>
            <input id="workspace-availability" class="form-input" value="${escapeHtml(portfolio.availability)}" />
          </label>
          <label class="form-field" for="workspace-tagline">
            <span>Tagline</span>
            <input id="workspace-tagline" class="form-input" value="${escapeHtml(portfolio.tagline)}" />
          </label>
        </div>
        <label class="form-field" for="workspace-skills">
          <span>Skills (comma separated)</span>
          <input id="workspace-skills" class="form-input" value="${escapeHtml(skills)}" />
        </label>
        <label class="form-field" for="workspace-summary">
          <span>About Summary</span>
          <textarea id="workspace-summary" class="form-input form-textarea">${escapeHtml(portfolio.summary)}</textarea>
        </label>
        <div class="project-grid">
          ${projectFields(portfolio.projects)}
        </div>
        <div class="form-actions">
          <button class="btn btn-accent" id="workspace-save-btn">Save Portfolio</button>
          <span id="workspace-status" class="form-status"></span>
        </div>
      </section>
    `;

    const saveButton = byId("workspace-save-btn");
    const status = byId("workspace-status");
    saveButton?.addEventListener("click", async () => {
      saveButton.setAttribute("disabled", "true");
      if (status) {
        status.textContent = "Saving...";
      }

      const displayName = (byId("workspace-display-name") as HTMLInputElement | null)?.value ?? "";
      const updatedHeadline = (byId("workspace-headline") as HTMLInputElement | null)?.value ?? "";
      const rateValue = Number((byId("workspace-rate") as HTMLInputElement | null)?.value ?? 0);
      const location = (byId("workspace-location") as HTMLInputElement | null)?.value ?? "";
      const availability = (byId("workspace-availability") as HTMLInputElement | null)?.value ?? "";
      const tagline = (byId("workspace-tagline") as HTMLInputElement | null)?.value ?? "";
      const summary = (byId("workspace-summary") as HTMLTextAreaElement | null)?.value ?? "";
      const updatedSkills = parseSkills((byId("workspace-skills") as HTMLInputElement | null)?.value ?? "");

      const projects: PortfolioProject[] = [0, 1, 2].map(index => ({
        title: (byId(`project-title-${index}`) as HTMLInputElement | null)?.value ?? "",
        tech: (byId(`project-tech-${index}`) as HTMLInputElement | null)?.value ?? "",
        imageUrl: (byId(`project-image-${index}`) as HTMLInputElement | null)?.value ?? "",
        summary: (byId(`project-summary-${index}`) as HTMLTextAreaElement | null)?.value ?? ""
      })).filter(project => project.title.trim().length > 0);

      const payload: FreelancerWorkspaceUpdate = {
        displayName,
        headline: updatedHeadline,
        hourlyRate: Number.isFinite(rateValue) ? rateValue : 0,
        skills: updatedSkills,
        portfolio: {
          tagline,
          summary,
          location,
          availability,
          skills: updatedSkills,
          projects,
          links: workspace.portfolio.links
        }
      };

      workspace = await MarketplaceApi.saveFreelancerWorkspace(payload);
      if (status) {
        status.textContent = "Saved.";
      }
      saveButton.removeAttribute("disabled");
      render();
    });
  };

  render();
}

export async function hydratePage(page: string): Promise<void> {
  if (page === "hire-direct") {
    const container = byId("hire-direct-list");
    if (!container) return;
    const people = await MarketplaceApi.hireDirect();
    container.innerHTML = people.map(person =>
      `<article class="card scroll-reveal">
        <span class="badge badge-blue">Top Rated</span>
        <h3>${escapeHtml(person.name)}</h3>
        <p>${escapeHtml(person.headline ?? "")}</p>
        <p class="mono">$${escapeHtml(person.hourlyRate)}/hr</p>
        <p class="small">${escapeHtml(person.skills.join(", "))}</p>
        <a class="btn btn-ghost btn-sm" href="${PageRoutes.freelancerPortfolio(person.userId)}">View Portfolio</a>
      </article>`
    ).join("");
    return;
  }

  if (page === "portfolios") {
    const container = byId("portfolio-grid");
    if (!container) return;
    const rows = await MarketplaceApi.portfolios();
    container.innerHTML = rows.map(row => {
      const cover = escapeHtml(row.coverImage);
      const background = cover
        ? `background-image:url('${cover}');`
        : "background: linear-gradient(135deg, rgba(41,121,255,0.25), rgba(0,229,255,0.1));";
      return `
        <article class="sexy-card scroll-reveal">
          <div class="sexy-card-bg" style="${background}"></div>
          <div class="card-content">
            <h3>${escapeHtml(row.domain)}</h3>
            <p>${escapeHtml(row.totalProjects)} completed projects</p>
            <span class="badge badge-fund">Funded Work</span>
          </div>
        </article>
      `;
    }).join("");
    return;
  }

  if (page === "freelancer") {
    const container = byId("freelancer-jobs");
    if (!container) return;
    const jobs = await MarketplaceApi.fundedJobs();
    container.innerHTML = jobs.map(job =>
      `<article class="card scroll-reveal">
        <span class="badge badge-fund">Funded</span>
        <h3>${escapeHtml(job.title)}</h3>
        <p>${escapeHtml(job.domain)}</p>
        <p class="mono">Escrow: $${escapeHtml(job.escrowBalance)}</p>
      </article>`
    ).join("");
    return;
  }

  if (page === "client-dashboard") {
    const container = byId("client-dashboard-root");
    if (!container) return;
    const dashboard = await MarketplaceApi.clientDashboard();
    container.textContent = JSON.stringify(dashboard, null, 2);
    return;
  }

  if (page === "freelancer-dashboard") {
    const container = byId("freelancer-dashboard-root");
    if (!container) return;
    await renderFreelancerWorkspace(container);
    return;
  }

  if (page === "freelancer-portfolio") {
    const container = byId("freelancer-portfolio-root");
    if (!container) return;
    const userId = Number(document.body.dataset.freelancerUserId ?? "");
    if (!Number.isFinite(userId)) {
      container.innerHTML = `<article class="card"><h2>Invalid Freelancer</h2><p>Unable to locate this freelancer profile.</p></article>`;
      return;
    }
    await renderPortfolioPage(userId, container);
  }
}
