# Tavall Contractors — Bottom-Line Escrow + Milestone Flow (Automated)

# Rules 
## Scope Tightening
- Before a freelancer gets a scope, we must finalize and SLA it,
  so the client can't update it without updating the quote.

## SLAs
- Freelancers are held to an SLA for certain projects.
    - If broken, punishments can ensue.
    - _See "FREELANCER_PUNISHMENTS.MD"_
- Clients are entitled to discounts/compenstation for missed SLA deadlines.

## Freelancer Limits
- A limited number of freelancers are hired in each field to keep the flow of work constant.
    - _The goal is to have our freelancers to have work almost always available._

# Account Flows (Web + Discord)

Web and Discord accounts are linked and should update data dynamically. 

## Web
- Users should have a way to sign in with a variety of account providers. (Google, Discord, GitHub, Microsoft, etc)
  - We should only use providers and not store any user details to mitigate risk.
    - _Mention this to clients._ 

## Discord
- Users can fill out a questionnaire but must make an account before continuing.
  - We can use providers like we do on the Web, just tailored to discord's UI constraints. 
- If the user wishing to continue after the system creates an offer, 
 and if the system thinks the scope is ready for review. 
When the user needs to sign in

## Account Linking
- We will provide linking between Discord and our Web Services for dynamic updates and mutation.
  - Everything we can do on Web, we should be able to do on Discord.


---


# Talent Flows

## Talent Contracts

### Contract Types
By default, clients do not have direct contact access to the talent. Direct communication is only enabled via the Direct Contact Add-On.
- IC Contracts
  - Standard Contract
    - Fixed scope, milestone, and hourly by default.
    - Can also be weekly, bi-weekly, monthly, or project based.
    - Payout Structures supported options
      - Hourly
      - 50/50 split
      - 25/75 split
      - Milestone-based payouts
      - Project-based payout
  - Retainer Contract
    - Ongoing recurring access to talent, usually for support, iteration, or advisory work.
  - On-Call Add-On
    - Optional add-on that can attach to other IC contracts.
  - Direct Contact Add-On
    - Optional add-on that grants the client direct access to the talent for communication.

- Agency Contracts
  - Agency Temp Placement / Temp Assignment
    - Talent is placed through the agency for time-based work under client supervision.
    - See: PAYROLL_DETAILS.MD
  - Contract to Hire
    - Talent starts as an agency placement with an optional path to convert into a client employee.
  - Direct Hire Placement
    - Client hires talent as a permanent employee immediately; platform/agency earns a placement fee.

- Team Contracts
  - Dedicated Team Contract
    - Client hires a managed group of talent for ongoing delivery across a product or workstream.
    - Supported types mirror IC Contracts, but applied at the team level.
    - Team Payout Structures supported options
      - Hourly
      - 50/50 split
      - 25/75 split
      - Milestone-based payouts
      - Project-based payout
  - Team Retainer
    - Recurring team capacity for ongoing delivery, support, iteration, or advisory work.
  - Team On-Call Add-On
    - Optional add-on that can attach to other team contracts.
  - Team Direct Contact Add-On
    - Optional add-on that grants the client direct access to the team for communication.




## Talent Account

### Talent Account Creation

#### Talent Account Rules
Before a Talent can accept a job:
- GitHub must be linked
- Payout information must be linked
- Portfolio page should be created/setup
- Demos should be created/setup


---


## Talent Job Board
- Talents will never have to speak to a client about scope.
  - _Clients have an option to speak to the Talent **for a fee** during the work, not before._
- When a Talent accepts a job, they should be able to start right 
  away with full scope, deadline, milestones, and pay details.
    - _Talents shouldn't even see jobs until they're ready to start._

###

## Freelancer Account

### Freelancer Account Creation

#### Freelancer Account Rules
Before a Freelancer can accept a job:
- GitHub must be linked
- Payout information must be linked
- Portfolio page should be created/setup
- Demos should be created/setup


---


## Freelancer Job Board
- Freelancers will never have to speak to a client about scope.
  - _Clients have an option to speak to the Freelancer **for a fee** during the work, not before._
- When a Freelancer accepts a job, they should be able to start right 
  away with full scope, deadline, milestones, and pay details.
    - _Freelancers shouldn't even see jobs until they're ready to start._

### Freelancer Job Board Rules

_A Job on the marketplace should be ready to take and start, this means the following..._

- Scope is tight with no holes.
- Budget is in Tavall escrow for instant payouts.
- Deadline is set if applicable.
- Milestone rules/types, sometimes defined by the client.
- We don't take money from developer commission directly, we just charge the client 10% flat fee and sell them add-ons.
   - Developer should keep the full advertised amount on the job.


---

## Freelancer Portfolios
- Freelancer Portfolios will live on: portolifo.tavall.org/{freelancer}-{freelancer-id}
  - _{freelancer} = User defined_
  - _{freelancer-id} = ID of the Freelancer, defined on signup._

- We will host freelancer portfolios.
  - All portfolios must flow though portfolios.tavall.org 
- Freelancers will be able to edit their portfolios deeply. 
   - Custom HTML, CSS, JS, and custom placements should be allowed in the dashboard to edit the portfolio.
   - The dashboard itself will have robust edit options outside custom HTML, CSS, and JS.
   - TODO: Determine if freelancers can link an entire external site to their portfolio.
      - Leaning towards: No. Why? We want freelancers not to worry about hosting, keep their portfolio/demos up to date automatically.
- _Option for a secondary link in case internal hosting is down._


---


## Freelancer Demos

### Minecraft Demo Hosting
- We will provide hosting for Minecraft demos.
  - *Option for a secondary link in case internal hosting is down.*
- See 'DEMOS_PLUGIN.MD' for more information.

### Web Demo Hosting
- Freelancers will be able to host their demo with us or link their demo site.
  - Important: For security, freelancers will need to allo their site to be viewed with Tavall. 
- A real demo if the site is embedded in the demo popup when a user clicks for quick access.

All projects must be tracked on the Studio's GitHub for automated payouts, tracking, and client support.

## Freelancer Acceptance Flow
1. Freelancer accepts contract terms:
    - On Web/Discord, Freelancer can accept a job.
    - SEE EXPERT_CONTRACTS.MD
2. Work can start right away.
    - Depending on the domain. Follow the below milestone flows.


---

## Freelancer Half-Pay Milestone Flows

### Developer Half-Pay Milestone Flow
_For Web, or unit/intergarion testable applications._

- Dev submits commits in a TavallStudios GitHub's **private repo** (or private fork)
- Commit to this repo. It will act as a support page for more tech-inclined clients. Normal support will still exist.
- To submit a test, Devs can use a branch with "milestone" in the branch name.
- This branch can auto-test and confirm flows.
    - _This branch can also create tests if they don't exist_
    - demo artifact (preview URL OR CI-generated run output)
    - changelog summary

### Developer Half-Pay Milestone Flow (Minecraft)
_Creating tests and verifying they work for Minecraft automatically is a bit
more complex than unit/integration testing a website or backend._

### Artist/Creative Half-Pay Milestone Flow


---


# Client Flows

# Client Account
## Client Account Creation


### Client Web Intake Flow
1. Client takes a questionnaire to determine expertise level, valid scope,
   domain (if not selected), deadline, and budget.
2. System suggests an estimated budget, reccamends a team (if applicable) and estimated deadline.
3. Page produces budget options, team options, option to shelf the quote (account or cookie gated. Think shopping cart.),
    - See how the platforms produce suggestions: 'Suggestion Flow' section in "This" document.


### Client Discord Intake Flow
1. Similar flow to Web Intake, just tailored to Discord UIs.

### Client Review Window Flow 
- Client gets a review window (ex: 72 hours)
- Client can:
    - **Accept** (one click)
    - **Dispute** (must map claim to acceptance criteria)
    - **No action** (auto-release when window expires)

---
## Client Questionnaire Flow (Client → Tavall Curation → Funded Contract)

### 1) Client Intake Questionnaire
_Goal: collect structured input so Tavall can produce a tight scope, not vague “client speak”._

Captures:
- Category / project type (web app, plugin, automation, design+dev, etc.)
- Budget range + flexibility
- Deadline + urgency (hard date vs “asap”)
- Stack preferences / constraints (must-use, can-use, can’t-use)
- Deliverables they think they want (features/pages/endpoints/integrations)
- Assets they already have (Figma, branding, API keys, repos, docs)
- Access needs (GitHub org access, cloud creds, staging env, etc.)
- Preferences (timezone overlap, communication frequency, solo vs team)
- Risk flags (unknown requirements, “I’ll know it when I see it,” etc.)

Note: This needs to bre friction-less, some clients don't want to spend 20 or even 5 minutes answering questions for a quote.

### 2) Tavall Curation Layer (Scope Finalization)
_Goal: convert questionnaire answers into a standardized, SLA’d contract._

Tavall maps the answers into a **scope template** containing:
- Deliverables + exclusions (explicit out-of-scope list)
- Acceptance criteria (testable, automatable when possible)
- Milestones + payout weights (auto-suggested by project type/size)
- Verification gates (CI/tests/artifacts/demo evidence)
- Risk tags + required update cadence (SLA hooks)

### 3) Escrow Funding Gate (Before Listing)
_Key rule: freelancers only see funded work._

- Default: **100% funded into escrow** before the job is visible on the marketplace
- Optional: **milestone-by-milestone funding**
    - Job pauses until the next milestone is funded
    - Used mainly for high-trust repeat clients or low-risk scopes

### 4) Posting as a Curated Contract (Freelancer-Facing)
Freelancers/teams browse **ready-to-start contracts**:
- Scope summary
- Milestones + payout schedule
- Stack requirements + constraints
- Verification gates + evidence requirements
- Risk tags + deadline expectations

### 5) Acceptance (Freelancer/Team)
Acceptance is intentionally simple:
- Accept contract terms
- Provide start date (if required)
- Connect private repo
- Begin the milestone cycle (`SUBMITTED → VERIFIED → REVIEW → PAID_OUT`)

### 6) Pre-Made Team Branch
- Teams are first-class objects (roles, availability, pricing, standards)
- Some contracts are **team-only** when multiple domains are required
- Reliability scoring applies to both **individuals and teams** (pass rate, disputes, on-time, responsiveness)

---

### Client Review Window Flow
- Client gets a review window (ex: 72 hours)
- Client can:
    - **Accept** (one click)
    - **Dispute** (must map to acceptance criteria)
    - **No action** (auto-release when window expires)

---

# Admin Flows

## 

---
  





### TODO: Add more Milestone Flow for other domains

# Team Flows

Freelancers can set up teams, or the platform can suggest them.

## Pre-Made Teams
- We'll support pre-made teams so freelancers from different domains can work together.
    - For example, some projects need an artist, developer, modeler, and a product manager.


## Suggested Teams
- Platform can suggest teams based on a number of criteria based on clients and freelancers.
    - TODO: Add suggest criteria


---

# Payout Flows


## Escrow

### Client Escrow Options

## Automated Freelancer Payout



**State:** `MILESTONE_CLIENT_REVIEW`

---

### 2.4 Funds Release (Escrow → Dev)
Triggers payout if:
- client accepts, OR
- review window expires with no dispute, OR
- dispute resolves in dev’s favor

Release amount:
- milestone weight (50/50, 40/60, 30/30/40, etc.)

**State:** `MILESTONE_FUNDS_RELEASED`

---

## 3) Dispute Handling (Only if Disputed)

### 3.1 Dispute Filed (Client)
- Must include:
    - which acceptance criteria failed
    - evidence (screenshots/logs) OR reproduction steps
- Platform compares claim vs:
    - locked criteria
    - CI artifacts
    - reproducibility

**State:** `MILESTONE_DISPUTED`

### 3.2 Resolution Paths (Platform)
- **Dev Fix Required:** return to Dev Work Phase (no payout yet)
- **Client Wrong / Out-of-Scope:** payout proceeds (criteria met)
- **Scope Change Detected:** create **Change Order**
    - new milestone
    - new funding requirement
    - updated verification plan (locked for that change only)

---

## 4) Completion
- Final milestone must include:
    - all acceptance criteria met
    - final documentation
    - handoff artifacts (deploy steps, env vars, credentials handoff checklist)
    - optional warranty period rules (bugfix window)

**State:** `PROJECT_COMPLETED`

---

# Default Milestone Definitions

## Milestone 1 (Recommended): “Golden Path Works”
Payout triggers when:
- platform CI passes in clean env
- one complete end-to-end user flow works (scope-defined)
- minimum docs + runnable demo artifact exists
- at least one failure/validation case handled

## Final Milestone: “Full Scope Done”
Payout triggers when:
- all acceptance criteria pass
- polish/UX/stability requirements met (if scoped)
- complete handoff package delivered

---

# Anti-Scam Guardrails (Both Sides)

## Prevent Dev Scams
- payouts only from platform-run CI success + artifacts
- protected branches + PR-only merges
- reproducible build requirement

## Prevent Client Stalling
- fixed review window + auto-release
- disputes must map to acceptance criteria
- out-of-scope requests become change orders