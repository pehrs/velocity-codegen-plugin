package com.pehrs.velocitycodegenplugin;

import org.apache.velocity.runtime.RuntimeServices;
import org.apache.velocity.runtime.log.LogChute;

public class MyNullLogChute  implements LogChute {
  public MyNullLogChute() {
  }

  public void init(RuntimeServices rs) throws Exception {
  }

  public void log(int level, String message) {
  }

  public void log(int level, String message, Throwable t) {
  }

  public boolean isLevelEnabled(int level) {
    return false;
  }
}
