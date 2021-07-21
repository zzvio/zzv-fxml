package io.zzv.debug;

import java.util.function.Supplier;

import org.semux.Kernel;
import org.semux.Kernel.State;

public class KernelFunc {

  public static Supplier<Boolean> StartKernel =
      new Supplier<Boolean>() {
        @Override
        public Boolean get() {
          Kernel kernel = Kernel.getInstance();
          // start kernel
          kernel.start();
          return kernel.getNodeManager().isRunning()
              && kernel.getPendingManager().isRunning()
              && kernel.getP2p().isRunning()
              && kernel.getBftManager().isRunning()
              && !kernel.getSyncManager().isRunning();
        }
      };

  public static Supplier<Boolean> StopKernel =
      new Supplier<Boolean>() {
        @Override
        public Boolean get() {
          Kernel kernel = Kernel.getInstance();
          // start kernel
          kernel.stop();
          return kernel.state() == State.STOPPED
              && !kernel.getNodeManager().isRunning()
              && !kernel.getPendingManager().isRunning()
              && !kernel.getP2p().isRunning()
              && !kernel.getBftManager().isRunning()
              && !kernel.getSyncManager().isRunning();
        }
      };
}
