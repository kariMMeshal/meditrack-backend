-- V1__init_schema.sql (FlyWay Migrations for database schema initialization to be used with Docker Compose)
-- =========================
-- Departments
-- =========================
CREATE TABLE departments (
  id INT NOT NULL AUTO_INCREMENT,
  description VARCHAR(255),
  name VARCHAR(255) NOT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY UK_departments_name (name)
);

-- =========================
-- Users
-- =========================
CREATE TABLE users (
  id INT NOT NULL AUTO_INCREMENT,
  deleted BIT(1) NOT NULL,
  email VARCHAR(255) NOT NULL,
  enabled BIT(1) NOT NULL,
  password VARCHAR(255) NOT NULL,
  username VARCHAR(255) NOT NULL,

  failed_login_attempts INT NOT NULL DEFAULT 0,
  locked_until DATETIME DEFAULT NULL,

  PRIMARY KEY (id),
  UNIQUE KEY UK_users_email (email),
  UNIQUE KEY UK_users_username (username)
);

-- =========================
-- Roles
-- =========================
CREATE TABLE roles (
  id INT NOT NULL AUTO_INCREMENT,
  role VARCHAR(255) NOT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY UK_roles_role (role)
);

-- =========================
-- User Roles (Many-to-Many)
-- =========================
CREATE TABLE user_roles (
  user_id INT NOT NULL,
  role_id INT NOT NULL,
  PRIMARY KEY (user_id, role_id),

  CONSTRAINT fk_user_roles_user FOREIGN KEY (user_id) REFERENCES users(id),
  CONSTRAINT fk_user_roles_role FOREIGN KEY (role_id) REFERENCES roles(id)
);

-- =========================
-- Medical Devices
-- =========================
CREATE TABLE medical_devices (
  id INT NOT NULL AUTO_INCREMENT,
  condition_description VARCHAR(255),
  manufacturer VARCHAR(255),
  model VARCHAR(255),
  name VARCHAR(255) NOT NULL,
  status VARCHAR(255),
  department_id INT,
  asset_tag VARCHAR(255),
  created_at DATETIME(6),
  last_maintenance_date DATE,
  location VARCHAR(255),
  next_maintenance_date DATE,
  purchase_date DATE,
  purchase_price DOUBLE,
  serial_number VARCHAR(255) NOT NULL,
  supplier VARCHAR(255),
  updated_at DATETIME(6),
  warranty_expiry_date DATE,
  last_cleaned_date DATETIME(6),
  last_sterilization_date DATETIME(6),
  max_usage_hours DOUBLE,
  sterilization_interval_hours INT,
  usage_hours DOUBLE,

  PRIMARY KEY (id),
  UNIQUE KEY UK_medical_devices_serial (serial_number),
  UNIQUE KEY UK_medical_devices_asset (asset_tag),

  CONSTRAINT fk_devices_department
    FOREIGN KEY (department_id) REFERENCES departments(id)
);

-- =========================
-- Maintenance Logs
-- =========================
CREATE TABLE maintenance_logs (
  id INT NOT NULL AUTO_INCREMENT,
  action_taken TEXT,
  cost DOUBLE,
  created_at DATETIME(6),
  issue_description TEXT NOT NULL,
  maintenance_date DATE,
  next_maintenance_date DATE,
  notes TEXT,
  priority ENUM('CRITICAL','HIGH','MEDIUM','LOW') NOT NULL,
  maintenance_status ENUM('PENDING','IN_PROGRESS','COMPLETED','CANCELLED') NOT NULL,
  updated_at DATETIME(6),
  device_id INT NOT NULL,
  performed_by_id INT NOT NULL,

  PRIMARY KEY (id),

  CONSTRAINT fk_maintenance_device
    FOREIGN KEY (device_id) REFERENCES medical_devices(id),

  CONSTRAINT fk_maintenance_user
    FOREIGN KEY (performed_by_id) REFERENCES users(id)
);

-- =========================
-- Alerts
-- =========================
CREATE TABLE alerts (
  id BIGINT NOT NULL AUTO_INCREMENT,
  ai_explanation TEXT,
  ai_provider VARCHAR(255),
  created_at DATETIME(6) NOT NULL,
  device_id INT,
  message TEXT NOT NULL,
  metadata TEXT,
  resolved_at DATETIME(6),
  severity ENUM('CRITICAL','INFO','WARNING') NOT NULL,
  status ENUM('ACKNOWLEDGED','NEW','READ','RESOLVED') NOT NULL,
  type ENUM('AI_ALERT','DEVICE_ALERT','SECURITY_ALERT','SYSTEM_ALERT','USER_ACTIVITY_ALERT') NOT NULL,
  user_id INT,

  PRIMARY KEY (id),

  KEY idx_alert_type (type),
  KEY idx_alert_status (status),
  KEY idx_alert_device (device_id),
  KEY idx_alert_user (user_id),
  KEY idx_alert_created (created_at)
);

-- =========================
-- Audit Logs
-- =========================
CREATE TABLE audit_logs (
  id BIGINT NOT NULL AUTO_INCREMENT,
  event_type VARCHAR(50) NOT NULL,
  username VARCHAR(100),
  ip_address VARCHAR(45),
  user_agent TEXT,
  details TEXT,
  success TINYINT(1) NOT NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,

  PRIMARY KEY (id),

  KEY idx_audit_username (username),
  KEY idx_audit_event (event_type),
  KEY idx_audit_created (created_at)
);

-- =========================
-- AI Predictions
-- =========================
CREATE TABLE ai_predictions (
  id BIGINT NOT NULL AUTO_INCREMENT,
  created_at DATETIME(6) NOT NULL,
  error_message TEXT,
  model_version VARCHAR(255) NOT NULL,
  prediction INT NOT NULL,
  probability DOUBLE NOT NULL,
  status VARCHAR(255) NOT NULL,
  threshold_used DOUBLE NOT NULL,
  device_id INT NOT NULL,
  maintenance_log_id INT,
  requested_by INT NOT NULL,
  avg_motor_vibration DOUBLE,
  avg_temperature_variance DOUBLE,
  avg_voltage_drop DOUBLE,

  PRIMARY KEY (id),

  KEY idx_ai_device (device_id),
  KEY idx_ai_maintenance (maintenance_log_id),
  KEY idx_ai_user (requested_by),

  CONSTRAINT fk_ai_device FOREIGN KEY (device_id) REFERENCES medical_devices(id),
  CONSTRAINT fk_ai_maintenance FOREIGN KEY (maintenance_log_id) REFERENCES maintenance_logs(id),
  CONSTRAINT fk_ai_user FOREIGN KEY (requested_by) REFERENCES users(id)
);

-- =========================
-- AI Reports
-- =========================
CREATE TABLE ai_reports (
  id BIGINT NOT NULL AUTO_INCREMENT,
  created_at DATETIME(6) NOT NULL,
  provider VARCHAR(255) NOT NULL,
  report_content TEXT NOT NULL,
  report_type VARCHAR(255) NOT NULL,
  session_id VARCHAR(255),
  user_id INT NOT NULL,

  PRIMARY KEY (id),

  CONSTRAINT fk_ai_reports_user FOREIGN KEY (user_id) REFERENCES users(id)
);

-- =========================
-- Chat Messages
-- =========================
CREATE TABLE chat_messages (
  id BIGINT NOT NULL AUTO_INCREMENT,
  ai_response TEXT NOT NULL,
  created_at DATETIME(6) NOT NULL,
  provider VARCHAR(255) NOT NULL,
  session_id VARCHAR(255) NOT NULL,
  user_message TEXT NOT NULL,
  user_id INT NOT NULL,

  PRIMARY KEY (id),

  CONSTRAINT fk_chat_user FOREIGN KEY (user_id) REFERENCES users(id)
);

-- =========================
-- Risk Assessments
-- =========================
CREATE TABLE risk_assessments (
  id BIGINT NOT NULL AUTO_INCREMENT,
  confidence_score DOUBLE NOT NULL,
  created_at DATETIME(6) NOT NULL,
  error_message TEXT,
  model_version VARCHAR(255) NOT NULL,
  predicted_class INT NOT NULL,
  predicted_label VARCHAR(255) NOT NULL,
  recommendation VARCHAR(255) NOT NULL,
  status VARCHAR(255) NOT NULL,
  device_id INT NOT NULL,
  requested_by INT NOT NULL,

  PRIMARY KEY (id),

  CONSTRAINT fk_risk_device FOREIGN KEY (device_id) REFERENCES medical_devices(id),
  CONSTRAINT fk_risk_user FOREIGN KEY (requested_by) REFERENCES users(id)
);

-- =========================
-- Refresh Tokens
-- =========================
CREATE TABLE refresh_tokens (
  id BIGINT NOT NULL AUTO_INCREMENT,
  token VARCHAR(64) NOT NULL,
  user_id INT NOT NULL,
  expires_at DATETIME(6) NOT NULL,
  revoked TINYINT(1) NOT NULL DEFAULT 0,
  created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),

  PRIMARY KEY (id),
  UNIQUE KEY UK_refresh_token (token),

  KEY idx_rt_token (token),
  KEY idx_rt_user (user_id),
  KEY idx_rt_revoked (revoked),

  CONSTRAINT fk_refresh_user FOREIGN KEY (user_id) REFERENCES users(id)
);