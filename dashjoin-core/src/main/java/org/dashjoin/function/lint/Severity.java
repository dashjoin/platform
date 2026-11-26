package org.dashjoin.function.lint;

/**
 * how serious a finding is
 */
public enum Severity {
  /** the app does not work as written: syntax errors, unknown widgets, dangling references */
  ERROR,
  /** works, but something is ignored or outdated: unknown or deprecated properties */
  WARNING,
  /** might be intended: unused queries, functions and libraries */
  HINT
}
