export type RouteMap = {
  api: {
    auth: {
      magicRequest: string;
      magicVerify: string;
    };
    intake: {
      state: string;
      cartItems: string;
      cartItem: string;
      addons: string;
      evaluate: string;
      spec: string;
      checkout: string;
    };
    marketplace: {
      hireDirect: string;
      portfolios: string;
      freelancerJobs: string;
      clientDashboard: string;
      freelancerDashboard: string;
      adminStats: string;
    };
    freelancer: {
      portfolio: string;
      workspace: string;
      workspaceActivate: string;
    };
  };
  pages: {
    home: string;
    hireDirect: string;
    portfolios: string;
    freelancer: string;
    clientDashboard: string;
    freelancerDashboard: string;
    freelancerPortfolio: string;
  };
};

declare global {
  interface Window {
    TavallRoutes?: RouteMap;
  }
}

function requireRoutes(): RouteMap {
  if (!window.TavallRoutes) {
    throw new Error("Routes are not initialized. Ensure constants/routes is rendered.");
  }
  return window.TavallRoutes;
}

function sanitizePath(path: string): string {
  return path.startsWith("#") ? path.slice(1) : path;
}

function resolvePath(template: string, params: Record<string, string | number>): string {
  let resolved = sanitizePath(template);
  Object.entries(params).forEach(([key, value]) => {
    resolved = resolved.replace(`{${key}}`, encodeURIComponent(String(value)));
  });
  return resolved;
}

const routes = requireRoutes();

export const ApiRoutes = {
  auth: {
    magicRequest: sanitizePath(routes.api.auth.magicRequest),
    magicVerify: sanitizePath(routes.api.auth.magicVerify)
  },
  intake: {
    state: (sessionKey: string) => resolvePath(routes.api.intake.state, { sessionKey }),
    cartItems: (sessionKey: string) => resolvePath(routes.api.intake.cartItems, { sessionKey }),
    cartItem: (sessionKey: string, index: number) => resolvePath(routes.api.intake.cartItem, { sessionKey, index }),
    addons: (sessionKey: string) => resolvePath(routes.api.intake.addons, { sessionKey }),
    evaluate: sanitizePath(routes.api.intake.evaluate),
    spec: sanitizePath(routes.api.intake.spec),
    checkout: sanitizePath(routes.api.intake.checkout)
  },
  marketplace: {
    hireDirect: sanitizePath(routes.api.marketplace.hireDirect),
    portfolios: sanitizePath(routes.api.marketplace.portfolios),
    freelancerJobs: sanitizePath(routes.api.marketplace.freelancerJobs),
    clientDashboard: sanitizePath(routes.api.marketplace.clientDashboard),
    freelancerDashboard: sanitizePath(routes.api.marketplace.freelancerDashboard),
    adminStats: sanitizePath(routes.api.marketplace.adminStats)
  },
  freelancer: {
    portfolio: (userId: number) => resolvePath(routes.api.freelancer.portfolio, { userId }),
    workspace: sanitizePath(routes.api.freelancer.workspace),
    workspaceActivate: sanitizePath(routes.api.freelancer.workspaceActivate)
  }
};

export const PageRoutes = {
  home: sanitizePath(routes.pages.home),
  hireDirect: sanitizePath(routes.pages.hireDirect),
  portfolios: sanitizePath(routes.pages.portfolios),
  freelancer: sanitizePath(routes.pages.freelancer),
  clientDashboard: sanitizePath(routes.pages.clientDashboard),
  freelancerDashboard: sanitizePath(routes.pages.freelancerDashboard),
  freelancerPortfolio: (userId: number) => resolvePath(routes.pages.freelancerPortfolio, { userId })
};
