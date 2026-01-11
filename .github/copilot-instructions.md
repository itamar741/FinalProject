Project-specific Copilot instructions

Summary
- **Purpose**: This repo implements a small retail/sales system with a server component. Key packages: [src/controller](src/controller), [src/model](src/model), [server](server). Use `ServerMain` to start the server and `Main` (in `src/model`) as the application entry used in local runs.

Architecture & big picture
- **Components**: Controller layer (`src/controller`) coordinates UI/requests; Model layer (`src/model`) holds entities and domain logic; `server/` contains socket-based server entry and `ClientHandler` for networked clients. Manager classes (under `src/model/managers`) encapsulate CRUD and business flows (e.g., `InventoryManager`, `SalesManager`).
- **Data flow (typical sale)**: `SalesManager` validates and creates a `Sale` → updates `InventoryManager` quantities → records action in `LogManager` (creates `LogEntry`). See `src/model/Sale.java`, `src/model/managers/SalesManager.java`, `src/model/managers/InventoryManager.java`, and `src/model/managers/LogManager.java` for examples.

Conventions & patterns
- **Package layout**: follow existing packages: `controller`, `model`, `server`, `service`, `storage`, `util`. Place coordinating logic in `managers` under `src/model/managers`.
- **Manager classes**: managers provide high-level operations (not DAO-style). New domain features should go into a manager, not directly on entity POJOs.
- **Exceptions**: business exceptions live under `src/model/exceptions` (e.g., `InsufficientStockException`, `DuplicateCustomerException`). Throw these from managers and let higher-level controllers or handlers decide how to respond.
- **Logging/Audit**: use `LogManager` and `LogEntry` to record important state changes (sales, inventory adjustments). If you update a manager to change state, add or update corresponding log entries.

Integration points
- **Server**: `server/ServerMain.java` is the socket server. `server/ClientHandler.java` shows how requests are parsed and routed—mimic its approach when adding new network endpoints.
- **Entry points**: run `server.ServerMain` for networked usage; run `model.Main` for standalone local runs or demos. See [server/ServerMain.java](server/ServerMain.java) and [src/model/Main.java](src/model/Main.java).

Build & run (discoverable):
- No build tool detected (no Maven/Gradle). Recommended: open the project in IntelliJ (project file FinalProject.iml present) and run the `ServerMain` and `Main` run configurations.
- Quick CLI approach (adjust classpath/packages if needed):
  - Compile all sources into `out/` (example in PowerShell):
    ```powershell
    Get-ChildItem -Recurse -Filter *.java | ForEach-Object { $_.FullName } | % { javac -d out $_ }
    ```
  - Run server: `java -cp out server.ServerMain`
  - Run local app: `java -cp out model.Main`

Code examples (patterns to follow)
- Adding a new product flow: add an operation in `ProductManager` (under `src/model/managers`) that creates a `Product` instance, updates `InventoryManager`, and calls `LogManager.record(...)` with a `LogEntry`.
- Handling insufficient stock: throw `InsufficientStockException` from `InventoryManager` and catch it in `SalesManager` or controller to produce a user-facing message or network error.

What to look for when editing
- Update `LogManager` whenever you change persistent state semantics.
- Keep managers thin: put validation and orchestration there; entities (`src/model/*.java`) should remain plain data + simple helpers.
- Use existing exceptions rather than creating ad-hoc runtime exceptions; this keeps behavior consistent across controllers and server handlers.

Missing information / asks for the maintainer
- Add how you typically run the project (IDE run configuration names or exact CLI commands you use) if different from above.
- If there's a test runner or CI flow you use, add the toolchain or scripts to the repo so agents can run tests and validate changes.

If anything above is unclear or you want different emphasis (e.g., focus on server APIs or persistence), tell me which area to expand.
