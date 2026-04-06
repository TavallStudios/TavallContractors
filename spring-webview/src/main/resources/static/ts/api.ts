import {
  CheckoutResponse,
  CartAddon,
  EvaluateResponse,
  FreelancerPortfolioView,
  FreelancerWorkspace,
  FreelancerWorkspaceUpdate,
  IntakeState,
  JobBoardItem,
  PortfolioCard,
  SpecResponse,
  TalentCard,
  WizardSessionState
} from "./types";
import { ApiRoutes } from "./routes";

async function fetchJson<T>(url: string, init?: RequestInit): Promise<T> {
  const response = await fetch(url, {
    credentials: "include",
    headers: {
      "Content-Type": "application/json",
      ...(init?.headers ?? {})
    },
    ...init
  });

  if (!response.ok) {
    const message = await response.text();
    throw new Error(`${response.status} ${response.statusText}: ${message}`);
  }
  return response.json() as Promise<T>;
}

export const IntakeApi = {
  getState: (sessionKey: string) =>
    fetchJson<WizardSessionState>(ApiRoutes.intake.state(sessionKey)),

  putState: (sessionKey: string, state: WizardSessionState) =>
    fetchJson<WizardSessionState>(ApiRoutes.intake.state(sessionKey), {
      method: "PUT",
      body: JSON.stringify(state)
    }),

  addToCart: (sessionKey: string, state: IntakeState) =>
    fetchJson<WizardSessionState>(ApiRoutes.intake.cartItems(sessionKey), {
      method: "POST",
      body: JSON.stringify({ scope: state })
    }),

  removeCartItem: (sessionKey: string, index: number) =>
    fetchJson<WizardSessionState>(ApiRoutes.intake.cartItem(sessionKey, index), {
      method: "DELETE"
    }),

  setAddons: (sessionKey: string, addons: CartAddon[]) =>
    fetchJson<WizardSessionState>(ApiRoutes.intake.addons(sessionKey), {
      method: "PUT",
      body: JSON.stringify({ addons })
    }),

  evaluate: (state: IntakeState) =>
    fetchJson<EvaluateResponse>(ApiRoutes.intake.evaluate, {
      method: "POST",
      body: JSON.stringify({ state })
    }),

  spec: (state: IntakeState) =>
    fetchJson<SpecResponse>(ApiRoutes.intake.spec, {
      method: "POST",
      body: JSON.stringify({ state })
    }),

  checkout: (sessionKey: string) =>
    fetchJson<CheckoutResponse>(ApiRoutes.intake.checkout, {
      method: "POST",
      body: JSON.stringify({ sessionKey })
    })
};

export const MarketplaceApi = {
  hireDirect: () => fetchJson<TalentCard[]>(ApiRoutes.marketplace.hireDirect),
  portfolios: () => fetchJson<PortfolioCard[]>(ApiRoutes.marketplace.portfolios),
  freelancerPortfolio: (userId: number) => fetchJson<FreelancerPortfolioView>(ApiRoutes.freelancer.portfolio(userId)),
  fundedJobs: () => fetchJson<JobBoardItem[]>(ApiRoutes.marketplace.freelancerJobs),
  clientDashboard: () => fetchJson<unknown>(ApiRoutes.marketplace.clientDashboard),
  freelancerDashboard: () => fetchJson<unknown>(ApiRoutes.marketplace.freelancerDashboard),
  freelancerWorkspace: () => fetchJson<FreelancerWorkspace>(ApiRoutes.freelancer.workspace),
  activateFreelancerWorkspace: () =>
    fetchJson<FreelancerWorkspace>(ApiRoutes.freelancer.workspaceActivate, { method: "POST" }),
  saveFreelancerWorkspace: (payload: FreelancerWorkspaceUpdate) =>
    fetchJson<FreelancerWorkspace>(ApiRoutes.freelancer.workspace, {
      method: "PUT",
      body: JSON.stringify(payload)
    }),
  adminStats: () => fetchJson<unknown>(ApiRoutes.marketplace.adminStats)
};
