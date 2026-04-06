import { IntakeApi } from "./api";
import { debounce } from "./debounce";
const SESSION_STORAGE_KEY = "tavall:intake:session";
const ADDON_CATALOG = [
    {
        code: "priority_onboarding",
        label: "Priority Onboarding",
        description: "Kickoff and team assignment within 24 hours.",
        price: 299
    },
    {
        code: "qa_automation",
        label: "QA Automation Pack",
        description: "Automated regression suite setup for your scope.",
        price: 599
    },
    {
        code: "analytics_setup",
        label: "Analytics Setup",
        description: "Dashboard instrumentation and reporting baseline.",
        price: 349
    }
];
function initialIntake() {
    return {
        domain: "",
        domainType: "",
        domainSubtype: "",
        language: "",
        techLevel: "",
        budget: "",
        customBudget: "",
        deadline: "",
        docsType: [],
        guidedAnswers: {},
        contractType: ""
    };
}
function getSessionKey() {
    const existing = window.sessionStorage.getItem(SESSION_STORAGE_KEY);
    if (existing)
        return existing;
    const created = crypto.randomUUID();
    window.sessionStorage.setItem(SESSION_STORAGE_KEY, created);
    return created;
}
function text(target, value) {
    if (target)
        target.textContent = value;
}
function estimateBudget(value) {
    const budget = value.budget ?? "";
    if (budget === "1000-3000")
        return 2000;
    if (budget === "3000-10000")
        return 6500;
    if (budget === "10000+")
        return 15000;
    if (budget === "custom")
        return Number((value.customBudget ?? "").replace(/[^0-9.]/g, "")) || 0;
    if (budget === "no_budget" || budget === "no_budget_quote")
        return 0;
    return Number((budget ?? "").replace(/[^0-9.]/g, "")) || 0;
}
function currency(value) {
    return new Intl.NumberFormat("en-US", {
        style: "currency",
        currency: "USD"
    }).format(value || 0);
}
export async function wireIntake() {
    const form = document.getElementById("intake-form");
    if (!form)
        return;
    const formEl = form;
    const overlay = document.getElementById("intake-modal-root");
    const openButtons = Array.from(document.querySelectorAll("[data-intake-open]"));
    const closeButtons = Array.from(document.querySelectorAll("[data-intake-close]"));
    const sessionKey = getSessionKey();
    const completenessEl = document.getElementById("completeness-badge");
    const riskEl = document.getElementById("risk-badge");
    const specEl = document.getElementById("generated-spec");
    const generateSpecButton = document.getElementById("generate-spec");
    const saveToCartButton = document.getElementById("save-to-cart");
    const checkoutButton = document.getElementById("checkout");
    const cartItemsEl = document.getElementById("cart-items");
    const cartCountEl = document.getElementById("cart-count");
    const cartIndicatorCountEl = document.getElementById("cart-indicator-count");
    const cartProjectsTotalEl = document.getElementById("cart-project-total");
    const cartAddonsTotalEl = document.getElementById("cart-addon-total");
    const cartGrandTotalEl = document.getElementById("cart-grand-total");
    const cartStatusEl = document.getElementById("cart-status");
    const addonCheckboxes = Array.from(document.querySelectorAll("[data-addon-code]"));
    let sessionState;
    try {
        sessionState = await IntakeApi.getState(sessionKey);
    }
    catch {
        sessionState = {
            sessionKey,
            currentIntake: initialIntake(),
            cart: [],
            addons: []
        };
        await IntakeApi.putState(sessionKey, sessionState);
    }
    if (!sessionState.currentIntake) {
        sessionState.currentIntake = initialIntake();
    }
    if (!sessionState.cart) {
        sessionState.cart = [];
    }
    if (!sessionState.addons) {
        sessionState.addons = [];
    }
    const pushStateDebounced = debounce(async () => {
        await IntakeApi.putState(sessionKey, sessionState);
    }, 300);
    function setOverlayOpen(open) {
        if (!overlay)
            return;
        overlay.classList.toggle("open", open);
        document.body.style.overflow = open ? "hidden" : "";
    }
    openButtons.forEach(button => {
        button.addEventListener("click", () => setOverlayOpen(true));
    });
    closeButtons.forEach(button => {
        button.addEventListener("click", () => setOverlayOpen(false));
    });
    overlay?.addEventListener("click", event => {
        const target = event.target;
        if (target && target.id === "intake-modal-root") {
            setOverlayOpen(false);
        }
    });
    function hydrateFormFromState() {
        const state = sessionState.currentIntake;
        const entries = {
            domain: state.domain ?? "",
            domainType: state.domainType ?? "",
            techLevel: state.techLevel ?? "",
            budget: state.budget ?? "",
            customBudget: state.customBudget ?? "",
            deadline: state.deadline ?? "",
            contractType: state.contractType ?? ""
        };
        Object.entries(entries).forEach(([name, value]) => {
            const field = formEl.elements.namedItem(name);
            if (field instanceof HTMLInputElement) {
                field.value = value;
            }
        });
        const docsType = new Set(state.docsType ?? []);
        formEl.querySelectorAll('input[name="docsType"]').forEach(input => {
            input.checked = docsType.has(input.value);
        });
    }
    function setStatus(message) {
        text(cartStatusEl, message);
    }
    function selectedAddonsFromUI() {
        const selectedCodes = new Set(addonCheckboxes.filter(box => box.checked).map(box => box.dataset.addonCode ?? ""));
        return ADDON_CATALOG.filter(item => selectedCodes.has(item.code));
    }
    function renderCart() {
        const cart = sessionState.cart ?? [];
        const addons = sessionState.addons ?? [];
        const projectTotal = cart.reduce((sum, item) => sum + estimateBudget(item), 0);
        const addonTotal = addons.reduce((sum, item) => sum + (item.price ?? 0), 0);
        const grandTotal = projectTotal + addonTotal;
        text(cartCountEl, `${cart.length}`);
        text(cartIndicatorCountEl, `${cart.length}`);
        text(cartProjectsTotalEl, currency(projectTotal));
        text(cartAddonsTotalEl, currency(addonTotal));
        text(cartGrandTotalEl, currency(grandTotal));
        addonCheckboxes.forEach(box => {
            const code = box.dataset.addonCode ?? "";
            box.checked = addons.some(item => item.code === code);
        });
        if (!cartItemsEl)
            return;
        if (cart.length === 0) {
            cartItemsEl.innerHTML = `<p class="cart-empty">No scoped projects yet. Save your current triage to start your cart.</p>`;
            return;
        }
        cartItemsEl.innerHTML = cart.map((item, index) => `
      <article class="cart-line-item">
        <div>
          <h4>${item.domainType || "Scoped Project"}</h4>
          <p>${item.domain || "General"} | ${item.techLevel || "Unspecified tech level"}</p>
          <small>${item.deadline || "No deadline selected"}</small>
        </div>
        <div class="cart-line-price">${currency(estimateBudget(item))}</div>
        <button type="button" data-remove-index="${index}" class="cart-remove-btn">Remove</button>
      </article>
    `).join("");
    }
    function readCurrentIntake() {
        const data = new FormData(formEl);
        const docsType = data.getAll("docsType").map(String);
        return {
            ...sessionState.currentIntake,
            domain: String(data.get("domain") ?? ""),
            domainType: String(data.get("domainType") ?? ""),
            techLevel: String(data.get("techLevel") ?? ""),
            budget: String(data.get("budget") ?? ""),
            customBudget: String(data.get("customBudget") ?? ""),
            deadline: String(data.get("deadline") ?? ""),
            contractType: String(data.get("contractType") ?? ""),
            docsType
        };
    }
    async function recomputeFromServer() {
        const evaluated = await IntakeApi.evaluate(sessionState.currentIntake);
        sessionState.currentIntake = evaluated.state;
        text(completenessEl, `${evaluated.completeness}% complete`);
        text(riskEl, `${evaluated.risk} risk`);
        if (riskEl) {
            riskEl.classList.remove("badge-risk", "badge-low-risk", "badge-warn");
            if (evaluated.risk === "Low") {
                riskEl.classList.add("badge-low-risk");
            }
            else if (evaluated.risk === "Med") {
                riskEl.classList.add("badge-warn");
            }
            else {
                riskEl.classList.add("badge-risk");
            }
        }
        pushStateDebounced();
        renderCart();
    }
    form.addEventListener("change", async () => {
        sessionState.currentIntake = readCurrentIntake();
        await recomputeFromServer();
    });
    generateSpecButton?.addEventListener("click", async () => {
        const spec = await IntakeApi.spec(sessionState.currentIntake);
        if (specEl) {
            specEl.textContent = spec.markdown;
        }
    });
    saveToCartButton?.addEventListener("click", async () => {
        setStatus("Saving scope to cart...");
        sessionState.currentIntake = readCurrentIntake();
        await recomputeFromServer();
        sessionState = await IntakeApi.addToCart(sessionKey, sessionState.currentIntake);
        setStatus("Scope added to cart.");
        renderCart();
    });
    cartItemsEl?.addEventListener("click", async (event) => {
        const target = event.target;
        const removeButton = target?.closest("[data-remove-index]");
        if (!removeButton)
            return;
        const index = Number(removeButton.dataset.removeIndex ?? "-1");
        if (!Number.isInteger(index) || index < 0)
            return;
        setStatus("Removing scope...");
        sessionState = await IntakeApi.removeCartItem(sessionKey, index);
        setStatus("Scope removed.");
        renderCart();
    });
    addonCheckboxes.forEach(box => {
        box.addEventListener("change", async () => {
            sessionState.addons = selectedAddonsFromUI();
            sessionState = await IntakeApi.setAddons(sessionKey, sessionState.addons);
            setStatus("Cart add-ons updated.");
            renderCart();
        });
    });
    checkoutButton?.addEventListener("click", async () => {
        setStatus("Submitting checkout...");
        const checkout = await IntakeApi.checkout(sessionKey);
        setStatus(`Checkout ${checkout.status} saved (ID ${checkout.checkoutId}).`);
        window.alert(`Checkout ${checkout.status} - ID ${checkout.checkoutId} - Total ${currency(checkout.grandTotal)}`);
    });
    hydrateFormFromState();
    renderCart();
    await recomputeFromServer();
}
//# sourceMappingURL=intake.js.map