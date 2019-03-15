package net.marvk.chess.lichess4j;

import lombok.extern.log4j.Log4j2;
import net.marvk.chess.lichess4j.model.Perf;
import net.marvk.chess.uci4j.EngineFactory;
import org.apache.http.nio.reactor.IOReactorException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

@Log4j2
public class LichessClientBuilder {
    private final String accountName;
    private final EngineFactory engineFactory;

    private String apiToken;

    private final List<Perf> allowedPerfs;

    public static LichessClientBuilder create(final String accountName, final EngineFactory engineFactory) {
        return new LichessClientBuilder(accountName, engineFactory);
    }

    private LichessClientBuilder(final String accountName, final EngineFactory engineFactory) {
        this.accountName = accountName;
        this.engineFactory = engineFactory;

        this.allowedPerfs = new ArrayList<>();
    }

    public LichessClientBuilder apiToken(final String apiToken) {
        this.apiToken = apiToken;

        return this;
    }

    public LichessClientBuilder apiTokenFromPath(final Path path) throws IOException {
        this.apiToken = String.join("\n", Files.readAllLines(path)).trim();

        if (this.apiToken.isEmpty()) {
            throw new IOException("Empty token file");
        }

        return this;
    }

    public LichessClientBuilder allowPerf(final Perf perf) {
        allowedPerfs.add(perf);
        return this;
    }

    public LichessClientBuilder allowAllPerfs(final Perf... perfs) {
        allowedPerfs.addAll(Arrays.asList(perfs));
        return this;
    }

    public LichessClientBuilder allowAllPerfs(final Collection<Perf> perfs) {
        allowedPerfs.addAll(perfs);
        return this;
    }

    public LichessClient build() throws IOReactorException {
        if (apiToken == null) {
            throw new IllegalStateException("Failed to specify API token");
        }

        if (allowedPerfs.isEmpty()) {
            throw new IllegalStateException("Failed to specify at least one allowed Perf");
        }

        return new LichessClient(accountName, apiToken, EnumSet.copyOf(allowedPerfs), engineFactory);
    }
}
