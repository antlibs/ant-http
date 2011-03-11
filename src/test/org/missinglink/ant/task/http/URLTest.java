package org.missinglink.ant.task.http;

import java.io.IOException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class URLTest extends AbstractHttpTest {

  public URLTest() {
    super();
  }

  @Before
  public void before() throws Exception {
    startHttpsServer();
  }

  @After
  public void after() {
    stopHttpsServer();
  }

  @Test
  public void test() throws IOException, InterruptedException {
    Thread.sleep(120000);
  }

}
