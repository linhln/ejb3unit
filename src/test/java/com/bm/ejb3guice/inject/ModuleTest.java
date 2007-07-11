// Copyright 2007 Google Inc. All Rights Reserved.

package com.bm.ejb3guice.inject;

import com.bm.ejb3guice.inject.Binder;
import com.bm.ejb3guice.inject.Ejb3Guice;
import com.bm.ejb3guice.inject.Module;

import junit.framework.TestCase;

/**
 * Tests relating to modules.
 *
 * @author kevinb
 */
public class ModuleTest extends TestCase {

  static class A implements Module {
    public void configure(Binder binder) {
      binder.bind(X.class);
      binder.install(new B());
      binder.install(new C());
    }
  }

  static class B implements Module {
    public void configure(Binder binder) {
      binder.bind(Y.class);
      binder.install(new D());
    }
  }

  static class C implements Module {
    public void configure(Binder binder) {
      binder.bind(Z.class);
      binder.install(new D());
    }
  }

  static class D implements Module {
    public void configure(Binder binder) {
      binder.bind(W.class);
    }
    @Override public boolean equals(Object obj) {
      return obj.getClass() == D.class; // we're all equal in the eyes of guice
    }
    @Override public int hashCode() {
      return D.class.hashCode();
    }
  }

  static class X {}
  static class Y {}
  static class Z {}
  static class W {}

  public void testDiamond() throws Exception {
    Ejb3Guice.createInjector(new A());
  }
}
