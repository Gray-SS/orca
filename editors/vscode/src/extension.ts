import * as cp from 'child_process';
import * as fs from 'fs';
import * as path from 'path';
import * as vscode from 'vscode';
import {
    LanguageClient,
    LanguageClientOptions,
    ServerOptions,
} from 'vscode-languageclient/node';

// Relative to editors/vscode/ (context.extensionPath)
const JAR_FROM_EXTENSION = path.join('..', '..', 'modules', 'lsp', 'build', 'libs', 'lsp-1.0-SNAPSHOT-fat.jar');

let client: LanguageClient | undefined;

export function activate(context: vscode.ExtensionContext): void {
    const config = vscode.workspace.getConfiguration('orca.server');
    const java = (config.get<string>('javaPath') || 'java').trim() || 'java';

    const jarPath = resolveJarPath(context, config.get<string>('jarPath') || '');
    if (!jarPath) {
        vscode.window.showErrorMessage(
            'Orca LSP: JAR not found. Build the server first with ' +
            '`./gradlew :modules:lsp:jar`, or set `orca.server.jarPath` in settings.'
        );
        return;
    }

    const serverOptions: ServerOptions = {
        command: java,
        args: ['-jar', jarPath],
    };

    const clientOptions: LanguageClientOptions = {
        documentSelector: [{ scheme: 'file', language: 'orca' }],
        synchronize: {
            fileEvents: vscode.workspace.createFileSystemWatcher('**/*.orca'),
        },
    };

    client = new LanguageClient('orca', 'Orca Language Server', serverOptions, clientOptions);
    client.start();

    // Register the manual sync command
    context.subscriptions.push(
        vscode.commands.registerCommand('orca.syncProject', () => syncAllProjects())
    );

    // Auto-sync all discovered Orca projects on activation
    syncAllProjects();

    // Re-sync whenever any Gradle build file changes
    const buildWatcher = vscode.workspace.createFileSystemWatcher('**/build.gradle{,.kts}');
    buildWatcher.onDidChange(() => syncAllProjects());
    buildWatcher.onDidCreate(() => syncAllProjects());
    context.subscriptions.push(buildWatcher);
}

export function deactivate(): Thenable<void> | undefined {
    return client?.stop();
}

/**
 * Scans the workspace root up to {@code maxDepth} levels deep and returns every
 * directory that contains both a Gradle build file and a Gradle wrapper.
 * Stops recursing into a directory once it is itself identified as a project.
 */
function findOrcaProjects(root: string, maxDepth = 3): string[] {
    const SKIP = new Set(['.git', '.gradle', 'build', 'out', 'node_modules']);
    const projects: string[] = [];

    if (hasGradleWrapper(root) && isGradleProject(root)) {
        projects.push(root);
        return projects; // don't recurse into sub-projects of a project
    }

    if (maxDepth <= 0) return projects;

    try {
        for (const entry of fs.readdirSync(root, { withFileTypes: true })) {
            if (!entry.isDirectory() || entry.name.startsWith('.') || SKIP.has(entry.name)) continue;
            projects.push(...findOrcaProjects(path.join(root, entry.name), maxDepth - 1));
        }
    } catch (_) { /* unreadable directory */ }

    return projects;
}

function isGradleProject(dir: string): boolean {
    return fs.existsSync(path.join(dir, 'build.gradle.kts'))
        || fs.existsSync(path.join(dir, 'build.gradle'));
}

function hasGradleWrapper(dir: string): boolean {
    const name = process.platform === 'win32' ? 'gradlew.bat' : 'gradlew';
    return fs.existsSync(path.join(dir, name));
}

function syncAllProjects(): void {
    const workspaceRoot = vscode.workspace.workspaceFolders?.[0]?.uri.fsPath;
    if (!workspaceRoot) return;

    const projects = findOrcaProjects(workspaceRoot);
    if (projects.length === 0) return;

    vscode.window.withProgress(
        { location: vscode.ProgressLocation.Window, title: 'Orca: Syncing projects...' },
        () => Promise.all(projects.map(syncGradleProject)).then(() => undefined)
    );
}

function syncGradleProject(projectRoot: string): Promise<void> {
    const gradlew = path.join(projectRoot, process.platform === 'win32' ? 'gradlew.bat' : 'gradlew');
    return new Promise<void>((resolve) => {
        cp.execFile(gradlew, ['generateOrcaProjectModel'], { cwd: projectRoot }, (err) => {
            if (err) {
                vscode.window.showWarningMessage(
                    `Orca: Sync failed for ${path.basename(projectRoot)} — ${err.message}`);
            }
            resolve();
        });
    });
}

function resolveJarPath(context: vscode.ExtensionContext, configured: string): string | undefined {
    if (configured) {
        return fs.existsSync(configured) ? configured : undefined;
    }

    // Primary: relative to the extension directory (always available)
    const fromExtension = context.asAbsolutePath(JAR_FROM_EXTENSION);
    if (fs.existsSync(fromExtension)) {
        return fromExtension;
    }

    // Fallback: relative to the workspace root
    const workspaceRoot = vscode.workspace.workspaceFolders?.[0]?.uri.fsPath;
    if (workspaceRoot) {
        const fromWorkspace = path.join(workspaceRoot, 'modules', 'lsp', 'build', 'libs', 'lsp-1.0-SNAPSHOT-fat.jar');
        if (fs.existsSync(fromWorkspace)) {
            return fromWorkspace;
        }
    }

    return undefined;
}
