# EchoesWars Engine 1.0

Deterministic, two-player card-game simulation written in Java. The engine reads one match configuration in JSON, runs the match, and writes a result with a versioned compact replay.

## Requirements

- JDK **25** (the Maven project uses `maven.compiler.release=25`)
- Apache Maven 3.9+ (or another version compatible with the configured plugins)

## Build and test

From `backend/engine`:

```bash
mvn clean test
mvn clean package
```

The package command builds `target/engine-1.0-SNAPSHOT.jar`, an executable JAR containing runtime dependencies. `target/original-engine-1.0-SNAPSHOT.jar` is **not** the standalone executable.

## Run

From `backend/engine`:

```bash
java -jar target/engine-1.0-SNAPSHOT.jar
```

The default input is `data/input/match.json` and the default output is `data/output/match-result.json`. When run from the repository root, the application also recognizes `backend/engine/data/input/match.json`.

Specify explicit paths (recommended for integrations):

```bash
java -jar target/engine-1.0-SNAPSHOT.jar data/input/match.json data/output/match-result.json
```

Paths are relative to the **current working directory**, not to the JAR location. The output directory is created if needed. The process exits with status `0` on success and `1` on input, file or engine errors; errors are printed to standard error.

## Input contract

See [`data/input/match.json`](data/input/match.json) for a complete example. Required top-level properties:

- `matchId`: nonempty string
- `rulesVersion`: nonempty string
- `seed`: signed 64-bit integer, used for deterministic deck shuffling
- `cardDefinitions`: array of card definitions with `cardId`, nonnegative integer `attack`, `defense`, `cost`, and enum values `echo`, `school`, optionally `effect` (defaults to `NONE`)
- `players`: array of **exactly two** players, each with a `playerId` and `deck` array; each deck entry has a `cardId` and nonnegative integer `quantity`

Unknown card IDs are rejected. Enum strings are case-sensitive; refer to `src/main/java/com/echoeswars/model` for allowed values. The engine uses a maximum of 100 rounds.

## Output and replay

The output includes match metadata, winner, status, round count, player life, and `replay`. The compact replay has `formatVersion: 2`, `cardDefinitions` keyed by card ID, `initialState` with compact card instances, and an ordered sequence of round events. `finalState` is intentionally **omitted** from the JSON; it is reconstructed by applying events to the initial state with `CompactReplayCodec.decode()`.

`MatchJsonWriter.writeCompact()` produces the version 2 wire format. `MatchJsonWriter.write()` remains available for the legacy expanded representation.

## Integration notes

This project is a command-line simulation engine, not an HTTP service. A backend can invoke the JAR as a subprocess, pass input/output file paths, inspect the exit code and read the JSON result. If the engine runs in a shared environment, give each match unique file paths to avoid concurrent writes to the same output.

## Release verification

Run `mvn clean test` and `mvn clean package` on JDK 25, then run the JAR against `data/input/match.json` and verify that the output includes `replay.formatVersion = 2`. The repository also contains determinism, replay and effect tests. A successful build alone does not constitute a security or production-load audit.
