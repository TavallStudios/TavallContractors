export type RiskLabel = "Low" | "Med" | "Critical";

export interface IntakeState {
  domain: string;
  domainType: string;
  domainSubtype: string;
  language: string;
  techLevel: string;
  budget: string;
  customBudget: string;
  deadline: string;
  docsType: string[];
  guidedAnswers: Record<string, unknown>;
  contractType: string;
  completeness?: number;
  risk?: RiskLabel;
}

export interface CartAddon {
  code: string;
  label: string;
  description: string;
  price: number;
}

export interface WizardSessionState {
  sessionKey: string;
  userId?: number;
  currentIntake: IntakeState;
  cart: IntakeState[];
  addons: CartAddon[];
}

export interface EvaluateResponse {
  state: IntakeState;
  completeness: number;
  risk: RiskLabel;
  badges: string[];
}

export interface SpecResponse {
  markdown: string;
}

export interface CheckoutResponse {
  checkoutId: number;
  projectCount: number;
  totalBudget: number;
  addonCount: number;
  addonTotal: number;
  grandTotal: number;
  status: string;
}

export interface TalentCard {
  userId: number;
  name: string;
  headline: string;
  hourlyRate: number;
  skills: string[];
}

export interface PortfolioCard {
  domain: string;
  totalProjects: number;
  coverImage: string;
}

export interface JobBoardItem {
  projectId: string;
  title: string;
  domain: string;
  escrowBalance: number;
  specPreview: string;
}

export interface PortfolioProject {
  title: string;
  summary: string;
  tech: string;
  imageUrl: string;
}

export interface PortfolioLink {
  label: string;
  url: string;
}

export interface PortfolioContent {
  tagline: string;
  summary: string;
  location: string;
  availability: string;
  skills: string[];
  projects: PortfolioProject[];
  links: PortfolioLink[];
}

export interface FreelancerPortfolioView {
  userId: number;
  displayName: string;
  headline: string;
  hourlyRate: number;
  portfolio: PortfolioContent;
}

export interface TalentProfileSummary {
  headline: string;
  hourlyRate: number;
  skills: string[];
}

export interface FreelancerWorkspace {
  userId: number;
  role: string;
  activated: boolean;
  portfolioUrl: string;
  displayName: string;
  talentProfile: TalentProfileSummary;
  portfolio: PortfolioContent;
}

export interface FreelancerWorkspaceUpdate {
  displayName: string;
  headline: string;
  hourlyRate: number;
  skills: string[];
  portfolio: PortfolioContent;
}
