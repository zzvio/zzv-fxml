/**
 * Copyright (c) 2017-2020 The Semux Developers
 *
 * <p>Distributed under the MIT software license, see the accompanying file LICENSE or
 * https://opensource.org/licenses/mit-license.php
 */
package org.semux;

import java.util.ArrayList;
import java.util.List;

import org.semux.cli.SemuxCli;

import io.zzv.MainGui;

public class Main {

  private static final String CLI = "--cli";
  private static final String GUI = "--gui";

  public static void main(String[] args) {
    List<String> startArgs = new ArrayList<>();
    boolean startGui = true;
    for (String arg : args) {
      if (CLI.equals(arg)) {
        startGui = false;
      } else if (GUI.equals(arg)) {
        startGui = true;
      } else {
        startArgs.add(arg);
      }
    }
    if (startArgs.size() == 0) {
      startArgs.add("--network=devnet");
    }

    if (startGui) {
      MainGui.main(startArgs.toArray(new String[0]));
    } else {
      SemuxCli.main(startArgs.toArray(new String[0]));
    }
  }
}
