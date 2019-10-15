package de.gamma.libvcs4j.gxl.export;

import picocli.CommandLine;

import java.util.concurrent.Callable;

import static picocli.CommandLine.*;

@Command(name = "gxlexport", mixinStandardHelpOptions = true, version = "gxlexport 1.0",
description = "Exports gxl graph data from repositorys")
public class CommandLineApp implements Callable<Integer> {

    @Parameters(index = "0", description = "The repository whose graph data to load.")
    private String repository;

    @Option(names = {"-mr", "--maxrevisions"}, description = "Defines the maximum number of loaded revisions, default 0 which loads all.")
    private int maxRevisions = 0;

    public static void main(String... args) {
        int exitCode = new CommandLine(new CommandLineApp()).execute(args);
        System.exit(exitCode);
    }

    @Override
    public Integer call() {
        // TODO load data
        return ExitCode.OK;
    }
}
