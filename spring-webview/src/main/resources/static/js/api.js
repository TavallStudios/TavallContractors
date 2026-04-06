import { ApiRoutes } from "./routes";
async function fetchJson(url, init) {
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
    return response.json();
}
export const IntakeApi = {
    getState: (sessionKey) => fetchJson(ApiRoutes.intake.state(sessionKey)),
    putState: (sessionKey, state) => fetchJson(ApiRoutes.intake.state(sessionKey), {
        method: "PUT",
        body: JSON.stringify(state)
    }),
    addToCart: (sessionKey, state) => fetchJson(ApiRoutes.intake.cartItems(sessionKey), {
        method: "POST",
        body: JSON.stringify({ scope: state })
    }),
    removeCartItem: (sessionKey, index) => fetchJson(ApiRoutes.intake.cartItem(sessionKey, index), {
        method: "DELETE"
    }),
    setAddons: (sessionKey, addons) => fetchJson(ApiRoutes.intake.addons(sessionKey), {
        method: "PUT",
        body: JSON.stringify({ addons })
    }),
    evaluate: (state) => fetchJson(ApiRoutes.intake.evaluate, {
        method: "POST",
        body: JSON.stringify({ state })
    }),
    spec: (state) => fetchJson(ApiRoutes.intake.spec, {
        method: "POST",
        body: JSON.stringify({ state })
    }),
    checkout: (sessionKey) => fetchJson(ApiRoutes.intake.checkout, {
        method: "POST",
        body: JSON.stringify({ sessionKey })
    })
};
export const MarketplaceApi = {
    hireDirect: () => fetchJson(ApiRoutes.marketplace.hireDirect),
    portfolios: () => fetchJson(ApiRoutes.marketplace.portfolios),
    freelancerPortfolio: (userId) => fetchJson(ApiRoutes.freelancer.portfolio(userId)),
    fundedJobs: () => fetchJson(ApiRoutes.marketplace.freelancerJobs),
    clientDashboard: () => fetchJson(ApiRoutes.marketplace.clientDashboard),
    freelancerDashboard: () => fetchJson(ApiRoutes.marketplace.freelancerDashboard),
    freelancerWorkspace: () => fetchJson(ApiRoutes.freelancer.workspace),
    activateFreelancerWorkspace: () => fetchJson(ApiRoutes.freelancer.workspaceActivate, { method: "POST" }),
    saveFreelancerWorkspace: (payload) => fetchJson(ApiRoutes.freelancer.workspace, {
        method: "PUT",
        body: JSON.stringify(payload)
    }),
    adminStats: () => fetchJson(ApiRoutes.marketplace.adminStats)
};
//# sourceMappingURL=api.js.map