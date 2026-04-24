package org.tavall.couriers.api.web.endpoints;

import java.util.Map;

public final class Routes {

    private Routes() {
    }

    public static final class Api {
        public static final String ROOT = "/api";
        public static final String ANY = ROOT + "/**";

        private Api() {
        }

        public static final class Auth {
            public static final String ROOT = Api.ROOT + "/auth";
            public static final String MAGIC_ROOT = ROOT + "/magic";
            public static final String MAGIC_REQUEST = MAGIC_ROOT + "/request";
            public static final String MAGIC_VERIFY = MAGIC_ROOT + "/verify";
            public static final String ANY = ROOT + "/**";

            private Auth() {
            }
        }

        public static final class Intake {
            public static final String ROOT = Api.ROOT + "/intake";
            public static final String STATE = ROOT + "/state/{sessionKey}";
            public static final String STATE_BASE = ROOT + "/state";
            public static final String STATE_ANY = STATE_BASE + "/**";
            public static final String CART_ITEMS = ROOT + "/cart/{sessionKey}/items";
            public static final String CART_ITEM = ROOT + "/cart/{sessionKey}/items/{index}";
            public static final String CART_BASE = ROOT + "/cart";
            public static final String CART_ANY = CART_BASE + "/**";
            public static final String ADDONS = ROOT + "/cart/{sessionKey}/addons";
            public static final String EVALUATE = ROOT + "/evaluate";
            public static final String SPEC = ROOT + "/spec";
            public static final String CHECKOUT = ROOT + "/checkout";

            private Intake() {
            }
        }

        public static final class Marketplace {
            public static final String ROOT = Api.ROOT;
            public static final String HIRE_DIRECT_TALENT = ROOT + "/hire-direct/talent";
            public static final String HIRE_DIRECT_BASE = ROOT + "/hire-direct";
            public static final String HIRE_DIRECT_ANY = HIRE_DIRECT_BASE + "/**";
            public static final String PORTFOLIOS = ROOT + "/portfolios";
            public static final String PORTFOLIOS_ANY = PORTFOLIOS + "/**";
            public static final String FREELANCER_JOBS = ROOT + "/freelancer/jobs";
            public static final String CLIENT_DASHBOARD = ROOT + "/dashboard/client";
            public static final String FREELANCER_DASHBOARD = ROOT + "/dashboard/freelancer";
            public static final String ADMIN_STATS = ROOT + "/admin/stats";
            public static final String ADMIN_BASE = ROOT + "/admin";
            public static final String ADMIN_ANY = ADMIN_BASE + "/**";

            private Marketplace() {
            }
        }

        public static final class Freelancer {
            public static final String FREELANCERS_ROOT = Api.ROOT + "/freelancers";
            public static final String FREELANCERS_ANY = FREELANCERS_ROOT + "/**";
            public static final String PORTFOLIO = FREELANCERS_ROOT + "/{userId}/portfolio";
            public static final String WORKSPACE_ROOT = Api.ROOT + "/freelancer/workspace";
            public static final String WORKSPACE = WORKSPACE_ROOT;
            public static final String WORKSPACE_ACTIVATE = WORKSPACE_ROOT + "/activate";

            private Freelancer() {
            }
        }
    }

    public static final class Page {
        public static final String HOME = "/";
        public static final String HIRE_DIRECT = "/hire-direct";
        public static final String PORTFOLIOS = "/portfolios";
        public static final String FREELANCER = "/freelancer";
        public static final String CLIENT_DASHBOARD = "/client/dashboard";
        public static final String FREELANCER_DASHBOARD = "/freelancer/dashboard";
        public static final String FREELANCER_PORTFOLIO = "/freelancers/{userId}/portfolio";
        public static final String FREELANCER_PORTFOLIO_PATTERN = "/freelancers/*/portfolio";
        public static final String AUTH_ROOT = "/auth";
        public static final String AUTH_ANY = AUTH_ROOT + "/**";

        private Page() {
        }
    }

    public static final class Endpoints {
        private Endpoints() {
        }

        public static final class Auth {
            public static final AuthEndpoint MAGIC_REQUEST = AuthEndpoint.MAGIC_REQUEST;
            public static final AuthEndpoint MAGIC_VERIFY = AuthEndpoint.MAGIC_VERIFY;

            private Auth() {
            }
        }

        public static final class Intake {
            public static final IntakeEndpoint STATE = IntakeEndpoint.STATE;
            public static final IntakeEndpoint CART_ITEMS = IntakeEndpoint.CART_ITEMS;
            public static final IntakeEndpoint CART_ITEM = IntakeEndpoint.CART_ITEM;
            public static final IntakeEndpoint ADDONS = IntakeEndpoint.ADDONS;
            public static final IntakeEndpoint EVALUATE = IntakeEndpoint.EVALUATE;
            public static final IntakeEndpoint SPEC = IntakeEndpoint.SPEC;
            public static final IntakeEndpoint CHECKOUT = IntakeEndpoint.CHECKOUT;

            private Intake() {
            }
        }

        public static final class Marketplace {
            public static final MarketplaceEndpoint HIRE_DIRECT_TALENT = MarketplaceEndpoint.HIRE_DIRECT_TALENT;
            public static final MarketplaceEndpoint PORTFOLIOS = MarketplaceEndpoint.PORTFOLIOS;
            public static final MarketplaceEndpoint FREELANCER_JOBS = MarketplaceEndpoint.FREELANCER_JOBS;
            public static final MarketplaceEndpoint CLIENT_DASHBOARD = MarketplaceEndpoint.CLIENT_DASHBOARD;
            public static final MarketplaceEndpoint FREELANCER_DASHBOARD = MarketplaceEndpoint.FREELANCER_DASHBOARD;
            public static final MarketplaceEndpoint ADMIN_STATS = MarketplaceEndpoint.ADMIN_STATS;

            private Marketplace() {
            }
        }

        public static final class Freelancer {
            public static final FreelancerEndpoint PORTFOLIO = FreelancerEndpoint.PORTFOLIO;
            public static final FreelancerEndpoint WORKSPACE = FreelancerEndpoint.WORKSPACE;
            public static final FreelancerEndpoint WORKSPACE_ACTIVATE = FreelancerEndpoint.WORKSPACE_ACTIVATE;

            private Freelancer() {
            }
        }

        public static final class Page {
            public static final PageEndpoint HOME = PageEndpoint.HOME;
            public static final PageEndpoint HIRE_DIRECT = PageEndpoint.HIRE_DIRECT;
            public static final PageEndpoint PORTFOLIOS = PageEndpoint.PORTFOLIOS;
            public static final PageEndpoint FREELANCER = PageEndpoint.FREELANCER;
            public static final PageEndpoint CLIENT_DASHBOARD = PageEndpoint.CLIENT_DASHBOARD;
            public static final PageEndpoint FREELANCER_DASHBOARD = PageEndpoint.FREELANCER_DASHBOARD;
            public static final PageEndpoint FREELANCER_PORTFOLIO = PageEndpoint.FREELANCER_PORTFOLIO;

            private Page() {
            }
        }
    }

    public static String fill(String template, String key, Object value) {
        if (template == null) {
            return null;
        }
        return template.replace("{" + key + "}", String.valueOf(value));
    }

    public static String fill(String template, Map<String, ?> params) {
        if (template == null || params == null || params.isEmpty()) {
            return template;
        }
        String resolved = template;
        for (Map.Entry<String, ?> entry : params.entrySet()) {
            resolved = resolved.replace("{" + entry.getKey() + "}", String.valueOf(entry.getValue()));
        }
        return resolved;
    }
}