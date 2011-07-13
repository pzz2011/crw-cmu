package org.ros.actionlib.example;

import org.ros.NodeConfiguration;
import org.ros.NodeRunner;

/**
 * A main for running the Fibonacci simple action server.
 */
public class RunFibonacciSimpleActionServer {

  public static void main(String[] args) {
    main();
  }

  public static void main() {
    try {
      // user code implementing the SimpleActionServerCallbacks interface
      FibonacciSimpleActionServerCallbacks impl = new FibonacciSimpleActionServerCallbacks();

      FibonacciActionSpec spec = new FibonacciActionSpec();
      FibonacciSimpleActionServer sas =
          spec.buildSimpleActionServer("fibonacci_server", impl, true);

      NodeConfiguration configuration = NodeConfiguration.createDefault();

      NodeRunner runner = NodeRunner.createDefault();

      runner.run(sas, configuration);
    } catch (Exception e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

}
