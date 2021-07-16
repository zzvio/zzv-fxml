package io.zzv.debug;

import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

import org.semux.Kernel;
import org.semux.Kernel.State;

import com.ea.async.Async;

public class KernelFunc {

    public static Supplier<Boolean> startKernel = new Supplier<Boolean>() {
        @Override
        public Boolean get() {
            Kernel kernel = Kernel.getInstance();
            // start kernel
            kernel.start();
            CompletableFuture<Boolean> completableFuture = CompletableFuture.supplyAsync(() ->
            {
                return kernel.getNodeManager().isRunning()
                        && kernel.getPendingManager().isRunning()
                        && kernel.getP2p().isRunning()
                        && kernel.getBftManager().isRunning()
                        && !kernel.getSyncManager().isRunning();
            });

            boolean result = Async.await(completableFuture);
            return result;
        }
    };
    public static Supplier<Boolean> stopKernel = new Supplier<Boolean>() {
        @Override
        public Boolean get() {
            Kernel kernel = Kernel.getInstance();
            // start kernel
            kernel.stop();
            CompletableFuture<Boolean> completableFuture = CompletableFuture.supplyAsync(() ->
            {
                return kernel.state() == State.STOPPED
                        && !kernel.getNodeManager().isRunning()
                        && !kernel.getPendingManager().isRunning()
                        && !kernel.getP2p().isRunning()
                        && !kernel.getBftManager().isRunning()
                        && !kernel.getSyncManager().isRunning();

            });

            boolean result = Async.await(completableFuture);
            return result;
        }
    };

    static {
        Async.init();
    }
}
