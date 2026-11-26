package org.dashjoin.function.lint;

import java.util.EnumSet;
import java.util.Set;

/**
 * a lint rule: checks the nodes of the kinds it is registered for and reports findings via
 * Linter.report
 */
public interface LintRule {

  /** e.g. unknown-property; used in findings and to switch the rule off */
  String id();

  Severity severity();

  /** the node kinds the rule checks */
  Set<Kind> on();

  void check(Node node, Linter lint);

  interface Check {
    void check(Node node, Linter lint);
  }

  static LintRule of(String id, Severity severity, Kind kind, Check check) {
    Set<Kind> on = EnumSet.of(kind);
    return new LintRule() {
      @Override
      public String id() {
        return id;
      }

      @Override
      public Severity severity() {
        return severity;
      }

      @Override
      public Set<Kind> on() {
        return on;
      }

      @Override
      public void check(Node node, Linter lint) {
        check.check(node, lint);
      }

      @Override
      public String toString() {
        return id;
      }
    };
  }
}
