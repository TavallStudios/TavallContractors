db = db.getSiblingDB("tavall_contractors");

db.createCollection("project_scopes");
db.createCollection("dashboard_configs");

db.project_scopes.createIndex({ status: 1, talentId: 1 });
db.project_scopes.createIndex({ clientId: 1 });
db.project_scopes.createIndex({ checkoutId: 1 });

db.dashboard_configs.createIndex({ userId: 1 }, { unique: true });
